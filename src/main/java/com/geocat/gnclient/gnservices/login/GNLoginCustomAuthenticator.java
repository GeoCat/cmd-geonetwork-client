package com.geocat.gnclient.gnservices.login;

import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.info.GNInfoServices;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class GNLoginCustomAuthenticator extends GNLogin {

    private static final Logger logger = LogManager.getLogger(GNLoginCustomAuthenticator.class);

    private String loginServiceUrl;
    private String customAuthenticationUrl;


    public GNLoginCustomAuthenticator(String baseUrl, String customAuthenticationUrl) {
        super(baseUrl);
        // Endpoint for SSO service
        this.customAuthenticationUrl = customAuthenticationUrl;
        // Shib login url in GeoNetwork
        this.loginServiceUrl = baseUrl + "srv/eng/shib.user.login.noforward";
    }

    protected String getLoginService() {
        return this.baseUrl;
    }


    @Override
    public void login(String username, String password, GNConnection connection) throws Exception {

        // the following httpclient follows redirect due to LaxRedirectStrategy

        boolean isAuthenticated = false;

        CloseableHttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

        try {
            HttpClientContext clientContext = HttpClientContext.create();
            clientContext.setCookieStore(new BasicCookieStore());

            HttpPost httpPostShibLogin = new HttpPost(loginServiceUrl);
            String responseStr = null;
            HttpResponse response = null;

            try {
                response = httpClient.execute(httpPostShibLogin, clientContext);
                if(response == null) {
                    logger.error("Response received for shiblogin is null");
                    throw new Exception("Error. Response from shib.user.login redirection is null");
                }

                responseStr = EntityUtils.toString(response.getEntity());

                if(logger.isDebugEnabled()) {
                    logger.debug("Response(login form) received from custom authenticator is:\n" + loginServiceUrl);
                    logger.debug("is\n" + responseStr);
                }
            } catch (ClientProtocolException e1) {
                logger.error("ClientProtocolException caught " + e1, e1);
                throw new Exception(e1);
            } catch (IOException e1) {
                logger.error("IOException caught " + e1, e1);
                throw new Exception(e1);
            } catch(Exception e1) {
                logger.error("Exception caught " + e1, e1);
                throw new Exception(e1);
            } finally {
                httpPostShibLogin.releaseConnection();
            }


            try {
                responseStr = responseStr.substring(responseStr.indexOf("<input type=\"hidden\" name=\"sessioninfo\""));
                String sessionInfo = responseStr.substring((responseStr.indexOf("value=\"")+7), responseStr.indexOf("\" />"));
                if(logger.isDebugEnabled()) {
                    logger.debug("parsed sessioninfo is " + sessionInfo);
                }

                HttpClientContext clientContext2 = HttpClientContext.create();
                clientContext2.setCookieStore(new BasicCookieStore());

                HttpPost httpPostSessionController = new HttpPost(customAuthenticationUrl);

                List <NameValuePair> nvps = new ArrayList <NameValuePair>();
                nvps.add(new BasicNameValuePair("login", "Logga in"));
                nvps.add(new BasicNameValuePair("user", username));
                nvps.add(new BasicNameValuePair("password", password));
                nvps.add(new BasicNameValuePair("sessioninfo", sessionInfo));

                httpPostSessionController.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

                HttpResponse sessionControllerResponse = httpClient.execute(httpPostSessionController, clientContext2);
                int responseCode = -1;
                if(sessionControllerResponse != null) {
                    responseCode = sessionControllerResponse.getStatusLine().getStatusCode();

                    if(200 != responseCode) {
                        isAuthenticated =  false;
                        logger.error("Error. customAuthenticationUrl response code is: " + responseCode);
                    } else {
                        String sessionControllerResponseStr = EntityUtils.toString(sessionControllerResponse.getEntity());

                        if(logger.isDebugEnabled()) {
                            logger.debug("customAuthenticationUrl returned response code: " + responseCode);
                            logger.debug("customAuthenticationUrl response:\n " + sessionControllerResponseStr);
                        }

                        Header[] headers = sessionControllerResponse.getAllHeaders();
                        if(logger.isDebugEnabled()) {
                            logger.debug("shib.user.login response headers are:");
                            for(Header h: headers) {
                                logger.debug(h.getName() + ": " + h.getValue());
                            }
                        }

                        headers = sessionControllerResponse.getHeaders("Set-Cookie");
                        for(Header h: headers) {
                            String headerValue =  h.getValue();
                            String[] tokens = headerValue.split(";");
                            for(String token: tokens) {
                                String[] subTokens = token.split("=");
                                if(subTokens[0].equalsIgnoreCase("JSESSIONID")) {

                                    isAuthenticated =  true;
                                }
                            }
                        }


                    }
                } else {
                    isAuthenticated =  false;
                    logger.error("customAuthenticationUrl response is null.");
                }

                if (isAuthenticated) {
                    connection.setHttpClientContext(clientContext2);
                    connection.setCustomAuthentication(true);

                    try {
                        // Do a request to a GeoNetwork service to confirm the login was fine,
                        // just checking JSESSIONID is not enough
                        GNInfoServices infoServices = new GNInfoServices(baseUrl);
                        infoServices.getVersion(connection);
                    } catch (Exception ex) {
                        logger.error("Seem login information is not valid, please check the credentials (server=" +
                            loginServiceUrl + ", user=" + username + ", password=" + password + ", sessionInfo=" + sessionInfo + ")");
                        throw new Exception("Error login in Geonetwork");
                    }



                } else {
                    throw new Exception("Error login in Geonetwork");
                }
            } catch (Exception e) {
                throw new Exception(e);
            }
        } finally {
            httpClient.close();
        }

    }

    protected void setupRequestHttpEntity(HttpPost request, HttpClientContext clientContext,
                                          String username, String password, GNConnection connection) throws UnsupportedEncodingException {

    }

}
