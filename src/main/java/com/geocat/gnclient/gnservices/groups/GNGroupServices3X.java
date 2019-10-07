package com.geocat.gnclient.gnservices.groups;

import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.groups.model.Group;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.geocat.gnclient.util.HelperUtility;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GNGroupServices3X extends GNGroupServices {
    private static final Logger logger = LogManager.getLogger(GNGroupServices3X.class);

    public GNGroupServices3X(String baseUrl) {
        super(baseUrl);
        groupListServiceUrl = HelperUtility.addPathToUrl(baseUrl, "srv/api/groups");
    }

    @Override
	public Map<String, Group> getGroupList(GNConnection connection) throws Exception {
		Map<String,Group> groupList = new HashMap<String, Group>();

        try {
            String response = doGet(connection, groupListServiceUrl,
                "application/json");

            Gson gson = new Gson();
            Group[] groupsArray = gson.fromJson(response, Group[].class);

            for(Group grpRecord: groupsArray) {
                groupList.put(grpRecord.getName(), grpRecord);
            }

        } catch (IOException ex) {
            logger.error("getGroupList: ", ex);
            throw new Exception("Error retrieving groups list, error: " + ex.getMessage());
        }

    	return groupList;
    }


    @Override
    public int createGroup(Group group, GNConnection connection) throws Exception {

        try {
            Gson gson = new Gson();
            String request = gson.toJson(group);

            String response = doPut(connection, groupListServiceUrl , request,
                "application/json", "application/json");

            return Integer.parseInt(response);

        } catch (Exception ex) {
            logger.error("createGroup: " + group.getName() + ", " + group.getId(), ex);
            throw new Exception("Error creating group: " + group.toString() + ", error: " + ex.getMessage());
        }
    }

    @Override
    public void updateGroup(Group group, GNConnection connection) throws Exception {

        try {
            Gson gson = new Gson();
            String request = gson.toJson(group);

            String response = doPut(connection, groupListServiceUrl+ "/" + group.getId(), request,
                "application/json", "application/json");

        } catch (IOException ex) {
            logger.error("updateGroup: " + group.getName() + ", " + group.getId(), ex);
            throw new Exception("Error updating group: " + group.toString() + ", error: " + ex.getMessage());
        }

    }

    @Override
    public void deleteGroup(Group group, GNConnection connection) throws Exception {

        try {
            String response = doDelete(connection, groupListServiceUrl+ "/" + group.getId(), "",
                "application/json", "application/json");

        } catch (IOException ex) {
            logger.error("deleteUser: " + group.getName() + ", " + group.getId(), ex);
            throw new Exception("Error deleting group: " + group.getName() + ", " + group.getId() + ", error: " + ex.getMessage());
        }

    }
}
