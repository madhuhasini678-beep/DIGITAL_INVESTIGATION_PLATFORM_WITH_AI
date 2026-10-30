from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime

class EvidenceRecord(BaseModel):
    record_id: str
    timestamp: Optional[str] = None
    entity_source: Optional[str] = None
    entity_target: Optional[str] = None
    numerical_value: Optional[float] = None
    text_payload: Optional[str] = None
    raw_attributes: Dict[str, Any] = Field(default_factory=dict)

class AnomalyDetectRequest(BaseModel):
    case_id: str
    case_title: str
    case_category: str
    records: List[EvidenceRecord] = Field(default_factory=list)

class DetectedAnomaly(BaseModel):
    anomaly_type: str
    title: str
    description: str
    severity: str  # LOW, MEDIUM, HIGH, CRITICAL
    confidence_score: float
    flagged_record_ids: List[str] = Field(default_factory=list)

class AnomalyDetectResponse(BaseModel):
    case_id: str
    anomalies: List[DetectedAnomaly] = Field(default_factory=list)
    total_records_analyzed: int
    engine: str = "python_isolation_forest_and_stats"

class TimelineSummaryItem(BaseModel):
    timestamp: str
    title: str
    description: Optional[str] = None
    is_anomaly: bool = False

class GraphSummaryData(BaseModel):
    node_count: int = 0
    edge_count: int = 0
    top_entities: List[str] = Field(default_factory=list)

class SummaryGenerateRequest(BaseModel):
    case_id: str
    case_title: str
    case_category: str
    case_description: Optional[str] = None
    incident_date: Optional[str] = None
    is_historical_demo: bool = False
    evidence_count: int = 0
    timeline_events: List[TimelineSummaryItem] = Field(default_factory=list)
    graph_data: Optional[GraphSummaryData] = None
    anomalies: List[DetectedAnomaly] = Field(default_factory=list)

class SummaryGenerateResponse(BaseModel):
    case_id: str
    factual_overview: str
    key_findings: str
    discrepancies_identified: str
    confidence_notes: str
    engine: str = "python_grounded_synthesis"
