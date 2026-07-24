package com.acme.slamonitor.slaengine.service;

import com.acme.slamonitor.slaengine.document.SlaEvaluationDocument;
import com.acme.slamonitor.slaengine.dto.EvaluationRequest;
import com.acme.slamonitor.slaengine.dto.EvaluationResponse;
import com.acme.slamonitor.slaengine.entity.SlaThreshold;
import com.acme.slamonitor.slaengine.exception.TenantNotFoundException;
import com.acme.slamonitor.slaengine.mapper.SlaEvaluationMapper;
import com.acme.slamonitor.slaengine.repository.SlaEvaluationRepository;
import com.acme.slamonitor.slaengine.repository.SlaThresholdRepository;
import com.acme.slamonitor.slaengine.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SlaEvaluationServiceImpl implements SlaEvaluationService {

    public static final long DEFAULT_WARNING_THRESHOLD_MS = 180000L;
    public static final long DEFAULT_CRITICAL_THRESHOLD_MS = 300000L;

    private final TenantRepository tenantRepository;
    private final SlaThresholdRepository slaThresholdRepository;
    private final SlaEvaluationRepository slaEvaluationRepository;
    private final SlaEvaluationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public EvaluationResponse evaluate(EvaluationRequest request) {
        tenantRepository.findByTenantCodeAndActiveTrue(request.getTenantCode())
                .orElseThrow(() -> new TenantNotFoundException(request.getTenantCode()));

        Optional<SlaThreshold> thresholdOpt = slaThresholdRepository
                .findByTenantTenantCodeAndJobType(request.getTenantCode(), request.getJobType());

        long warningThreshold = thresholdOpt.map(SlaThreshold::getWarningThresholdMs)
                .orElse(DEFAULT_WARNING_THRESHOLD_MS);
        long criticalThreshold = thresholdOpt.map(SlaThreshold::getCriticalThresholdMs)
                .orElse(DEFAULT_CRITICAL_THRESHOLD_MS);

        long durationMs = request.getDurationMs();
        String status;
        if (durationMs >= criticalThreshold) {
            status = "BREACH";
        } else if (durationMs >= warningThreshold) {
            status = "WARNING";
        } else {
            status = "NORMAL";
        }

        SlaEvaluationDocument doc = SlaEvaluationDocument.builder()
                .tenantCode(request.getTenantCode())
                .jobType(request.getJobType())
                .jobId(request.getJobId())
                .durationMs(durationMs)
                .status(status)
                .warningThresholdMs(warningThreshold)
                .criticalThresholdMs(criticalThreshold)
                .build();

        SlaEvaluationDocument saved = slaEvaluationRepository.save(doc);
        return mapper.toResponse(saved);
    }

    @Override
    public Page<EvaluationResponse> findEvaluations(String tenantCode, String status, Pageable pageable) {
        Page<SlaEvaluationDocument> page = (status == null || status.isBlank())
                ? slaEvaluationRepository.findByTenantCode(tenantCode, pageable)
                : slaEvaluationRepository.findByTenantCodeAndStatus(tenantCode, status.toUpperCase(), pageable);

        return page.map(mapper::toResponse);
    }
}
