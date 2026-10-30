package com.investigation.dto;

import com.investigation.entity.EvidenceEntity;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class EvidenceResponse {

    private UUID id;
    private UUID caseId;
    private String fileName;
    private String fileType;
    private long fileSizeBytes;
    private String sha256Hash;
    private List<String> parsedColumns;
    private int recordCount;
    private boolean isSimulated;
    private Instant uploadedAt;

    public EvidenceResponse() {}

    public static EvidenceResponse fromEntity(EvidenceEntity entity) {
        EvidenceResponse resp = new EvidenceResponse();
        resp.setId(entity.getId());
        resp.setCaseId(entity.getCaseEntity().getId());
        resp.setFileName(entity.getFileName());
        resp.setFileType(entity.getFileType());
        resp.setFileSizeBytes(entity.getFileSizeBytes());
        resp.setSha256Hash(entity.getSha256Hash());
        resp.setRecordCount(entity.getRecordCount());
        resp.setSimulated(entity.isSimulated());
        resp.setUploadedAt(entity.getUploadedAt());

        if (entity.getParsedColumns() != null && !entity.getParsedColumns().isBlank()) {
            resp.setParsedColumns(Arrays.asList(entity.getParsedColumns().split(",")));
        } else {
            resp.setParsedColumns(Collections.emptyList());
        }

        return resp;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCaseId() { return caseId; }
    public void setCaseId(UUID caseId) { this.caseId = caseId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }

    public List<String> getParsedColumns() { return parsedColumns; }
    public void setParsedColumns(List<String> parsedColumns) { this.parsedColumns = parsedColumns; }

    public int getRecordCount() { return recordCount; }
    public void setRecordCount(int recordCount) { this.recordCount = recordCount; }

    public boolean isSimulated() { return isSimulated; }
    public void setSimulated(boolean simulated) { isSimulated = simulated; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
