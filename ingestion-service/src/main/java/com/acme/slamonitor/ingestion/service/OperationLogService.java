package com.acme.slamonitor.ingestion.service;

import com.acme.slamonitor.ingestion.dto.OperationLogRequest;
import com.acme.slamonitor.ingestion.dto.OperationLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OperationLogService {
    OperationLogResponse ingest(OperationLogRequest request);
    Page<OperationLogResponse> findByTenant(String tenantCode, Pageable pageable);
    Page<OperationLogResponse> findByTenantAndJobType(String tenantCode, String jobType, Pageable pageable);
}
