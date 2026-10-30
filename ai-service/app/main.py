from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from .models.schemas import (
    AnomalyDetectRequest, AnomalyDetectResponse,
    SummaryGenerateRequest, SummaryGenerateResponse
)
from .anomaly.detector import AnomalyDetector
from .summarizer.generator import GroundedCaseSummarizer

app = FastAPI(
    title="Digital Investigation AI Microservice",
    description="Python microservice for statistical anomaly detection and evidence-grounded summarization",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

anomaly_detector = AnomalyDetector()
case_summarizer = GroundedCaseSummarizer()

@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": "ai-service",
        "models": ["IsolationForest", "ZScoreAnomalyDetector", "GroundedCaseSummarizer"]
    }

@app.post("/api/v1/anomalies/detect", response_model=AnomalyDetectResponse)
def detect_anomalies(request: AnomalyDetectRequest):
    try:
        anomalies = anomaly_detector.detect(
            case_title=request.case_title,
            case_category=request.case_category,
            records=request.records
        )
        return AnomalyDetectResponse(
            case_id=request.case_id,
            anomalies=anomalies,
            total_records_analyzed=len(request.records)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Anomaly detection failed: {str(e)}")

@app.post("/api/v1/summary/generate", response_model=SummaryGenerateResponse)
def generate_summary(request: SummaryGenerateRequest):
    try:
        return case_summarizer.summarize(request)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Summary generation failed: {str(e)}")
