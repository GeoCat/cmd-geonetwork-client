package com.geocat.gnclient.gnservices.users;

import com.geocat.gnclient.gnservices.GNConnection;
import com.geocat.gnclient.gnservices.users.model.Address;
import com.geocat.gnclient.gnservices.users.model.Id;
import com.geocat.gnclient.gnservices.users.model.User;
import com.geocat.gnclient.gnservices.users.model.UserGroup;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import com.geocat.gnclient.util.HelperUtility;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GNUserServices210 extends GNUserServices {
    private static final Logger logger = LogManager.getLogger(GNUserServices210.class);

    public GNUserServices210(String baseUrl) {
        super(baseUrl);
        this.usersServiceUrl = HelperUtility.addPathToUrl(baseUrl, "srv/eng");
    }

    @Override
    public User[] getUserList(GNConnection connection) throws Exception {
        List<User> userList = new ArrayList<>();

        try {
            String response = doGet(connection,
                HelperUtility.addPathToUrl(usersServiceUrl, "xml.user.list"), "application/xml");

            SAXBuilder saxBuilder = new SAXBuilder();
            StringReader xmlReader = new StringReader(response);
            org.jdom2.Document doc = saxBuilder.build(xmlReader);

            Element rootElem = doc.getRootElement();
            for(Element userEl: rootElem.getChildren("record")) {
                User userRecord = new User();

                // GN 2.10 doesn't have the enabled information for the users, set as enabled
                userRecord.setEnabled(true);

                userRecord.setId(Integer.parseInt(userEl.getChild("id").getText()));
                userRecord.setUsername(userEl.getChild("username").getText());
                userRecord.setName(userEl.getChild("name").getText());
                userRecord.setSurname(userEl.getChild("surname").getText());
                userRecord.setProfile(userEl.getChild("profile").getText());
                Address address = new Address();
                address.setAddress(userEl.getChild("address").getText());
                address.setCity(userEl.getChild("city").getText());
                address.setState(userEl.getChild("state").getText());
                address.setCountry(userEl.getChild("country").getText());
                address.setZip(userEl.getChild("zip").getText());

                userRecord.getAddresses().add(address);

                userRecord.getEmailAddresses().add(userEl.getChild("email").getText());
                userRecord.setOrganisation(userEl.getChild("organisation").getText());
                userList.add(userRecord);


                UserGroup[] userGroups = getUserGroupsInternal(userRecord.getId(), connection);
                addUserGroupsToUser(userRecord, userGroups);

                //Thread.sleep(2000);
            }

        } catch (Exception ex) {
            logger.error("getUserList: ", ex);
            throw new Exception("Error retrieving users list, error: " + ex.getMessage());
        }

        return userList.stream().toArray(User[]::new);
    }


    /**
     * Example response:
     *             <response>
     *                 <record>
     *                     <id>2</id>
     *                     <username>useradminA</username>
     *                     <password>
     *                                     191d9a55d688d55b2d482f10e3163655e727d7d91700b03bcf3a38377ab27e5fa326b6352bb3114c
     *                                     </password>
     *                     <surname>useradminA</surname>
     *                     <name>useradminA</name>
     *                     <profile>UserAdmin</profile>
     *                     <address/>
     *                     <city/>
     *                     <state/>
     *                     <zip/>
     *                     <country/>
     *                     <email>useradminA@mail.com</email>
     *                     <organisation/>
     *                     <kind>gov</kind>
     *                     <security/>
     *                     <authtype/>
     *                 </record>
     *                 <groups>
     *                     <id profile="Editor">3</id>
     *                     <id profile="Reviewer">3</id>
     *                     <id profile="UserAdmin">3</id>
     *                 </groups>
     *             </response
     * @param userId
     * @param connection
     * @return
     * @throws Exception
     */
    public UserGroup[] getUserGroupsInternal(int userId, GNConnection connection) throws Exception {
        List<UserGroup> userGroupsList = new ArrayList<>();

        // xml.usergroups.list?id= doesn't return the user profile in the group!!
        // No xml service for xml.user.get, so using the UI service with "!", not optimal, but no other good option : user.get!?id=
        try {
            logger.info("getUserGroupsInternal url:" + HelperUtility.addPathToUrl(usersServiceUrl, "xml.user.get?id=" + userId));

            String response = doGet(connection, HelperUtility.addPathToUrl(usersServiceUrl, "xml.user.get?id=" + userId),
                "application/xml");

            SAXBuilder saxBuilder = new SAXBuilder();
            StringReader xmlReader = new StringReader(response);
            org.jdom2.Document doc = saxBuilder.build(xmlReader);

            Element rootElem = doc.getRootElement();

            for(Element userRecord: rootElem.getChild("groups").getChildren()) {

                UserGroup userGroup = new UserGroup();

                String groupId = userRecord.getText();
                String profile = userRecord.getAttributeValue("profile");

                Id id = new Id();
                id.setGroupId(groupId);
                id.setUserId(userId + "");
                id.setProfile(profile);

                userGroup.setId(id);

                userGroupsList.add(userGroup);
            }
        } catch (Exception ex) {
            logger.error("getUserGroupsInternal error:", ex);
            throw new Exception("Error retrieving user groups for user " + userId + ", error: " + ex.getMessage());
        }

        return userGroupsList.stream().toArray(UserGroup[]::new);
    }

    public Map<String, String> getUserGroups(String userId, GNConnection connection) throws Exception {
        Map<String,String> userGroupsList = new HashMap<String,String>();

        try {
            String response = doGet(connection, HelperUtility.addPathToUrl(usersServiceUrl,  userId + "/groups"),
                "application/json");

            Gson gson = new Gson();
            UserGroup[] userGroupsArray = gson.fromJson(response, UserGroup[].class);

            for(UserGroup userGroupRecord: userGroupsArray) {
                userGroupsList.put(userGroupRecord.getId().getGroupId(), userGroupRecord.getId().getProfile());
            }

        } catch (IOException ex) {
            logger.error("getUserGroups: ", ex);
            throw new Exception("Error retrieving user groups for user " + userId + ", error: " + ex.getMessage());
        }

        return userGroupsList;
    }


    @Override
    public int createUser(User user, GNConnection connection) throws Exception {
        throw new Exception("Not implemented");
    }


    @Override
    public void updateUser(User user, GNConnection connection) throws Exception {
        throw new Exception("Not implemented");
    }

    @Override
    public void deleteUser(User user, GNConnection connection) throws Exception {
        throw new Exception("Not implemented");
    }
}
