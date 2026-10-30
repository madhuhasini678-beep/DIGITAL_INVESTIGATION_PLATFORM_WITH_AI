package com.investigation.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EvidencePreviewResponse {

    private UUID evidenceId;
    private String fileName;
    private List<String> headers;
    private List<Map<String, String>> rows;
    private int totalRows;
    private int page;
    private int pageSize;

    public EvidencePreviewResponse() {}

    public EvidencePreviewResponse(UUID evidenceId, String fileName, List<String> headers, List<Map<String, String>> rows, int totalRows, int page, int pageSize) {
        this.evidenceId = evidenceId;
        this.fileName = fileName;
        this.headers = headers;
        this.rows = rows;
        this.totalRows = totalRows;
        this.page = page;
        this.pageSize = pageSize;
    }

    public UUID getEvidenceId() { return evidenceId; }
    public void setEvidenceId(UUID evidenceId) { this.evidenceId = evidenceId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }

    public List<Map<String, String>> getRows() { return rows; }
    public void setRows(List<Map<String, String>> rows) { this.rows = rows; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
