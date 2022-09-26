package com.geocat.gnclient.gnservices.metadata;

import com.geocat.gnclient.constants.Geonetwork;
import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.groups.model.Group;
import com.geocat.gnclient.gnservices.metadata.model.MetadataPrivilege;
import com.geocat.gnclient.gnservices.metadata.model.MetadataSearchResults;
import com.geocat.gnclient.gnservices.metadata.model.MetadataSharing;
import com.geocat.gnclient.gnservices.metadata.model.MetadataStatus;
import com.geocat.gnclient.gnservices.metadata.model.v4.MetadataStatusParameter;
import com.geocat.gnclient.gnservices.metadata.model.v4.MetadataStatusV4;
import com.geocat.gnclient.util.HelperUtility;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class GNMetadataServices4 extends GNMetadataServices {
    private static final Logger logger = LogManager.getLogger(GNMetadataServices4.class);


    public GNMetadataServices4(String baseUrl, String cswEndpoint) {
        super(baseUrl, cswEndpoint);
        metadataServiceUrl = HelperUtility.addPathToUrl(baseUrl, "srv/api/records");
    }


    @Override
    public MetadataSharing getMetadataSharing(String metadataUuid, Map<String, Group> groups, GNConnection connection) throws Exception {
        MetadataSharing metadataSharingList = null;

        String uuidToCheck = metadataUuid.replace("_utkast", "");

        boolean isUuidValue = HelperUtility.isUUID(uuidToCheck);

        int mdId = -1;

        if (!isUuidValue) {
            mdId = retrieveMetadataId(metadataUuid, connection);
        }

        if (mdId > -1) {
            metadataUuid = mdId + "";
        }

        try {
            String response = doGet(connection,
                HelperUtility.addPathToUrl(metadataServiceUrl,  metadataUuid + "/sharing"), "application/json");

            Gson gson = new Gson();
            MetadataSharing groupsArray = gson.fromJson(response, MetadataSharing.class);

            metadataSharingList = groupsArray;

        } catch (IOException ex) {
            // log
            ex.printStackTrace();
        }

        return metadataSharingList;
    }


    @Override
    public List<MetadataStatusV4> getMetadataStatusV4(String metadataUuid, GNConnection connection) throws Exception {
        List<MetadataStatusV4> metadataStatus = new ArrayList<>();

        try {
            String response =  doGet(connection,
                HelperUtility.addPathToUrl(baseUrl,  "srv/api/records/" + metadataUuid + "/status"),
                "application/json");

            if (StringUtils.isNotEmpty(response)) {
                Gson gson = new Gson();
                MetadataStatusV4[] metadataStatusArray = gson.fromJson(response, MetadataStatusV4[].class);

                metadataStatus = Arrays.asList(metadataStatusArray);
            }

        } catch (IOException ex) {
            // log
            ex.printStackTrace();
        }

        return metadataStatus;
    }


    @Override
    public long countMetadataForGroup(int groupId, GNConnection connection) throws Exception {
        long count = 0;

        try {
            //http://localhost:8080/geonetwork/srv/eng/q?&_groupOwner=2&fast=&summaryOnly=true&_content_type=json
            String response = doGet(connection,
                HelperUtility.addPathToUrl(baseUrl,
                    "srv/eng/q?fast=&summaryOnly=true&_content_type=json&_groupOwner=" + groupId),
                "application/json");


            JsonParser parser = new JsonParser();
            JsonObject rootObj = (JsonObject) parser.parse(response).getAsJsonArray().get(0);

            count = rootObj.get("@count").getAsLong();

        } catch (IOException ex) {
            // log
            ex.printStackTrace();
        }

        return count;
    }


    @Override
    public long insertMetadata(String metadata, String uuid,  int ownerId, int groupOwnerId, String fullRightsForOwnerGroup,
                                  String metadataPublic, String status, String isTemplate, String schema, String styleSheet,
                                  GNConnection connection) throws Exception {


        if(metadata.contains("<?xml")) {
            //  else, gn throws exception while inserting: content not allowed in prolog
            metadata = metadata.substring(metadata.indexOf("?>") + 2);
        }

        if (StringUtils.isEmpty(styleSheet)) {
            styleSheet = "_none_";
        }

        String reqXml = "<request>" +
            "<owner>"+ownerId+"</owner>" +
            "<group>"+groupOwnerId+"</group>" +
            "<fullRightsForGroup>"+fullRightsForOwnerGroup+"</fullRightsForGroup>" +
            "<metadataPublic>"+metadataPublic+"</metadataPublic>" +
            "<status>"+status+"</status>" +
            "<schema>"+schema+"</schema>" +
            "<isTemplate>"+ isTemplate +"</isTemplate>" +
            "<styleSheet>"+ styleSheet +"</styleSheet>" +
            "<category>_none_</category>" +
            "<data>" +
            "<![CDATA["+ metadata +"]]>" +
            "</data>" +
            "<uuidAction>overwrite</uuidAction>" +
            "<skipIndexing>false</skipIndexing>" +
            "<notify>false</notify>" +
            "</request>";

        long metadataId = -1;

        try {
            String url = metadataServiceUrl + "?group=" + groupOwnerId +
                "&publishToAll="+ metadataPublic + "&metadataType=" + (isTemplate=="y"?"TEMPLATE":"METADATA") + "&transformWith=" + styleSheet +
                "&uuidProcessing=OVERWRITE";

            String response = doPut(connection, url,
                metadata, "application/json", "application/xml");

            JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();

            metadataId = Integer.parseInt(jsonObject.get("metadataInfos").getAsJsonObject().keySet().toArray(new String[0])[0]);

            String urlOwnership = metadataServiceUrl + "/" + metadataId + "/ownership" +
                "?groupIdentifier=" + groupOwnerId + "&userIdentifier=" + ownerId + "&approved=false";

            String responseOwnership = doPut(connection, urlOwnership,
                metadata, "application/json", "application/xml");

        } catch (Exception ex) {
            logger.error("insertMetadata metadata " + uuid, ex);
            throw new Exception("Error inserting metadata " + uuid + ", error: " + ex.getMessage());
        }

        return metadataId;
    }


    @Override
    public boolean updateMetadataPrivilege(long metadataId, List<MetadataPrivilege> privileges, GNConnection connection) throws Exception {
        boolean isPrivilegeUpdated = false;
        logger.info("updateMetadataPrivilege, metadata: " + metadataId);

        try {
            String url = HelperUtility.addPathToUrl(metadataServiceUrl, metadataId + "/sharing");
            logger.info("updateMetadataPrivilege, url: " + url);

            String request = "{\n" +
                "  \"clear\": true,\n" +
                "  \"privileges\": [\n" +
                "    @@privileges@@\n" +
                "  ]\n" +
                "}";

            List<String> privList = new ArrayList<>();

            privileges.forEach(p -> {

                String priv = "{\n" +
                    "  \"group\": " + p.getGroup() + ",\n" +
                    "  \"operations\": {\n" +
                    "    \"view\": " + p.getOperations().isView() + ",\n" +
                    "    \"editing\": " + p.getOperations().isEditing() + ",\n" +
                    "    \"download\": " + p.getOperations().isDownload() + ",\n" +
                    "    \"dynamic\": " + p.getOperations().isDynamic() + ",\n" +
                    "    \"featured\": " + p.getOperations().isFeatured() + ",\n" +
                    "    \"notify\": " + p.getOperations().isNotify() + "\n" +
                    "  }\n" +
                    "}";

                privList.add(priv);


            });

            request = request.replaceFirst("@@privileges@@", String.join(", ", privList));
            logger.info("updateMetadataPrivilege, request: " + request);

            String response = doPut(connection, url, request, "application/json", "application/json");

            logger.info("updateMetadataPrivilege, response: " + response);

            isPrivilegeUpdated = true;
        } catch (Exception ex) {
            logger.error("updateMetadataPrivilege metadata " + metadataId, ex);
            throw new Exception("Error updating privileges for metadata " + metadataId + ", error: " + ex.getMessage());
        }

        return isPrivilegeUpdated;
    }

    @Override
    public boolean updateMetadataStatus(String metadataUuid, long metadataId, MetadataStatus status, GNConnection connection) throws Exception {
        boolean isStatusUpdated = false;

        try {
            MetadataStatusParameter stCreate = MetadataStatusParameter.build(status);

            Gson gson = new Gson();
            String body = gson.toJson(stCreate, MetadataStatusParameter.class);

            String url = HelperUtility.addPathToUrl(metadataServiceUrl, metadataUuid + "/status");

            String response = doPut(connection, url, body, "application/json", "application/json");

            isStatusUpdated = true;

        } catch (Exception ex) {
            logger.error("updateMetadataStatus metadata " + metadataUuid, ex);
            throw new Exception("Error updating status for metadata " + metadataUuid + ", error: " + ex.getMessage());
        }

        return isStatusUpdated;
    }

    public void deleteAllMetadataStatus(String uuid, long metadataId, GNConnection connection) throws Exception {
        try {
            String url = HelperUtility.addPathToUrl(metadataServiceUrl, uuid + "/status");

            String response = doDelete(connection, url, "", "application/json", "application/json");

        } catch (Exception ex) {
            logger.error("deleteAllMetadataStatus metadata " + uuid, ex);
            throw new Exception("Error deleting statuses for metadata " + uuid + ", error: " + ex.getMessage());
        }
    }

    public MetadataSearchResults retrieveMetadataForGroup(int groupId, String filter, int from, GNConnection connection) throws Exception {
        Map<String, Element> results;

        SAXBuilder saxBuilder = new SAXBuilder();
        XPathFactory xpfac = XPathFactory.instance();

        String cswUrl = baseUrl + "srv/eng" + cswEndpoint;

        StringBuilder cswUrlBuilder = new StringBuilder(cswUrl);

        cswUrlBuilder.append("?").append(Geonetwork.cswGetRecordsGETReqParamsByGroup_4);

        final String getRecordsRequestUrlTmpl = cswUrlBuilder.toString();

        MetadataSearchResults metadataSearchResults = new MetadataSearchResults();

        if (StringUtils.isNotEmpty(filter)) {
            filter = "%20AND%20" + filter;
        }
        // align the startPosition with nextRecord value from response
        // do this till numberOfRecordsReturned = 0 or startPosition > noOfRecordsMatched
        String getRecordsRequestUrl = getRecordsRequestUrlTmpl
            .replaceFirst("@@maxRecords@@", "200")
            .replaceFirst("@@startPosition@@", String.valueOf(from))
            .replaceFirst("@@groupOwner@@",  String.valueOf(groupId))
            .replaceFirst("@@constraint@@", filter);

        StringBuilder logMsg = new StringBuilder()
            .append("Calling CSW GetRecords service with url: ")
            .append(getRecordsRequestUrl)
            .append("\n at start position: " + from);
        logger.info(logMsg.toString());

        try {
            String response = doGet(connection, getRecordsRequestUrl, "application/xml");

            org.jdom2.Document responseDoc = saxBuilder.build(new StringReader(response));

            XPathExpression<Element> xp = xpfac.compile("//csw:SearchResults",
                Filters.element(),
                null,
                Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"));

            List<Element> cswSearchResults = xp.evaluate(responseDoc);

            if(cswSearchResults != null && cswSearchResults.size() > 0) {
                Element cswSearchResult = cswSearchResults.get(0);
                if(cswSearchResult != null) {
                    String numberOfRecordsMatched = cswSearchResult.getAttribute("numberOfRecordsMatched").getValue();
                    String numberOfRecordsReturned = cswSearchResult.getAttribute("numberOfRecordsReturned").getValue();
                    String nextRecord = cswSearchResult.getAttribute("nextRecord").getValue();

                    metadataSearchResults.setTotalMatched(Integer.parseInt(numberOfRecordsMatched));
                    metadataSearchResults.setTotalReturned(Integer.parseInt(numberOfRecordsReturned));
                    metadataSearchResults.setNextRecord(Integer.parseInt(nextRecord));

                    if(Integer.valueOf(numberOfRecordsReturned) > 0) {
                        xp = xpfac.compile("/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata",
                            Filters.element(),
                            null,
                            Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"),
                            Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd"));

                        List<Element> metadataList = xp.evaluate(responseDoc);

                        metadataList.forEach(md -> {
                            // Extract UUID

                            XPathExpression<Element> xp1  = xpfac.compile("gmd:fileIdentifier",
                                Filters.element(),
                                null,
                                Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd"));

                            Element uuidEl = xp1.evaluateFirst(md);
                            String uuid = uuidEl.getValue().trim();

                            XMLOutputter outp = new XMLOutputter();
                            String mdText = outp.outputString(md);

                            metadataSearchResults.getMetadata().put(uuid, mdText);

                        });
                    }
                }
            }
        } catch(Exception e) {
            throw new Exception(e);
        }

        return metadataSearchResults;

    }

    @Override
    public boolean deleteMetadata(String metadataUuid, GNConnection connection) throws Exception {
        boolean isDeleted = false;

        try {
            String url =  HelperUtility.addPathToUrl(metadataServiceUrl, metadataUuid);

            String response = doDelete(connection, url, "", "", "");

            isDeleted = true;
        } catch (Exception ex) {
            logger.error("Delete metadata " + metadataUuid, ex);
            throw new Exception("Error deleting metadata " + metadataUuid + ", error: " + ex.getMessage());
        }

        return isDeleted;
    }

}
