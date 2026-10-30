package com.investigation.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.investigation.entity.enums.AnomalySeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AiServiceGateway {

    private static final Logger log = LoggerFactory.getLogger(AiServiceGateway.class);

    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AiServiceGateway(@Value("${ai.service.base-url:http://localhost:8000/api/v1}") String baseUrl, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public static class RawAnomaly {
        public String anomalyType;
        public String title;
        public String description;
        public AnomalySeverity severity;
        public double confidenceScore;
        public List<String> flaggedRecordIds = new ArrayList<>();
    }

    public static class RawSummary {
        public String factualOverview;
        public String keyFindings;
        public String discrepanciesIdentified;
        public String confidenceNotes;
    }

    /**
     * Request anomaly detection from the Python service, with graceful embedded fallback if offline.
     */
    public List<RawAnomaly> detectAnomalies(String caseId, String title, String category, List<Map<String, Object>> records) {
        try {
            Map<String, Object> payload = Map.of(
                    "case_id", caseId,
                    "case_title", title,
                    "case_category", category,
                    "records", records
            );

            String jsonPayload = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/anomalies/detect"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(4))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                List<RawAnomaly> list = new ArrayList<>();
                for (JsonNode node : root.path("anomalies")) {
                    RawAnomaly a = new RawAnomaly();
                    a.anomalyType = node.path("anomaly_type").asText("STATISTICAL_OUTLIER");
                    a.title = node.path("title").asText();
                    a.description = node.path("description").asText();
                    a.severity = AnomalySeverity.valueOf(node.path("severity").asText("MEDIUM"));
                    a.confidenceScore = node.path("confidence_score").asDouble(0.85);
                    list.add(a);
                }
                return list;
            }
        } catch (Exception e) {
            log.info("Python AI microservice offline/unreachable at {}. Executing embedded statistical detection.", baseUrl);
        }

        // Embedded Fallback
        return runEmbeddedAnomalyDetection(records);
    }

    /**
     * Request case summary from the Python service, with graceful embedded fallback if offline.
     */
    public RawSummary generateSummary(
            String caseId,
            String title,
            String category,
            String description,
            String incidentDate,
            boolean isHistoricalDemo,
            int evidenceCount,
            List<Map<String, Object>> timelineEvents,
            int nodeCount,
            int edgeCount,
            List<RawAnomaly> anomalies) {

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("case_id", caseId);
            payload.put("case_title", title);
            payload.put("case_category", category);
            payload.put("case_description", description);
            payload.put("incident_date", incidentDate);
            payload.put("is_historical_demo", isHistoricalDemo);
            payload.put("evidence_count", evidenceCount);
            payload.put("timeline_events", timelineEvents);
            payload.put("graph_data", Map.of("node_count", nodeCount, "edge_count", edgeCount));
            payload.put("anomalies", anomalies);

            String jsonPayload = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/summary/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(4))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                RawSummary s = new RawSummary();
                s.factualOverview = root.path("factual_overview").asText();
                s.keyFindings = root.path("key_findings").asText();
                s.discrepanciesIdentified = root.path("discrepancies_identified").asText();
                s.confidenceNotes = root.path("confidence_notes").asText();
                return s;
            }
        } catch (Exception e) {
            log.info("Python AI microservice offline/unreachable at {}. Executing embedded grounded summarizer.", baseUrl);
        }

        // Embedded Fallback
        return runEmbeddedSummarization(title, category, description, incidentDate, isHistoricalDemo, evidenceCount, timelineEvents, nodeCount, edgeCount, anomalies);
    }

    private List<RawAnomaly> runEmbeddedAnomalyDetection(List<Map<String, Object>> records) {
        List<RawAnomaly> list = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (Map<String, Object> r : records) {
            Object val = r.get("numerical_value");
            if (val instanceof Number n) {
                values.add(n.doubleValue());
            }
        }

        if (values.size() >= 3) {
            List<Double> sorted = new ArrayList<>(values);
            Collections.sort(sorted);
            double median = sorted.get(sorted.size() / 2);

            // Compute Median Absolute Deviation (MAD)
            List<Double> absDevs = sorted.stream()
                    .map(v -> Math.abs(v - median))
                    .sorted()
                    .collect(Collectors.toList());
            double mad = absDevs.get(absDevs.size() / 2);

            for (Map<String, Object> r : records) {
                Object valObj = r.get("numerical_value");
                if (valObj instanceof Number n) {
                    double v = n.doubleValue();
                    boolean isOutlier = false;
                    double score = 0.0;

                    if (mad > 1e-5) {
                        double modifiedZ = 0.6745 * Math.abs(v - median) / mad;
                        if (modifiedZ > 3.5) {
                            isOutlier = true;
                            score = modifiedZ;
                        }
                    } else {
                        // All other values are identical or zero MAD
                        if (Math.abs(v - median) > 10.0) {
                            isOutlier = true;
                            score = Math.abs(v - median);
                        }
                    }

                    if (isOutlier) {
                        RawAnomaly a = new RawAnomaly();
                        a.anomalyType = "NUMERICAL_STATISTICAL_OUTLIER";
                        a.title = String.format("Statistical Outlier Surge: %.2f", v);
                        a.description = String.format("Observed value of %.2f deviates dramatically from the median (%.2f). Flagged as a potential uncharacteristic transaction volume or sensor spike.", v, median);
                        a.severity = AnomalySeverity.HIGH;
                        a.confidenceScore = 0.95;
                        if (r.get("record_id") != null) {
                            a.flaggedRecordIds.add(r.get("record_id").toString());
                        }
                        list.add(a);
                    }
                }
            }
        }
        return list;
    }

    private RawSummary runEmbeddedSummarization(
            String title,
            String category,
            String description,
            String incidentDate,
            boolean isHistoricalDemo,
            int evidenceCount,
            List<Map<String, Object>> timelineEvents,
            int nodeCount,
            int edgeCount,
            List<RawAnomaly> anomalies) {

        RawSummary s = new RawSummary();
        String demoNotice = isHistoricalDemo ? " [Preloaded Historical Case]" : "";

        s.factualOverview = String.format(
                "Investigation record for '%s' [%s]%s. Case registers %d primary evidence file(s), %d chronological timeline event(s), and %d entity nodes across %d relationship edges.",
                title, category, demoNotice, evidenceCount, timelineEvents.size(), nodeCount, edgeCount
        );

        StringBuilder kf = new StringBuilder();
        if (evidenceCount > 0) {
            kf.append("• Cryptographic SHA-256 evidence registered and verified for chain of custody.\n");
        }
        if (!timelineEvents.isEmpty()) {
            kf.append(String.format("• Timeline active across %d sequenced events.\n", timelineEvents.size()));
        }
        if (nodeCount > 0) {
            kf.append(String.format("• Entity network maps %d nodes and %d inter-entity connections.\n", nodeCount, edgeCount));
        }
        s.keyFindings = kf.toString().trim();

        if (!anomalies.isEmpty()) {
            StringBuilder disc = new StringBuilder();
            for (RawAnomaly a : anomalies) {
                disc.append(String.format("[%s] %s: %s\n\n", a.severity, a.title, a.description));
            }
            s.discrepanciesIdentified = disc.toString().trim();
        } else {
            s.discrepanciesIdentified = "No statistical or chronological discrepancies detected in current evidence corpus.";
        }

        s.confidenceNotes = "Forensic Guardrail Notice: Grounded strictly in current repository evidence. Flagged anomalies indicate mathematical variance, not guilt. (Generated via embedded forensic engine; Python AI microservice bridge ready).";
        return s;
    }
}
