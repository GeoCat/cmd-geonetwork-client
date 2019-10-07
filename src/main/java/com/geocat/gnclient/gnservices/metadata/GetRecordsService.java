package com.geocat.gnclient.gnservices.metadata;

import com.geocat.gnclient.constants.Geonetwork;
import com.geocat.gnclient.gnservices.GNConnection;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import com.geocat.gnclient.gnservices.metadata.model.MetadataResource;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class GetRecordsService {
    private IMetadataProcessor metadataProcessor;
	private String cswUrl;

	private static final Logger logger = LogManager.getLogger(GetRecordsService.class);

	public GetRecordsService(String cswUrl, IMetadataProcessor metadataProcessor) {
	    this.cswUrl = cswUrl;
	    this.metadataProcessor = metadataProcessor;
    }

	public List<MetadataResource> getRecords(GNConnection connection) throws Exception {
		// first set system properties as this is starting point
		//System.setProperty("javax.xml.transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");


		// fetch metadata list from catalogue, if needed
		List<MetadataResource> metadataList = new ArrayList<MetadataResource>();

        try {
            metadataList = getMetadataResourceList(connection);
        } catch (Exception e) {
            logger.error("Error fetching records from csw. Exception is ", e);
            throw e;
        }

		return metadataList;
	}

	private List<MetadataResource> getMetadataResourceList(GNConnection connection) throws Exception {
		List<MetadataResource> metadataResourceList = new ArrayList<MetadataResource>();

        Cookie jsessionidCookie = connection.getJsessionidCookie();
        CloseableHttpClient closeableHttpClient = connection.getCloseableHttpClient();

        MutableInt startPosition = new MutableInt(1);
		MutableInt noOfRecordsReturned = new MutableInt(0);

		SAXBuilder saxBuilder = new SAXBuilder();
		XPathFactory xpfac = XPathFactory.instance();


		StringBuilder cswUrlBuilder = new StringBuilder(this.cswUrl);

		cswUrlBuilder.append("?").append(Geonetwork.cswGetRecordsGETReqParams);

		final String getRecordsRequestUrlTmpl = cswUrlBuilder.toString();

		HttpGet httpGet = null;

		try {
			while(true) {
				// align the startPosition with nextRecord value from response
				// do this till numberOfRecordsReturned = 0 or startPosition > noOfRecordsMatched
				String getRecordsRequestUrl = getRecordsRequestUrlTmpl
						.replaceFirst("@@maxRecords@@", "200")
						.replaceFirst("@@startPosition@@", String.valueOf(startPosition.intValue()));

				if(logger.isDebugEnabled()) {
					 StringBuilder logMsg = new StringBuilder()
							.append("Calling CSW GetRecords service with url: ")
							.append(getRecordsRequestUrl)
					 		.append("\n at start position: " + startPosition.intValue());
					 logger.debug(logMsg.toString());
				}
				httpGet = new HttpGet(getRecordsRequestUrl);
                httpGet.setHeader("Cookie", jsessionidCookie.getName()+"="+jsessionidCookie.getValue());


				List<Element> dcIdentifierList = null;
				try {
                    processResponse(closeableHttpClient, httpGet, startPosition, noOfRecordsReturned, saxBuilder, xpfac);
				} catch(Exception e) {
					throw new Exception(e);
				}
				if(logger.isDebugEnabled()) {
					StringBuilder logMsg = new StringBuilder()
					.append("No of records returned (parsed from response xml): ")
					.append(noOfRecordsReturned.intValue())
			 		.append("\n Next start position: " + startPosition.intValue());
					logger.debug(logMsg.toString());
				}

				if(!dcIdentifierList.isEmpty()) {
					for(Element dcIdentifierElem: dcIdentifierList) {
						String uuid = dcIdentifierElem.getValue();
						if(uuid != null && !uuid.isEmpty()) {
							metadataResourceList.add(new MetadataResource(uuid));
						}
					}
				}

				if(noOfRecordsReturned.intValue() == 0 || startPosition.intValue() == 0) break;
			}
		} finally {
			httpGet.releaseConnection();
		}

		if(logger.isDebugEnabled()) {
			StringBuilder logMsg = new StringBuilder();
			logMsg.append("Total no of records fetched: ");
			logMsg.append(metadataResourceList.size());
			logMsg.append("\n UUIDs of fetched records are listed below:");
			for(MetadataResource mRes: metadataResourceList) {
				logMsg.append("\n");
				logMsg.append("uuid: " + mRes.getLayerName());
			}
			logMsg.append("-- END OF LIST --");
			logger.debug(logMsg.toString());
		}
		return metadataResourceList;
	}

	private void processResponse(CloseableHttpClient closeableHttpClient, HttpGet httpGet, MutableInt startPosition,
                                      MutableInt noOfRecordsReturned, SAXBuilder saxBuilder, XPathFactory xpfac) throws Exception {


		String responseXml = "";
		CloseableHttpResponse closeableResponse = null;

		try {
			closeableResponse = closeableHttpClient.execute(httpGet);
			reportErrorsIfAny(closeableResponse);

			if(closeableResponse != null) {
				int resCode = closeableResponse.getStatusLine().getStatusCode();
				if(resCode == HttpStatus.SC_OK) {
					responseXml = EntityUtils.toString(closeableResponse.getEntity(), HTTP.UTF_8);
				}
			}
		} catch (ClientProtocolException e) {
			throw new Exception(e);
		} catch (IOException e) {
			throw new Exception(e);
		} finally {
			if(closeableResponse != null) {
				try {
					closeableResponse.close();
				} catch (IOException e) {}
			}
		}

		if(responseXml != null && !responseXml.isEmpty()) {
			try {
				org.jdom2.Document responseDoc = saxBuilder.build(new StringReader(responseXml));

				XPathExpression<Element> xp = xpfac.compile("//csw:SearchResults",
						Filters.element(),
						null,
						Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"));

				List<Element> cswSearchResults = xp.evaluate(responseDoc);

				if(cswSearchResults != null && cswSearchResults.size() > 0) {
					Element cswSearchResult = cswSearchResults.get(0);
					if(cswSearchResult != null) {
						String numberOfRecordsReturned = cswSearchResult.getAttribute("numberOfRecordsReturned").getValue();
						String nextRecord = cswSearchResult.getAttribute("nextRecord").getValue();

						noOfRecordsReturned.setValue(Integer.parseInt(numberOfRecordsReturned));
						startPosition.setValue(Integer.parseInt(nextRecord));

						if(Integer.valueOf(numberOfRecordsReturned) > 0) {
							xp = xpfac.compile("/csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata",
									Filters.element(),
									null,
									Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"),
									Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd"));

                            List<Element> metadataList = xp.evaluate(responseDoc);

                            metadataList.forEach(md -> metadataProcessor.process(md));
						}
					}
				}
			} catch (Exception e) {
				throw new Exception(e);
			}
		}
	}

	private void reportErrorsIfAny(CloseableHttpResponse closeableResponse) {
		if(closeableResponse == null) {
			logger.error("Response for csw GetRecords is null. Returning empty identifier list");
		} else {
			int resCode = closeableResponse.getStatusLine().getStatusCode();
			if(resCode != HttpStatus.SC_OK) {
				logger.error("Error response code for csw GetRecords: " + resCode);
			}
		}
	}

}
