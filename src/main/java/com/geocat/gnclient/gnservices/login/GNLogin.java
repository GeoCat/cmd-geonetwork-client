package com.geocat.gnclient.gnservices.login;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.geocat.gnclient.gnservices.GNConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

public abstract class GNLogin {
    private static final Logger logger = LogManager.getLogger(GNLogin.class);

    protected String baseUrl;

    public GNLogin(String baseUrl) {
        if(!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        this.baseUrl = baseUrl ;
    }

    abstract protected String getLoginService();

    abstract protected void setupRequestHttpEntity(HttpPost request, HttpClientContext clientContext,
                                                   String username, String password, GNConnection connection) throws UnsupportedEncodingException;

    public void login(String username, String password, GNConnection connection) throws Exception {

        CloseableHttpClient closeableHttpClient = connection.getCloseableHttpClient();
        Cookie jsessionidCookie = null;

        String gnLoginUrl = getLoginService();

        if(!gnLoginUrl.isEmpty()) {
            CookieStore cookieStore = new BasicCookieStore();

            HttpClientContext clientContext = HttpClientContext.create();
            clientContext.setCookieStore(cookieStore);

            if(closeableHttpClient == null) {
                closeableHttpClient = HttpClients.createDefault();
            }

            HttpPost httpost = new HttpPost(gnLoginUrl);
            CloseableHttpResponse response = null;
            try {
                setupRequestHttpEntity(httpost, clientContext, username, password, connection);

                response = closeableHttpClient.execute(httpost, clientContext);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                httpost.releaseConnection();

                jsessionidCookie = clientContext.getCookieStore().getCookies().get(0);
                logger.debug("Authentication session cookie: " + jsessionidCookie.getName()
                    + " = " + jsessionidCookie.getValue()); // should print JSESSIONID =
            } else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                httpost.releaseConnection();

                jsessionidCookie = clientContext.getCookieStore().getCookies().get(0);
                logger.debug("Authentication session cookie: " + jsessionidCookie.getName()
                    + " = " + jsessionidCookie.getValue()); // should print JSESSIONID =
            } else {
                httpost.releaseConnection();
                System.out.println("User " + username + " not able to login to Geonetwork");
                System.out.println("response code from login url is " + response.getStatusLine().getStatusCode());
            }
        } else {}

        if (jsessionidCookie != null) {
            connection.setJsessionidCookie(jsessionidCookie);
        } else {
            throw new Exception("Error login in Geonetwork");
        }
    }

    protected String createRequestXML(String userName,String password) throws Exception {
        DocumentBuilderFactory documentBuilderFactory =   DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder =   documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElement("request");
        document.appendChild(rootElement);

        Element userNameElement = document.createElement("username");
        rootElement.appendChild(userNameElement);
        userNameElement.appendChild(document.createTextNode(userName));
        Element passwordElement = document.createElement("password");
        rootElement.appendChild(passwordElement);
        passwordElement.appendChild(document.createTextNode(password));
        StringWriter stw = new StringWriter();
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.transform(new DOMSource(document), new StreamResult(stw));

        return stw.toString();

    }

}
