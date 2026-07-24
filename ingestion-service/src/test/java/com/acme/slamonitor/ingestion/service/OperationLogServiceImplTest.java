package com.acme.slamonitor.ingestion.service;

import com.acme.slamonitor.ingestion.document.OperationLogDocument;
import com.acme.slamonitor.ingestion.dto.OperationLogRequest;
import com.acme.slamonitor.ingestion.dto.OperationLogResponse;
import com.acme.slamonitor.ingestion.entity.Tenant;
import com.acme.slamonitor.ingestion.exception.TenantNotFoundException;
import com.acme.slamonitor.ingestion.mapper.OperationLogMapper;
import com.acme.slamonitor.ingestion.repository.OperationLogRepository;
import com.acme.slamonitor.ingestion.repository.TenantRepository;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationLogServiceImplTest {

    @Mock
    private OperationLogRepository operationLogRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private OperationLogMapper mapper;

    @InjectMocks
    private OperationLogServiceImpl service;

    private OperationLogRequest validRequest;
    private Tenant activeTenant;

    @BeforeEach
    void setUp() {
        Instant start = Instant.now().minus(10, ChronoUnit.MINUTES);
        Instant end = Instant.now();

        validRequest = new OperationLogRequest(
                "ACME", "PAYMENT_BATCH", "job-123", start, end, "SUCCESS", null);

        activeTenant = new Tenant();
        activeTenant.setId(1L);
        activeTenant.setTenantCode("ACME");
        activeTenant.setActive(true);
    }

    @Test
    void ingest_withValidRequestAndKnownTenant_savesAndReturnsResponse() {
        when(tenantRepository.findByTenantCodeAndActiveTrue("ACME"))
                .thenReturn(Optional.of(activeTenant));

        OperationLogDocument mappedDoc = OperationLogDocument.builder()
                .tenantCode("ACME").jobType("PAYMENT_BATCH").jobId("job-123").build();
        OperationLogDocument savedDoc = OperationLogDocument.builder()
                .id("mongo-id-1").tenantCode("ACME").jobType("PAYMENT_BATCH").jobId("job-123").build();
        OperationLogResponse expectedResponse = OperationLogResponse.builder()
                .id("mongo-id-1").tenantCode("ACME").jobType("PAYMENT_BATCH").jobId("job-123").build();

        when(mapper.toDocument(validRequest)).thenReturn(mappedDoc);
        when(operationLogRepository.save(mappedDoc)).thenReturn(savedDoc);
        when(mapper.toResponse(savedDoc)).thenReturn(expectedResponse);

        OperationLogResponse result = service.ingest(validRequest);

        assertThat(result).isEqualTo(expectedResponse);
        verify(tenantRepository).findByTenantCodeAndActiveTrue("ACME");
        verify(operationLogRepository).save(mappedDoc);
    }

    @Test
    void ingest_withUnknownTenant_throwsTenantNotFoundException() {
        when(tenantRepository.findByTenantCodeAndActiveTrue("UNKNOWN"))
                .thenReturn(Optional.empty());

        OperationLogRequest request = new OperationLogRequest(
                "UNKNOWN", "PAYMENT_BATCH", "job-1", Instant.now(), Instant.now(), "SUCCESS", null);

        assertThatThrownBy(() -> service.ingest(request))
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessageContaining("UNKNOWN");

        verifyNoInteractions(operationLogRepository);
    }

    @Test
    void ingest_withCompletedBeforeStarted_throwsIllegalArgumentException() {
        when(tenantRepository.findByTenantCodeAndActiveTrue("ACME"))
                .thenReturn(Optional.of(activeTenant));

        Instant start = Instant.now();
        Instant end = start.minus(5, ChronoUnit.MINUTES);
        OperationLogRequest badRequest = new OperationLogRequest(
                "ACME", "PAYMENT_BATCH", "job-1", start, end, "SUCCESS", null);

        assertThatThrownBy(() -> service.ingest(badRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("completedAt cannot be before startedAt");

        verifyNoInteractions(operationLogRepository);
    }

    @Test
    void findByTenant_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OperationLogDocument doc = OperationLogDocument.builder().id("1").tenantCode("ACME").build();
        Page<OperationLogDocument> docPage = new PageImpl<>(List.of(doc));
        OperationLogResponse response = OperationLogResponse.builder().id("1").tenantCode("ACME").build();

        when(operationLogRepository.findByTenantCode(eq("ACME"), any(Pageable.class))).thenReturn(docPage);
        when(mapper.toResponse(doc)).thenReturn(response);

        Page<OperationLogResponse> result = service.findByTenant("ACME", pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findByTenantAndJobType_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        OperationLogDocument doc = OperationLogDocument.builder().id("2").tenantCode("ACME").jobType("PAYMENT_BATCH").build();
        Page<OperationLogDocument> docPage = new PageImpl<>(List.of(doc));
        OperationLogResponse response = OperationLogResponse.builder().id("2").tenantCode("ACME").jobType("PAYMENT_BATCH").build();

        when(operationLogRepository.findByTenantCodeAndJobType(eq("ACME"), eq("PAYMENT_BATCH"), any(Pageable.class)))
                .thenReturn(docPage);
        when(mapper.toResponse(doc)).thenReturn(response);

        Page<OperationLogResponse> result = service.findByTenantAndJobType("ACME", "PAYMENT_BATCH", pageable);

        assertThat(result.getContent()).containsExactly(response);
    }
}
