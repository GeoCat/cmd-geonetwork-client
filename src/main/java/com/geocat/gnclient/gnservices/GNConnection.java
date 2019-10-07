package com.geocat.gnclient.gnservices;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class GNConnection {
    private CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
    private HttpClientContext clientContext = null;
    private Cookie jsessionidCookie = null;
    private Cookie crsfTokenCookie = null;
    private boolean customAuthentication = false;

    public CloseableHttpClient getCloseableHttpClient() {
        return closeableHttpClient;
    }

    public Cookie getJsessionidCookie() {
        return jsessionidCookie;
    }

    public void setJsessionidCookie(Cookie jsessionidCookie) {
        this.jsessionidCookie = jsessionidCookie;
    }

    public Cookie getCrsfTokenCookie() {
        return crsfTokenCookie;
    }

    public void setCrsfTokenCookie(Cookie crsfTokenCookie) {
        this.crsfTokenCookie = crsfTokenCookie;
    }

    public void setHttpClientContext(HttpClientContext clientContext) {
        this.clientContext = clientContext;
    }

    public boolean isCustomAuthentication() {
        return customAuthentication;
    }

    public void setCustomAuthentication(boolean customAuthentication) {
        this.customAuthentication = customAuthentication;
    }

    public HttpClientContext getHttpClientContext() {
        if (clientContext == null) {
            CookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(jsessionidCookie);

            if (crsfTokenCookie != null) {
                cookieStore.addCookie(crsfTokenCookie);
            }

            clientContext = new HttpClientContext();
            clientContext.setCookieStore(cookieStore);
        }

        return clientContext;
    }

    public HttpClientContext getNewHttpClientContext() {
        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(jsessionidCookie);

        if (crsfTokenCookie != null) {
            cookieStore.addCookie(crsfTokenCookie);
        }

        HttpClientContext localClientContext = new HttpClientContext();
        localClientContext.setCookieStore(cookieStore);

        return localClientContext;
    }

    public void close() {
        try {
            if (closeableHttpClient != null) {
                closeableHttpClient.close();
            }
        } catch (IOException ex) {

        }
    }
}
