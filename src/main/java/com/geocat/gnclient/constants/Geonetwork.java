package com.geocat.gnclient.constants;

public final class Geonetwork {
    public static final String LOGIN_SERVICE_3_4_X = "signin";
	public static final String LOGIN_SERVICE_2_10_X = "j_spring_security_check";
	public static final String LOGIN_SERVICE_2_6_X = "srv/en/xml.user.login";
	public static final String SHIB_LOGIN_URL_2_10_X ="srv/eng/shib.user.login";

    public static final String VERSION_4_2_X = "4.2";
    public static final String VERSION_4_0_X = "4.0";
    public static final String VERSION_3_X = "3.";
	public static final String VERSION_2_10_X = "2.10";
	public static final String VERSION_2_6_X = "2.6";


	public static final class Privilege {
		public static final String VIEW   	= "view";      // 0: view
		public static final String DOWNLOAD = "download";  // 1: download
		public static final String EDITING  = "editing";   // 2: editing
		public static final String NOTIFY  	= "notify";    // 3: notify
		public static final String DYNAMIC	= "dynamic";   // 4: dynamic
		public static final String FEATURED = "featured";  // 5: featured

		public static final String PUBLISH = "publish";
		public static final String INTERACTIVEMAP = "interactivemap";

	}
	public static final class Status {
		public static final String UNKNOWN = "unknown";
		public static final String DRAFT = "draft";
		public static final String APPROVED = "approved";
		public static final String RETIRED = "retired";
		public static final String SUBMITTED = "submitted";
		public static final String REJECTED = "rejected";
		public static final int UNKNOWN_CODE = 0;
		public static final int DRAFT_CODE = 1;
		public static final int APPROVED_CODE = 2;
		public static final int RETIRED_CODE = 3;
		public static final int SUBMITTED_CODE = 4;
		public static final int REJECTED_CODE = 5;
	}


	public static final class Profile {
		public static final String ADMINISTRATOR   = "Administrator";
		public static final String USER_ADMIN      = "UserAdmin";
		public static final String REVIEWER        = "Reviewer";
		public static final String EDITOR          = "Editor";
		public static final String REGISTERED_USER = "RegisteredUser";
		public static final String GUEST           = "Guest";
		public static final String MONITOR         = "Monitor";
	}

	public static final class UserUpdaterReqParams {
		public static final String OPERATION_EDITINFO = "editinfo";

		public static final String OPERATION = "operation";
		public static final String ID = "id";
		public static final String USERNAME = "username";
		public static final String PASSWORD = "password";
		public static final String SURNAME = "surname";
		public static final String NAME = "name";
		public static final String PROFILE = "profile";
		public static final String ADDRESS = "address";
		public static final String CITY = "city";
		public static final String STATE = "state";
		public static final String ZIP = "zip";
		public static final String COUNTRY = "country";
		public static final String EMAIL = "email";
		public static final String ORGANISATION = "org";
		public static final String KIND = "kind";
		public static final String GROUPS = "groups";
		public static final String GROUP_USER_ADMIN = GROUPS + "_" + Geonetwork.Profile.USER_ADMIN;
		public static final String GROUP_REVIEWER = GROUPS + "_" + Geonetwork.Profile.REVIEWER;
		public static final String GROUP_EDITOR = GROUPS + "_" + Geonetwork.Profile.EDITOR;
		public static final String GROUP_REGISTERED_USER = GROUPS + "_" + Geonetwork.Profile.REGISTERED_USER;
	}

	public static final class XmlInfoTypeUsersResponseElems {
		public static final String OPERATION_EDITINFO = "editinfo";

		public static final String OPERATION = "operation";
		public static final String ID = "id";
		public static final String USERNAME = "username";
		public static final String SURNAME = "surname";
		public static final String NAME = "name";
		public static final String PROFILE = "profile";
		public static final String ADDRESS = "address";
		public static final String CITY = "city";
		public static final String STATE = "state";
		public static final String ZIP = "zip";
		public static final String COUNTRY = "country";
		public static final String EMAIL = "email";
		public static final String ORGANISATION = "organisation";
		public static final String KIND = "kind";
	}

    public static final class RequestXmls {
        public static final String cswGetRecordsDctNoFilterReqXml =
            "<?xml version=\"1.0\"?>" +
                "<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" " +
                "xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" service=\"CSW\" version=\"2.0.2\" " +
                "resultType=\"results\" " +
                "maxRecords=\"@@maxRecords@@\" " +
                "startPosition=\"@@startPosition@@\">" +
                "<csw:Query typeNames=\"gmd:MD_Metadata\">" +
                "<csw:Constraint version=\"1.1.0\">" +
                "<Filter xmlns=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"/>" +
                "</csw:Constraint>" +
                "</csw:Query>" +
                "</csw:GetRecords>";
    }

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

    public static final String cswGetRecordsGETReqParamsByGroup_4 = "request=GetRecords&service=CSW&version=2.0.2"
        + "&namespace=xmlns(csw=http://www.opengis.net/cat/csw),xmlns(gmd=http://www.isotc211.org/2005/gmd)"
        + "&outputSchema=http://www.isotc211.org/2005/gmd"
        + "&resultType=results"
        + "&maxRecords=@@maxRecords@@"
        + "&startPosition=@@startPosition@@"
        + "&elementSetName=full"
        + "&typeNames=gmd:MD_Metadata"
        + "&constraintLanguage=CQL_TEXT"
        + "&constraint_language_version=1.1.0"
        + "&constraint=groupOwner%3D@@groupOwner@@@@constraint@@";


    public static final String cswGetRecordsMetadataXPath = "/csw:GetRecordsResponse/csw:SearchResults/...";

}
