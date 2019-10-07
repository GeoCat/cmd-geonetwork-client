package com.geocat.gnclient.constants;

public final class Geonetwork {
    public static final String VERSION_3_X = "3.";
	public static final String VERSION_2_10_X = "2.10";
	public static final String VERSION_2_6_X = "2.6";

    public static final String cswGetRecordsGETReqParams = "request=GetRecords&service=CSW&version=2.0.2"
        + "&namespace=xmlns(csw=http://www.opengis.net/cat/csw),xmlns(gmd=http://www.isotc211.org/2005/gmd)"
        + "&outputSchema=http://www.isotc211.org/2005/gmd"
        + "&resultType=results"
        + "&maxRecords=@@maxRecords@@"
        + "&startPosition=@@startPosition@@"
        + "&elementSetName=summary"
        + "&typeNames=gmd:MD_Metadata"
        + "&constraintLanguage=CQL_TEXT"
        + "&constraint_language_version=1.1.0"
        + "&constraint=@@filter@@";

    public static final String cswGetRecordsGETReqParamsByGroup = "request=GetRecords&service=CSW&version=2.0.2"
        + "&namespace=xmlns(csw=http://www.opengis.net/cat/csw),xmlns(gmd=http://www.isotc211.org/2005/gmd)"
        + "&outputSchema=http://www.isotc211.org/2005/gmd"
        + "&resultType=results"
        + "&maxRecords=@@maxRecords@@"
        + "&startPosition=@@startPosition@@"
        + "&elementSetName=full"
        + "&typeNames=gmd:MD_Metadata"
        + "&constraintLanguage=CQL_TEXT"
        + "&constraint_language_version=1.1.0"
        + "&constraint=_groupOwner%3D@@groupOwner@@@@constraint@@";


    public static final String cswGetRecordsGETReqParamsByGroup_210 = "request=GetRecords&service=CSW&version=2.0.2"
        + "&namespace=xmlns(csw=http://www.opengis.net/cat/csw),xmlns(gmd=http://www.isotc211.org/2005/gmd)"
        + "&outputSchema=http://www.isotc211.org/2005/gmd"
        + "&resultType=results"
        + "&maxRecords=@@maxRecords@@"
        + "&startPosition=@@startPosition@@"
        + "&elementSetName=full"
        + "&typeNames=gmd:MD_Metadata"
        + "&constraintLanguage=CQL_TEXT"
        + "&constraint_language_version=1.1.0"
        + "&constraint=@@filter@@";

    public static final String cswGetRecordsMetadataXPath = "/csw:GetRecordsResponse/csw:SearchResults/...";

}
