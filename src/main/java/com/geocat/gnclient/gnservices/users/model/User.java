package com.geocat.gnclient.gnservices.users.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String password;
    private String name;
    private String surname;
    private String profile;
    private String organisation;
    private boolean enabled;
    private List<String> emailAddresses;
    private List<Address> addresses;

    private List<String> groupsRegisteredUser;
    private List<String> groupsEditor;
    private List<String> groupsReviewer;
    private List<String> groupsUserAdmin;

    //private List<Id> userGroups;

    public User() {
        // no-args constructor
        emailAddresses = new ArrayList<>();
        addresses = new ArrayList<>();

        groupsRegisteredUser = new ArrayList<>();
        groupsEditor = new ArrayList<>();
        groupsReviewer = new ArrayList<>();
        groupsUserAdmin = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<String> getGroupsRegisteredUser() {
        return groupsRegisteredUser;
    }

    public void setGroupsRegisteredUser(List<String> groupsRegisteredUser) {
        this.groupsRegisteredUser = groupsRegisteredUser;
    }

    public List<String> getGroupsEditor() {
        return groupsEditor;
    }

    public void setGroupsEditor(List<String> groupsEditor) {
        this.groupsEditor = groupsEditor;
    }

    public List<String> getGroupsReviewer() {
        return groupsReviewer;
    }

    public void setGroupsReviewer(List<String> groupsReviewer) {
        this.groupsReviewer = groupsReviewer;
    }

    public List<String> getGroupsUserAdmin() {
        return groupsUserAdmin;
    }

    public void setGroupsUserAdmin(List<String> groupsUserAdmin) {
        this.groupsUserAdmin = groupsUserAdmin;
    }

    /*public List<Id> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<Id> userGroups) {
        this.userGroups = userGroups;
    }*/
}
