package com.geocat.gnclient.gnservices.metadata;

import com.geocat.gnclient.constants.Geonetwork;

public class GNMetadataServicesFactory {

    public static GNMetadataServices getMetadataServices(String gnVersion, String baseUrl) throws Exception {
        if (gnVersion.startsWith(Geonetwork.VERSION_3_X)) {
           return new GNMetadataServices3X(baseUrl);

        } else if (gnVersion.startsWith(Geonetwork.VERSION_2_10_X)) {
            return new GNMetadataServices210(baseUrl);

        } else {
            throw new Exception("GeoNetwork version not supported");
        }
    }
}
