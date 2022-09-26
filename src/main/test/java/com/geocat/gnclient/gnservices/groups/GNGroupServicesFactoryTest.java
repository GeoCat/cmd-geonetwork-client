package com.geocat.gnclient.gnservices.groups;

import org.junit.Assert;
import org.junit.Test;

public class GNGroupServicesFactoryTest {

    @Test
    public void testFactory34Version() {
        try {
            GNGroupServices groupServices = GNGroupServicesFactory.getGroupServices("3.4.3", "/geonetwork");
            Assert.assertTrue(groupServices instanceof GNGroupServices3X);

            groupServices = GNGroupServicesFactory.getGroupServices("3.4.0", "/geonetwork");
            Assert.assertTrue(groupServices instanceof GNGroupServices3X);


        } catch (Exception ex) {
            Assert.fail();
        }
    }


    @Test
    public void testFactory210Version() {
        try {
            GNGroupServices groupServices = GNGroupServicesFactory.getGroupServices("2.10.5", "/geonetwork");
            Assert.assertTrue(groupServices instanceof GNGroupServices210);

            groupServices = GNGroupServicesFactory.getGroupServices("2.10.0", "/geonetwork");
            Assert.assertTrue(groupServices instanceof GNGroupServices210);

        } catch (Exception ex) {
            Assert.fail();
        }
    }

    @Test
    public void testFactoryNoValidVersion() {
        try {
            GNGroupServices groupServices = GNGroupServicesFactory.getGroupServices("2.6.1", "/geonetwork");
            Assert.fail();

        } catch (Exception ex) {
            Assert.assertEquals("GeoNetwork version not supported", ex.getMessage());
        }
    }

}
