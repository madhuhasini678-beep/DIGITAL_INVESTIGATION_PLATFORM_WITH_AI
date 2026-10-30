package com.investigation.evidence.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public class EvidenceRow {

    private final int rowNumber;
    private final Map<String, String> data;
    private final Instant parsedTimestamp;

    public EvidenceRow(int rowNumber, Map<String, String> data, Instant parsedTimestamp) {
        this.rowNumber = rowNumber;
        this.data = Collections.unmodifiableMap(data);
        this.parsedTimestamp = parsedTimestamp;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getValue(String column) {
        return data.get(column);
    }

    public Instant getParsedTimestamp() {
        return parsedTimestamp;
    }
}
