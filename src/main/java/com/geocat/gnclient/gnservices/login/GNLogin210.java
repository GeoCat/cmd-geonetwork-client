package com.geocat.gnclient.gnservices.login;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.geocat.gnclient.gnservices.GNConnection;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class GNLogin210 extends GNLogin {

	private static final Logger logger = LogManager.getLogger(GNLogin210.class);

    private String loginServiceUrl;

    public GNLogin210(String baseUrl) {
        super(baseUrl);
        this.loginServiceUrl = baseUrl + "j_spring_security_check";
    }

    protected String getLoginService() {
        return this.loginServiceUrl;
    }

    protected void setupRequestHttpEntity(HttpPost request, HttpClientContext clientContext,
                                          String username, String password, GNConnection connection) throws UnsupportedEncodingException {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("username", username));
        nvps.add(new BasicNameValuePair("password", password));

        request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    }



}
