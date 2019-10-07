package com.geocat.gnclient.gnservices;

import com.geocat.gnclient.gnservices.groups.model.Group;
import com.geocat.gnclient.gnservices.info.GNInfoServices;
import com.geocat.gnclient.gnservices.login.GNLogin;
import com.geocat.gnclient.gnservices.metadata.*;
import com.geocat.gnclient.gnservices.metadata.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.geocat.gnclient.gnservices.groups.GNGroupServices;
import com.geocat.gnclient.gnservices.groups.GNGroupServicesFactory;
import com.geocat.gnclient.gnservices.login.GNLoginCustomAuthenticator;
import com.geocat.gnclient.gnservices.login.GNLoginFactory;
import com.geocat.gnclient.gnservices.users.GNUserServices;
import com.geocat.gnclient.gnservices.users.GNUserServicesFactory;
import com.geocat.gnclient.gnservices.users.model.User;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GNServicesClient {
    private static final Logger logger = LogManager.getLogger(GNServicesClient.class);

    private GNConnection connection;
    private IMetadataProcessor metadataProcessor;
    private String baseUrl;
    private String version;
    private boolean doCustomAuthentication;
    private String customAuthenticationUrl;

    GNGroupServices groupServices;
    GNUserServices userServices;
    GNMetadataServices metadataServices;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getVersion() {
        return version;
    }


    public GNServicesClient(Builder builder) throws Exception {

        this.connection = new GNConnection();

        this.baseUrl = builder.getBaseUrl();

        if (!builder.isDoCustomAuthentication()) {
            this.version = retrieveGeoNetworkVersion(baseUrl);

            this.groupServices = GNGroupServicesFactory.getGroupServices(this.version, this.baseUrl);
            this.userServices = GNUserServicesFactory.getUserServices(this.version, this.baseUrl);
            this.metadataServices = GNMetadataServicesFactory.getMetadataServices(this.version, this.baseUrl);
        } else {
            this.doCustomAuthentication = true;
            this.customAuthenticationUrl = builder.getCustomAuthenticationUrl();
        }

        this.metadataProcessor = new ExportMetadataProcessor(builder.getOutputPath());
    }


    // LOGIN service
    public void login(String username, String password) throws Exception {
        GNLogin loginService;

        if (doCustomAuthentication) {
            loginService = new GNLoginCustomAuthenticator(baseUrl, customAuthenticationUrl);
        } else {
            loginService = GNLoginFactory.getLogin(this.version, this.baseUrl);
        }

        loginService.login(username, password, connection);


        if (doCustomAuthentication) {
            logger.info("Logged with custom authentication to GeoNetwork.");
            this.version = retrieveGeoNetworkVersion(baseUrl);
            logger.info("Retrieved GeoNetwork version:----" + this.version + "----");

            this.groupServices = GNGroupServicesFactory.getGroupServices(this.version, this.baseUrl);
            this.userServices = GNUserServicesFactory.getUserServices(this.version, this.baseUrl);
            this.metadataServices = GNMetadataServicesFactory.getMetadataServices(this.version, this.baseUrl);
        }
    }


    // METADATA services
    public List<MetadataResource> retrieveMetadata() throws Exception {
        String cswUrl = this.baseUrl + "srv/eng/csw";

        GetRecordsService metadataServices = new GetRecordsService(cswUrl, metadataProcessor);
        List<MetadataResource> metadataResourceList = metadataServices.getRecords(connection);

        return metadataResourceList;
    }

    public MetadataInfo retrieveMetadataInfo(String uuid, Map<String, Group> groups) throws Exception {
        MetadataSharing mdPrivileges = metadataServices.getMetadataSharing(uuid, groups, connection);
        //System.out.println(mdPrivileges);

        MetadataStatus mdStatus = metadataServices.getMetadataStatus(uuid, connection);
        //System.out.println(mdStatus);

        MetadataInfo mdInfo = new MetadataInfo();
        mdInfo.setPrivileges(mdPrivileges);
        mdInfo.setStatus(mdStatus);
        mdInfo.setUuid(uuid);

        return mdInfo;
    }

    public long countMetadataForGroup(int groupId) throws Exception {
        long count =  metadataServices.countMetadataForGroup(groupId, connection);
        return count;
    }

    public MetadataSearchResults retrieveMetadataPaginated(String filter, int from) throws Exception {
        MetadataSearchResults results = metadataServices.retrieveMetadata(filter, from, connection);

        return results;
    }

    public MetadataSearchResults retrieveMetadataForGroupPaginated(int groupId, String filter, int from) throws Exception {
        MetadataSearchResults results = metadataServices.retrieveMetadataForGroup(groupId, filter, from, connection);

        return results;
    }


    public Map<String, String> retrieveAllMetadataForGroup(int groupId, String filter) throws Exception {
        //long count =  metadataServices.countMetadataForGroup(groupId, connection);

        Map<String, String> metadata = new HashMap<>();

        MetadataSearchResults results = metadataServices.retrieveMetadataForGroup(groupId, filter, 1, connection);
        metadata.putAll(results.getMetadata());

        int total = results.getTotalReturned();
        int nextRecord = results.getNextRecord();

        while ((nextRecord > 0) && (nextRecord < total)) {
            results = metadataServices.retrieveMetadataForGroup(groupId, filter, nextRecord, connection);
            metadata.putAll(results.getMetadata());

            total = results.getTotalReturned();
            nextRecord = results.getNextRecord();
        }

        return metadata;
    }


    public long insertMetadata(String metadata, String uuid, int ownerId, int groupOwnerId, String fullRightsForOwnerGroup,
                                  String metadataPublic, String status, String isTemplate, String schema, String styleSheet) throws Exception {
        return metadataServices.insertMetadata( metadata, uuid, ownerId, groupOwnerId, fullRightsForOwnerGroup, metadataPublic,
            status, isTemplate, schema, styleSheet, connection);
    }


    public boolean updateMetadataPrivilege(long metadataId, List<MetadataPrivilege> privileges) throws Exception {
        return metadataServices.updateMetadataPrivilege(metadataId, privileges, connection);
    }

    public boolean updateMetadataStatus(String uuid, long metadataId, MetadataStatus status) throws Exception {
        return metadataServices.updateMetadataStatus(uuid, metadataId, status, connection);
    }

    public boolean deleteMetadata(String uuid) throws Exception {
        return metadataServices.deleteMetadata(uuid, connection);
    }

    // GROUP operations
    public Map<String, Group> retrieveGroups() throws Exception {
        Map<String, Group> groups =
            groupServices.getGroupList(connection);
        return groups;
    }


    public int createGroup(Group group) throws Exception {
        return groupServices.createGroup(group, connection);
    }


    public void updateGroup(Group group) throws Exception {
        groupServices.updateGroup(group, connection);
    }

    public void deleteGroup(Group group) throws Exception {
        groupServices.deleteGroup(group, connection);
    }

    // USER operations
    public User[] retrieveUsers() throws Exception {
        User[] users =
            userServices.getUserList(connection);

        return users;
    }


    public Map<String, String> retrieveUserGroups(String userId) throws Exception {
        Map<String, String> userGroups =
            userServices.getUserGroups(userId, connection);

        userGroups.forEach((k, v) -> {
            System.out.println(k + " - " + v);
        });

        return userGroups;
    }


    public int createUser(User user) throws Exception {
        return userServices.createUser(user, connection);
    }


    public void updateUser(User user) throws Exception {
        userServices.updateUser(user, connection);
    }

    public void deleteUser(User user) throws Exception {
        userServices.deleteUser(user, connection);
    }

    public void close() {
      connection.close();
    }


    private String retrieveGeoNetworkVersion(String baseUrl) throws Exception {
        GNInfoServices infoServices = new GNInfoServices(baseUrl);

        return infoServices.getVersion(connection);
    }


    public static class Builder {

        private final Path outputPath;
        private String baseUrl = "";
        private boolean doCustomAuthentication = false;
        private String customAuthenticationUrl = "";

        public Path getOutputPath() {
            return outputPath;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public boolean isDoCustomAuthentication() {
            return doCustomAuthentication;
        }

        public String getCustomAuthenticationUrl() {
            return customAuthenticationUrl;
        }


        public Builder(String baseUrl, Path outputPath) {
            if(!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }

            this.baseUrl = baseUrl;
            this.outputPath = outputPath;
        }

        public Builder doCustomAuthentication(boolean doCustomAuthentication) {
            this.doCustomAuthentication = doCustomAuthentication;
            return this;
        }

        public Builder customAuthenticationUrl(String customAuthenticationUrl) {
            this.customAuthenticationUrl = customAuthenticationUrl;
            return this;
        }

        public GNServicesClient build() throws Exception {
            return new GNServicesClient(this);
        }
    }
}
