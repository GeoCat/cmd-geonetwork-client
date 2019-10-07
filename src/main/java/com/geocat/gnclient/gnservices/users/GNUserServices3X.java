package com.geocat.gnclient.gnservices.users;

import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.users.model.User;
import com.geocat.gnclient.gnservices.users.model.UserGroup;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.geocat.gnclient.util.HelperUtility;

import java.io.IOException;
import java.util.*;

public class GNUserServices3X extends GNUserServices {
    private static final Logger logger = LogManager.getLogger(GNUserServices3X.class);

    public GNUserServices3X(String baseUrl) {
        super(baseUrl);
        this.usersServiceUrl = HelperUtility.addPathToUrl(baseUrl,"srv/api/users");
    }

    @Override
    public User[] getUserList(GNConnection connection) throws Exception {
        User[] usersArray = null;

        try {
            String response = doGet(connection, usersServiceUrl, "application/json");

            Gson gson = new Gson();
            usersArray = gson.fromJson(response, User[].class);


            for(User userRecord: usersArray) {
                UserGroup[] userGroups = getUserGroupsInternal(userRecord.getId(), connection);

                addUserGroupsToUser(userRecord, userGroups);
                //userRecord.setUserGroups(ids);
            }

        } catch (Exception ex) {
            logger.error("getUserList: ", ex);
            throw new Exception("Error retrieving users list, error: " + ex.getMessage());
        }

        return usersArray;
    }


    public UserGroup[] getUserGroupsInternal(int userId, GNConnection connection) throws Exception {
        UserGroup[] userGroupsArray = null;

        try {
            String response = doGet(connection, usersServiceUrl + "/" + userId + "/groups",
                "application/json");

            Gson gson = new Gson();
            userGroupsArray = gson.fromJson(response, UserGroup[].class);

        } catch (IOException ex) {
            logger.error("getUserGroupsInternal: " + userId, ex);
            throw new Exception("Error retrieving user groups for user " + userId + ", error: " + ex.getMessage());
        }

        return userGroupsArray;
    }

    public Map<String, String> getUserGroups(String userId, GNConnection connection) throws Exception {
        Map<String,String> userGroupsList = new HashMap<String,String>();

        try {
            String response = doGet(connection, usersServiceUrl + "/" + userId + "/groups",
                "application/json");

            Gson gson = new Gson();
            UserGroup[] userGroupsArray = gson.fromJson(response, UserGroup[].class);

            for(UserGroup userGroupRecord: userGroupsArray) {
                userGroupsList.put(userGroupRecord.getId().getGroupId(), userGroupRecord.getId().getProfile());
            }

        } catch (Exception ex) {
            logger.error("getUserGroups: " + userId, ex);
            throw new Exception("Error retrieving user groups for user " + userId + ", error: " + ex.getMessage());
        }

        return userGroupsList;
    }


    @Override
    public int createUser(User user, GNConnection connection) throws Exception {

        try {
            Gson gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                        return fieldAttributes.getName().equals("userGroups");
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> aClass) {
                        return false;
                    }
                })
                .create();

            String request = gson.toJson(user);

            String response = doPut(connection, usersServiceUrl, request,
                "application/json", "application/json");

            String userListResponse = doGet(connection, usersServiceUrl, "application/json");
            gson = new Gson();
            User[] usersArray = gson.fromJson(userListResponse, User[].class);

            int userId = -99;

            for (int i = 0; i < usersArray.length; i++) {
                if (usersArray[i].getUsername().equalsIgnoreCase(user.getUsername())) {
                    userId = usersArray[i].getId();
                    break;
                }
            }
            return userId;
        } catch (Exception ex) {
            logger.error("createUser: " + user.getUsername() + ", " + user.getId(), ex);
            throw new Exception("Error creating user: " + user.getUsername() + ", " + user.getId() + ", error: " + ex.getMessage());
        }
    }


    @Override
    public void updateUser(User user, GNConnection connection) throws Exception {

        try {
            Gson gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                        return fieldAttributes.getName().equals("userGroups");
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> aClass) {
                        return false;
                    }
                })
                .create();

            String request = gson.toJson(user);

            String response = doPut(connection, usersServiceUrl + "/" + user.getId(), request,
                "application/json", "application/json");


        } catch (Exception ex) {
            logger.error("updateUser: " + user.getUsername() + ", " + user.getId(), ex);
            throw new Exception("Error updating user: " + user.getUsername() + ", " + user.getId() + ", error: " + ex.getMessage());
        }

    }

    @Override
    public void deleteUser(User user, GNConnection connection) throws Exception {

        try {
            String response = doDelete(connection, usersServiceUrl+ "/" + user.getId(), "",
                "application/json", "application/json");

        } catch (Exception ex) {
            logger.error("deleteUser: " + user.getUsername() + ", " + user.getId(), ex);
            throw new Exception("Error deleting user: " + user.getUsername() + ", " + user.getId() + ", error: " + ex.getMessage());
        }

    }
}
