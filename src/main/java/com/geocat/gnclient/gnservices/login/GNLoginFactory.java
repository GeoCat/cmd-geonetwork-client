package com.geocat.gnclient.gnservices.login;

import com.geocat.gnclient.constants.Geonetwork;

public class GNLoginFactory {

    public static GNLogin getLogin(String gnVersion, String baseUrl) throws Exception {
        if (gnVersion.startsWith(Geonetwork.VERSION_3_X)) {
           return new GNLogin34(baseUrl);

        } else if (gnVersion.startsWith(Geonetwork.VERSION_2_10_X)) {
            return new GNLogin210(baseUrl);

        } else {
            throw new Exception("GeoNetwork version not supported");
        }
    }
}
