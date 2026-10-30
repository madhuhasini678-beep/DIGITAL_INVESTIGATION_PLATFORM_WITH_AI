@echo off
echo ========================================================
echo Starting Digital Investigation Platform - AI Service
echo Module: ai-service/ (FastAPI, Python)
echo Models: Isolation Forest Anomaly Detection + Grounded Summarizer
echo ========================================================
cd ai-service
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
pause
