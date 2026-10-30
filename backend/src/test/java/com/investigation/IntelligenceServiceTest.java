package com.investigation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investigation.dto.CaseCreateRequest;
import com.investigation.entity.enums.CaseCategory;
import com.investigation.entity.enums.CaseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class IntelligenceServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testAnomalyDetectionAndSummaryGeneration() throws Exception {
        // 1. Create a Case
        CaseCreateRequest createReq = new CaseCreateRequest(
                "Mundhra Financial Irregularity",
                "CASE-1957-MF",
                "Investigation into fraudulent share pricing",
                CaseCategory.FINANCIAL_FRAUD,
                CaseStatus.ACTIVE,
                true,
                Instant.parse("1957-06-25T11:00:00Z")
        );

        String caseResponseStr = mockMvc.perform(post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String caseId = objectMapper.readTree(caseResponseStr).path("data").path("id").asText();

        // 2. Upload CSV with values that contain a statistical outlier (120, 125, 118, then 95000)
        String csvContent = "TransactionId,Timestamp,Company,Buyer,AmountRupees\n" +
                "TX-1,1957-06-20T10:00:00Z,Jessop and Co,LIC,120.0\n" +
                "TX-2,1957-06-21T11:00:00Z,Richardson Cruddas,LIC,125.0\n" +
                "TX-3,1957-06-22T12:00:00Z,Smith Stainistreet,LIC,118.0\n" +
                "TX-4,1957-06-23T14:00:00Z,Bhopal Sugar,LIC,95000.0\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "mundhra_share_prices.csv",
                "text/csv",
                csvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/cases/" + caseId + "/evidence/upload")
                        .file(file)
                        .param("isSimulated", "true"))
                .andExpect(status().isCreated());

        // 3. Trigger Anomaly Detection
        mockMvc.perform(post("/api/cases/" + caseId + "/anomalies/detect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].anomalyType", is("NUMERICAL_STATISTICAL_OUTLIER")));

        // 4. Retrieve anomalies
        mockMvc.perform(get("/api/cases/" + caseId + "/anomalies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        // 5. Generate AI Summary
        mockMvc.perform(post("/api/cases/" + caseId + "/summary/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.factualOverview", containsString("Mundhra Financial Irregularity")))
                .andExpect(jsonPath("$.data.keyFindings", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.confidenceNotes", containsString("Forensic Guardrail Notice")));

        // 6. Retrieve AI Summary
        mockMvc.perform(get("/api/cases/" + caseId + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.factualOverview", not(emptyOrNullString())));
    }
}
