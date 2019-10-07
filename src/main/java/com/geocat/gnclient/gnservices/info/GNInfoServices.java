package com.geocat.gnclient.gnservices.info;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.util.HelperUtility;

import java.io.IOException;
import java.io.StringReader;

public class GNInfoServices {
    private static final Logger logger = LogManager.getLogger(GNInfoServices.class);

    protected String infoServiceUrl;

    public GNInfoServices(String baseUrl) {
        this.infoServiceUrl = HelperUtility.addPathToUrl(baseUrl, "srv/eng/xml.info?type=site");
    }

    public String getVersion(GNConnection connection) throws Exception {
        String version = "";

        try {
            String response = doGet(connection, infoServiceUrl,
                "application/xml");

            logger.info("GNInfoServices.getVersion response: " + response);
            SAXBuilder saxBuilder = new SAXBuilder();
            StringReader xmlReader = new StringReader(response);
            org.jdom2.Document responseDoc = saxBuilder.build(xmlReader);

            XPathFactory xpfac = XPathFactory.instance();

            XPathExpression<Element> xp = xpfac.compile("/info/site/platform/version",
                Filters.element(), null);

            Element versionEl = xp.evaluateFirst(responseDoc);
            if (versionEl != null) {
                version = versionEl.getValue();
            }

            logger.error("GNInfoServices.getVersion version:", version);

        } catch (IOException ex) {
            logger.error("GNInfoServices.getVersion error:", ex);

            // log
            ex.printStackTrace();
        }

        return version;
    }

	public String getVersionJson(GNConnection connection) throws Exception {
        String version = "";

        try {
            String response = doGet(connection, infoServiceUrl + "&_content_type=json",
                "application/json");

            JsonParser parser = new JsonParser();
            JsonObject rootObj = (JsonObject) parser.parse(response).getAsJsonObject();

            version = rootObj.getAsJsonObject("site").getAsJsonObject("platform").get("version").getAsString();

        } catch (IOException ex) {
            // log
            ex.printStackTrace();
        }

        return version;



    }


    private String doGet(GNConnection connection, String url, String acceptHeader) throws IOException {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", acceptHeader);

        CloseableHttpResponse response = null;

        try {
            HttpClientContext clientContext;

            if (connection.isCustomAuthentication()) {
                clientContext = connection.getHttpClientContext();
            } else {
                // When using the GN default authentication, use an isolated HttpClientContext as
                // this service doesn't require authentication.
                // The method getNewHttpClientContext doesn't cache the HttpClientContext in GNConnection,
                // otherwise the JSESSIONID would not be valid for services that require login
                clientContext = connection.getNewHttpClientContext();
            }
            response = closeableHttpClient.execute(httpGet, clientContext);
        } catch (ClientProtocolException e) {
            // log
        } catch (IOException e) {
            // log
        }


        String responseBody = "";

        try {
            if(response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();

                responseBody = EntityUtils.toString(entity, "UTF-8");

                EntityUtils.consume(entity);
            } else if (response != null) {
                throw new IOException("Http response error (no response): Check the server is available");
            } else {
                throw new IOException("Http response error (" + response.getStatusLine().getStatusCode() +
                    "): " + response.getStatusLine().getReasonPhrase());
            }

        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(closeableHttpClient);
        }

        return responseBody;
    }

}
