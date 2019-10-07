package com.geocat.gnclient.gnservices.metadata.model;

import java.util.HashMap;
import java.util.Map;

public class MetadataSearchResults {
    private int totalMatched;
    private int totalReturned;
    private int nextRecord;
    Map<String, String> metadata;

    public MetadataSearchResults() {
        metadata = new HashMap<>();
    }

    public int getTotalMatched() {
        return totalMatched;
    }

    public void setTotalMatched(int totalMatched) {
        this.totalMatched = totalMatched;
    }

    public int getTotalReturned() {
        return totalReturned;
    }

    public void setTotalReturned(int totalReturned) {
        this.totalReturned = totalReturned;
    }

    public int getNextRecord() {
        return nextRecord;
    }

    public void setNextRecord(int nextRecord) {
        this.nextRecord = nextRecord;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
