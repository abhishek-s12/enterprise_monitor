package com.acme.slamonitor.ingestion.controller;

import com.acme.slamonitor.ingestion.dto.OperationLogRequest;
import com.acme.slamonitor.ingestion.dto.OperationLogResponse;
import com.acme.slamonitor.ingestion.exception.TenantNotFoundException;
import com.acme.slamonitor.ingestion.service.OperationLogService;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OperationLogController.class)
class OperationLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OperationLogService operationLogService;

    @Test
    void ingest_withValidPayload_returns201() throws Exception {
        Instant start = Instant.now().minus(5, ChronoUnit.MINUTES);
        Instant end = Instant.now();
        OperationLogRequest request = new OperationLogRequest(
                "ACME", "PAYMENT_BATCH", "job-1", start, end, "SUCCESS", null);

        OperationLogResponse response = OperationLogResponse.builder()
                .id("mongo-id-1").tenantCode("ACME").jobType("PAYMENT_BATCH").jobId("job-1")
                .status("SUCCESS").build();

        when(operationLogService.ingest(any(OperationLogRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("mongo-id-1"))
                .andExpect(jsonPath("$.tenantCode").value("ACME"));
    }

    @Test
    void ingest_withMissingFields_returns400() throws Exception {
        String invalidJson = "{\"tenantCode\":\"\",\"jobType\":\"\"}";

        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void ingest_withUnknownTenant_returns404() throws Exception {
        Instant start = Instant.now().minus(5, ChronoUnit.MINUTES);
        Instant end = Instant.now();
        OperationLogRequest request = new OperationLogRequest(
                "UNKNOWN", "PAYMENT_BATCH", "job-1", start, end, "SUCCESS", null);

        when(operationLogService.ingest(any(OperationLogRequest.class)))
                .thenThrow(new TenantNotFoundException("UNKNOWN"));

        mockMvc.perform(post("/api/v1/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No active tenant found for tenantCode='UNKNOWN'"));
    }

    @Test
    void findLogs_withTenantOnly_returnsPage() throws Exception {
        OperationLogResponse response = OperationLogResponse.builder()
                .id("1").tenantCode("ACME").jobType("PAYMENT_BATCH").build();
        Page<OperationLogResponse> page = new PageImpl<>(List.of(response));

        when(operationLogService.findByTenant(eq("ACME"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/logs").param("tenantCode", "ACME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tenantCode").value("ACME"));
    }

    @Test
    void findLogs_withTenantAndJobType_returnsFilteredPage() throws Exception {
        OperationLogResponse response = OperationLogResponse.builder()
                .id("2").tenantCode("ACME").jobType("PAYMENT_BATCH").build();
        Page<OperationLogResponse> page = new PageImpl<>(List.of(response));

        when(operationLogService.findByTenantAndJobType(eq("ACME"), eq("PAYMENT_BATCH"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/logs")
                        .param("tenantCode", "ACME")
                        .param("jobType", "PAYMENT_BATCH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].jobType").value("PAYMENT_BATCH"));
    }
}
