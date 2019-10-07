package com.geocat.gnclient.exporter;

/**
 * Report for the import process result.
 *
 */
public class ExporterReport {
    private boolean exportGroups;
    private boolean exportUsers;
    private boolean exportMetadata;
    private boolean errorsExportingGroups;
    private boolean errorsExportingUsers;
    private boolean errorsExportingMetadata;
    private String errorsExportingGroupsText;
    private String errorsExportingUsersText;
    private String errorsExportingMetadataText;

    public boolean isExportGroups() {
        return exportGroups;
    }

    public void setExportGroups(boolean exportGroups) {
        this.exportGroups = exportGroups;
    }

    public boolean isExportUsers() {
        return exportUsers;
    }

    public void setExportUsers(boolean exportUsers) {
        this.exportUsers = exportUsers;
    }

    public boolean isExportMetadata() {
        return exportMetadata;
    }

    public void setExportMetadata(boolean exportMetadata) {
        this.exportMetadata = exportMetadata;
    }

    public boolean isErrorsExportingGroups() {
        return errorsExportingGroups;
    }

    public void setErrorsExportingGroups(boolean errorsExportingGroups) {
        this.errorsExportingGroups = errorsExportingGroups;
    }

    public boolean isErrorsExportingUsers() {
        return errorsExportingUsers;
    }

    public void setErrorsExportingUsers(boolean errorsExportingUsers) {
        this.errorsExportingUsers = errorsExportingUsers;
    }

    public boolean isErrorsExportingMetadata() {
        return errorsExportingMetadata;
    }

    public void setErrorsExportingMetadata(boolean errorsExportingMetadata) {
        this.errorsExportingMetadata = errorsExportingMetadata;
    }

    public String getErrorsExportingGroupsText() {
        return errorsExportingGroupsText;
    }

    public void setErrorsExportingGroupsText(String errorsExportingGroupsText) {
        this.errorsExportingGroupsText = errorsExportingGroupsText;
    }

    public String getErrorsExportingUsersText() {
        return errorsExportingUsersText;
    }

    public void setErrorsExportingUsersText(String errorsExportingUsersText) {
        this.errorsExportingUsersText = errorsExportingUsersText;
    }

    public String getErrorsExportingMetadataText() {
        return errorsExportingMetadataText;
    }

    public void setErrorsExportingMetadataText(String errorsExportingMetadataText) {
        this.errorsExportingMetadataText = errorsExportingMetadataText;
    }

    @Override
    public String toString() {
        return "Export report:\n" +
            "  - Export groups: " + (exportGroups ?"yes":"no") + "\n" +
            (exportGroups?"  - Errors exporting groups: " +  (errorsExportingGroups?"yes":"no") + "\n":"") +
            (exportGroups && errorsExportingGroups?errorsExportingGroupsText + "\n":"") +
            "  - Export users: " + (exportUsers ?"yes":"no") + "\n" +
            (exportUsers?"  - Errors exporting users: " +  (errorsExportingUsers?"yes":"no") + "\n":"") +
            (exportUsers && errorsExportingUsers?errorsExportingUsersText + "\n":"") +
            "  - Export metadata: " + (exportMetadata ?"yes":"no") + "\n" +
            (exportMetadata?"  - Errors exporting metadata: " +  (errorsExportingMetadata?"yes":"no") + "\n":"") +
            (exportMetadata && errorsExportingMetadata?errorsExportingMetadataText + "\n":"");
    }
}
