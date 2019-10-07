package com.geocat.gnclient.gnservices.metadata.model;

public class MetadataSharing{
    private String owner;
    private String groupOwner;
    private MetadataPrivilege[] privileges;

    public MetadataSharing() {
        // no-args constructor
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(String groupOwner) {
        this.groupOwner = groupOwner;
    }

    public MetadataPrivilege[] getPrivileges() {
        return privileges;
    }

    public void setPrivileges(MetadataPrivilege[] privileges) {
        this.privileges = privileges;
    }
}
