package com.geocat.gnclient.gnservices.metadata.model;

import org.apache.commons.io.IOUtils;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

public class GNInfoServicesTest {

    @Test
    public void testVersion() {
        try {
            java.net.URL url = GNInfoServicesTest.class.getResource("/info.xml");
            java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
            String infoXml = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");


            SAXBuilder saxBuilder = new SAXBuilder();
            StringReader xmlReader = new StringReader(infoXml);
            org.jdom2.Document responseDoc = saxBuilder.build(xmlReader);

            XPathFactory xpfac = XPathFactory.instance();

            XPathExpression<Element> xp = xpfac.compile("/info/site/platform/version",
                Filters.element(), null);

            String version = "";
            Element versionEl = xp.evaluateFirst(responseDoc);
            if (versionEl != null) {
                version = versionEl.getValue();
            }

            Assert.assertEquals("3.4.3", version);

        } catch (Exception ex) {
            Assert.fail();
        }
    }
}
