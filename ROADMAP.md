# DEVELOPMENT ROADMAP

## Digital Investigation Platform with AI

This roadmap outlines the complete phase-by-phase progression of the project. Each phase is self-contained, testable, and builds toward the full desktop investigation suite.

---

## 🗺️ Phase Overview

```
Phase 1: Project Scaffolding & Specifications (Current Phase)
   │
   ▼
Phase 2: PostgreSQL Database & Spring Boot Core API
   │
   ▼
Phase 3: Evidence Ingestion & Forensic Parsing Engine
   │
   ▼
Phase 4: Python AI Microservice (Anomaly Detection & Summarization)
   │
   ▼
Phase 5: JavaFX Desktop Shell & Case Management UI
   │
   ▼
Phase 6: Forensic Views (Evidence Explorer, Timeline, Relationship Graph)
   │
   ▼
Phase 7: Intelligence Views (Anomalies & AI Case Summary)
   │
   ▼
Phase 8: Preloaded Historical Cases & End-to-End Verification
```

---

## 📋 Phase-by-Phase Details

### Phase 1: Project Scaffolding & Specifications (CURRENT)
- **Goal**: Establish the repository layout, complete architectural blueprint, data model schemas, API contracts, and beginner guidance.
- **Deliverables**:
  - `README.md`, `ARCHITECTURE.md`, `ROADMAP.md`
  - Documentation folder (`docs/`): API specifications, data models, historical cases dossier
  - Directory skeleton for `backend/`, `frontend/`, `ai-service/`, and `data/samples/`
- **Testing & Verification**:
  - Verify directory tree exists and is cleanly structured.
  - No broken references or missing documentation files.

---

### Phase 2: PostgreSQL Database & Spring Boot Core API
- **Goal**: Set up the Spring Boot 3.x project running on Java 21, configure PostgreSQL database connections (with embedded H2 profile for zero-config testing), and implement Case CRUD REST endpoints.
- **Deliverables**:
  - `backend/pom.xml` with dependencies (Spring Web, Spring Data JPA, PostgreSQL driver, Jackson, Lombok, Flyway/Hibernate).
  - JPA Entities: `CaseEntity`, `EvidenceEntity`, `TimelineEventEntity`, `GraphNodeEntity`, `GraphEdgeEntity`, `AnomalyEntity`, `AiSummaryEntity`.
  - Spring Repositories, Services, and `CaseController`.
  - Global Exception Handling and standard `ApiResponse<T>` wrapper.
- **How to Run & Test**:
  ```powershell
  cd backend
  mvn clean compile
  mvn spring-boot:run
  ```
  Test Case Creation via PowerShell:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/api/cases" -Method GET
  ```

---

### Phase 3: Evidence Ingestion & Forensic Parsing Engine
- **Goal**: Implement the Java evidence processing subsystem using CSV parsing, Java Collections, Generics, and the `java.time` API.
- **Deliverables**:
  - Cryptographic SHA-256 evidence hashing utility.
  - Generic Evidence Parser (`EvidenceParser<T>`) supporting multi-column CSVs (financial transactions, telephone call detail records, event logs).
  - Automatic timeline event extraction from parsed timestamp columns using `DateTimeFormatter` and `ZonedDateTime`.
  - REST endpoints for file upload: `POST /api/cases/{caseId}/evidence/upload`.
- **How to Run & Test**:
  - Upload a test CSV via `curl` or PowerShell:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/api/cases/{id}/evidence/upload" -Method POST -InFile "data/samples/test_transactions.csv" -ContentType "multipart/form-data"
  ```
  - Verify SHA-256 hash returned and timeline events auto-generated in `/api/cases/{id}/timeline`.

---

### Phase 4: Python AI Microservice (Anomaly Detection & Summaries)
- **Goal**: Build the independent Python AI microservice using FastAPI or Flask, scikit-learn, and statistical routines.
- **Deliverables**:
  - `ai-service/app/main.py`: REST endpoints `/api/v1/anomalies/detect` and `/api/v1/summary/generate`.
  - Anomaly Detector: Isolation Forest + Z-Score analysis over numerical/frequency values in evidence records.
  - Evidence Summarizer: Context-grounded synthesis generator that processes case timeline, entity graph, and flagged anomalies to construct an investigation debrief.
  - Spring Boot bridge client: `AiServiceGateway.java` making calls from Spring Boot to Python.
- **How to Run & Test**:
  ```powershell
  cd ai-service
  python -m venv venv
  .\venv\Scripts\Activate.ps1
  pip install -r requirements.txt
  python main.py
  ```
  Test Anomaly Detection:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8000/api/v1/anomalies/detect" -Method POST -Body '{"case_id":"...","records":[...]}' -ContentType "application/json"
  ```

---

### Phase 5: JavaFX Desktop Shell & Case Management UI
- **Goal**: Build the JavaFX 21 desktop application shell with modern dark theme and the 7 permanent navigation sections.
- **Deliverables**:
  - `frontend/pom.xml` with JavaFX controls, FXML, graphics, Jackson, and test harness.
  - Navigation Rail Controller: Switching seamlessly between:
    1. Dashboard
    2. Cases
    3. Evidence
    4. Timeline
    5. Relationship Graph
    6. Anomalies
    7. AI Summary
  - Modern Dark CSS stylesheet (`dark-theme.css`) with electric cyan accents and responsive card layouts.
  - Active Case Context State Manager (keeps active case synchronized across all views).
  - Case Manager View: List, create, edit, and delete cases.
- **How to Run & Test**:
  ```powershell
  cd frontend
  mvn clean javafx:run
  ```
  - Verify navigation across all 7 tabs without errors or lag.
  - Create a new case from the UI and verify it appears in the list.

---

### Phase 6: Forensic Views (Evidence Explorer, Timeline, Relationship Graph)
- **Goal**: Implement the core visual investigation tools in JavaFX.
- **Deliverables**:
  - **Evidence View**: File drop-zone, list of uploaded files, SHA-256 badges, parsed tabular data viewer.
  - **Timeline View**: Visual chronologically ordered event stream using JavaFX custom nodes with zoom and event category filtering.
  - **Relationship Graph View**: Interactive JavaFX Canvas/Group graph visualization showing entities (Persons, Companies, Accounts) connected by lines, draggable nodes, and click-to-inspect side panel.
- **How to Run & Test**:
  ```powershell
  cd frontend
  mvn javafx:run
  ```
  - Upload sample CSV evidence in Evidence view.
  - Switch to Timeline and observe chronological events.
  - Switch to Relationship Graph and interact with nodes.

---

### Phase 7: Intelligence Views (Anomalies & AI Case Summary)
- **Goal**: Connect the Anomaly Detection and AI Summary frontend views to the Spring Boot -> Python pipeline.
- **Deliverables**:
  - **Anomalies View**: Severity score badges (High / Medium / Low), outlier breakdown, anomaly inspection drawer with evidence links.
  - **AI Summary View**: Formatted intelligence report with executive overview, identified discrepancies, key findings, and explicit confidence limitations.
- **How to Run & Test**:
  - Open a case with evidence, click "Run AI Analysis".
  - Verify anomaly cards populate with severity scores and explanations.
  - Verify AI summary report is rendered with transparent limitations.

---

### Phase 8: Preloaded Historical Cases & End-to-End Verification
- **Goal**: Preload and verify the six historic benchmark cases with structured demo data, verified public historical facts, and labeled simulated records.
- **Deliverables**:
  - Data seeding service loading:
    1. Mona Lisa Theft (1911)
    2. Lindbergh Kidnapping (1932)
    3. Harshad Mehta Securities Scam (1992)
    4. Bhopal Gas Tragedy (1984)
    5. Mundhra Scandal (1957)
    6. Lufthansa Heist (1978)
  - Sample evidence datasets in `data/samples/`.
  - Comprehensive end-to-end testing script.
- **How to Run & Test**:
  - Launch all services (PostgreSQL -> Backend -> AI Service -> Frontend).
  - Open each of the 6 historical cases.
  - Verify all 7 sections populate cleanly for each case.
  - Create a 7th custom test case, upload new evidence, and verify full pipeline functionality.
