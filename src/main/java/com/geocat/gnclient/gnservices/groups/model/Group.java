package com.geocat.gnclient.gnservices.groups.model;

import org.jdom2.Element;

import java.util.HashMap;
import java.util.Map;

public class Group {
    private int id;
    private String name;
    private String logo;
    private String website;
    private String defaultCategory;
    private String[] allowedCategories;
    private boolean enableAllowedCategories;
    private String referrer;
    private String email;
    private String description;
    private Map<String, String> label = new HashMap<>();


    public Group() {
        // no-args constructor
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDefaultCategory() {
        return defaultCategory;
    }

    public void setDefaultCategory(String defaultCategory) {
        this.defaultCategory = defaultCategory;
    }

    public String[] getAllowedCategories() {
        return allowedCategories;
    }

    public void setAllowedCategories(String[] allowedCategories) {
        this.allowedCategories = allowedCategories;
    }

    public boolean isEnableAllowedCategories() {
        return enableAllowedCategories;
    }

    public void setEnableAllowedCategories(boolean enableAllowedCategories) {
        this.enableAllowedCategories = enableAllowedCategories;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getLabel() {
        return label;
    }

    public void setLabel(Map<String, String> label) {
        this.label = label;
    }

    public Element toXml() {
        Element groupEl = new Element("group");
        groupEl.addContent(new Element("id").setText(id + ""));
        groupEl.addContent(new Element("name").setText(name));
        groupEl.addContent(new Element("logo").setText(logo));
        groupEl.addContent(new Element("website").setText(website));
        groupEl.addContent(new Element("defaultCategory").setText(defaultCategory));

        if (allowedCategories != null) {
            Element categoriesEl = new Element("categories");

            for(int i = 0; i < allowedCategories.length; i++) {
                Element catEl = new Element("category").setText(allowedCategories[i]);
                categoriesEl.addContent(catEl);
            }

            groupEl.addContent(categoriesEl);
        }

        groupEl.addContent(new Element("enableAllowedCategories").setText(enableAllowedCategories + ""));
        groupEl.addContent(new Element("referrer").setText(referrer + ""));
        groupEl.addContent(new Element("email").setText(email + ""));
        groupEl.addContent(new Element("description").setText(description + ""));

        return groupEl;
    }
}
