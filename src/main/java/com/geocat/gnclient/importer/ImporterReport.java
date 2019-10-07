package com.geocat.gnclient.importer;

/**
 * Report for the import process result.
 *
 */
public class ImporterReport {
    private boolean removeData;
    private boolean errorsRemovingData;
    private boolean existsGroupsFile;
    private boolean existsUsersFile;
    private boolean errorsImportingGroups;
    private boolean errorsImportingUsers;
    private boolean errorsImportingMetadata;
    private boolean existsMetadataToImport;
    private String errorsRemoveDataText;
    private String errorsImportingGroupsText;
    private String errorsImportingUsersText;
    private String errorsImportingMetadataText;


    public boolean isRemoveData() {
        return removeData;
    }

    public void setRemoveData(boolean removeData) {
        this.removeData = removeData;
    }

    public boolean isErrorsRemovingData() {
        return errorsRemovingData;
    }

    public void setErrorsRemovingData(boolean errorsRemovingData) {
        this.errorsRemovingData = errorsRemovingData;
    }

    public boolean isExistsGroupsFile() {
        return existsGroupsFile;
    }

    public void setExistsGroupsFile(boolean existsGroupsFile) {
        this.existsGroupsFile = existsGroupsFile;
    }

    public boolean isExistsUsersFile() {
        return existsUsersFile;
    }

    public void setExistsUsersFile(boolean existsUsersFile) {
        this.existsUsersFile = existsUsersFile;
    }

    public boolean isErrorsImportingGroups() {
        return errorsImportingGroups;
    }

    public void setErrorsImportingGroups(boolean errorsImportingGroups) {
        this.errorsImportingGroups = errorsImportingGroups;
    }

    public boolean isErrorsImportingUsers() {
        return errorsImportingUsers;
    }

    public void setErrorsImportingUsers(boolean errorsImportingUsers) {
        this.errorsImportingUsers = errorsImportingUsers;
    }

    public boolean isErrorsImportingMetadata() {
        return errorsImportingMetadata;
    }

    public void setErrorsImportingMetadata(boolean errorsImportingMetadata) {
        this.errorsImportingMetadata = errorsImportingMetadata;
    }


    public boolean isExistsMetadataToImport() {
        return existsMetadataToImport;
    }

    public void setExistsMetadataToImport(boolean existsMetadataToImport) {
        this.existsMetadataToImport = existsMetadataToImport;
    }

    public String getErrorsRemoveDataText() {
        return errorsRemoveDataText;
    }

    public void setErrorsRemoveDataText(String errorsRemoveDataText) {
        this.errorsRemoveDataText = errorsRemoveDataText;
    }

    public String getErrorsImportingGroupsText() {
        return errorsImportingGroupsText;
    }

    public void setErrorsImportingGroupsText(String errorsImportingGroupsText) {
        this.errorsImportingGroupsText = errorsImportingGroupsText;
    }

    public String getErrorsImportingUsersText() {
        return errorsImportingUsersText;
    }

    public void setErrorsImportingUsersText(String errorsImportingUsersText) {
        this.errorsImportingUsersText = errorsImportingUsersText;
    }

    public String getErrorsImportingMetadataText() {
        return errorsImportingMetadataText;
    }

    public void setErrorsImportingMetadataText(String errorsImportingMetadataText) {
        this.errorsImportingMetadataText = errorsImportingMetadataText;
    }

    @Override
    public String toString() {
        return "Import report:\n" +
            "  - Remove existing data option selected: " + (removeData ?"yes":"no") + "\n" +
            (removeData?"  - Errors removing existing data: " +  (errorsRemovingData?"yes":"no") + "\n":"") +
            (removeData && errorsRemovingData?errorsRemoveDataText + "\n":"") +
            "  - Exists groups file: " + (existsGroupsFile?"yes":"no") + "\n" +
            (existsGroupsFile?"  - Errors importing groups: " + (errorsImportingGroups?"yes":"no") + "\n":"") +
            (errorsImportingGroups?errorsImportingGroupsText + "\n":"") +
            "  - Exists users file: " + (existsUsersFile?"yes":"no") + "\n" +
            (existsUsersFile?"  - Errors importing users: " + (errorsImportingUsers?"yes":"no") + "\n":"") +
            (errorsImportingUsers?errorsImportingUsersText + "\n":"") +
            "  - Exist metadata to import: " + (existsMetadataToImport ?"yes":"no") + "\n" +
            "  - Errors importing metadata: " + (errorsImportingMetadata?"yes":"no");
    }
}
