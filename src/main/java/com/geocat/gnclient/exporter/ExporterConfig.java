package com.geocat.gnclient.exporter;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Configuration parameters for CLI export mode.
 *
 */
public class ExporterConfig {
    // Path to export the metadata
    private final Path outputPath;
    private String defaultExportMetadataTypes;
    private String defaultCswConstraint;
    private boolean defaultExportFilterByGroupLocally = false;
    private int defaultRequestDelay = 0;
    private String defaultExportType;
    private LocalDateTime defaultMetadataModifiedDateSinceFilter;

    public Path getOutputPath() {
        return outputPath;
    }

    public String getDefaultExportMetadataTypes() {
        return defaultExportMetadataTypes;
    }

    public String getDefaultCswConstraint() {
        return defaultCswConstraint;
    }

    public boolean getDefaultExportFilterByGroupLocally() {
        return defaultExportFilterByGroupLocally;
    }

    public int getDefaultRequestDelay() {
        return defaultRequestDelay;
    }

    public String getDefaultExportType() {
        return defaultExportType;
    }

    public LocalDateTime getDefaultMetadataModifiedDateSinceFilter() {
        return defaultMetadataModifiedDateSinceFilter;
    }

    private ExporterConfig(Builder builder) {
        outputPath = builder.outputPath;
        defaultExportMetadataTypes = builder.defaultExportMetadataTypes;
        defaultCswConstraint = builder.defaultCswConstraint;
        defaultExportFilterByGroupLocally = builder.defaultExportFilterByGroupLocally;
        defaultRequestDelay = builder.defaultRequestDelay;
        defaultExportType = builder.defaultExportType;
        defaultMetadataModifiedDateSinceFilter = builder.defaultMetadataModifiedDateSinceFilter;
    }

    public static class Builder {
        private final Path outputPath;
        private String defaultExportMetadataTypes = "";
        private String defaultCswConstraint = "";
        private boolean defaultExportFilterByGroupLocally = false;
        private int defaultRequestDelay = 0;
        private String defaultExportType = "all";
        private LocalDateTime defaultMetadataModifiedDateSinceFilter;

        public Builder(Path outputPath) {
            this.outputPath = outputPath;
        }

        public Builder defaultExportMetadataTypes(String defaultExportMetadataTypes) {
            this.defaultExportMetadataTypes = defaultExportMetadataTypes;
            return this;
        }

        public Builder defaultCswConstraint(String defaultCswConstraint) {
            this.defaultCswConstraint = defaultCswConstraint;
            return this;
        }

        public Builder defaultExportFilterByGroupLocally(boolean defaultExportFilterByGroupLocally) {
            this.defaultExportFilterByGroupLocally = defaultExportFilterByGroupLocally;
            return this;
        }

        public Builder defaultRequestDelay(int defaultRequestDelay) {
            this.defaultRequestDelay = defaultRequestDelay;
            return this;
        }

        public Builder defaultExportType(String defaultExportType) {
            this.defaultExportType = defaultExportType;
            return this;
        }

        public Builder defaultExportMetadataModifiedSince(LocalDateTime defaultMetadataModifiedDateSinceFilter) {
            this.defaultMetadataModifiedDateSinceFilter = defaultMetadataModifiedDateSinceFilter;
            return this;
        }


        public ExporterConfig build() {
            return new ExporterConfig(this);
        }
    }
}
