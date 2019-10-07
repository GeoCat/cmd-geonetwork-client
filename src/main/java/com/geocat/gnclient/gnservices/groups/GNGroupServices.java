package com.geocat.gnclient.gnservices.groups;


import com.geocat.gnclient.gnservices.GNBaseServices;
import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.groups.model.Group;

import java.util.Map;

public abstract class GNGroupServices extends GNBaseServices {
	protected String groupListServiceUrl;

	public GNGroupServices(String baseUrl) {
	    super(baseUrl);
    }

	public abstract Map<String, Group>  getGroupList(GNConnection connection) throws Exception;

    /**
     * Creates a group and returns the identifier assigned.
     * @param group
     * @param connection
     * @return
     * @throws Exception
     */
    public abstract int createGroup(Group group, GNConnection connection) throws Exception;

    public abstract void updateGroup(Group group, GNConnection connection) throws Exception;

    public abstract void deleteGroup(Group group, GNConnection connection) throws Exception;
}
