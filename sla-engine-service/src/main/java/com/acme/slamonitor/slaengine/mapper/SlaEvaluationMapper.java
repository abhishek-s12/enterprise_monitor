package com.acme.slamonitor.slaengine.mapper;

import com.acme.slamonitor.slaengine.document.SlaEvaluationDocument;
import com.acme.slamonitor.slaengine.dto.EvaluationResponse;
import org.springframework.stereotype.Component;

@Component
public class SlaEvaluationMapper {

    public EvaluationResponse toResponse(SlaEvaluationDocument doc) {
        return EvaluationResponse.builder()
                .id(doc.getId())
                .tenantCode(doc.getTenantCode())
                .jobType(doc.getJobType())
                .jobId(doc.getJobId())
                .durationMs(doc.getDurationMs())
                .status(doc.getStatus())
                .warningThresholdMs(doc.getWarningThresholdMs())
                .criticalThresholdMs(doc.getCriticalThresholdMs())
                .evaluatedAt(doc.getEvaluatedAt())
                .build();
    }
}
