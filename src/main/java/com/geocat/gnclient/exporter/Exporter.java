package com.geocat.gnclient.exporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import com.geocat.gnclient.gnservices.GNServicesClient;
import com.geocat.gnclient.gnservices.groups.model.Group;
import com.geocat.gnclient.gnservices.metadata.model.MetadataInfo;
import com.geocat.gnclient.gnservices.metadata.model.MetadataSearchResults;
import com.geocat.gnclient.gnservices.users.model.User;
import com.geocat.gnclient.util.ConsoleProgress;
import com.geocat.gnclient.util.HelperUtility;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exports users/groups/metadata from a GeoNetwork instance to a provided folder.
 *
 */
public class Exporter {
    private static final Logger logger = LogManager.getLogger(Exporter.class.getName());

    private ExporterReport exporterReport;

    public ExporterReport getExporterReport() {
        return exporterReport;
    }

    public void exec(GNServicesClient client, ExporterConfig config) throws Exception {
        Path outputPath = config.getOutputPath();

        Files.createDirectories(outputPath);

        exporterReport = new ExporterReport();

        if (config.getDefaultExportType().equals("all")) {
            FileUtils.cleanDirectory(outputPath.toFile());
        } else if (config.getDefaultExportType().equals("users")) {
           Files.deleteIfExists(outputPath.resolve("users.json"));
        } else if (config.getDefaultExportType().equals("groups")) {
            Files.deleteIfExists(outputPath.resolve("groups.json"));
        }

        if (config.getDefaultExportType().equals("all") ||
            config.getDefaultExportType().equals("users")) {
            exporterReport.setExportUsers(true);

            // Retrieve users
            logger.info("Exporting users");
            System.out.print("Exporting users.");

            try {
                User[] users = client.retrieveUsers();
                logger.info("  Retrieved " + users.length  + " user(s).");

                exportUsers(client, outputPath, users);

                exporterReport.setErrorsExportingUsers(false);
            } catch (Exception ex) {
                exporterReport.setErrorsExportingUsers(true);
                exporterReport.setErrorsExportingUsersText("Error exporting users, " + ex.getMessage());
            }

            logger.info("  Exporting users completed.");

            if (!exporterReport.isErrorsExportingUsers()) {
                System.out.print(" Completed.");
            } else {
                System.out.print(" Completed with errors.");
            }

            System.out.println();

        } else {
            exporterReport.setExportUsers(false);

        }

        Map<String, Group>  groups = new HashMap<>();

        if (config.getDefaultExportType().equals("all") ||
            config.getDefaultExportType().equals("groups")) {
            exporterReport.setExportGroups(true);

            // Retrieve groups and create folders for the groups
            logger.info("Exporting groups");
            System.out.print("Exporting groups.");

            try {
                groups = client.retrieveGroups();
                logger.info("  Retrieved " + groups.size()  + " group(s).");

                exportGroups(client, outputPath, groups);

                exporterReport.setErrorsExportingGroups(false);
            } catch (Exception ex) {
                System.out.print(" Completed with errors: ");
                exporterReport.setErrorsExportingGroups(true);
                exporterReport.setErrorsExportingGroupsText("Error exporting groups, " + ex.getMessage());
            }

            logger.info("  Exporting groups completed.");

            if (!exporterReport.isErrorsExportingGroups()) {
                System.out.print(" Completed.");
            } else {
                System.out.print(" Completed with errors.");
            }

            System.out.println();
        } else {
            exporterReport.setExportGroups(false);
        }


        if (config.getDefaultExportType().equals("all")) {
            exporterReport.setExportMetadata(true);

            if (!config.getDefaultExportFilterByGroupLocally()) {
                // Retreive metadata per group
                execFilterByGroupCsw(client, config, groups);
            } else {
                // Retrieve all metadata and check which group to export it
                execFilterByGroupLocally(client, config, groups);
            }
        } else {
            exporterReport.setExportMetadata(false);
        }
    }

    private void execFilterByGroupCsw(GNServicesClient client, ExporterConfig config, Map<String, Group>  groups) throws Exception {
        Path outputPath = config.getOutputPath();

        Path metadataPath = outputPath.resolve("metadata");
        Files.createDirectories(metadataPath);

        String filter = buildCswFilter(config);

        // Process groups metadata
        groups.forEach((groupName, group) -> {
            try {
                //Files.createDirectories(outputPath.resolve(v));
                int groupId = group.getId();

                Path metadataGroupPath = metadataPath.resolve("group_" + groupId);
                Files.createDirectories(metadataGroupPath);

                logger.info("Retrieving metadata in group: " + ", id: " + group.getId());
                System.out.println("Retrieving metadata in group: " + groupName + ", id: " + group.getId());

                MetadataSearchResults metadataSearchResults = client.retrieveMetadataForGroupPaginated(groupId, filter, 1);
                int totalReturned = metadataSearchResults.getTotalReturned();
                int totalMatched = metadataSearchResults.getTotalMatched();
                int nextRecord = metadataSearchResults.getNextRecord();

                logger.info("Exporting " + totalMatched + " metadata in group: " + groupName);
                logger.info("  Next record: " + nextRecord);

                System.out.print("Exporting " + totalMatched + " metadata in group: " + groupName);

                exportMetadata(client, config, metadataGroupPath, metadataSearchResults.getMetadata(),
                    groups, group,1, totalMatched);

                logger.info("  Continue (nextRecord > 0) && (nextRecord < totalMatched)?: " +
                    ((nextRecord > 0) && (nextRecord < totalMatched)));

                while ((nextRecord > 0) && (nextRecord < totalMatched)) {

                    int retry = 1;

                    while (retry <= 3) {
                        metadataSearchResults = retrieveMetadataForGroupPaginated(client, groupId, filter, nextRecord);

                        if (metadataSearchResults == null) {
                            retry++;
                        } else {
                            break;
                        }
                    }

                    if ((metadataSearchResults ==  null) && (retry == 3)) {
                        logger.error("Max retry for retrieveMetadataForGroupPaginated");
                        break;
                    }

                    exportMetadata(client, config, metadataGroupPath, metadataSearchResults.getMetadata(),
                        groups, group, nextRecord, totalMatched);

                    totalReturned = metadataSearchResults.getTotalReturned();
                    nextRecord = metadataSearchResults.getNextRecord();

                    logger.info("  Next record: " + nextRecord);
                    logger.info("  Continue (nextRecord > 0) && (nextRecord < totalMatched)?: " +
                        ((nextRecord > 0) && (nextRecord < totalMatched)));

                }

                System.out.println();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    private void execFilterByGroupLocally(GNServicesClient client, ExporterConfig config, Map<String, Group>  groups) throws Exception {
        Path outputPath = config.getOutputPath();

        Path metadataPath = outputPath.resolve("metadata");
        Files.createDirectories(metadataPath);

        String filter = buildCswFilter(config);

        // Create group folders
        groups.forEach((groupName, group) -> {
            try {
                //Files.createDirectories(outputPath.resolve(v));
                int groupId = group.getId();

                Path metadataGroupPath = metadataPath.resolve("group_" + groupId);
                Files.createDirectories(metadataGroupPath);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        MetadataSearchResults metadataSearchResults = client.retrieveMetadataPaginated(filter, 1);
        int totalReturned = metadataSearchResults.getTotalReturned();
        int totalMatched = metadataSearchResults.getTotalMatched();
        int nextRecord = metadataSearchResults.getNextRecord();

        logger.info("Exporting " + totalMatched + " metadata");
        logger.info("  Next record: " + nextRecord);

        System.out.print("Exporting " + totalMatched + " metadata");
        //client.countMetadataForGroup(groupId)

        ConsoleProgress progress = new ConsoleProgress(totalMatched, ". Completed : ");

        exportMetadata2(client, config, metadataPath, metadataSearchResults.getMetadata(),
            groups,1, totalMatched, progress);

        logger.info("  Continue (nextRecord > 0) && (nextRecord < totalMatched)?: " +
            ((nextRecord > 0) && (nextRecord < totalMatched)));

        while ((nextRecord > 0) && (nextRecord < totalMatched)) {

            int retry = 1;

            while (retry <= 3) {
                metadataSearchResults = client.retrieveMetadataPaginated(filter, nextRecord);

                if (metadataSearchResults == null) {
                    retry++;
                } else {
                    break;
                }
            }

            if ((metadataSearchResults ==  null) && (retry == 3)) {
                logger.error("Max retry for retrieveMetadataForGroupPaginated");
                break;
            }

            exportMetadata2(client, config, metadataPath, metadataSearchResults.getMetadata(),
                groups, nextRecord, totalMatched, progress);

            totalReturned = metadataSearchResults.getTotalReturned();
            nextRecord = metadataSearchResults.getNextRecord();

            logger.info("  Next record: " + nextRecord);
            logger.info("  Continue (nextRecord > 0) && (nextRecord < totalMatched)?: " +
                ((nextRecord > 0) && (nextRecord < totalMatched)));

        }


    }

    private MetadataSearchResults retrieveMetadataForGroupPaginated(GNServicesClient client, int groupId, String filter, int nextRecord) {
        try {
            return client.retrieveMetadataForGroupPaginated(groupId, filter, nextRecord);
        } catch (Exception ex) {
            logger.error("retrieveMetadataForGroupPaginated: " + ex.getMessage());
        }

        return null;
    }


    private void exportGroups(GNServicesClient client, Path outputPath, Map<String, Group> groups) throws Exception {
        String groupsFileName = "groups.json";

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String groupList =  gson.toJson(groups.values());

            Files.write(outputPath.resolve(groupsFileName),
                groupList.getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE);
        } catch (Exception ex) {
            logger.error("Error exporting groups: " + ex.getMessage(), ex);
            throw ex;
        }
    }


    private void exportUsers(GNServicesClient client, Path outputPath, User[] users) throws Exception {
        String usersFileName = "users.json";

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String userList =  gson.toJson(users);

            Files.write(outputPath.resolve(usersFileName),
                userList.getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE);
        } catch (Exception ex) {
            logger.error("Error exporting users: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    private void exportMetadata(GNServicesClient client, ExporterConfig config, Path outputPath,
                                Map<String, String> metadata,
                                Map<String, Group> groups, Group exportGroup, int counter, int total) {


        if (metadata.size() == 0) return;

        ConsoleProgress progress = new ConsoleProgress(total, ". Completed : ");

        StringBuilder exportMetadataErrors = new StringBuilder();

        metadata.forEach((uuid, md) -> {

            try {
                //Thread.currentThread().sleep(500);
                progress.updateProgress();
                //String counterValue = HelperUtility.pad(runCount.getAndIncrement() * 100 / total) + "%";

                //System.out.print(StringUtils.repeat("\b", counterSize[0]) + counterValue);
                //counterSize[0] = counterValue.length();

                logger.debug("Retrieving metadata info for metadata: " + uuid);

                // Add request delay to avoid exhausting the server
                if (config.getDefaultRequestDelay() > 0) {
                    Thread.sleep(config.getDefaultRequestDelay());
                }

                MetadataInfo mdInfo = client.retrieveMetadataInfo(uuid, groups);

                String groupOwner =  mdInfo.getPrivileges().getGroupOwner();

                // Check metadata owner group is the same as the group that is being processed
                // This is for LST where CSW GetRecords can't be filtered by group owner
                if (Integer.parseInt(groupOwner) == exportGroup.getId()) {
                    logger.info("  Exporting metadata: " + uuid);

                    String groupName = "";

                    // TODO: To improve this
                    for (Group g : groups.values()) {
                        if (g.getId() == Integer.parseInt(groupOwner)) {
                            groupName = g.getName();
                            break;
                        }
                    }

                    //String groupName = groups.get(groupOwner).getName();
                    if (StringUtils.isNotEmpty(groupName)) {
                        mdInfo.setGroupName(groupName);
                    }

                    String fileName = mdInfo.getPrivileges().getGroupOwner() + "_" +
                        mdInfo.getPrivileges().getOwner() + "_" +  HelperUtility.removeFileSystemSpecialChars(uuid);


                    // Metadata info
                    logger.debug("  Creating metadata info file for metadata: " + uuid);
                    // <group>_<ownwer>_<File-identfier>_info.xml
                    String fileNameMetadataInfo = fileName + "_info.xml";
                    Files.write(outputPath.resolve(fileNameMetadataInfo), mdInfo.getAsXml().getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE);


                    // Metadata xml
                    logger.debug("  Creating metadata xml file for metadata: " + uuid);
                    // <group>_<ownwer>_<File-identfier>.xml
                    String fileNameMetadata = fileName + ".xml";

                    Files.write(outputPath.resolve(fileNameMetadata), md.getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE);
                } else {
                    logger.info("  Skipping metadata: " + uuid + ", not owned by group " + exportGroup.getName() + "(" + exportGroup.getId() + ")");
                }


            } catch (Exception ex) {
                logger.error("Error exporting metadata " + uuid + ", error:" + ex.getMessage(), ex);
                exportMetadataErrors.append("Error exporting metadata " + uuid + ", error:" + ex.getMessage());
                exportMetadataErrors.append("\\n");
            }
        });


        if (StringUtils.isNotEmpty(exportMetadataErrors.toString())) {
            exporterReport.setErrorsExportingMetadata(true);
            exporterReport.setErrorsExportingMetadataText(exportMetadataErrors.toString());
            System.out.print("Exporting metadata errors:" + exportMetadataErrors.toString());
        }
    }


    private void exportMetadata2(GNServicesClient client, ExporterConfig config, Path outputPath,
                                 Map<String, String> metadata,
                                 Map<String, Group> groups, int counter, int total, ConsoleProgress progress) {


        if (metadata.size() == 0) return;

        StringBuilder exportMetadataErrors = new StringBuilder();

        metadata.forEach((uuid, md) -> {

            try {
                //Thread.currentThread().sleep(500);
                progress.updateProgress();
                //String counterValue = HelperUtility.pad(runCount.getAndIncrement() * 100 / total) + "%";

                //System.out.print(StringUtils.repeat("\b", counterSize[0]) + counterValue);
                //counterSize[0] = counterValue.length();

                logger.debug("Retrieving metadata info for metadata: " + uuid);

                // Add request delay to avoid exhausting the server
                if (config.getDefaultRequestDelay() > 0) {
                    Thread.sleep(config.getDefaultRequestDelay());
                }

                MetadataInfo mdInfo = client.retrieveMetadataInfo(uuid, groups);

                String groupOwner =  mdInfo.getPrivileges().getGroupOwner();

                Path outputPathGroup = outputPath.resolve("group_" + groupOwner);

                logger.info("  Exporting metadata: " + uuid + " to folder: " + outputPathGroup.toString());

                String groupName = "";

                // TODO: To improve this
                for (Group g : groups.values()) {
                    if (g.getId() == Integer.parseInt(groupOwner)) {
                        groupName = g.getName();
                        break;
                    }
                }

                //String groupName = groups.get(groupOwner).getName();
                if (StringUtils.isNotEmpty(groupName)) {
                    mdInfo.setGroupName(groupName);
                }

                String fileName = mdInfo.getPrivileges().getGroupOwner() + "_" +
                    mdInfo.getPrivileges().getOwner() + "_" +  HelperUtility.removeFileSystemSpecialChars(uuid);


                // Metadata info
                logger.debug("  Creating metadata info file for metadata: " + uuid);
                // <group>_<ownwer>_<File-identfier>_info.xml
                String fileNameMetadataInfo = fileName + "_info.xml";
                Files.write(outputPathGroup.resolve(fileNameMetadataInfo), mdInfo.getAsXml().getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE);


                // Metadata xml
                logger.debug("  Creating metadata xml file for metadata: " + uuid);
                // <group>_<ownwer>_<File-identfier>.xml
                String fileNameMetadata = fileName + ".xml";

                Files.write(outputPathGroup.resolve(fileNameMetadata), md.getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE);


            } catch (Exception ex) {
                logger.error("Error exporting metadata " + uuid + ", error:" + ex.getMessage(), ex);
                exportMetadataErrors.append("Error exporting metadata " + uuid + ", error:" + ex.getMessage());
                exportMetadataErrors.append("\\n");
            }
        });


        if (StringUtils.isNotEmpty(exportMetadataErrors.toString())) {
            exporterReport.setErrorsExportingMetadata(true);
            exporterReport.setErrorsExportingMetadataText(exportMetadataErrors.toString());
            System.out.print("Exporting metadata errors:" + exportMetadataErrors.toString());
        }
    }


    private void exportGroupsXml(GNServicesClient client, Path outputPath, Map<String, Group> groups) {
        String groupsFileName = "groups.xml";

        Element rootEl = new Element("groups");

        groups.forEach((groupId, group) -> {
            rootEl.addContent(group.toXml());
        });

        try {
            XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
            String groupsInfo = outp.outputString(rootEl);

            Files.write(outputPath.resolve(groupsFileName), groupsInfo.getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE);
        } catch (Exception ex) {
            logger.error("Error exporting groups xml, error:" + ex.getMessage(), ex);
        }
    }


    private void exportUsersXml(GNServicesClient client, Path outputPath, User[] users) {
        String usersFileName = "users.xml";

        Element rootEl = new Element("users");

        for(int i = 0; i < users.length; i++) {
            User user = users[i];

            Element userEl = new Element("user");
            userEl.addContent(new Element("id").setText(user.getId() + ""));
            userEl.addContent(new Element("name").setText(user.getUsername()));

            Element userGroupsEl = new Element("userGroups");
            /*user.getUserGroups().forEach(ug -> {

                Element userGroupEl = new Element("group");
                userGroupEl.addContent(new Element("groupId").setText(ug.getGroupId()));
                userGroupEl.addContent(new Element("profile").setText(ug.getProfile()));

                userGroupsEl.addContent(userGroupEl);
            });*/

            userEl.addContent(userGroupsEl);
            rootEl.addContent(userEl);
        };


        try {
            XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
            String groupsInfo = outp.outputString(rootEl);

            Files.write(outputPath.resolve(usersFileName), groupsInfo.getBytes(Charset.forName("UTF8")), StandardOpenOption.CREATE);
        } catch (Exception ex) {
            logger.error("Error exporting users xml, error:" + ex.getMessage(), ex);
        }
    }


    /**
     * Builds CSW CQL filter query to exclude the harvested metadata and
     * filtering for the metadata types configured.
     *
     * @param config
     * @return
     */
    private String buildCswFilter(ExporterConfig config) {
        String filter = config.getDefaultCswConstraint();

        if (StringUtils.isEmpty(filter)) {
            filter = "_isHarvested%3D'n'";
        } else {
            filter = filter + "%20AND%20_isHarvested%3D'n'";
        }

        List<String> metadataTypes;
        String filterMetadataType = "";

        if (StringUtils.isNotEmpty(config.getDefaultExportMetadataTypes())) {
            metadataTypes = Arrays.asList(config.getDefaultExportMetadataTypes().split(","));

            filterMetadataType = metadataTypes.stream()
                .map(s -> "type='" + s + "'")
                .collect(Collectors.joining("%20OR%20", "%20AND%20(", ")"));
        }

        if (StringUtils.isNotEmpty(filterMetadataType)) {
            filter = filter + filterMetadataType;
        }

        String filterDate = "";
        LocalDateTime metadataModifiedDateSinceFilter = config.getDefaultMetadataModifiedDateSinceFilter();

        if (metadataModifiedDateSinceFilter != null) {
            filterDate = "%20AND%20Modified%3E%3D'" +
                metadataModifiedDateSinceFilter.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) + "'";
        }

        if (StringUtils.isNotEmpty(filterDate)) {
            filter = filter + filterDate;
        }

        return filter;
    }

}
