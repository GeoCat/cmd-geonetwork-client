package com.geocat.gnclient.importer;

import com.geocat.gnclient.gnservices.GNServicesClient;
import com.geocat.gnclient.gnservices.groups.model.Group;
import com.geocat.gnclient.gnservices.metadata.model.MetadataPrivilege;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Namespace;
import org.xml.sax.InputSource;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.DOMBuilder;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import com.geocat.gnclient.gnservices.metadata.model.MetadataStatus;
import com.geocat.gnclient.gnservices.metadata.model.Operations;
import com.geocat.gnclient.gnservices.users.model.User;
import com.geocat.gnclient.util.ConsoleProgress;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 * Imports users/groups/metadata to a GeoNetwork instance from a provided folder.
 *
 */
public class Importer {
    private static final Logger logger = LogManager.getLogger(Importer.class.getName());

    private Map<String, Group> remoteGroups = new HashMap<>();
    private User[] remoteUsers;
    private ImporterReport importerReport;
    private MetadataImportCsvLog metadataImportCsvLog;

    public ImporterReport getImporterReport() {
        return importerReport;
    }

    public MetadataImportCsvLog getMetadataImportCsvLog() {
        return metadataImportCsvLog;
    }

    /**
     * Executes importer task and returns the following codes:
     *
     *      0 = Data was found and imported with no errors
     *      1 = No data was found and imported
     *      2 = Data was found and imported with errors
     *
     * @param client
     * @param config
     * @return
     * @throws Exception
     */
    public int exec(GNServicesClient client, ImporterConfig config) throws Exception {
        if (!Files.exists(config.getOutputPath())) {
            System.out.println("Import path doesn't exists: " + config.getOutputPath());
            logger.info("Import path doesn't exists: " + config.getOutputPath());
            return 1;
        }

        Path metadataPath = config.getOutputPath().resolve("metadata");

        if (!Files.exists(metadataPath)) {
            System.out.println("Metadata folder doesn't exists in import path: " + metadataPath + ", will use the import path: " + config.getOutputPath());
            logger.info("Metadata folder doesn't exists in import path: " + metadataPath + ", will use the import path: " + config.getOutputPath());
        }

        importerReport = new ImporterReport();

        metadataImportCsvLog = new MetadataImportCsvLog();

        if (config.isDeleteMetadataAndNonDefaultUsersAndGroups()) {
            importerReport.setRemoveData(true);
            removeExistingData(client, config);

        } else {
            importerReport.setRemoveData(false);
        }

        // Load groups / users from the Geonetwork instance to import the metadata
        remoteGroups = client.retrieveGroups();
        remoteUsers = client.retrieveUsers();

        // Import groups
        Map<Integer, Integer> groupMapper = importGroups(client, config);

        // Import users
        Map<Integer, Integer> userMapper = importUsers(client, config, groupMapper);

        // Import metadata
        importMetadata(client, config, groupMapper, userMapper);

        if (importerReport.isErrorsImportingMetadata()) {
            return 2;
        } else {
            return 0;
        }
    }


    private void removeExistingData(GNServicesClient client, ImporterConfig config) throws Exception {

        StringBuilder removeDataErrors = new StringBuilder();

        Path groupsFilePath = config.getOutputPath().resolve("groups.json");

        if (Files.exists(groupsFilePath)) {
            remoteGroups = client.retrieveGroups();

            remoteGroups.forEach((groupName, group) -> {
                try {

                    System.out.println("Removing metadata for group: " + groupName);
                    Map<String, String> metadataForGroup = client.retrieveAllMetadataForGroup(group.getId(), "");

                    metadataForGroup.forEach((uuid, md) -> {
                        try {
                            client.deleteMetadata(uuid);
                        } catch (Exception ex) {
                            logger.error("Error removing metadata '" + uuid + "' for  group: " + groupName, ex);
                            removeDataErrors.append("Error removing metadata '" + uuid + "' for  group: " + groupName + ", " + ex.getMessage());
                            removeDataErrors.append("\n");
                        }
                    });

                    System.out.println("Removing group: " + groupName);
                    client.deleteGroup(group);

                } catch (Exception ex) {
                    logger.error("Error removing group: " + groupName, ex);
                    removeDataErrors.append("Error removing group: " + groupName + ", " + ex.getMessage());
                    removeDataErrors.append("\n");
                }
            });
        } else {
            System.out.println("Groups file 'groups.json' not found, skipping removal of existing groups (-r option)'.");
        }

        Path usersFilePath = config.getOutputPath().resolve("users.json");

        if (Files.exists(usersFilePath)) {
            System.out.println("Removing users without profiles 'Administrator'.");
            remoteUsers = client.retrieveUsers();

            // Remove users that are not Administrator or UserAdmin
            for(int i = 0; i < remoteUsers.length; i++) {
                try {
                    if (!remoteUsers[i].getProfile().equalsIgnoreCase("administrator")) {
                        System.out.println("Removing user: " + remoteUsers[i].getUsername());
                        client.deleteUser(remoteUsers[i]);
                    }
                } catch (Exception ex) {
                    logger.error("Error removing user '" + remoteUsers[i].getUsername(), ex);
                    removeDataErrors.append("Error removing user '" + remoteUsers[i].getUsername() + ", " + ex.getMessage());
                    removeDataErrors.append("\n");
                }
            }
        } else {
            System.out.println("Users file 'users.json' not found, skipping removal of existing users without profiles 'Administrator'  (-r option).");
        }


        if (StringUtils.isNotEmpty(removeDataErrors.toString())) {
            importerReport.setErrorsRemovingData(true);
            importerReport.setErrorsRemoveDataText(removeDataErrors.toString());
            System.out.print("Removing data errors:" + removeDataErrors.toString());
        } else {
            importerReport.setErrorsRemovingData(false);
        }
    }

    private void importMetadata(GNServicesClient client, ImporterConfig config,
                                Map<Integer, Integer> groupMapper,
                                Map<Integer, Integer> userMapper) throws IOException {
        Path metadataPath = config.getOutputPath().resolve("metadata");

        if (!Files.exists(metadataPath)) {
            // Try to use the output path configured to import the metadata
            metadataPath = config.getOutputPath();
        }

        long totalMetadata = Files.walk(metadataPath)
            .filter(p -> !p.toString().endsWith("_info.xml") && p.toString().endsWith(".xml")).count();

        System.out.print("Importing metadata.");
        ConsoleProgress progress = new ConsoleProgress(totalMetadata, " Completed : ");

        StringBuilder metadataImportErrors = new StringBuilder();

        File f = new File(metadataPath.toString());

        int count = f.listFiles(p -> !p.toString().endsWith("_info.xml") && p.toString().endsWith(".xml")).length;

        importerReport.setExistsMetadataToImport(importerReport.isExistsMetadataToImport() || (count > 0));

        metadataImportCsvLog.initCsv("import-metadata-result-" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss")) + ".csv");

        try(Stream<Path> paths = Files.walk(metadataPath)
            .filter(p -> !p.toString().endsWith("_info.xml") && p.toString().endsWith(".xml"))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        /**
                         * Set status to OK by default
                         *
                         * Status values:
                         *      - 0: Import OK
                         *      - 1: Error importing metadata
                         *      - 2: Error assigning privileges
                         *      - 3: Error assigning status
                         */
                        int importStatus = 0;
                        String importStatusMessage = "";

                        progress.updateProgress(filePath.toString());
                        //System.out.println(filePath.toString());

                        // Read metadata file
                        String xmlFile = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                        //System.out.println(xmlFile);

                        String uuid = extractUuid(xmlFile);

                        String infoFileName = filePath.toString().replace(".xml", "_info.xml");
                        Path infoFile = Paths.get(infoFileName);
                        boolean existsInfoFile = Files.exists(infoFile);

                        // Process info file if exists, otherwise returns default configuration
                        Map<String, String> metadataInfo = processInfoFile(client, config, Paths.get(infoFileName), uuid);
                        int ownerId = Integer.parseInt(metadataInfo.get("ownerId"));
                        int groupId = Integer.parseInt(metadataInfo.get("groupId"));

                        int destGroupId;
                        int destOwnerId;

                        if (existsInfoFile) {
                            destGroupId = groupMapper.get(groupId);
                            destOwnerId = userMapper.get(ownerId);
                        } else {
                            // No info file, the default ownerId and groupId correspond to the remote server, no need to apply any mapping
                            destGroupId = groupId;
                            destOwnerId = ownerId;
                        }

                        String status = metadataInfo.get("status");
                        String importXslName =  config.getImportRemoteXslt();

                        long mdId = -1;
                        Exception exImport = null;

                        try {
                            // Applies xslt to xml if required
                            xmlFile = applyXslt(xmlFile, config.getImportLocalXslt());

                            mdId = client.insertMetadata(xmlFile, uuid, destOwnerId, destGroupId, "true", "true",
                                status, "n", "iso19139", importXslName);

                        } catch (Exception ex) {
                            importStatus = 1;
                            exImport = ex;
                        }

                        if (importStatus == 0) {
                            try {
                                // Retrieves the privileges/status. Process info file if exists, otherwise returns default configuration
                                List<MetadataPrivilege> privileges = processInfoFilePrivileges(config, Paths.get(infoFileName), groupMapper);

                                // Updates metadata privileges
                                client.updateMetadataPrivilege(mdId, privileges);

                            } catch (Exception ex) {
                                importStatus = 2;
                                exImport = ex;
                            }
                        }

                        if (importStatus == 0) {
                            try {
                                MetadataStatus st = new MetadataStatus();
                                st.setName(status);
                                st.setChangeDate(metadataInfo.get("statusDate"));
                                st.setChangeMessage(metadataInfo.get("statusMessage"));
                                st.setUserId(metadataInfo.get("statusUser"));

                                // Updates metadata status
                                client.updateMetadataStatus(uuid, mdId, st);
                            } catch (Exception ex) {
                                importStatus = 3;
                                exImport = ex;
                            }
                        }


                        if (exImport != null) {
                            importStatusMessage = exImport.getMessage();
                        }

                        metadataImportCsvLog.addMetadataImportEntry(LocalDateTime.now(), importStatus, importStatusMessage,
                            filePath.toString());

                        if (exImport != null) {
                            throw exImport;
                        }

                    } catch (Exception ex) {
                        logger.error("Error processing metadata file " + filePath, ex);
                        metadataImportErrors.append("Error processing metadata file " + filePath + ", " + ex.getMessage());
                        metadataImportErrors.append("\n");
                    }

                }
            });
        } finally {
            metadataImportCsvLog.closeCsv();
        };

        if (StringUtils.isNotEmpty(metadataImportErrors.toString())) {
            importerReport.setErrorsImportingMetadata(true);
            importerReport.setErrorsImportingMetadataText(metadataImportErrors.toString());
            System.out.print("Importing metadata errors:" + metadataImportErrors.toString());
        } else {
            importerReport.setErrorsImportingMetadata(false);
        }
    }


    private Map<Integer, Integer> importUsers(GNServicesClient client,
                                              ImporterConfig config,
                                              Map<Integer, Integer> groupMapper) throws Exception {

        Map<String, User> usersExported = new HashMap<>();

        // Load remote users
        Map<String, User> remoteUsersMap = new HashMap<>();
        for (User user : remoteUsers) {
            remoteUsersMap.put(user.getUsername(), user);
        }

        Map<Integer, Integer> userMapper = new HashMap<>();

        // Load users from export
        Path usersFilePath = config.getOutputPath().resolve("users.json");

        if (Files.exists(usersFilePath)) {
            importerReport.setExistsUsersFile(true);

            usersExported = processUsersFile(usersFilePath);

            System.out.print("Importing users.");
            ConsoleProgress progress = new ConsoleProgress(usersExported.size(), " Completed : ");

            // Create/update users
            StringBuilder userImportErrors = new StringBuilder();

            usersExported.forEach((userName, user) -> {
                try {
                    progress.updateProgress();

                    if (remoteUsersMap.containsKey(userName)) {
                        // Update the user

                        // Map exported groups id with remote groups for metadata import
                        userMapper.put(user.getId(), remoteUsersMap.get(userName).getId());

                        // Assign user to update
                        user.setId(remoteUsersMap.get(userName).getId());

                        updateUserGroupsWithMappedGroups(user, groupMapper);

                        client.updateUser(user);

                    } else {
                        // Create the user
                        int importedUserId = user.getId();

                        // Reset user id
                        user.setId(-99);
                        user.setPassword(config.getDefaultUsersPassword());

                        updateUserGroupsWithMappedGroups(user, groupMapper);

                        int userId = client.createUser(user);

                        // Map exported groups id with remote groups for metadata import
                        userMapper.put(importedUserId, userId);
                    }

                } catch (Exception ex) {
                    logger.error("Error processing user " + userName, ex);
                    userImportErrors.append("Error processing user " + userName + ", " + ex.getMessage());
                    userImportErrors.append("\n");
                }
            });

            if (StringUtils.isNotEmpty(userImportErrors.toString())) {
                importerReport.setErrorsImportingUsers(true);
                importerReport.setErrorsImportingUsersText(userImportErrors.toString());
                System.out.print("Importing groups errors:" + userImportErrors.toString());
            } else {
                importerReport.setErrorsImportingUsers(false);
            }

        } else {
            importerReport.setExistsUsersFile(false);
            System.out.print("Users file 'users.json' not found, ignoring import of users.");
        }

        System.out.println();
        // Map exported groups id with remote users for metadata import (by user name)

        return userMapper;
    }


    private Map<Integer, Integer> importGroups(GNServicesClient client, ImporterConfig config) throws Exception {

        Map<String, Group> groupsExported = new HashMap<>();

        // Load remote groups
        Map<Integer, Integer> groupMapper = new HashMap<>();

        // Load groups from export
        Path groupsFilePath = config.getOutputPath().resolve("groups.json");

        if (Files.exists(groupsFilePath)) {
            importerReport.setExistsGroupsFile(true);

            groupsExported = processGroupsFile(groupsFilePath);

            System.out.print("Importing groups.");
            ConsoleProgress progress = new ConsoleProgress(groupsExported.size(), ". Completed : ");

            // Create/update groups
            StringBuilder groupImportErrors = new StringBuilder();

            groupsExported.forEach((groupName, group) -> {
                try {
                    progress.updateProgress();

                    if (remoteGroups.containsKey(groupName)) {
                        // Update the group

                        // Map exported groups id with remote groups for metadata import
                        groupMapper.put(group.getId(), remoteGroups.get(groupName).getId());

                        // Assign group to update
                        group.setId(remoteGroups.get(groupName).getId());

                        client.updateGroup(group);

                    } else {
                        // Create the group

                        int importedGroupId = group.getId();

                        // Reset group id
                        group.setId(-99);

                        int groupId = client.createGroup(group);

                        // Map exported groups id with remote groups for metadata import
                        groupMapper.put(importedGroupId, groupId);

                        group.setId(groupId);

                    }

                } catch (Exception ex) {
                    logger.error("Error processing group " + groupName, ex);
                    groupImportErrors.append("Error processing group " + groupName + ", " + ex.getMessage());
                    groupImportErrors.append("\n");
                }
            });

            if (StringUtils.isNotEmpty(groupImportErrors.toString())) {
                importerReport.setErrorsImportingGroups(true);
                importerReport.setErrorsImportingGroupsText(groupImportErrors.toString());
                System.out.print("Importing groups errors:" + groupImportErrors.toString());

            } else {
                importerReport.setErrorsImportingGroups(false);
            }

        } else {
            importerReport.setExistsGroupsFile(false);
            System.out.print("Groups file 'groups.json' not found, ignoring import of groups.");
        }

       System.out.println();

        return groupMapper;
    }


    private Map<String, String> processInfoFile(GNServicesClient client, ImporterConfig config, Path filePath, String uuid) throws Exception {
        Map<String, String> metadataInfo = new HashMap<>();

        if (Files.exists(filePath)) {
            // Read info file
            String infoFile = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            //System.out.println(infoFile);

            SAXBuilder saxBuilder = new SAXBuilder();
            XPathFactory xpfac = XPathFactory.instance();
            org.jdom2.Document responseDoc = saxBuilder.build(new StringReader(infoFile));

            //String uuid = getXmlValue(responseDoc, "/info/uuid", xpfac);
            metadataInfo.put("uuid", uuid);

            String groupId = getXmlValue(responseDoc, "/info/groupId", xpfac);
            metadataInfo.put("groupId", groupId);

            String ownerId = getXmlValue(responseDoc, "/info/owner", xpfac);
            metadataInfo.put("ownerId", ownerId);

            String status = getXmlValue(responseDoc, "/info/status/name", xpfac);
            metadataInfo.put("status", status);

            String statusMessage = getXmlValue(responseDoc, "/info/status/changeMessage", xpfac);
            metadataInfo.put("statusMessage", statusMessage);

            String statusDate = getXmlValue(responseDoc, "/info/status/changeDate", xpfac);
            metadataInfo.put("statusDate", statusDate);

            String statusUser = getXmlValue(responseDoc, "/info/status/userId", xpfac);
            metadataInfo.put("statusUser", statusUser);
        } else {
            Group defaultImportGroup = remoteGroups.get(config.getDefaultImportGroup());

            if (defaultImportGroup != null) {
                metadataInfo.put("groupId", defaultImportGroup.getId() + "");
            } else {
                // Set sample group
                metadataInfo.put("groupId", "2");
            }

            int defaultImportUserId = 1; // admin user

            for(User u : remoteUsers) {
                if (u.getUsername().equalsIgnoreCase(config.getDefaultImportUser())) {
                    defaultImportUserId = u.getId();
                    break;
                }
            }

            // Use default values
            metadataInfo.put("uuid", uuid);
            metadataInfo.put("ownerId",  defaultImportUserId + "");
            metadataInfo.put("status", config.getDefaultImportStatus());
            metadataInfo.put("statusMessage", config.getDefaultImportStatus());
            String currentDate = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
            metadataInfo.put("statusDate", currentDate);
            metadataInfo.put("statusUser", defaultImportUserId + "");
        }


        return metadataInfo;
    }

    private List<MetadataPrivilege> processInfoFilePrivileges(ImporterConfig config, Path filePath, Map<Integer, Integer> groupMapper) throws Exception {
        List<MetadataPrivilege> metadataPrivilegeList = new ArrayList<>();

        if (Files.exists(filePath)) {
            // Read info file
            String infoFile = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            //System.out.println(infoFile);


            SAXBuilder saxBuilder = new SAXBuilder();
            XPathFactory xpfac = XPathFactory.instance();
            org.jdom2.Document responseDoc = saxBuilder.build(new StringReader(infoFile));

            XPathExpression<Element> xp = xpfac.compile("/info/privileges/privilege", Filters.element(),null);
            List<Element> privileges = xp.evaluate(responseDoc);

            privileges.forEach(p -> {
                MetadataPrivilege priv = new MetadataPrivilege();

                XPathExpression<Element> xp1 = xpfac.compile("group", Filters.element(),null);
                Element groupEl = xp1.evaluateFirst(p);

                String importedGroup = groupEl.getValue().trim();

                if (groupMapper.containsKey(Integer.parseInt(importedGroup))) {
                    priv.setGroup(groupMapper.get(Integer.parseInt(importedGroup)) + "");
                } else {
                    priv.setGroup(importedGroup);
                }


                xp1 = xpfac.compile("view", Filters.element(),null);
                Element op = xp1.evaluateFirst(p);
                priv.getOperations().setView(Boolean.parseBoolean(op.getValue().trim()));

                xp1 = xpfac.compile("editing", Filters.element(),null);
                op = xp1.evaluateFirst(p);
                priv.getOperations().setEditing(Boolean.parseBoolean(op.getValue().trim()));

                xp1 = xpfac.compile("download", Filters.element(),null);
                op = xp1.evaluateFirst(p);
                priv.getOperations().setDownload(Boolean.parseBoolean(op.getValue().trim()));

                xp1 = xpfac.compile("featured", Filters.element(),null);
                op = xp1.evaluateFirst(p);
                priv.getOperations().setFeatured(Boolean.parseBoolean(op.getValue().trim())) ;

                xp1 = xpfac.compile("dynamic", Filters.element(),null);
                op = xp1.evaluateFirst(p);
                priv.getOperations().setDynamic(Boolean.parseBoolean(op.getValue().trim())); ;

                xp1 = xpfac.compile("notify", Filters.element(),null);
                op = xp1.evaluateFirst(p);
                priv.getOperations().setNotify(Boolean.parseBoolean(op.getValue().trim())); ;

                metadataPrivilegeList.add(priv);
            });
        } else {
            // Use default values
            String defaultPrivileges = config.getDefaultImportPrivileges();
            logger.info("defaultPrivileges: " + defaultPrivileges);

            String[] privGroups = defaultPrivileges.split(";");

            for (String privInGroup : privGroups) {
                // Format GROUPNAME:priv1#...#;GROUPNAME#priv1#...

                String group = privInGroup.split(":")[0];

                boolean specialGroup = (group.equalsIgnoreCase("all") ||
                    group.equalsIgnoreCase("intranet") ||
                    group.equalsIgnoreCase("guest"));

                int groupId = -99;

                if (group.equalsIgnoreCase("all")) {
                    groupId = 1;
                } else if (group.equalsIgnoreCase("intranet")) {
                    groupId = 0;
                } else if (group.equalsIgnoreCase("guest")) {
                    groupId = -1;
                } else if (remoteGroups.containsKey(group)) {
                    groupId = remoteGroups.get(group).getId();
                }


                if (groupId != -99) {
                    String[] ops = privInGroup.split(":")[1].split("#");

                    MetadataPrivilege metadataPrivilege = new MetadataPrivilege();
                    Operations op = new Operations();

                    for (String opVal : ops) {
                        if (opVal.equalsIgnoreCase("publish")) {
                            op.setView(true);
                        } else if (opVal.equalsIgnoreCase("download")) {
                            op.setDownload(true);
                        } else if (opVal.equalsIgnoreCase("editing")) {
                            op.setEditing(true);
                        } else if (opVal.equalsIgnoreCase("notify")) {
                            op.setNotify(true);
                        } else if (opVal.equalsIgnoreCase("featured")) {
                            op.setFeatured(true);
                        } else if (opVal.equalsIgnoreCase("dynamic")) {
                            op.setDynamic(true);
                        }
                    }

                    metadataPrivilege.setGroup(groupId + "");
                    metadataPrivilege.setOperations(op);

                    metadataPrivilegeList.add(metadataPrivilege);
                }
            }
        }

        return metadataPrivilegeList;
    }


    private Map<String, Group> processGroupsFile(Path filePath) throws Exception {
        String groups = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);

        Gson gson = new Gson();
        Group[] groupList = gson.fromJson(groups, Group[].class);

        Map<String, Group> groupsMap = new HashMap<>();
        for (Group group : groupList) {
            groupsMap.put(group.getName(), group);
        }

        return groupsMap;
    }


    private Map<String, User>  processUsersFile(Path filePath) throws Exception {
        String users = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);

        Gson gson = new Gson();
        User[] userList = gson.fromJson(users, User[].class);

        Map<String, User> userMap = new HashMap<>();
        for (User user : userList) {
            userMap.put(user.getUsername(), user);
        }

        return userMap;
    }



    private String applyXslt(String xml, String importXslt) throws Exception {
        // Apply xslt to metadata
        if (StringUtils.isNotEmpty(importXslt) &&
            Files.exists( Paths.get(importXslt))) {

            //  read the XML to a JDOM2 document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder dombuilder = factory.newDocumentBuilder();

            InputSource inputSource = new InputSource( new StringReader( xml ) );

            org.w3c.dom.Document w3cDocument = dombuilder.parse(inputSource);
            DOMBuilder jdomBuilder = new DOMBuilder();
            Document jdomDocument = jdomBuilder.build(w3cDocument);

            // create the JDOMSource from JDOM2 document
            JDOMSource source = new JDOMSource(jdomDocument);

            // create the transformer
            Transformer transformer = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
                    .newTransformer(new StreamSource(importXslt));

            // create the JDOMResult object
            JDOMResult out = new JDOMResult();

            // perform the transformation
            transformer.transform(source, out);

            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            return outputter.outputString(out.getDocument());

        } else {
            return xml;
        }
    }

    private Map<String, Group> processGroupsFileXml(Path filePath) throws Exception {
        Map<String, Group> groups = new HashMap<>();

        // Read info file
        String infoFile = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        //System.out.println(infoFile);


        SAXBuilder saxBuilder = new SAXBuilder();
        XPathFactory xpfac = XPathFactory.instance();
        org.jdom2.Document responseDoc = saxBuilder.build(new StringReader(infoFile));


        List<Element> groupElList = getElements(responseDoc, "/groups/group", xpfac);

        for (Element element : groupElList) {
            Group g = new Group();

            g.setId(Integer.parseInt(getXmlValue(element, "id", xpfac)));
            g.setName(getXmlValue(element, "name", xpfac));
            g.setLogo(getXmlValue(element, "logo", xpfac));

            String defaultCategory = getXmlValue(element, "defaultCategory", xpfac);
            if (StringUtils.isNotEmpty(defaultCategory)) {
                g.setDefaultCategory(defaultCategory);
            }

            g.setDescription(getXmlValue(element, "description", xpfac));
            g.setEmail(getXmlValue(element, "email", xpfac));
            g.setWebsite(getXmlValue(element, "website", xpfac));

            groups.put(g.getName(), g);
        }

        return groups;
    }


    private String getXmlValue(Element el, String xpath, XPathFactory xpfac) {
        String value = "";

        XPathExpression<Element> xp = xpfac.compile(xpath, Filters.element(),null);
        Element elValue = xp.evaluateFirst(el);

        if (elValue != null) {
            value = elValue.getValue().trim();
        }

        return value;
    }

    private String getXmlValue(Document doc, String xpath, XPathFactory xpfac) {
        String value = "";

        XPathExpression<Element> xp = xpfac.compile(xpath, Filters.element(),null);
        Element elValue = xp.evaluateFirst(doc);

        if (elValue != null) {
            value = elValue.getValue().trim();
        }

        return value;
    }

    private List<Element> getElements(Document doc, String xpath, XPathFactory xpfac) {
        String value = "";

        XPathExpression<Element> xp = xpfac.compile(xpath, Filters.element(),null);
        List<Element> list = xp.evaluate(doc);

        return list;
    }


    private void updateUserGroupsWithMappedGroups(User user, Map<Integer, Integer> groupMapper) {
        List<String> groupsRegisteredUserMapped = new ArrayList<>();
        user.getGroupsRegisteredUser().forEach(g -> groupsRegisteredUserMapped.add(groupMapper.get(Integer.parseInt(g)) + ""));
        user.setGroupsRegisteredUser(groupsRegisteredUserMapped);

        List<String> groupsEditorMapped = new ArrayList<>();
        user.getGroupsEditor().forEach(g -> groupsEditorMapped.add(groupMapper.get(Integer.parseInt(g)) + ""));
        user.setGroupsEditor(groupsEditorMapped);

        List<String> groupsReviewerMapped = new ArrayList<>();
        user.getGroupsReviewer().forEach(g -> groupsReviewerMapped.add(groupMapper.get(Integer.parseInt(g)) + ""));
        user.setGroupsReviewer(groupsReviewerMapped);

        List<String> groupsUserAdminMapped = new ArrayList<>();
        user.getGroupsUserAdmin().forEach(g -> groupsUserAdminMapped.add(groupMapper.get(Integer.parseInt(g)) + ""));
        user.setGroupsUserAdmin(groupsUserAdminMapped);
    }


    private String extractUuid(String xml) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        XPathFactory xpfac = XPathFactory.instance();

        org.jdom2.Document responseDoc = saxBuilder.build(new StringReader(xml
        ));

        XPathExpression<Element> xp = xpfac.compile("/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString",
            Filters.element(),
            null,
            Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd"),
            Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco"));

        Element uuidEl = xp.evaluateFirst(responseDoc);

        return uuidEl.getValue();
    }
}
