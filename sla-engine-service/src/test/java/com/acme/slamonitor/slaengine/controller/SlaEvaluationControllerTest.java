package com.acme.slamonitor.slaengine.controller;

import com.acme.slamonitor.slaengine.dto.EvaluationRequest;
import com.acme.slamonitor.slaengine.dto.EvaluationResponse;
import com.acme.slamonitor.slaengine.exception.TenantNotFoundException;
import com.acme.slamonitor.slaengine.service.SlaEvaluationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SlaEvaluationController.class)
class SlaEvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SlaEvaluationService slaEvaluationService;

    @Test
    void evaluate_withValidPayload_returns201() throws Exception {
        EvaluationRequest request = new EvaluationRequest("ACME", "PAYMENT_BATCH", "job-100", 350000L);
        EvaluationResponse response = EvaluationResponse.builder()
                .id("eval-1").tenantCode("ACME").jobType("PAYMENT_BATCH").jobId("job-100")
                .durationMs(350000L).status("BREACH").build();

        when(slaEvaluationService.evaluate(any(EvaluationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/evaluations/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("eval-1"))
                .andExpect(jsonPath("$.status").value("BREACH"));
    }

    @Test
    void evaluate_withMissingFields_returns400() throws Exception {
        String invalidJson = "{\"tenantCode\":\"\",\"durationMs\":-10}";

        mockMvc.perform(post("/api/v1/evaluations/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void evaluate_withUnknownTenant_returns404() throws Exception {
        EvaluationRequest request = new EvaluationRequest("UNKNOWN", "PAYMENT_BATCH", "job-101", 100000L);

        when(slaEvaluationService.evaluate(any(EvaluationRequest.class)))
                .thenThrow(new TenantNotFoundException("UNKNOWN"));

        mockMvc.perform(post("/api/v1/evaluations/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No active tenant found for tenantCode='UNKNOWN'"));
    }

    @Test
    void findEvaluations_withTenantOnly_returns200() throws Exception {
        EvaluationResponse response = EvaluationResponse.builder()
                .id("eval-1").tenantCode("ACME").status("NORMAL").build();
        Page<EvaluationResponse> page = new PageImpl<>(List.of(response));

        when(slaEvaluationService.findEvaluations(eq("ACME"), eq(null), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/evaluations").param("tenantCode", "ACME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("NORMAL"));
    }

    @Test
    void findEvaluations_withTenantAndStatus_returns200() throws Exception {
        EvaluationResponse response = EvaluationResponse.builder()
                .id("eval-2").tenantCode("ACME").status("BREACH").build();
        Page<EvaluationResponse> page = new PageImpl<>(List.of(response));

        when(slaEvaluationService.findEvaluations(eq("ACME"), eq("BREACH"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/evaluations")
                        .param("tenantCode", "ACME")
                        .param("status", "BREACH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("BREACH"));
    }
}
