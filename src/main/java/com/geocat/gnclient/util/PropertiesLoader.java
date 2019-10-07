package com.geocat.gnclient.util;

import com.geocat.gnclient.constants.PropertyFile;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

public enum PropertiesLoader {

    INSTANCE;

    private final Properties props = new Properties();

    public static String insertUuidAction = "overwrite"; // default

    public boolean load(String propFileName) throws FileNotFoundException {
    	InputStream propsIn = getClass().getClassLoader().getResourceAsStream(propFileName);
    	if(propsIn != null) {
    	    return internalPropertiesFile(propsIn);

    	} else {
            FileInputStream file = new FileInputStream(propFileName);

            if (file != null) {
                return internalPropertiesFile(file);
            } else {
                throw new FileNotFoundException("property file " + propFileName + " could not be found");
            }
    	}
    }

    private boolean internalPropertiesFile(InputStream in) {
		Reader reader = null;
		try {
			reader = new InputStreamReader(in, Charset.forName("UTF-8"));
			props.load(reader);
		} catch (UnsupportedEncodingException e1) {
			return false;
		} catch (IOException e) {
			return false;
		}  finally {
            IOUtils.closeQuietly(reader);
    	}
    	return true;
    }

    public String doCustomAuthentication() {
    	if(props == null) {
    		return "";
    	}
    	return props.getProperty(PropertyFile.DO_CUSTOM_AUTHENTICATION_KEY, "false");
    }

    public String getAuthenticationUrl() {
    	if(props == null) {
    		return "";
    	}
    	return props.getProperty(PropertyFile.CUSTOM_AUTHENTICATION_URL_KEY);
    }

    public String getGNUserName() {
    	if(props == null) {
    		return "";
    	}
    	return props.getProperty(PropertyFile.GN_USER_NAME_KEY);
    }

    public String getGNUserPwd() {
    	if(props == null) {
    		return "";
    	}
    	return props.getProperty(PropertyFile.GN_USER_PWD_KEY);
    }


    public String getGNBaseUrl() {
    	if(props == null) {
    		return "";
    	}
    	return props.getProperty(PropertyFile.GN_BASE_URL_KEY);
    }


    public String getGNUsersDefaultPassword() {
        if(props == null) {
            return null;
        }
        return props.getProperty(PropertyFile.GN_USERS_DEFAULT_PASSWORD_KEY);
    }

    public String getOutputPath() {
        if(props == null) {
            return "";
        }
        return props.getProperty(PropertyFile.OUTPUT_PATH);
    }

    public String getGNImportLocalXslt() {
        if(props == null) {
            return "";
        }
        return props.getProperty(PropertyFile.GN_IMPORT_LOCAL_XSLT);
    }

    public String getGNImportRemoteXslt() {
        if(props == null) {
            return "";
        }
        return props.getProperty(PropertyFile.GN_IMPORT_REMOTE_XSLT);
    }

    public String getDefaultImportUser() {
        if(props == null) {
            return null;
        }
        return props.getProperty(PropertyFile.GN_DEFAULT_IMPORT_USER_KEY);
    }

    public String getDefaultImportGroup() {
        if(props == null) {
            return null;
        }
        return props.getProperty(PropertyFile.GN_DEFAULT_IMPORT_GROUP_KEY);
    }

    public String getDefaultImportStatus() {
        if(props == null) {
            return null;
        }
        return props.getProperty(PropertyFile.GN_DEFAULT_IMPORT_STATUS_KEY);
    }

    public String getDefaultImportPrivileges() {
        if(props == null) {
            return null;
        }
        return props.getProperty(PropertyFile.GN_DEFAULT_IMPORT_PRIVILEGES_KEY);
    }

    public String getDefaultExportMetadataTypes() {
        if(props == null) {
            return null;
        }
        return props.getProperty(PropertyFile.GN_EXPORT_METADATA_TYPES);
    }
    public String getDefaultExportMetadataModifiedSince() {
        if(props == null) {
            return null;
        }
        return props.getProperty(PropertyFile.GN_EXPORT_METADATA_MODIFIED_SINCE, "");
    }

    public String getDefaultCswConstraint() {
        if(props == null) {
            return null;
        }
        return props.getProperty(PropertyFile.GN_EXPORT_CSW_CONSTRAINT);
    }

    public boolean getDefaultExportFilterByGroupLocally() {
        if(props == null) {
            return false;
        }

        return Boolean.parseBoolean(props.getProperty(PropertyFile.GN_EXPORT_FILTER_BY_GROUP_LOCALLY));
    }

    public int gnDefaultRequestDelay() {
        if(props == null) {
            return 0;
        }

        try {
            return Integer.parseInt(props.getProperty(PropertyFile.GN_EXPORT_REQUEST_DELAY));
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    public String getXpathFactoryClass() {
        if(props == null) {
            return null;
        }
        return props.getProperty(PropertyFile.XPATH_FACTORY_CLASS);
    }
}
