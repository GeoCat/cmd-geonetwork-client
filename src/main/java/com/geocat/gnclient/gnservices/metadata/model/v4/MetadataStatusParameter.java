package com.geocat.gnclient.gnservices.metadata.model.v4;


import com.geocat.gnclient.gnservices.metadata.model.MetadataStatus;

public class MetadataStatusParameter {
    private StatusValueType type;
    private int status;
    private String changeMessage;
    private String dueDate;
    private String closeDate;
    private Integer owner;

    public StatusValueType getType() {
        return type;
    }

    public void setType(StatusValueType type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getChangeMessage() {
        return changeMessage;
    }

    public void setChangeMessage(String changeMessage) {
        this.changeMessage = changeMessage;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(String closeDate) {
        this.closeDate = closeDate;
    }

    public Integer getOwner() {
        return owner;
    }

    public void setOwner(Integer owner) {
        this.owner = owner;
    }

    public static MetadataStatusParameter build(MetadataStatus st) {
        MetadataStatusParameter statusParameter = new MetadataStatusParameter();
        statusParameter.setChangeMessage(st.getChangeMessage());

        if (st.getName().equals("draft")) {
            statusParameter.setStatus(1);
        } else if (st.getName().equals("submitted")) {
            statusParameter.setStatus(4);
        } else if (st.getName().equals("approved")) {
            statusParameter.setStatus(2);
        } else if (st.getName().equals("retired")) {
            statusParameter.setStatus(3);
        } else {
            statusParameter.setStatus(0);
        }

        return statusParameter;
    }
}
