package com.acme.slamonitor.ingestion.repository;

import com.acme.slamonitor.ingestion.document.OperationLogDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends MongoRepository<OperationLogDocument, String> {
    Page<OperationLogDocument> findByTenantCode(String tenantCode, Pageable pageable);
    Page<OperationLogDocument> findByTenantCodeAndJobType(String tenantCode, String jobType, Pageable pageable);
}
