package com.investigation.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EvidenceModel {

    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty caseId = new SimpleStringProperty();
    private final StringProperty fileName = new SimpleStringProperty();
    private final StringProperty filePath = new SimpleStringProperty();
    private final LongProperty fileSizeBytes = new SimpleLongProperty();
    private final StringProperty sha256Hash = new SimpleStringProperty();
    private final IntegerProperty recordCount = new SimpleIntegerProperty();
    private final BooleanProperty isSimulated = new SimpleBooleanProperty();
    private final StringProperty uploadedAt = new SimpleStringProperty();

    public EvidenceModel() {}

    public String getId() { return id.get(); }
    public void setId(String id) { this.id.set(id); }
    public StringProperty idProperty() { return id; }

    public String getCaseId() { return caseId.get(); }
    public void setCaseId(String caseId) { this.caseId.set(caseId); }
    public StringProperty caseIdProperty() { return caseId; }

    public String getFileName() { return fileName.get(); }
    public void setFileName(String fileName) { this.fileName.set(fileName); }
    public StringProperty fileNameProperty() { return fileName; }

    public String getFilePath() { return filePath.get(); }
    public void setFilePath(String filePath) { this.filePath.set(filePath); }
    public StringProperty filePathProperty() { return filePath; }

    public long getFileSizeBytes() { return fileSizeBytes.get(); }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes.set(fileSizeBytes); }
    public LongProperty fileSizeBytesProperty() { return fileSizeBytes; }

    public String getSha256Hash() { return sha256Hash.get(); }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash.set(sha256Hash); }
    public StringProperty sha256HashProperty() { return sha256Hash; }

    public int getRecordCount() { return recordCount.get(); }
    public void setRecordCount(int recordCount) { this.recordCount.set(recordCount); }
    public IntegerProperty recordCountProperty() { return recordCount; }

    public boolean isIsSimulated() { return isSimulated.get(); }
    public void setIsSimulated(boolean isSimulated) { this.isSimulated.set(isSimulated); }
    public BooleanProperty isSimulatedProperty() { return isSimulated; }

    public String getUploadedAt() { return uploadedAt.get(); }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt.set(uploadedAt); }
    public StringProperty uploadedAtProperty() { return uploadedAt; }
}
