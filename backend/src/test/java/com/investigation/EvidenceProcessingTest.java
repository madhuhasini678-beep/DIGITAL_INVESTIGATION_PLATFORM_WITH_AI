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
public class EvidenceProcessingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testEvidenceUploadAndProcessingFlow() throws Exception {
        // 1. Create a test Case
        CaseCreateRequest createReq = new CaseCreateRequest(
                "Securities Fraud Investigation",
                "CASE-1992-BR",
                "Investigating unauthorized bank receipts",
                CaseCategory.FINANCIAL_FRAUD,
                CaseStatus.ACTIVE,
                true,
                Instant.parse("1992-04-23T10:00:00Z")
        );

        String caseResponseStr = mockMvc.perform(post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String caseId = objectMapper.readTree(caseResponseStr).path("data").path("id").asText();

        // 2. Prepare sample CSV evidence
        String csvContent = "TransactionId,Timestamp,Sender,Receiver,AmountCrores,Action\n" +
                "TX-101,1992-04-10T09:30:00Z,Bank of Karad,State Bank of India,150.5,BR_ISSUANCE\n" +
                "TX-102,1992-04-11T14:15:00Z,State Bank of India,National Housing Bank,220.0,FUNDS_SETTLEMENT\n" +
                "TX-103,1992-04-12T16:45:00Z,National Housing Bank,Metropolitan Co-op Bank,95.0,INTER_BANK_LOAN\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bank_receipts_ledger.csv",
                "text/csv",
                csvContent.getBytes()
        );

        // 3. Upload Evidence
        String uploadResponseStr = mockMvc.perform(multipart("/api/cases/" + caseId + "/evidence/upload")
                        .file(file)
                        .param("isSimulated", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fileName", is("bank_receipts_ledger.csv")))
                .andExpect(jsonPath("$.data.sha256Hash", notNullValue()))
                .andExpect(jsonPath("$.data.recordCount", is(3)))
                .andReturn().getResponse().getContentAsString();

        String evidenceId = objectMapper.readTree(uploadResponseStr).path("data").path("id").asText();

        // 4. Test Preview Endpoint
        mockMvc.perform(get("/api/evidence/" + evidenceId + "/preview?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRows", is(3)))
                .andExpect(jsonPath("$.data.headers", hasItem("Sender")))
                .andExpect(jsonPath("$.data.rows[0].Sender", is("Bank of Karad")));

        // 5. Test Timeline Events Auto-Extracted
        mockMvc.perform(get("/api/cases/" + caseId + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].title", is("BR_ISSUANCE")))
                .andExpect(jsonPath("$.data[0].sourceReference", containsString("bank_receipts_ledger.csv")));

        // 6. Test Relationship Graph Auto-Extracted
        mockMvc.perform(get("/api/cases/" + caseId + "/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.data.edges", hasSize(3)));

        // 7. Cleanup Evidence
        mockMvc.perform(delete("/api/evidence/" + evidenceId))
                .andExpect(status().isOk());
    }
}
