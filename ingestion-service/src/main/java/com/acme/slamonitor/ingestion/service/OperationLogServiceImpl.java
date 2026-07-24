package com.acme.slamonitor.ingestion.service;

import com.acme.slamonitor.ingestion.document.OperationLogDocument;
import com.acme.slamonitor.ingestion.dto.OperationLogRequest;
import com.acme.slamonitor.ingestion.dto.OperationLogResponse;
import com.acme.slamonitor.ingestion.exception.TenantNotFoundException;
import com.acme.slamonitor.ingestion.mapper.OperationLogMapper;
import com.acme.slamonitor.ingestion.repository.OperationLogRepository;
import com.acme.slamonitor.ingestion.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final TenantRepository tenantRepository;
    private final OperationLogMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public OperationLogResponse ingest(OperationLogRequest request) {
        tenantRepository.findByTenantCodeAndActiveTrue(request.getTenantCode())
                .orElseThrow(() -> new TenantNotFoundException(request.getTenantCode()));

        if (request.getCompletedAt().isBefore(request.getStartedAt())) {
            throw new IllegalArgumentException("completedAt cannot be before startedAt");
        }

        OperationLogDocument document = mapper.toDocument(request);
        OperationLogDocument saved = operationLogRepository.save(document);
        return mapper.toResponse(saved);
    }

    @Override
    public Page<OperationLogResponse> findByTenant(String tenantCode, Pageable pageable) {
        return operationLogRepository.findByTenantCode(tenantCode, pageable)
                .map(mapper::toResponse);
    }

    @Override
    public Page<OperationLogResponse> findByTenantAndJobType(String tenantCode, String jobType, Pageable pageable) {
        return operationLogRepository.findByTenantCodeAndJobType(tenantCode, jobType, pageable)
                .map(mapper::toResponse);
    }
}