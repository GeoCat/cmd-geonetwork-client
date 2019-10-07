package com.geocat.gnclient.gnservices.groups;

import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.groups.model.Group;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import com.geocat.gnclient.util.HelperUtility;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class GNGroupServices210 extends GNGroupServices {
    private static final Logger logger = LogManager.getLogger(GNGroupServices210.class);

    public GNGroupServices210(String baseUrl) {
        super(baseUrl);

        groupListServiceUrl =  HelperUtility.addPathToUrl(baseUrl, "srv/eng");
    }

    @Override
	public Map<String, Group> getGroupList(GNConnection connection) throws Exception {
		Map<String,Group> groupList = new HashMap<String, Group>();

        try {
            String response = doGet(connection,
                HelperUtility.addPathToUrl(groupListServiceUrl, "xml.group.list"), "application/xml");

            SAXBuilder saxBuilder = new SAXBuilder();
            StringReader xmlReader = new StringReader(response);
            org.jdom2.Document doc = saxBuilder.build(xmlReader);

            Element rootElem = doc.getRootElement();
            for(Element groupEl: rootElem.getChildren("record")) {
                Group grpRecord = new Group();


                grpRecord.setId(Integer.parseInt(groupEl.getChild("id").getText()));
                grpRecord.setName(groupEl.getChild("name").getText());
                grpRecord.setDescription(groupEl.getChild("description").getText());
                grpRecord.setReferrer(groupEl.getChild("referrer").getText());

                groupList.put(grpRecord.getName(), grpRecord);
            }

        } catch (Exception ex) {
            logger.error("getGroupList: ", ex);
            throw new Exception("Error retrieving groups list, error: " + ex.getMessage());
        }

    	return groupList;
    }


    @Override
    public int createGroup(Group group, GNConnection connection) throws Exception {
        throw new Exception("Not implemented");
    }

    @Override
    public void updateGroup(Group group, GNConnection connection) throws Exception {
        throw new Exception("Not implemented");
    }


    @Override
    public void deleteGroup(Group group, GNConnection connection) throws Exception {
        throw new Exception("Not implemented");
    }
}
