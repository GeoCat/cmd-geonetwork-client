package com.geocat.gnclient.gnservices;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public abstract class GNBaseServices  {
    private static final Logger logger = LogManager.getLogger(GNBaseServices.class);

    protected String baseUrl;

    public GNBaseServices(String baseUrl) {
        this.baseUrl = baseUrl;
    }

	protected String doGet(GNConnection connection, String url, String acceptHeader) throws IOException {
        Cookie jsessionidCookie = connection.getJsessionidCookie();
        CloseableHttpClient closeableHttpClient = connection.getCloseableHttpClient();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", acceptHeader);
        if (jsessionidCookie != null) {
            httpGet.setHeader("Cookie", jsessionidCookie.getName()+"="+jsessionidCookie.getValue());
        }

        CloseableHttpResponse response = null;
        String responseBody = "";

        try {
            response = closeableHttpClient.execute(httpGet, connection.getHttpClientContext());

            checkSuccessStatusCode(response);

            HttpEntity entity = response.getEntity();
            responseBody = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);

        } catch (ClientProtocolException e) {
            // log
        } catch (IOException e) {
            // log
        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return responseBody;
    }


    protected String doPost(GNConnection connection, String url, String body, String acceptHeader, String contentType) throws IOException {
        HttpPost httpPost = new HttpPost(url);
	    return sendRequest(connection, httpPost, body, acceptHeader, contentType);
    }


    protected String doPut(GNConnection connection, String url, String body, String acceptHeader, String contentType) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        return sendRequest(connection, httpPut, body, acceptHeader, contentType);
    }

    protected String doDelete(GNConnection connection, String url, String body, String acceptHeader, String contentType) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        configureHttpRequestHeaders(httpDelete, connection, acceptHeader, contentType);

        HttpClientContext clientContext = connection.getHttpClientContext();

        CloseableHttpClient closeableHttpClient = connection.getCloseableHttpClient();
        CloseableHttpResponse response = null;

        String responseBody = "";

        try {
            response = closeableHttpClient.execute(httpDelete, clientContext);
            checkSuccessStatusCode(response);

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
            }

        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return responseBody;
    }

    private String sendRequest(GNConnection connection, HttpEntityEnclosingRequestBase httpMethod, String body, String acceptHeader, String contentType) throws IOException {
        configureHttpRequestHeaders(httpMethod, connection, acceptHeader, contentType);

        HttpClientContext clientContext = connection.getHttpClientContext();

        HttpEntity entity;

        String responseBody = "";

        try {
            entity = new ByteArrayEntity(body.getBytes("UTF-8"));
            httpMethod.setEntity(entity);
        } catch (UnsupportedEncodingException e1) {
            // log
        }

        CloseableHttpClient closeableHttpClient = connection.getCloseableHttpClient();
        CloseableHttpResponse response = null;

        try {
            response = closeableHttpClient.execute(httpMethod, clientContext);
            checkSuccessStatusCode(response);

            logger.info("response status:" + response.getStatusLine().getStatusCode());

            entity = response.getEntity();

            if (entity != null) {
                responseBody = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity);
            }

        } finally {
            HttpClientUtils.closeQuietly(response);
        }

        return responseBody;
    }


    private void configureHttpRequestHeaders(HttpRequestBase request, GNConnection connection,
                                             String acceptHeader, String contentType) {
        Cookie crsfTokenCookie = connection.getCrsfTokenCookie();

        if (StringUtils.isNotEmpty(acceptHeader)) {
            request.addHeader("Accept", acceptHeader);
        }

        //httpPost.addHeader("Cookie", jsessionidCookie.getName()+"="+jsessionidCookie.getValue());
        if (crsfTokenCookie != null) {
            request.addHeader("X-XSRF-TOKEN", crsfTokenCookie.getValue());
        }

        if (StringUtils.isNotEmpty(contentType)) {
            request.addHeader("Content-type", contentType);
        }

    }

    private void checkSuccessStatusCode(HttpResponse response) throws IOException {
        if (response == null || !(response.getStatusLine().getStatusCode() >= HttpStatus.SC_OK &&
            response.getStatusLine().getStatusCode() <= 299)) {

            throw new IOException("Http response error (" + response.getStatusLine().getStatusCode() +
                "): " + response.getStatusLine().getReasonPhrase());
        }
    }
}
