package com.geocat.gnclient.gnservices.metadata;

import com.geocat.gnclient.constants.Geonetwork;
import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.groups.model.Group;
import com.geocat.gnclient.gnservices.metadata.model.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import com.geocat.gnclient.util.HelperUtility;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GNMetadataServices210 extends GNMetadataServices {
    private static final Logger logger = LogManager.getLogger(GNMetadataServices210.class);


    public GNMetadataServices210(String baseUrl) {
        super(baseUrl);
        metadataServiceUrl = HelperUtility.addPathToUrl(baseUrl, "srv/eng/");
    }


    @Override
    public MetadataSearchResults retrieveMetadata(String filter, int from, GNConnection connection) throws Exception {
        Map<String, Element> results;

        SAXBuilder saxBuilder = new SAXBuilder();
        XPathFactory xpfac = XPathFactory.instance();

        String cswUrl = baseUrl + "srv/eng/csw";

        StringBuilder cswUrlBuilder = new StringBuilder(cswUrl);

        cswUrlBuilder.append("?").append(Geonetwork.cswGetRecordsGETReqParamsByGroup_210);

        final String getRecordsRequestUrlTmpl = cswUrlBuilder.toString();

        MetadataSearchResults metadataSearchResults = new MetadataSearchResults();

        // align the startPosition with nextRecord value from response
        // do this till numberOfRecordsReturned = 0 or startPosition > noOfRecordsMatched
        String getRecordsRequestUrl = getRecordsRequestUrlTmpl
            .replaceFirst("@@maxRecords@@", "20")
            .replaceFirst("@@startPosition@@", String.valueOf(from))
            .replaceFirst("@@filter@@", filter);

        StringBuilder logMsg = new StringBuilder()
            .append("Calling CSW GetRecords service with url: ")
            .append(getRecordsRequestUrl)
            .append("\n at start position: " + from);
        logger.info(logMsg.toString());

        String response = "";

        try {
            response = doGet(connection, getRecordsRequestUrl, "application/xml");

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
        } catch(Exception ex) {
            logger.error("retrieveMetadata response: " +  response, ex);
            throw new Exception(ex);
        }

        return metadataSearchResults;

    }


    @Override
    public MetadataSharing getMetadataSharing(String metadataUuid, Map<String, Group> groups, GNConnection connection) throws Exception {
        MetadataSharing metadataSharingList = new MetadataSharing();

        String response = "";

        int retry = 0;

        try {
           //String url = HelperUtility.addPathToUrl(metadataServiceUrl, "metadata.admin.form!?uuid=" + metadataUuid);
            String url = HelperUtility.addPathToUrl(metadataServiceUrl, "xml.metadata.admin.get?uuid=" + metadataUuid);
            logger.info("getMetadataSharing url: " +  url);

            SAXBuilder saxBuilder = new SAXBuilder();
            XPathFactory xpfac = XPathFactory.instance();

            org.jdom2.Document responseDoc = null;
            boolean empty = true;

            while ((retry < 3) && (empty == true)) {
                try {
                    Thread.sleep(1000);

                    response = doGet(connection, url, "application/xml");
                    logger.info("getMetadataSharing response: " +  response);

                    responseDoc = saxBuilder.build(new StringReader(response));

                    empty = false;
                } catch (JDOMParseException ex) {
                    retry++;
                    if (retry >= 3) {
                        throw ex;
                    } else {
                        logger.info("getMetadataSharing retrying request");
                    }
                }
            }

            Element responseElement = responseDoc.getRootElement();

            metadataSharingList.setOwner(responseElement.getChildText("ownerid"));
            String groupOwner = responseElement.getChildText("groupOwner");
            metadataSharingList.setGroupOwner(groupOwner);

            List<MetadataPrivilege> privileges = new ArrayList<>();
            for (Element privRecord : responseElement.getChild("groups").getChildren()) {

                MetadataPrivilege privilege = new MetadataPrivilege();
                privilege.setGroup(privRecord.getChildText("id"));

                Operations op = new Operations();

                for (Element operEl : privRecord.getChildren("oper")) {
                    int opId = Integer.parseInt(operEl.getChildText("id"));
                    boolean isSelected = (operEl.getChild("on") != null);

                    if (opId == 0) {
                        op.setView(isSelected);
                    } else if (opId == 1) {
                        op.setDownload(isSelected);
                    } else if (opId == 2) {
                        op.setEditing(isSelected);
                    } else if (opId == 3) {
                        op.setNotify(isSelected);
                    } else if (opId == 5) {
                        op.setDynamic(isSelected);
                    } else if (opId == 6) {
                        op.setFeatured(isSelected);
                    }
                }

                privilege.setOperations(op);

                privileges.add(privilege);
            }

            metadataSharingList.setPrivileges(privileges.stream().toArray(MetadataPrivilege[]::new));

        } catch (org.jdom2.input.JDOMParseException ex) {
            logger.error("getMetadataSharing response: " +  response, ex);
            throw ex;

        } catch (IOException ex) {
            logger.error("getMetadataSharing response: " +  response, ex);
            throw ex;
        }

        return metadataSharingList;
    }


    @Override
    public MetadataStatus getMetadataStatus(String metadataUuid, GNConnection connection) throws Exception {
        MetadataStatus metadataStatus = new MetadataStatus();

        String response = "";
        try {
            String url = HelperUtility.addPathToUrl(metadataServiceUrl, "xml.metadata.status.get?uuid=" + metadataUuid);

            response = doGet(connection, url, "application/xml");

            if (StringUtils.isNotEmpty(response)) {
                SAXBuilder saxBuilder = new SAXBuilder();
                XPathFactory xpfac = XPathFactory.instance();
                org.jdom2.Document responseDoc = saxBuilder.build(new StringReader(response));

                if (responseDoc.getRootElement().getChildren().size() > 0) {
                    XPathExpression<Element> xp = xpfac.compile("/response/record/userid", Filters.element(),null);

                    Element userId = xp.evaluateFirst(responseDoc);
                    metadataStatus.setUserId(userId.getValue().trim());

                    xp = xpfac.compile("/response/record/name", Filters.element(),null);
                    Element name = xp.evaluateFirst(responseDoc);
                    metadataStatus.setName(name.getValue().trim());

                    xp = xpfac.compile("/response/record/changedate", Filters.element(),null);
                    Element changeDate = xp.evaluateFirst(responseDoc);
                    metadataStatus.setChangeDate(changeDate.getValue().trim());

                    xp = xpfac.compile("/response/record/changemessage", Filters.element(),null);
                    Element changeMessage = xp.evaluateFirst(responseDoc);
                    metadataStatus.setChangeMessage(changeMessage.getValue().trim());
                }
            }

        } catch (org.jdom2.input.JDOMParseException ex) {
            logger.error("getMetadataSharing response: " +  response, ex);
            throw ex;

        } catch (IOException ex) {
            logger.error("getMetadataSharing response: " +  response, ex);
            throw ex;
        }

        return metadataStatus;
    }


    @Override
    public long countMetadataForGroup(int groupId, GNConnection connection) throws Exception {
        long count = 0;

        try {
            //http://localhost:8080/geonetwork/srv/eng/q?&_groupOwner=2&fast=&summaryOnly=true&_content_type=json
            String url = HelperUtility.addPathToUrl(baseUrl, "srv/eng/q?fast=&summaryOnly=true&_groupOwner=" + groupId);

            String response = doGet(connection, url ,"application/xml");

            SAXBuilder saxBuilder = new SAXBuilder();
            XPathFactory xpfac = XPathFactory.instance();
            org.jdom2.Document responseDoc = saxBuilder.build(new StringReader(response));

            XPathExpression<Attribute> xp = xpfac.compile("/response/summary/@count", Filters.attribute(),null);

            Attribute countAttr = xp.evaluateFirst(responseDoc);

            count = countAttr.getLongValue();

        } catch (IOException ex) {
            // log
            ex.printStackTrace();
        }

        return count;
    }


    @Override
    public long insertMetadata(String metadata, String uuid, int ownerId, int groupOwnerId, String fullRightsForOwnerGroup,
                               String metadataPublic, String status, String isTemplate,
                               String schema, String styleSheet, GNConnection connection) throws Exception {

       throw new Exception("Not implemented");
    }


    @Override
    public boolean updateMetadataPrivilege(long metadataId, List<MetadataPrivilege> privileges,
                                           GNConnection connection)  throws Exception {
        throw new Exception("Not implemented");
    }


    @Override
    public boolean updateMetadataStatus(String uuid, long metadataId, MetadataStatus status, GNConnection connection) throws Exception {
        throw new Exception("Not implemented");
    }

    @Override
    public boolean deleteMetadata(String metadataUuid, GNConnection connection) throws Exception {
        throw new Exception("Not implemented");
    }
}
