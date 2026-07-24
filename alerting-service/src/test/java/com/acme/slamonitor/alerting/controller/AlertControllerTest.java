package com.acme.slamonitor.alerting.controller;

import com.acme.slamonitor.alerting.dto.AlertDispatchRequest;
import com.acme.slamonitor.alerting.dto.AlertNotificationResponse;
import com.acme.slamonitor.alerting.exception.TenantNotFoundException;
import com.acme.slamonitor.alerting.service.AlertDispatcherService;
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

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertDispatcherService alertDispatcherService;

    @Test
    void dispatchAlert_withValidPayload_returns201() throws Exception {
        AlertDispatchRequest request = new AlertDispatchRequest(
                "ACME", "PAYMENT_BATCH", "job-100", "BREACH", 360000L, "SLA Breach Detected");

        AlertNotificationResponse response = AlertNotificationResponse.builder()
                .id("1").tenantCode("ACME").jobType("PAYMENT_BATCH").severity("BREACH").build();

        when(alertDispatcherService.dispatch(any(AlertDispatchRequest.class))).thenReturn(List.of(response));

        mockMvc.perform(post("/api/v1/alerts/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].severity").value("BREACH"));
    }

    @Test
    void dispatchAlert_withMissingFields_returns400() throws Exception {
        String invalidJson = "{\"tenantCode\":\"\",\"severity\":\"\"}";

        mockMvc.perform(post("/api/v1/alerts/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void dispatchAlert_withUnknownTenant_returns404() throws Exception {
        AlertDispatchRequest request = new AlertDispatchRequest(
                "UNKNOWN", "PAYMENT_BATCH", "job-101", "BREACH", 360000L, "SLA Breach Detected");

        when(alertDispatcherService.dispatch(any(AlertDispatchRequest.class)))
                .thenThrow(new TenantNotFoundException("UNKNOWN"));

        mockMvc.perform(post("/api/v1/alerts/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No active tenant found for tenantCode='UNKNOWN'"));
    }

    @Test
    void findAlerts_withTenantOnly_returns200() throws Exception {
        AlertNotificationResponse response = AlertNotificationResponse.builder()
                .id("1").tenantCode("ACME").severity("BREACH").build();
        Page<AlertNotificationResponse> page = new PageImpl<>(List.of(response));

        when(alertDispatcherService.findAlerts(eq("ACME"), eq(null), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/alerts").param("tenantCode", "ACME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].severity").value("BREACH"));
    }

    @Test
    void findAlerts_withTenantAndSeverity_returns200() throws Exception {
        AlertNotificationResponse response = AlertNotificationResponse.builder()
                .id("2").tenantCode("ACME").severity("WARNING").build();
        Page<AlertNotificationResponse> page = new PageImpl<>(List.of(response));

        when(alertDispatcherService.findAlerts(eq("ACME"), eq("WARNING"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/alerts")
                        .param("tenantCode", "ACME")
                        .param("severity", "WARNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].severity").value("WARNING"));
    }
}
