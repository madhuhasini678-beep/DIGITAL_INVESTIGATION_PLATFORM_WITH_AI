package com.investigation.ui.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.investigation.ui.model.CaseModel;
import com.investigation.ui.model.DashboardSummaryModel;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<Boolean> checkBackendHealth() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cases/dashboard/summary"))
                .GET()
                .timeout(Duration.ofSeconds(2))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> res.statusCode() == 200)
                .exceptionally(ex -> false);
    }

    public CompletableFuture<DashboardSummaryModel> getDashboardSummary() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cases/dashboard/summary"))
                .GET()
                .timeout(Duration.ofSeconds(4))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::extractData)
                .thenApply(json -> {
                    try {
                        return objectMapper.treeToValue(json, DashboardSummaryModel.class);
                    } catch (Exception e) {
                        return new DashboardSummaryModel();
                    }
                });
    }

    public CompletableFuture<List<CaseModel>> getCases(String search, String category, String status) {
        StringBuilder url = new StringBuilder(BASE_URL + "/cases?");
        if (search != null && !search.isBlank()) url.append("search=").append(search).append("&");
        if (category != null && !category.isBlank()) url.append("category=").append(category).append("&");
        if (status != null && !status.isBlank()) url.append("status=").append(status).append("&");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .GET()
                .timeout(Duration.ofSeconds(4))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::extractData)
                .thenApply(json -> {
                    try {
                        return objectMapper.convertValue(json, new TypeReference<List<CaseModel>>() {});
                    } catch (Exception e) {
                        return Collections.emptyList();
                    }
                });
    }

    public CompletableFuture<CaseModel> createCase(Map<String, Object> caseData) {
        try {
            String jsonBody = objectMapper.writeValueAsString(caseData);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/cases"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::extractData)
                    .thenApply(json -> {
                        try {
                            return objectMapper.treeToValue(json, CaseModel.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse case response", e);
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<CaseModel> updateCase(String caseId, Map<String, Object> caseData) {
        try {
            String jsonBody = objectMapper.writeValueAsString(caseData);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/cases/" + caseId))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::extractData)
                    .thenApply(json -> {
                        try {
                            return objectMapper.treeToValue(json, CaseModel.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse updated case response", e);
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<Void> deleteCase(String caseId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cases/" + caseId))
                .DELETE()
                .timeout(Duration.ofSeconds(5))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() != 200) {
                        throw new RuntimeException("Delete failed with status: " + res.statusCode());
                    }
                });
    }

    public CompletableFuture<JsonNode> getEvidence(String caseId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cases/" + caseId + "/evidence"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(this::extractData);
    }

    public CompletableFuture<JsonNode> uploadEvidence(String caseId, File file, boolean isSimulated) {
        String boundary = "---InvBoundary" + System.currentTimeMillis();
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String fileName = file.getName();

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            baos.write(("--" + boundary + "\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            baos.write("Content-Disposition: form-data; name=\"isSimulated\"\r\n\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            baos.write((String.valueOf(isSimulated) + "\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));

            baos.write(("--" + boundary + "\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            baos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            baos.write("Content-Type: text/csv\r\n\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            baos.write(fileBytes);
            baos.write("\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));

            baos.write(("--" + boundary + "--\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/cases/" + caseId + "/evidence/upload"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(this::extractData);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<JsonNode> getEvidencePreview(String evidenceId, int page, int size) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/evidence/" + evidenceId + "/preview?page=" + page + "&size=" + size))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(this::extractData);
    }

    public CompletableFuture<Void> deleteEvidence(String evidenceId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/evidence/" + evidenceId))
                .DELETE()
                .timeout(Duration.ofSeconds(5))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(res -> {
            if (res.statusCode() != 200) {
                throw new RuntimeException("Delete evidence failed with status: " + res.statusCode());
            }
        });
    }

    public CompletableFuture<JsonNode> getTimeline(String caseId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cases/" + caseId + "/timeline"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(this::extractData);
    }

    public CompletableFuture<JsonNode> getGraph(String caseId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cases/" + caseId + "/graph"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(this::extractData);
    }

    public CompletableFuture<JsonNode> getAnomalies(String caseId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cases/" + caseId + "/anomalies"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(this::extractData);
    }

    public CompletableFuture<JsonNode> detectAnomalies(String caseId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cases/" + caseId + "/anomalies/detect"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(this::extractData);
    }

    public CompletableFuture<JsonNode> getSummary(String caseId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cases/" + caseId + "/summary"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(this::extractData);
    }

    public CompletableFuture<JsonNode> generateSummary(String caseId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/cases/" + caseId + "/summary/generate"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(this::extractData);
    }

    private JsonNode extractData(HttpResponse<String> response) {
        try {
            JsonNode root = objectMapper.readTree(response.body());
            if (root.path("success").asBoolean(false)) {
                return root.path("data");
            } else {
                throw new RuntimeException(root.path("message").asText("API Error"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode response: " + e.getMessage(), e);
        }
    }
}
