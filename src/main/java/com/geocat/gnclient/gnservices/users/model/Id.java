package com.geocat.gnclient.gnservices.users.model;

public class Id {
    private String groupId;
    private String userId;
    private String profile;

    public Id() {
        // no-args constructor
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

}
