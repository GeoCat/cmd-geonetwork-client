package com.geocat.gnclient.gnservices.users;

import org.junit.Assert;
import org.junit.Test;

public class GNUserServicesFactoryTest {

    @Test
    public void testFactory34Version() {
        try {
            GNUserServices userServices = GNUserServicesFactory.getUserServices("3.4.3", "/geonetwork");
            Assert.assertTrue(userServices instanceof GNUserServices3X);

            userServices = GNUserServicesFactory.getUserServices("3.4.0", "/geonetwork");
            Assert.assertTrue(userServices instanceof GNUserServices3X);

        } catch (Exception ex) {
            Assert.fail();
        }
    }


    @Test
    public void testFactory210Version() {
        try {
            GNUserServices userServices = GNUserServicesFactory.getUserServices("2.10.5", "/geonetwork");
            Assert.assertTrue(userServices instanceof GNUserServices210);

            userServices = GNUserServicesFactory.getUserServices("2.10.0", "/geonetwork");
            Assert.assertTrue(userServices instanceof GNUserServices210);

        } catch (Exception ex) {
            Assert.fail();
        }
    }

    @Test
    public void testFactoryNoValidVersion() {
        try {
            GNUserServices userServices = GNUserServicesFactory.getUserServices("2.6.1", "/geonetwork");
            Assert.fail();

        } catch (Exception ex) {
            Assert.assertEquals("GeoNetwork version not supported", ex.getMessage());
        }
    }
}
