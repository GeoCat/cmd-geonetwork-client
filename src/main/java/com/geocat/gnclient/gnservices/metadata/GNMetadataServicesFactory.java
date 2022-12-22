package com.geocat.gnclient.gnservices.metadata;

import com.geocat.gnclient.constants.Geonetwork;

public class GNMetadataServicesFactory {

    public static GNMetadataServices getMetadataServices(String gnVersion, String baseUrl, String cswEndpoint) throws Exception {
        if (gnVersion.startsWith(Geonetwork.VERSION_4_0_X) ||
            gnVersion.startsWith(Geonetwork.VERSION_4_2_X)) {
            return new GNMetadataServices4(baseUrl, cswEndpoint);

        } else if (gnVersion.startsWith(Geonetwork.VERSION_3_X)) {
           return new GNMetadataServices3X(baseUrl, cswEndpoint);

        } else if (gnVersion.startsWith(Geonetwork.VERSION_2_10_X)) {
            return new GNMetadataServices210(baseUrl, cswEndpoint);

        } else {
            throw new Exception("GeoNetwork version not supported");
        }
    }
}
