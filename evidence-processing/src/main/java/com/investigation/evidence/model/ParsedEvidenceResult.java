package com.investigation.evidence.model;

import java.util.Collections;
import java.util.List;

public class ParsedEvidenceResult {

    private final List<String> headers;
    private final List<EvidenceRow> rows;
    private final String detectedTimestampColumn;
    private final List<String> warnings;

    public ParsedEvidenceResult(List<String> headers, List<EvidenceRow> rows, String detectedTimestampColumn, List<String> warnings) {
        this.headers = Collections.unmodifiableList(headers);
        this.rows = Collections.unmodifiableList(rows);
        this.detectedTimestampColumn = detectedTimestampColumn;
        this.warnings = warnings != null ? Collections.unmodifiableList(warnings) : Collections.emptyList();
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<EvidenceRow> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rows.size();
    }

    public String getDetectedTimestampColumn() {
        return detectedTimestampColumn;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
