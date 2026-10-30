# REST API SPECIFICATION

This specification details all endpoints exposed by the Spring Boot backend (`http://localhost:8080/api`).

---

## 1. Response Standard

All responses are wrapped in a standard JSON envelope:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-10-30T15:30:00Z"
}
```

Error response envelope:

```json
{
  "success": false,
  "message": "Case not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "errorCode": "ENTITY_NOT_FOUND",
  "timestamp": "2026-10-30T15:30:00Z"
}
```

---

## 2. Case Management Endpoints

### 2.1 List All Cases
- **GET** `/api/cases`
- **Query Parameters**:
  - `category` (optional, string): e.g. `FINANCIAL_FRAUD`
  - `status` (optional, string): e.g. `ACTIVE`
  - `search` (optional, string): text match on title and description
- **Response `200 OK`**:
```json
{
  "success": true,
  "data": [
    {
      "id": "c1a10000-0000-0000-0000-000000000001",
      "caseNumber": "CASE-1911-ML",
      "title": "Mona Lisa Theft (1911)",
      "description": "Theft of Leonardo da Vinci's portrait from the Salon Carré of the Louvre.",
      "category": "HEIST_THEFT",
      "status": "CLOSED",
      "isHistoricalDemo": true,
      "incidentDate": "1911-08-21T07:00:00Z",
      "evidenceCount": 4,
      "anomalyCount": 3,
      "hasAiSummary": true,
      "createdAt": "1911-08-21T07:00:00Z",
      "updatedAt": "2026-10-30T15:30:00Z"
    }
  ]
}
```

### 2.2 Get Case Details
- **GET** `/api/cases/{id}`
- **Response `200 OK`**: Single case object with aggregated statistics.

### 2.3 Create Case
- **POST** `/api/cases`
- **Request Body**:
```json
{
  "title": "Operation Midnight Shadow",
  "caseNumber": "CASE-2026-0042",
  "description": "Investigation into unauthorized fund diversions across corporate subsidiaries.",
  "category": "FINANCIAL_FRAUD",
  "status": "ACTIVE",
  "incidentDate": "2026-10-15T09:30:00Z"
}
```
- **Response `201 Created`**: Returns created `CaseResponse` with generated UUID.

### 2.4 Update Case
- **PUT** `/api/cases/{id}`
- **Request Body**: Partial or full updates to title, description, category, or status.
- **Response `200 OK`**: Updated case object.

### 2.5 Delete Case
- **DELETE** `/api/cases/{id}`
- **Response `200 OK`**: Confirms deletion and cascading removal of evidence/timeline links.

---

## 3. Evidence Endpoints

### 3.1 List Evidence for Case
- **GET** `/api/cases/{caseId}/evidence`
- **Response `200 OK`**: Array of evidence descriptors with SHA-256 hashes and record counts.

### 3.2 Upload Evidence File
- **POST** `/api/cases/{caseId}/evidence/upload`
- **Content-Type**: `multipart/form-data`
- **Form Parameters**:
  - `file`: Multipart file binary (CSV / TXT / JSON)
  - `evidenceType`: `FINANCIAL_LEDGER`, `PHONE_LOGS`, `ACCESS_CONTROL`, `INVENTORY`
  - `isSimulated`: boolean (indicates whether this is demo synthetic evidence)
- **Response `201 Created`**:
```json
{
  "success": true,
  "data": {
    "id": "e1a10000-0000-0000-0000-000000000001",
    "fileName": "bank_receipts_ledger_1992.csv",
    "fileSizeBytes": 14205,
    "sha256Hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    "recordCount": 184,
    "parsedColumns": ["TransactionID", "Date", "IssuingBank", "ReceivingBank", "AmountCrores", "Status"],
    "isSimulated": true,
    "uploadedAt": "2026-10-30T15:35:00Z"
  }
}
```

### 3.3 Preview Evidence Content
- **GET** `/api/evidence/{id}/preview?page=0&size=50`
- **Response `200 OK`**: Parsed JSON tabular rows with column headers and rows.

---

## 4. Timeline Endpoints

### 4.1 Get Case Timeline
- **GET** `/api/cases/{caseId}/timeline`
- **Response `200 OK`**: Array of events ordered chronologically by `eventTimestamp`.
```json
{
  "success": true,
  "data": [
    {
      "id": "t1a10000-0000-0000-0000-000000000001",
      "timestamp": "1911-08-21T07:15:00Z",
      "title": "Peruggia enters Salon Carré",
      "description": "Peruggia removes the Mona Lisa from the wall mounting hooks.",
      "eventType": "HEIST_ACTION",
      "sourceReference": "Louvre Guard Log 1911-08-21",
      "isAnomaly": false
    }
  ]
}
```

---

## 5. Relationship Graph Endpoints

### 5.1 Get Case Graph
- **GET** `/api/cases/{caseId}/graph`
- **Response `200 OK`**:
```json
{
  "success": true,
  "data": {
    "nodes": [
      {
        "id": "n1",
        "label": "Vincenzo Peruggia",
        "type": "PERSON",
        "metadata": { "role": "Former glazier / Louvre worker", "nationality": "Italian" }
      },
      {
        "id": "n2",
        "label": "Mona Lisa (La Gioconda)",
        "type": "EVIDENCE_OBJECT",
        "metadata": { "medium": "Oil on poplar panel", "location": "Salon Carré" }
      }
    ],
    "edges": [
      {
        "id": "e1",
        "source": "n1",
        "target": "n2",
        "relationship": "REMOVED_FROM_MUSEUM",
        "weight": 1.0,
        "notes": "Verified historical act"
      }
    ]
  }
}
```

---

## 6. Anomaly & AI Endpoints

### 6.1 Get Case Anomalies
- **GET** `/api/cases/{caseId}/anomalies`
- **Response `200 OK`**: List of detected anomalies with confidence, metric explanations, and severity ratings (`LOW`, `MEDIUM`, `HIGH`).

### 6.2 Trigger Anomaly Detection
- **POST** `/api/cases/{caseId}/anomalies/detect`
- **Response `200 OK`**: Re-evaluates case evidence through the Python service and stores new anomalies.

### 6.3 Get AI Summary
- **GET** `/api/cases/{caseId}/summary`
- **Response `200 OK`**:
```json
{
  "success": true,
  "data": {
    "caseId": "c1a10000-0000-0000-0000-000000000001",
    "factualOverview": "On August 21, 1911, the Mona Lisa was stolen from the Louvre...",
    "keyFindings": "- The theft went unnoticed for over 24 hours...\n- Frame and glass were abandoned on a service staircase.",
    "discrepanciesIdentified": "- Security guard rounds showed a 45-minute unmonitored window.\n- Worker signatures in the courtyard log do not reconcile with exit records.",
    "confidenceNotes": "Summary derived from historical police reports and court testimony. Simulated demo evidence flagged appropriately.",
    "generatedAt": "2026-10-30T15:35:00Z"
  }
}
```

### 6.4 Generate AI Summary
- **POST** `/api/cases/{caseId}/summary/generate`
- **Response `200 OK`**: Synthesizes and saves a fresh investigative summary.
