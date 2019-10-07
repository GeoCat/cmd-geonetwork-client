package com.geocat.gnclient.gnservices.metadata;

import org.junit.Assert;
import org.junit.Test;

public class GNMetadataServicesFactoryTest {

    @Test
    public void testFactory34Version() {
        try {
            GNMetadataServices metadataServicesServices = GNMetadataServicesFactory.getMetadataServices("3.4.3", "/geonetwork");
            Assert.assertTrue(metadataServicesServices instanceof GNMetadataServices3X);

            metadataServicesServices = GNMetadataServicesFactory.getMetadataServices("3.4.0", "/geonetwork");
            Assert.assertTrue(metadataServicesServices instanceof GNMetadataServices3X);

        } catch (Exception ex) {
            Assert.fail();
        }
    }


    @Test
    public void testFactory210Version() {
        try {
            GNMetadataServices metadataServicesServices = GNMetadataServicesFactory.getMetadataServices("2.10.5", "/geonetwork");
            Assert.assertTrue(metadataServicesServices instanceof GNMetadataServices210);

            metadataServicesServices = GNMetadataServicesFactory.getMetadataServices("2.10.0", "/geonetwork");
            Assert.assertTrue(metadataServicesServices instanceof GNMetadataServices210);

        } catch (Exception ex) {
            Assert.fail();
        }
    }

    @Test
    public void testFactoryNoValidVersion() {
        try {
            GNMetadataServices metadataServicesServices = GNMetadataServicesFactory.getMetadataServices("2.6.1", "/geonetwork");
            Assert.fail();

        } catch (Exception ex) {
            Assert.assertEquals("GeoNetwork version not supported", ex.getMessage());
        }
    }
}
