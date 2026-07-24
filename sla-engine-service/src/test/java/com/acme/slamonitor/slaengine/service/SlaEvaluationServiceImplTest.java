package com.acme.slamonitor.slaengine.service;

import com.acme.slamonitor.slaengine.document.SlaEvaluationDocument;
import com.acme.slamonitor.slaengine.dto.EvaluationRequest;
import com.acme.slamonitor.slaengine.dto.EvaluationResponse;
import com.acme.slamonitor.slaengine.entity.SlaThreshold;
import com.acme.slamonitor.slaengine.entity.Tenant;
import com.acme.slamonitor.slaengine.exception.TenantNotFoundException;
import com.acme.slamonitor.slaengine.mapper.SlaEvaluationMapper;
import com.acme.slamonitor.slaengine.repository.SlaEvaluationRepository;
import com.acme.slamonitor.slaengine.repository.SlaThresholdRepository;
import com.acme.slamonitor.slaengine.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaEvaluationServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private SlaThresholdRepository slaThresholdRepository;

    @Mock
    private SlaEvaluationRepository slaEvaluationRepository;

    @Mock
    private SlaEvaluationMapper mapper;

    @InjectMocks
    private SlaEvaluationServiceImpl service;

    private Tenant activeTenant;
    private SlaThreshold slaThreshold;

    @BeforeEach
    void setUp() {
        activeTenant = new Tenant();
        activeTenant.setId(1L);
        activeTenant.setTenantCode("ACME");
        activeTenant.setActive(true);

        slaThreshold = new SlaThreshold();
        slaThreshold.setId(1L);
        slaThreshold.setTenant(activeTenant);
        slaThreshold.setJobType("PAYMENT_BATCH");
        slaThreshold.setWarningThresholdMs(180000L); // 3 min
        slaThreshold.setCriticalThresholdMs(300000L); // 5 min
    }

    @Test
    void evaluate_whenDurationLessThanWarning_returnsNormalStatus() {
        when(tenantRepository.findByTenantCodeAndActiveTrue("ACME")).thenReturn(Optional.of(activeTenant));
        when(slaThresholdRepository.findByTenantTenantCodeAndJobType("ACME", "PAYMENT_BATCH"))
                .thenReturn(Optional.of(slaThreshold));

        EvaluationRequest request = new EvaluationRequest("ACME", "PAYMENT_BATCH", "job-1", 100000L);
        SlaEvaluationDocument savedDoc = SlaEvaluationDocument.builder().id("eval-1").status("NORMAL").build();
        EvaluationResponse expectedResponse = EvaluationResponse.builder().id("eval-1").status("NORMAL").build();

        when(slaEvaluationRepository.save(any(SlaEvaluationDocument.class))).thenReturn(savedDoc);
        when(mapper.toResponse(savedDoc)).thenReturn(expectedResponse);

        EvaluationResponse response = service.evaluate(request);

        assertThat(response.getStatus()).isEqualTo("NORMAL");
        verify(slaEvaluationRepository).save(argThat(doc -> "NORMAL".equals(doc.getStatus())));
    }

    @Test
    void evaluate_whenDurationBetweenWarningAndCritical_returnsWarningStatus() {
        when(tenantRepository.findByTenantCodeAndActiveTrue("ACME")).thenReturn(Optional.of(activeTenant));
        when(slaThresholdRepository.findByTenantTenantCodeAndJobType("ACME", "PAYMENT_BATCH"))
                .thenReturn(Optional.of(slaThreshold));

        EvaluationRequest request = new EvaluationRequest("ACME", "PAYMENT_BATCH", "job-2", 200000L);
        SlaEvaluationDocument savedDoc = SlaEvaluationDocument.builder().id("eval-2").status("WARNING").build();
        EvaluationResponse expectedResponse = EvaluationResponse.builder().id("eval-2").status("WARNING").build();

        when(slaEvaluationRepository.save(any(SlaEvaluationDocument.class))).thenReturn(savedDoc);
        when(mapper.toResponse(savedDoc)).thenReturn(expectedResponse);

        EvaluationResponse response = service.evaluate(request);

        assertThat(response.getStatus()).isEqualTo("WARNING");
        verify(slaEvaluationRepository).save(argThat(doc -> "WARNING".equals(doc.getStatus())));
    }

    @Test
    void evaluate_whenDurationGreaterThanCritical_returnsBreachStatus() {
        when(tenantRepository.findByTenantCodeAndActiveTrue("ACME")).thenReturn(Optional.of(activeTenant));
        when(slaThresholdRepository.findByTenantTenantCodeAndJobType("ACME", "PAYMENT_BATCH"))
                .thenReturn(Optional.of(slaThreshold));

        EvaluationRequest request = new EvaluationRequest("ACME", "PAYMENT_BATCH", "job-3", 350000L);
        SlaEvaluationDocument savedDoc = SlaEvaluationDocument.builder().id("eval-3").status("BREACH").build();
        EvaluationResponse expectedResponse = EvaluationResponse.builder().id("eval-3").status("BREACH").build();

        when(slaEvaluationRepository.save(any(SlaEvaluationDocument.class))).thenReturn(savedDoc);
        when(mapper.toResponse(savedDoc)).thenReturn(expectedResponse);

        EvaluationResponse response = service.evaluate(request);

        assertThat(response.getStatus()).isEqualTo("BREACH");
        verify(slaEvaluationRepository).save(argThat(doc -> "BREACH".equals(doc.getStatus())));
    }

    @Test
    void evaluate_whenUnknownTenant_throwsTenantNotFoundException() {
        when(tenantRepository.findByTenantCodeAndActiveTrue("UNKNOWN")).thenReturn(Optional.empty());

        EvaluationRequest request = new EvaluationRequest("UNKNOWN", "PAYMENT_BATCH", "job-4", 100000L);

        assertThatThrownBy(() -> service.evaluate(request))
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessageContaining("UNKNOWN");

        verifyNoInteractions(slaEvaluationRepository);
    }

    @Test
    void evaluate_whenThresholdNotFound_usesDefaultThresholds() {
        when(tenantRepository.findByTenantCodeAndActiveTrue("ACME")).thenReturn(Optional.of(activeTenant));
        when(slaThresholdRepository.findByTenantTenantCodeAndJobType("ACME", "CUSTOM_JOB"))
                .thenReturn(Optional.empty());

        EvaluationRequest request = new EvaluationRequest("ACME", "CUSTOM_JOB", "job-5", 350000L);
        SlaEvaluationDocument savedDoc = SlaEvaluationDocument.builder().id("eval-5").status("BREACH").build();
        EvaluationResponse expectedResponse = EvaluationResponse.builder().id("eval-5").status("BREACH").build();

        when(slaEvaluationRepository.save(any(SlaEvaluationDocument.class))).thenReturn(savedDoc);
        when(mapper.toResponse(savedDoc)).thenReturn(expectedResponse);

        EvaluationResponse response = service.evaluate(request);

        assertThat(response.getStatus()).isEqualTo("BREACH");
        verify(slaEvaluationRepository).save(argThat(doc ->
                doc.getWarningThresholdMs() == SlaEvaluationServiceImpl.DEFAULT_WARNING_THRESHOLD_MS &&
                        doc.getCriticalThresholdMs() == SlaEvaluationServiceImpl.DEFAULT_CRITICAL_THRESHOLD_MS
        ));
    }

    @Test
    void findEvaluations_withTenantOnly_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        SlaEvaluationDocument doc = SlaEvaluationDocument.builder().id("1").tenantCode("ACME").build();
        Page<SlaEvaluationDocument> docPage = new PageImpl<>(List.of(doc));
        EvaluationResponse response = EvaluationResponse.builder().id("1").tenantCode("ACME").build();

        when(slaEvaluationRepository.findByTenantCode(eq("ACME"), any(Pageable.class))).thenReturn(docPage);
        when(mapper.toResponse(doc)).thenReturn(response);

        Page<EvaluationResponse> result = service.findEvaluations("ACME", null, pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findEvaluations_withTenantAndStatus_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        SlaEvaluationDocument doc = SlaEvaluationDocument.builder().id("2").tenantCode("ACME").status("BREACH").build();
        Page<SlaEvaluationDocument> docPage = new PageImpl<>(List.of(doc));
        EvaluationResponse response = EvaluationResponse.builder().id("2").tenantCode("ACME").status("BREACH").build();

        when(slaEvaluationRepository.findByTenantCodeAndStatus(eq("ACME"), eq("BREACH"), any(Pageable.class)))
                .thenReturn(docPage);
        when(mapper.toResponse(doc)).thenReturn(response);

        Page<EvaluationResponse> result = service.findEvaluations("ACME", "breach", pageable);

        assertThat(result.getContent()).containsExactly(response);
    }
}
