package com.geocat.gnclient.gnservices;

import com.geocat.gnclient.constants.Geonetwork;
import com.geocat.gnclient.constants.PropertyFile;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public enum GNCustomAuthenticator {
	INSTANCE;

	private static final Logger logger = LogManager.getLogger(GNCustomAuthenticator.class);

	// the following httpclient follows redirect due to LaxRedirectStrategy
	private final HttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
	private boolean isAuthenticated = false;

	public void doLoginToGnWithCustomFilter(String customAuthenticationUrl, String gnUserName, String gnUserPwd,
			String gnBaseUrl, String gnLoginService, String gnVersion) throws Exception {


		if(gnBaseUrl == null || gnVersion == null || gnUserName == null || gnUserPwd == null
				|| gnUserName.isEmpty() || gnUserPwd.isEmpty() || gnBaseUrl.isEmpty() || gnVersion.isEmpty()) {

			StringBuilder errorLogMsg = new StringBuilder("Error logging to geonetwork. Following required parameters have issues (Please check properties file):\n");

			if(gnBaseUrl == null || gnBaseUrl.isEmpty()) errorLogMsg.append(PropertyFile.GN_BASE_URL_KEY + " property is either not set properly or some encoding issue.\n");
			if(gnVersion == null || gnVersion.isEmpty())  errorLogMsg.append(PropertyFile.GN_VERSION_KEY + " property is either not set properly or some encoding issue.\n");
			if(gnUserName == null || gnUserName.isEmpty())  errorLogMsg.append(PropertyFile.GN_USER_NAME_KEY + " property is either not set properly or some encoding issue.\n");
			if(gnUserPwd == null || gnUserPwd.isEmpty())  errorLogMsg.append(PropertyFile.GN_USER_PWD_KEY + " property is either not set properly or some encoding issue.\n");

			throw new Exception(errorLogMsg.toString());
		}
		if(!gnBaseUrl.endsWith("/")) {
			gnBaseUrl = gnBaseUrl + "/";
		}
		String gnShibLoginUrl = "";
		if(Geonetwork.VERSION_2_10_X.equalsIgnoreCase(gnVersion)) {
			//gnShibLoginUrl = gnBaseUrl + Geonetwork.SHIB_LOGIN_URL_2_10_X;
			gnShibLoginUrl = gnBaseUrl + gnLoginService;
		} else if(Geonetwork.VERSION_2_6_X.equalsIgnoreCase(gnVersion)) {
			throw new Exception("Error. Geonetwork version " + Geonetwork.VERSION_2_6_X + " is not supported");
		} else {
			throw new Exception("Error. UNKNOWN Geonetwork version.");
		}
		HttpClientContext clientContext = HttpClientContext.create();
	    clientContext.setCookieStore(new BasicCookieStore());

	    HttpPost httpPostShibLogin = new HttpPost(gnShibLoginUrl);
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
				logger.debug("Response(login form) received from custom authenticator is:\n" + gnShibLoginUrl);
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

			HttpPost httpPostSessionController = new HttpPost(customAuthenticationUrl);

			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	        nvps.add(new BasicNameValuePair("login", "Logga in"));
	        nvps.add(new BasicNameValuePair("user", gnUserName));
	        nvps.add(new BasicNameValuePair("password", gnUserPwd));
	        nvps.add(new BasicNameValuePair("sessioninfo", sessionInfo));

	        httpPostSessionController.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
	        HttpResponse sessionControllerResponse = httpClient.execute(httpPostSessionController);
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
		} catch (Exception e) {
			throw new Exception(e);
		}
    }

	public String xmlMetadataGet(String metadataUuid, String gnBaseUrl) throws Exception {
		String strMetadataXML = "";

		if(!gnBaseUrl.endsWith("/")) {
			gnBaseUrl = gnBaseUrl + "/";
		}
		String gnXmlMetadataGetUrl = gnBaseUrl + "srv/eng/xml.metadata.get";

		HttpPost httpPostXmlMetadataGet = new HttpPost(gnXmlMetadataGetUrl);
		String xmlMetadataGetReqXml = "<request>" +
										 "<uuid>"+metadataUuid+"</uuid>" +
									  "</request>";

		HttpEntity entity = null;
		try {
			entity = new ByteArrayEntity(xmlMetadataGetReqXml.getBytes("UTF-8"));
			httpPostXmlMetadataGet.setEntity(entity);
		} catch (UnsupportedEncodingException e1) {
			throw new Exception(e1);
		}
		httpPostXmlMetadataGet.addHeader("Content-Type", "application/xml");

		HttpResponse xmlMetadataGetResponse = null;
		try {
			xmlMetadataGetResponse = httpClient.execute(httpPostXmlMetadataGet);
			int resCode = xmlMetadataGetResponse.getStatusLine().getStatusCode();
			 if(200 == resCode) {
				 strMetadataXML = EntityUtils.toString(xmlMetadataGetResponse.getEntity());
				 httpPostXmlMetadataGet.releaseConnection();
			 } else {
				 httpPostXmlMetadataGet.releaseConnection();
			 }
		} catch (ClientProtocolException e) {
			throw new Exception(e);
		} catch (IOException e) {
			throw new Exception(e);
		}
		return strMetadataXML;
	}

	public String cswGetRecordById(String metadataUuid, String cswGetRecordByIdUrl) throws Exception {
		String strMetadataXML = "";

		String getRecordByIdReq =
    			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<csw:GetRecordById xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" " +
									"service=\"CSW\" version=\"2.0.2\" outputSchema=\"csw:IsoRecord\">" +
					  "<csw:Id>"+metadataUuid+"</csw:Id>" +
					  "<csw:ElementSetName>full</csw:ElementSetName>" +
				"</csw:GetRecordById>" ;

		HttpPost post = new HttpPost(cswGetRecordByIdUrl);

		HttpResponse getRecordByIdResponse = null;
		try {
			HttpEntity entity = null;
			try {
				entity = new ByteArrayEntity(getRecordByIdReq.getBytes("UTF-8"));
				post.setEntity(entity);
			} catch (UnsupportedEncodingException e1) {
				throw new Exception(e1);
			}

			getRecordByIdResponse = httpClient.execute(post);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			throw new Exception(e);
		} catch (IOException e) {
			throw new Exception(e);
		}
		if(getRecordByIdResponse == null) {
			return "";
		}
		int resCode = getRecordByIdResponse.getStatusLine().getStatusCode();
		logger.debug("GetRecordById => response code " + resCode);
	    if(200 == resCode) {
			try {
				String responseXml = EntityUtils.toString(getRecordByIdResponse.getEntity());

				SAXBuilder saxBuilder = new SAXBuilder();
				StringReader xmlReader = new StringReader(responseXml);
				org.jdom2.Document doc = saxBuilder.build(xmlReader);

				org.jdom2.xpath.XPathFactory xpfac = org.jdom2.xpath.XPathFactory.instance();
				XPathExpression<Element> xp = xpfac.compile("/csw:GetRecordByIdResponse/gmd:MD_Metadata",
												Filters.element(),
												null,
												Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd"),
												Namespace.getNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2"));

				List<Element> gmdMetadataElem = xp.evaluate(doc);

				if (gmdMetadataElem != null && gmdMetadataElem.size() > 0) {
					XMLOutputter xmlOutputter = new XMLOutputter();
					strMetadataXML = xmlOutputter.outputString(gmdMetadataElem.get(0));
				}
			} catch (Exception e) {
				throw new Exception(e);
			}
	    	post.releaseConnection();
        } else {
      	  	post.releaseConnection();
        }
	    return strMetadataXML;
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}
	public HttpClient getAuthenticatedClient() {
		return httpClient;
	}


}
