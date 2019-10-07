package com.geocat.gnclient;

import com.geocat.gnclient.constants.PropertyFile;
import com.geocat.gnclient.exporter.Exporter;
import com.geocat.gnclient.exporter.ExporterConfig;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.geocat.gnclient.gnservices.GNServicesClient;
import com.geocat.gnclient.importer.Importer;
import com.geocat.gnclient.importer.ImporterConfig;
import com.geocat.gnclient.util.PropertiesLoader;

import java.io.FileNotFoundException;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class Cli {
    private static final Logger logger = LogManager.getLogger(Cli.class.getName());

    private String[] args = null;
    private Options options = new Options();

    public Cli(String[] args) {

        this.args = args;

        // Create CLI options
        Option exportMode = new Option("e", "export", true, "Export mode");
        exportMode.setOptionalArg(true);
        Option importMode = new Option("i", "import", false, "Import mode");
        Option testMode = new Option("t", "test", false, "Test server connection");

        Option helpMode = new Option("h", "help", false, "Show help");

        OptionGroup optgrp = new OptionGroup();
        optgrp.setRequired(true);
        optgrp.addOption(exportMode);
        optgrp.addOption(importMode);
        optgrp.addOption(testMode);
        optgrp.addOption(helpMode);
        options.addOptionGroup(optgrp);

        Option configConfigurationFile = new Option("c", "config", true, "configuration file");
        configConfigurationFile.setRequired(true);
        options.addOption(configConfigurationFile);

        Option configRemove = new Option("r", "remove", false, "Remove all metadata AND non-default users and groups in import (only if users.json and groups.json are available in the import)");
        configRemove.setRequired(false);
        options.addOption(configRemove);
    }

    public void parse() {
        // Lock the application to avoid multiple instances running
        String appId = "cmd-geonetwork-client";
        boolean alreadyRunning;
        try {
            JUnique.acquireLock(appId);
            alreadyRunning = false;
        } catch (AlreadyLockedException e) {
            alreadyRunning = true;
        }

        if (alreadyRunning) {
            System.out.println("Application already running");
            System.exit(1);
        }

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = null;
        try {
            if (hasHelp(args)) {
                help();
            }

            cmd = parser.parse(options, args);

            boolean isTest = cmd.hasOption("t");
            boolean isExport = cmd.hasOption("e");
            boolean isImport = cmd.hasOption("i");
            boolean hasDeleteOption = cmd.hasOption("r");

            // Ask confirmation if delete option is enabled in import
            if (isImport && hasDeleteOption) {
                try (Scanner scanner = new Scanner(System.in)) {
                    while (true) {
                        System.out.print("You have selected the option delete all metadata and " +
                            "non default users and groups. Do you want to proceed? [y/N]: ");
                        String option = scanner.nextLine();
                        if ("y".equalsIgnoreCase(option)){
                            break;
                        } else if ("n".equalsIgnoreCase(option) || "".equalsIgnoreCase(option)) {
                            logger.info("Import cancelled.");
                            System.out.println("Import cancelled.");
                            System.exit(0);
                        }
                    }
                }
            }

            if (isExport || isImport || isTest) {
                boolean isLoaded = loadProperties(cmd.getOptionValue("config"));
                if(isLoaded == false) {
                    System.exit(1);
                }

                GNServicesClient gnClient = null;

                try {
                    String gnBaseUrl = PropertiesLoader.INSTANCE.getGNBaseUrl();
                    String gnUserName = PropertiesLoader.INSTANCE.getGNUserName();
                    String gnUserPwd = PropertiesLoader.INSTANCE.getGNUserPwd();
                    String gnDefaultUsersPassword = PropertiesLoader.INSTANCE.getGNUsersDefaultPassword();
                    String gnImportLocalXslt = PropertiesLoader.INSTANCE.getGNImportLocalXslt();
                    String gnImportRemoteXslt = PropertiesLoader.INSTANCE.getGNImportRemoteXslt();
                    String outputPath = PropertiesLoader.INSTANCE.getOutputPath();
                    boolean doCustomAuthentication =
                        (PropertiesLoader.INSTANCE.doCustomAuthentication().equalsIgnoreCase("true"));
                    String customAuthenticationUrl = PropertiesLoader.INSTANCE.getAuthenticationUrl();

                    System.out.println("Configuration file: " + cmd.getOptionValue("config"));
                    logger.info("Configuration file " + cmd.getOptionValue("config"));

                    System.out.println("GeoNetwork url: " + gnBaseUrl);
                    if (!doCustomAuthentication) {
                        gnClient = new GNServicesClient.Builder(gnBaseUrl, Paths.get(outputPath)).build();
                    } else {
                        gnClient = new GNServicesClient.Builder(gnBaseUrl, Paths.get(outputPath)).doCustomAuthentication(true)
                            .customAuthenticationUrl(customAuthenticationUrl).build();
                    }

                    if (!doCustomAuthentication) {
                        System.out.println("Using GeoNetwork integrated authentication" );
                        System.out.println("GeoNetwork version: " + gnClient.getVersion());
                    } else {
                        System.out.println("Custom authentication enabled: " + customAuthenticationUrl);
                    }

                    // login to geonetwork
                    gnClient.login(gnUserName, gnUserPwd);

                    if (doCustomAuthentication) {
                        System.out.println("GeoNetwork version: " + gnClient.getVersion());
                    }

                    if (isExport) {
                        String exportType = cmd.getOptionValue("e");

                        if (StringUtils.isEmpty(exportType)) {
                            exportType = "all";
                        } else {
                            exportType = exportType.toLowerCase();

                            if (!exportType.equals("all") &&
                                !exportType.equals("users") &&
                                !exportType.equals("groups")) {
                                exportType = "all";
                            }
                        }

                        logger.info("Using cli argument in export mode");
                        logger.info("Export path: " + outputPath);
                        logger.info("GeoNetwork url: " + gnBaseUrl);
                        logger.info("GeoNetwork version: " + gnClient.getVersion());
                        logger.info("Export type: " + exportType);

                        System.out.println("Export type: " + exportType);

                        String gnDefaultExportMetadataTypes = PropertiesLoader.INSTANCE.getDefaultExportMetadataTypes();
                        if (StringUtils.isNotEmpty(gnDefaultExportMetadataTypes)) {
                            System.out.println("Metadata types to export: " + gnDefaultExportMetadataTypes);
                        } else {
                            System.out.println("Metadata types to export: all");
                        }

                        LocalDateTime metadataModifiedDateSinceFilter = null;

                        String gnDefaultExportMetadataModifiedSince = PropertiesLoader.INSTANCE.getDefaultExportMetadataModifiedSince();

                        if (StringUtils.isNotEmpty(gnDefaultExportMetadataModifiedSince)) {
                          metadataModifiedDateSinceFilter = parseMetadataModifiedSinceFilter(gnDefaultExportMetadataModifiedSince);

                          if (metadataModifiedDateSinceFilter != null) {
                              System.out.println("Metadata modified since: " +
                                  metadataModifiedDateSinceFilter.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                          }
                        }

                        String gnDefaultCswConstraint = PropertiesLoader.INSTANCE.getDefaultCswConstraint();
                        boolean gnDefaultExportFilterByGroupLocally =
                            PropertiesLoader.INSTANCE.getDefaultExportFilterByGroupLocally();
                        int gnDefaultRequestDelay = PropertiesLoader.INSTANCE.gnDefaultRequestDelay();


                        System.out.println("Export path: " + outputPath);
                        System.out.println(StringUtils.repeat("-", 20));

                        ExporterConfig exporterConfig = new ExporterConfig.Builder(Paths.get(outputPath))
                            .defaultExportMetadataTypes(gnDefaultExportMetadataTypes)
                            .defaultCswConstraint(gnDefaultCswConstraint)
                            .defaultExportFilterByGroupLocally(gnDefaultExportFilterByGroupLocally)
                            .defaultRequestDelay(gnDefaultRequestDelay)
                            .defaultExportType(exportType)
                            .defaultExportMetadataModifiedSince(metadataModifiedDateSinceFilter)
                            .build();

                        Exporter exporter = new Exporter();
                        exporter.exec(gnClient, exporterConfig);

                        logger.info("Export finished.");
                        System.out.println("Export finished.");


                        if (exporter.getExporterReport() != null) {
                            System.out.println("==========================");
                            System.out.println(exporter.getExporterReport().toString());
                            logger.info(exporter.getExporterReport().toString());
                        }
                    } else if (isImport) {
                        logger.info("Using cli argument in import mode, import path: " + outputPath +
                            ", delete all metadata and non default users and groups: " + hasDeleteOption);
                        logger.info("Export path: " + outputPath);
                        logger.info("GeoNetwork url: " + gnBaseUrl);
                        logger.info("GeoNetwork version: " + gnClient.getVersion());

                        String gnDefaultImportUser = PropertiesLoader.INSTANCE.getDefaultImportUser();
                        String gnDefaultImportGroup = PropertiesLoader.INSTANCE.getDefaultImportGroup();
                        String gnDefaultImportStatus = PropertiesLoader.INSTANCE.getDefaultImportStatus();
                        String gnDefaultImportPrivileges = PropertiesLoader.INSTANCE.getDefaultImportPrivileges();

                        ImporterConfig importerConfig = new ImporterConfig.Builder(Paths.get(outputPath))
                            .defaultUsersPassword(gnDefaultUsersPassword).importLocalXslt(gnImportLocalXslt)
                            .importRemoteXslt(gnImportRemoteXslt).defaultImportUser(gnDefaultImportUser)
                            .defaultImportGroup(gnDefaultImportGroup).defaultImportStatus(gnDefaultImportStatus)
                            .defaultImportPrivileges(gnDefaultImportPrivileges)
                            .deleteMetadataAndNonDefaultUsersAndGroups(hasDeleteOption)
                            .build();

                        System.out.println("Import path: " + outputPath);
                        System.out.println(StringUtils.repeat("-", 20));

                        Importer importer = new Importer();
                        int resultCode = importer.exec(gnClient, importerConfig);

                        logger.info("Import finished.");
                        System.out.println();
                        System.out.println("Import finished.");

                        if (importer.getImporterReport() != null) {
                            System.out.println("==========================");
                            System.out.println(importer.getImporterReport().toString());
                            logger.info(importer.getImporterReport().toString());
                        }

                        System.exit(resultCode);

                    } else if (isTest) {
                        logger.info("Using cli argument in test mode");
                        logger.info("Output path: " + outputPath);
                        logger.info("GeoNetwork url: " + gnBaseUrl);
                        logger.info("GeoNetwork version: " + gnClient.getVersion());
                    }


                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                    System.exit(1);
                } finally {
                    if (gnClient != null) gnClient.close();
                }


            } else {
                logger.info("Missing mode (export/import) option");
                help();
            }

        } catch (ParseException e) {
            logger.error("Failed to parse command line properties", e);
            help();
        }
    }

    /**
     * Print help message.
     */
    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("Cli", options);
        System.exit(0);
    }

    private boolean hasHelp(final String[] args) throws ParseException {
        Options options = new Options();
        Option helpMode = new Option("h", "help", false, "Show help");
        options.addOption(helpMode);
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args, true);
        return cmd.hasOption(helpMode.getOpt());
    }

    /**
     * Load properties file.
     *
     * @param propertyFileName
     * @return
     */
    private boolean loadProperties(String propertyFileName) {
        boolean isLoaded = false;

        try {
            PropertyFile.PROP_FILE_NAME = propertyFileName;
            isLoaded = PropertiesLoader.INSTANCE.load(PropertyFile.PROP_FILE_NAME);

        } catch (FileNotFoundException e) {
            logger.error("Error loading properties file. " + e.getMessage());
        }

        return isLoaded;
    }

    /**
     * Parse the metadata modified since filter.
     *
     * Format: VALUE:UNITS.
     *
     * Units supported: `MINUTES`, `HOURS`, `DAYS`, `MONTHS`, `YEARS`.
     *
     * Examples: 5:MINUTES (5 minutes ago), 2:HOURS (2 hours ago).
     *
     * @param gnDefaultExportMetadataModifiedSince
     * @return
     */
    private LocalDateTime parseMetadataModifiedSinceFilter(String gnDefaultExportMetadataModifiedSince) {
        LocalDateTime metadataModifiedDateSinceFilter = null;

        String[] modifiedSinceTokens = gnDefaultExportMetadataModifiedSince.split(":");

        if (modifiedSinceTokens.length == 2) {
            int dateUnitsValue = Integer.parseInt(modifiedSinceTokens[0]);
            String dateUnits = modifiedSinceTokens[1];

            ChronoUnit chronoUnit = null;

            if (dateUnits.equalsIgnoreCase("MINUTES")) {
                chronoUnit = ChronoUnit.MINUTES;
            } else if (dateUnits.equalsIgnoreCase("HOURS")) {
                chronoUnit =  ChronoUnit.HOURS;
            } else if (dateUnits.equalsIgnoreCase("DAYS")) {
                chronoUnit = ChronoUnit.DAYS;
            } else if (dateUnits.equalsIgnoreCase("MONTHS")) {
                chronoUnit =  ChronoUnit.MONTHS;
            } else if (dateUnits.equalsIgnoreCase("YEARS")) {
                chronoUnit =  ChronoUnit.YEARS;
            }

            if (chronoUnit != null) {
                metadataModifiedDateSinceFilter = LocalDateTime.now().minus(dateUnitsValue, chronoUnit);
            }

        } else {
            logger.info("Modified since date format is not valid: " + gnDefaultExportMetadataModifiedSince);
        }

        return metadataModifiedDateSinceFilter;
    }

    public static void main(String[] args) throws Exception {
        new Cli(args).parse();
    }
}
