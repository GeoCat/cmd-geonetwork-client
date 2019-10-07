package com.geocat.gnclient.gnservices.metadata.model;

public class MetadataPrivilege {
    private String group;
    private Operations operations;

    public MetadataPrivilege() {
        // no-args constructor
        operations = new Operations();
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Operations getOperations() {
        return operations;
    }

    public void setOperations(Operations operations) {
        this.operations = operations;
    }
}
