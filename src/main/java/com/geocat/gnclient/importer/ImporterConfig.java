package com.geocat.gnclient.importer;

import java.nio.file.Path;

/**
 * Configuration parameters for CLI import mode.
 *
 */
public class ImporterConfig {
    // Path of the metadata to import
    private final Path outputPath;
    private String defaultUsersPassword;
    // Path to local xslt to apply to the imported metadata
    private String importLocalXslt;
    // Name of remote (available in GeoNetwork) xslt to apply to the imported metadata
    private String importRemoteXslt;
    // Default info to apply to the metadata if no UUID_info.xml file is available
    private String defaultImportUser;
    private String defaultImportGroup;
    private String defaultImportStatus;
    private String defaultImportPrivileges;
    private boolean deleteMetadataAndNonDefaultUsersAndGroups;

    public Path getOutputPath() {
        return outputPath;
    }

    public String getDefaultUsersPassword() {
        return defaultUsersPassword;
    }

    public String getImportLocalXslt() {
        return importLocalXslt;
    }

    public String getImportRemoteXslt() {
        return importRemoteXslt;
    }

    public String getDefaultImportUser() {
        return defaultImportUser;
    }

    public String getDefaultImportGroup() {
        return defaultImportGroup;
    }

    public String getDefaultImportStatus() {
        return defaultImportStatus;
    }

    public String getDefaultImportPrivileges() {
        return defaultImportPrivileges;
    }

    public boolean isDeleteMetadataAndNonDefaultUsersAndGroups() {
        return deleteMetadataAndNonDefaultUsersAndGroups;
    }

    private ImporterConfig(Builder builder) {
        outputPath = builder.outputPath;
        defaultUsersPassword = builder.defaultUsersPassword;
        importLocalXslt = builder.importLocalXslt;
        importRemoteXslt = builder.importRemoteXslt;
        defaultImportUser = builder.defaultImportUser;
        defaultImportGroup = builder.defaultImportGroup;
        defaultImportStatus = builder.defaultImportStatus;
        defaultImportPrivileges = builder.defaultImportPrivileges;
        deleteMetadataAndNonDefaultUsersAndGroups = builder.deleteMetadataAndNonDefaultUsersAndGroups;
    }

    public static class Builder {
        private final Path outputPath;
        private String defaultUsersPassword = "";
        private String importLocalXslt = "";
        private String importRemoteXslt = "";
        private String defaultImportUser = "";
        private String defaultImportGroup = "";
        private String defaultImportStatus = "";
        private String defaultImportPrivileges = "";
        private boolean deleteMetadataAndNonDefaultUsersAndGroups = false;

        public Builder(Path outputPath) {
            this.outputPath = outputPath;
        }

        public Builder defaultUsersPassword(String defaultUsersPassword) {
            this.defaultUsersPassword = defaultUsersPassword;
            return this;
        }

        public Builder importLocalXslt(String importLocalXslt) {
            this.importLocalXslt = importLocalXslt;
            return this;
        }

        public Builder importRemoteXslt(String importRemoteXslt) {
            this.importRemoteXslt = importRemoteXslt;
            return this;
        }

        public Builder defaultImportUser(String defaultImportUser) {
            this.defaultImportUser = defaultImportUser;
            return this;
        }

        public Builder defaultImportGroup(String defaultImportGroup) {
            this.defaultImportGroup = defaultImportGroup;
            return this;
        }

        public Builder defaultImportStatus(String defaultImportStatus) {
            this.defaultImportStatus = defaultImportStatus;
            return this;
        }

        public Builder defaultImportPrivileges(String defaultImportPrivileges) {
            this.defaultImportPrivileges = defaultImportPrivileges;
            return this;
        }


        public Builder deleteMetadataAndNonDefaultUsersAndGroups(boolean deleteMetadataAndNonDefaultUsersAndGroups) {
            this.deleteMetadataAndNonDefaultUsersAndGroups = deleteMetadataAndNonDefaultUsersAndGroups;
            return this;
        }

        public ImporterConfig build() {
            return new ImporterConfig(this);
        }
    }
}
