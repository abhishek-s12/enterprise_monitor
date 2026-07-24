package com.acme.slamonitor.slaengine.service;

import com.acme.slamonitor.slaengine.dto.EvaluationRequest;
import com.acme.slamonitor.slaengine.dto.EvaluationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SlaEvaluationService {
    EvaluationResponse evaluate(EvaluationRequest request);
    Page<EvaluationResponse> findEvaluations(String tenantCode, String status, Pageable pageable);
}
