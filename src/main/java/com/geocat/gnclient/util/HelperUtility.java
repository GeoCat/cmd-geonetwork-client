package com.geocat.gnclient.util;


import org.apache.commons.lang.StringUtils;

import java.util.UUID;

public class HelperUtility {
    public static final String FILE_SYSTEM_SPECIAL_CHARS = "*<>|/:;?\"";
    public static final Character UNDERSCORE  = '_';

    /**
     * Joins a path to url dealing with / between both.
     *
     * @param url
     * @param path
     * @return
     */
    public static String addPathToUrl(String url, String path) {
        if(!url.endsWith("/")){
            url = url + "/";
        }

        if(path.startsWith("/")){
            path = path.substring(1);
        }

        return url + path;
    }


    public static String removeFileSystemSpecialChars(String pValue) {
        return getReplacedString(pValue, FILE_SYSTEM_SPECIAL_CHARS, UNDERSCORE);
    }


    public static String getReplacedString(String pValue, String pCharactersString, char pReplaceChar) {
        if(StringUtils.isNotEmpty(pValue) && (pCharactersString != null)) {
            char[] charsArray = pCharactersString.toCharArray();
            int length = charsArray.length;
            if(length > 0) {
                for(int index = 0; index < length; index++) {
                    pValue = pValue.replace(charsArray[index], pReplaceChar);
                }
            }
        }
        return pValue;
    }


    public static boolean isUUID(String uuidVal) {
        try {
            UUID.fromString(uuidVal);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
