package com.geocat.gnclient.gnservices.metadata.model.v4;

import com.geocat.gnclient.gnservices.metadata.model.MetadataPrivilege;
import com.geocat.gnclient.gnservices.metadata.model.MetadataSharing;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.List;

public class MetadataInfoV4 {
    private List<MetadataStatusV4> status;
    private MetadataSharing privileges;

    private String uuid;
    private String groupName;

    public MetadataInfoV4() {
        // no-args constructor
    }

    public List<MetadataStatusV4> getStatus() {
        return status;
    }

    public void setStatus(List<MetadataStatusV4> status) {
        this.status = status;
    }

    public MetadataSharing getPrivileges() {
        return privileges;
    }

    public void setPrivileges(MetadataSharing privileges) {
        this.privileges = privileges;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAsXml() {
        /*
         * UUID
         * GroupID
         * Groupname
         * Owner
         * Metadata-status
         * Privileges
         * Datecreated (from.DB)
         * DateUpdated (from db)
         */
        Element rootEl = new Element("info");
        rootEl.addContent(new Element("uuid").setText(getUuid()));
        rootEl.addContent(new Element("groupId").setText(privileges.getGroupOwner()));
        rootEl.addContent(new Element("groupName").setText(getGroupName()));
        rootEl.addContent(new Element("owner").setText(privileges.getOwner()));

        Element statusesEl = new Element("statuses");

        for(MetadataStatusV4 s: status) {
            Element statusEl = new Element("status");

            statusEl.addContent(new Element("name").setText(s.getName()));
            statusEl.addContent(new Element("changeDate").setText(s.getDateChange()));
            statusEl.addContent(new Element("changeMessage").setText(s.getChangeMessage()));
            statusEl.addContent(new Element("userId").setText(s.getUserId()));

            statusesEl.addContent(statusEl);
        }


        rootEl.addContent(statusesEl);

        Element privilegesEl = new Element("privileges");

        for(int i = 0; i < privileges.getPrivileges().length; i++) {
            MetadataPrivilege priv = privileges.getPrivileges()[i];

            Element privEl = new Element("privilege");
            privEl.addContent(new Element("group").setText(priv.getGroup()));
            privEl.addContent(new Element("view").setText(priv.getOperations().isView() + ""));
            privEl.addContent(new Element("download").setText(priv.getOperations().isDownload() + ""));
            privEl.addContent(new Element("editing").setText(priv.getOperations().isEditing() + ""));
            privEl.addContent(new Element("dynamic").setText(priv.getOperations().isDynamic() + ""));
            privEl.addContent(new Element("featured").setText(priv.getOperations().isFeatured() + ""));
            privEl.addContent(new Element("notify").setText(priv.getOperations().isNotify() + ""));

            privilegesEl.addContent(privEl);
        }

        rootEl.addContent(privilegesEl);

        XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
        String info = outp.outputString(rootEl);

        return info;

    }
}
