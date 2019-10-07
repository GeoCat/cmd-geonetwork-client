package com.geocat.gnclient.gnservices.metadata;

import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.groups.model.Group;
import com.geocat.gnclient.gnservices.metadata.model.MetadataPrivilege;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import com.geocat.gnclient.gnservices.metadata.model.MetadataSharing;
import com.geocat.gnclient.gnservices.metadata.model.MetadataStatus;
import com.geocat.gnclient.gnservices.metadata.model.MetadataStatusCreate;
import com.geocat.gnclient.util.HelperUtility;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GNMetadataServices3X extends GNMetadataServices {
    private static final Logger logger = LogManager.getLogger(GNMetadataServices3X.class);


    public GNMetadataServices3X(String baseUrl) {
        super(baseUrl);
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
    public MetadataStatus getMetadataStatus(String metadataUuid, GNConnection connection) throws Exception {
        MetadataStatus metadataStatus = new MetadataStatus();

        try {
            String response =  doGet(connection,
                HelperUtility.addPathToUrl(baseUrl,  "srv/eng/xml.metadata.status.get?uuid=" + metadataUuid),
                "application/xml");

            if (StringUtils.isNotEmpty(response)) {
                SAXBuilder saxBuilder = new SAXBuilder();
                XPathFactory xpfac = XPathFactory.instance();
                org.jdom2.Document responseDoc = saxBuilder.build(new StringReader(response));

                XPathExpression<Element> xp = xpfac.compile("/response/userId", Filters.element(),null);

                Element userId = xp.evaluateFirst(responseDoc);
                metadataStatus.setUserId(userId.getValue().trim());

                xp = xpfac.compile("/response/name", Filters.element(),null);
                Element name = xp.evaluateFirst(responseDoc);
                metadataStatus.setName(name.getValue().trim());

                xp = xpfac.compile("/response/changeDate", Filters.element(),null);
                Element changeDate = xp.evaluateFirst(responseDoc);
                metadataStatus.setChangeDate(changeDate.getValue().trim());

                xp = xpfac.compile("/response/changeMessage", Filters.element(),null);
                Element changeMessage = xp.evaluateFirst(responseDoc);
                metadataStatus.setChangeMessage(changeMessage.getValue().trim());
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



        String insertUrl = "srv/eng/xml.metadata.insert";

        if(metadata.contains("<?xml")) {
            //  else, gn throws exception while inserting: content not allowed in prolog
            metadata = metadata.substring(metadata.indexOf("?>") + 2);
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
            String response = doPost(connection, HelperUtility.addPathToUrl(baseUrl, insertUrl),
                reqXml, "application/xml", "application/xml");

            SAXBuilder saxBuilder = new SAXBuilder();
            StringReader xmlReader = new StringReader(response);
            org.jdom2.Document doc = saxBuilder.build(xmlReader);

            Element rootElem = doc.getRootElement();
            metadataId = Long.parseLong(rootElem.getChild("id").getText());

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
            MetadataStatusCreate stCreate = MetadataStatusCreate.build(status);

            if (stCreate.getStatus() != null) {
                String url = HelperUtility.addPathToUrl(metadataServiceUrl, metadataId + "/status") + "?status="  +
                    stCreate.getStatus() + "&comment=" + URLEncoder.encode(stCreate.getComment(), "UTF-8");

                String response = doPut(connection, url, "", "application/json", "application/json");

                isStatusUpdated = true;
            } else {
                logger.info("updateMetadataStatus metadata " + metadataUuid + ", status is empty, not assigned");

            }
        } catch (Exception ex) {
            logger.error("updateMetadataStatus metadata " + metadataUuid, ex);
            throw new Exception("Error updating status for metadata " + metadataUuid + ", error: " + ex.getMessage());
        }

        return isStatusUpdated;
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
