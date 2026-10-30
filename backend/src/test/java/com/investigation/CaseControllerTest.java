package com.investigation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investigation.dto.CaseCreateRequest;
import com.investigation.dto.CaseUpdateRequest;
import com.investigation.entity.enums.CaseCategory;
import com.investigation.entity.enums.CaseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class CaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCaseLifecycle() throws Exception {
        // 1. Create a Case
        CaseCreateRequest createReq = new CaseCreateRequest(
                "Test Security Breach",
                "CASE-TEST-001",
                "Investigation into network intrusion logs",
                CaseCategory.CYBER_INCIDENT,
                CaseStatus.ACTIVE,
                false,
                Instant.now()
        );

        String createJson = objectMapper.writeValueAsString(createReq);

        String responseStr = mockMvc.perform(post("/api/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Test Security Breach")))
                .andExpect(jsonPath("$.data.caseNumber", is("CASE-TEST-001")))
                .andReturn().getResponse().getContentAsString();

        // Extract ID
        String caseId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        // 2. Fetch by ID
        mockMvc.perform(get("/api/cases/" + caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(caseId)))
                .andExpect(jsonPath("$.data.category", is("CYBER_INCIDENT")));

        // 3. Update Case
        CaseUpdateRequest updateReq = new CaseUpdateRequest();
        updateReq.setTitle("Updated Security Breach Title");
        updateReq.setStatus(CaseStatus.UNDER_REVIEW);

        mockMvc.perform(put("/api/cases/" + caseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title", is("Updated Security Breach Title")))
                .andExpect(jsonPath("$.data.status", is("UNDER_REVIEW")));

        // 4. List all cases with search filter
        mockMvc.perform(get("/api/cases?search=Updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        // 5. Test Dashboard summary
        mockMvc.perform(get("/api/cases/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCases", greaterThanOrEqualTo(1)));

        // 6. Delete Case
        mockMvc.perform(delete("/api/cases/" + caseId))
                .andExpect(status().isOk());

        // 7. Verify 404 on deleted case
        mockMvc.perform(get("/api/cases/" + caseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errorCode", is("ENTITY_NOT_FOUND")));
    }
}
