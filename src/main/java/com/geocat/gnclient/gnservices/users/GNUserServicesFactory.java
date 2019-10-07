package com.geocat.gnclient.gnservices.users;

import com.geocat.gnclient.constants.Geonetwork;

public class GNUserServicesFactory {

    public static GNUserServices getUserServices(String gnVersion, String baseUrl) throws Exception {
        if(gnVersion.startsWith(Geonetwork.VERSION_3_X)) {
           return new GNUserServices3X(baseUrl);

        } else if(gnVersion.startsWith(Geonetwork.VERSION_2_10_X)) {
            return new GNUserServices210(baseUrl);

        } else {
            throw new Exception("GeoNetwork version not supported");
        }
    }
}
