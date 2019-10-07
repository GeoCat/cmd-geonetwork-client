package com.geocat.gnclient.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to create a CSV log report of the metadata import process.
 *
 */
public class MetadataImportCsvLog {
    private final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FileWriter writer = null;
    private CSVPrinter csvFilePrinter = null;

    public MetadataImportCsvLog() throws Exception {
    }


    public void addMetadataImportEntry(LocalDateTime dateTime, int status,
                                       String statusError, String fileName) throws IOException {
        MetadataImportCsvLogEntry entry =
            new MetadataImportCsvLogEntry(dateTime, status, statusError, fileName);

        csvFilePrinter.printRecord(entry.asList());
    }

    public void initCsv(String filePath) throws IOException {
        writer = new FileWriter(filePath);

        //initialize CSVPrinter object
        CSVFormat csvFileFormat =
            CSVFormat.DEFAULT.withRecordSeparator("\n");

        //initialize CSVPrinter object
        csvFilePrinter = new CSVPrinter(writer, csvFileFormat);

        String[] entries = (
            "Datetime#Status#StatusMessage#Filename").split("#");
        csvFilePrinter.printRecord(Arrays.asList(entries));
    }

    public void closeCsv(){
        try {
            if (csvFilePrinter != null) {
                csvFilePrinter.close(true);
            }

            IOUtils.closeQuietly(writer);
        } catch (Exception ex) {

        }
    }


    private class MetadataImportCsvLogEntry {
        LocalDateTime dateTime;
        int status;
        String statusError;
        String fileName;

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public int getStatus() {
            return status;
        }

        public String getFileName() {
            return fileName;
        }

        public MetadataImportCsvLogEntry(LocalDateTime dateTime, int status,
                                         String statusError, String fileName) {
            this.dateTime = dateTime;
            this.status = status;
            this.statusError = statusError;
            this.fileName = fileName;
        }

        public List<String> asList() {
            List<String> values = new ArrayList<>();
            values.add(dateTime.format(MetadataImportCsvLog.dateFormatter));
            values.add(status + "");
            values.add(statusError);
            values.add(fileName);

            return values;
        }
    }
}
