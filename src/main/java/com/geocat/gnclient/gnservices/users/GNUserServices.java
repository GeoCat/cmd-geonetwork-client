package com.geocat.gnclient.gnservices.users;


import com.geocat.gnclient.gnservices.GNBaseServices;
import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.users.model.Id;
import com.geocat.gnclient.gnservices.users.model.User;
import com.geocat.gnclient.gnservices.users.model.UserGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class GNUserServices extends GNBaseServices {
    protected String usersServiceUrl;


    public GNUserServices(String baseUrl) {
        super(baseUrl);
    }


    public abstract User[] getUserList(GNConnection connection) throws Exception;

    //public abstract Map<String, String> getUserList(GNConnection connection);

    public abstract Map<String, String> getUserGroups(String userId, GNConnection connection) throws Exception;

    public abstract int createUser(User user, GNConnection connection) throws Exception;

    public abstract void updateUser(User user, GNConnection connection) throws Exception;

    public abstract void deleteUser(User user, GNConnection connection) throws Exception;

    protected void addUserGroupsToUser(User user, UserGroup[] userGroups) {
        List<Id> ids = new ArrayList<>();
        for(int i = 0; i < userGroups.length; i++) {
            ids.add(userGroups[i].getId());
            String profile = userGroups[i].getId().getProfile();

            if (profile.equalsIgnoreCase("registereduser")) {
                user.getGroupsRegisteredUser().add(userGroups[i].getId().getGroupId());
            } else if (profile.equalsIgnoreCase("editor")) {
                user.getGroupsEditor().add(userGroups[i].getId().getGroupId());
            } else if (profile.equalsIgnoreCase("reviewer")) {
                user.getGroupsReviewer().add(userGroups[i].getId().getGroupId());
            } else if (profile.equalsIgnoreCase("useradmin")) {
                user.getGroupsUserAdmin().add(userGroups[i].getId().getGroupId());
            }
        }
    }
}
