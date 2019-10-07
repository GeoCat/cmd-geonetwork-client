package com.geocat.gnclient.gnservices.metadata.model;

import org.apache.commons.lang.StringUtils;

public class MetadataStatusCreate {
    private Integer status;
    private String comment;

    public MetadataStatusCreate() {
        // no-args constructor
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static MetadataStatusCreate build(MetadataStatus st) {
        MetadataStatusCreate statusCreate = new MetadataStatusCreate();

        statusCreate.setComment(st.getChangeMessage());

        if (StringUtils.isEmpty(st.getName()) ||
            st.getName().equalsIgnoreCase("unknown")) {
            statusCreate.setStatus(0);

        } else if (st.getName().equalsIgnoreCase("draft")) {
            statusCreate.setStatus(1);

        } else if (st.getName().equalsIgnoreCase("approved")) {
            statusCreate.setStatus(2);

        } else if (st.getName().equalsIgnoreCase("retired")) {
            statusCreate.setStatus(3);

        } else if (st.getName().equalsIgnoreCase("submitted")) {
            statusCreate.setStatus(4);

        } else if (st.getName().equalsIgnoreCase("rejected")) {
            statusCreate.setStatus(5);

        } else {
            statusCreate.setStatus(0);
        }

        return statusCreate;
    }
}
