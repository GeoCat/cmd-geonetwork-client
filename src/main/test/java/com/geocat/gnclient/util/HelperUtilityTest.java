package com.geocat.gnclient.util;

import org.junit.Assert;
import org.junit.Test;


public class HelperUtilityTest {

    @Test
    public void testAddPathToUrl() {
        Assert.assertEquals("http://my.url.com/path1/info",
            HelperUtility.addPathToUrl("http://my.url.com", "path1/info"));

        Assert.assertEquals("http://my.url.com/path1/info",
            HelperUtility.addPathToUrl("http://my.url.com", "/path1/info"));

        Assert.assertEquals("http://my.url.com/path1/info",
            HelperUtility.addPathToUrl("http://my.url.com/", "path1/info"));

        Assert.assertEquals("http://my.url.com/path1/info",
            HelperUtility.addPathToUrl("http://my.url.com/", "/path1/info"));

    }

    @Test
    public void testRemoveFileSystemSpecialChars() {
        Assert.assertEquals("filename232",
            HelperUtility.removeFileSystemSpecialChars("filename232"));

        Assert.assertEquals("filename_s_dd_f_h_s_g_h_3s2",
            HelperUtility.removeFileSystemSpecialChars("filename>s<dd/f|h:s;g?h\"3s2"));

    }
}
