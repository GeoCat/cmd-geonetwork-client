package com.geocat.gnclient.gnservices.metadata;

import com.geocat.gnclient.constants.Geonetwork;
import com.geocat.gnclient.gnservices.GNBaseServices;
import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.groups.model.Group;
import com.geocat.gnclient.gnservices.metadata.model.MetadataPrivilege;
import com.geocat.gnclient.gnservices.metadata.model.MetadataSearchResults;
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
import com.geocat.gnclient.gnservices.metadata.model.MetadataSharing;
import com.geocat.gnclient.gnservices.metadata.model.MetadataStatus;
import com.geocat.gnclient.util.HelperUtility;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;


public abstract class GNMetadataServices extends GNBaseServices {
    private static final Logger logger = LogManager.getLogger(GNMetadataServices.class);

    protected String metadataServiceUrl;

    public GNMetadataServices(String baseUrl) {
       super(baseUrl);
    }

    public MetadataSearchResults retrieveMetadata(String filter, int from, GNConnection connection) throws Exception {
        Map<String, Element> results;

        SAXBuilder saxBuilder = new SAXBuilder();
        XPathFactory xpfac = XPathFactory.instance();

        String cswUrl = baseUrl + "srv/eng/csw";

        StringBuilder cswUrlBuilder = new StringBuilder(cswUrl);

        cswUrlBuilder.append("?").append(Geonetwork.cswGetRecordsGETReqParamsByGroup);

        final String getRecordsRequestUrlTmpl = cswUrlBuilder.toString();

        MetadataSearchResults metadataSearchResults = new MetadataSearchResults();

        // align the startPosition with nextRecord value from response
        // do this till numberOfRecordsReturned = 0 or startPosition > noOfRecordsMatched
        String getRecordsRequestUrl = getRecordsRequestUrlTmpl
            .replaceFirst("@@maxRecords@@", "20")
            .replaceFirst("@@startPosition@@", String.valueOf(from))
            .replaceFirst("@@constraint@@", filter);

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


    public MetadataSearchResults retrieveMetadataForGroup(int groupId, String filter, int from, GNConnection connection) throws Exception {
        Map<String, Element> results;

        SAXBuilder saxBuilder = new SAXBuilder();
        XPathFactory xpfac = XPathFactory.instance();

        String cswUrl = baseUrl + "srv/eng/csw";

        StringBuilder cswUrlBuilder = new StringBuilder(cswUrl);

        cswUrlBuilder.append("?").append(Geonetwork.cswGetRecordsGETReqParamsByGroup);

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

    public int retrieveMetadataId(String metadataUuid, GNConnection connection) throws Exception {
        int mdId = -1;

        String response = "";
        try {
            response = doGet(connection,
                HelperUtility.addPathToUrl(baseUrl,  "/srv/eng/q?uuid=" + URLEncoder.encode(metadataUuid, "UTF-8") + "&fast=true&buildSummary=false"), "text/xml");

            SAXBuilder saxBuilder = new SAXBuilder();
            XPathFactory xpfac = XPathFactory.instance();

            org.jdom2.Document responseDoc = saxBuilder.build(new StringReader(response));

            XPathExpression<Element> xp = xpfac.compile("//metadata/geonet:info/id",
                Filters.element(),
                null,
                Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork"));


            Element info = xp.evaluateFirst(responseDoc);

            String val = info.getText();

            if (StringUtils.isNotEmpty(val)) {
                mdId = Integer.parseInt(val);
            }

        } catch (IOException ex) {
            logger.error("retrieveMetadata response: " +  response, ex);
            throw ex;
        }

        return mdId;
    }

    public abstract MetadataSharing getMetadataSharing(String metadataUuid, Map<String, Group> groups, GNConnection connection) throws Exception;

    public abstract MetadataStatus getMetadataStatus(String metadataUuid, GNConnection connection) throws Exception;

    public abstract long countMetadataForGroup(int groupName, GNConnection connection) throws Exception;

    public abstract long insertMetadata(String metadata, String uuid, int ownerId, int groupOwnerId, String fullRightsForOwnerGroup,
                                           String metadataPublic, String status, String isTemplate, String schema,
                                           String styleSheet, GNConnection connection) throws Exception;

    public abstract boolean updateMetadataPrivilege(long metadataUuid, List<MetadataPrivilege> privileges, GNConnection connection) throws Exception;

    public abstract boolean updateMetadataStatus(String uuid, long metadataId, MetadataStatus status, GNConnection connection) throws Exception;

    public abstract boolean deleteMetadata(String metadataUuid, GNConnection connection) throws Exception;

}
