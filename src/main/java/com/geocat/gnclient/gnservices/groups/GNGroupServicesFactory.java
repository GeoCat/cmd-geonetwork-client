package com.geocat.gnclient.gnservices.groups;

import com.geocat.gnclient.constants.Geonetwork;

public class GNGroupServicesFactory {

    public static GNGroupServices getGroupServices(String gnVersion, String baseUrl) throws Exception {
        if (gnVersion.startsWith(Geonetwork.VERSION_3_X)) {
           return new GNGroupServices3X(baseUrl);

        } else if  (gnVersion.startsWith(Geonetwork.VERSION_2_10_X)) {
            return new GNGroupServices210(baseUrl);

        } else {
            throw new Exception("GeoNetwork version not supported");
        }
    }
}
