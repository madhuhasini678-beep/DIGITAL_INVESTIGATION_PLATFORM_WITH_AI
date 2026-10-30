# DATA MODELS SPECIFICATION

This document details the relational PostgreSQL schema, Java JPA entity models, and Python microservice exchange structures.

---

## 1. Relational Database Tables (PostgreSQL)

```sql
-- 1. Cases Table
CREATE TABLE cases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_number VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    is_historical_demo BOOLEAN NOT NULL DEFAULT FALSE,
    incident_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Evidence Table
CREATE TABLE evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    sha256_hash CHAR(64) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    parsed_columns JSONB,
    record_count INTEGER NOT NULL DEFAULT 0,
    is_simulated BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Timeline Events Table
CREATE TABLE timeline_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    evidence_id UUID REFERENCES evidence(id) ON DELETE SET NULL,
    event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,
    source_reference VARCHAR(255),
    is_anomaly BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Graph Nodes Table
CREATE TABLE graph_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    node_label VARCHAR(255) NOT NULL,
    node_type VARCHAR(50) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. Graph Edges Table
CREATE TABLE graph_edges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    source_node_id UUID NOT NULL REFERENCES graph_nodes(id) ON DELETE CASCADE,
    target_node_id UUID NOT NULL REFERENCES graph_nodes(id) ON DELETE CASCADE,
    relationship_type VARCHAR(100) NOT NULL,
    weight FLOAT NOT NULL DEFAULT 1.0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. Anomaly Records Table
CREATE TABLE anomaly_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    anomaly_type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    confidence_score FLOAT NOT NULL DEFAULT 0.0,
    evidence_references JSONB,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. AI Summaries Table
CREATE TABLE ai_summaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID UNIQUE NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    factual_overview TEXT NOT NULL,
    key_findings TEXT NOT NULL,
    discrepancies_identified TEXT,
    confidence_notes TEXT,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_evidence_case_id ON evidence(case_id);
CREATE INDEX idx_timeline_case_id ON timeline_events(case_id);
CREATE INDEX idx_timeline_timestamp ON timeline_events(event_timestamp);
CREATE INDEX idx_graph_nodes_case_id ON graph_nodes(case_id);
CREATE INDEX idx_graph_edges_case_id ON graph_edges(case_id);
CREATE INDEX idx_anomalies_case_id ON anomaly_records(case_id);
```

---

## 2. Core Java JPA Entities & Enumerations

### Enums:
```java
public enum CaseStatus {
    ACTIVE, UNDER_REVIEW, CLOSED, ARCHIVED
}

public enum CaseCategory {
    FINANCIAL_FRAUD,
    HEIST_THEFT,
    INDUSTRIAL_DISASTER,
    KIDNAPPING_ABDUCTION,
    CYBER_INCIDENT,
    GENERAL_FORENSIC
}

public enum NodeType {
    PERSON,
    ORGANIZATION,
    BANK_ACCOUNT,
    LOCATION,
    VEHICLE,
    EVIDENCE_OBJECT
}

public enum AnomalySeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}
```

---

## 3. Python AI Microservice Schemas (Pydantic)

```python
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from datetime import datetime

class EvidenceRecord(BaseModel):
    record_id: str
    timestamp: Optional[datetime] = None
    entity_source: Optional[str] = None
    entity_target: Optional[str] = None
    numerical_value: Optional[float] = None
    text_payload: Optional[str] = None
    raw_attributes: Dict[str, Any] = {}

class AnomalyDetectRequest(BaseModel):
    case_id: str
    case_title: str
    case_category: str
    records: List[EvidenceRecord]

class DetectedAnomaly(BaseModel):
    anomaly_type: str
    title: str
    description: str
    severity: str  # LOW, MEDIUM, HIGH, CRITICAL
    confidence_score: float
    flagged_record_ids: List[str]

class AnomalyDetectResponse(BaseModel):
    case_id: str
    anomalies: List[DetectedAnomaly]
    total_records_analyzed: int

class SummaryGenerateRequest(BaseModel):
    case_id: str
    case_title: str
    case_category: str
    incident_date: Optional[datetime] = None
    verified_historical_facts: Optional[str] = None
    evidence_count: int
    timeline_event_count: int
    detected_anomalies: List[DetectedAnomaly] = []

class SummaryGenerateResponse(BaseModel):
    case_id: str
    factual_overview: str
    key_findings: str
    discrepancies_identified: str
    confidence_notes: str
```
