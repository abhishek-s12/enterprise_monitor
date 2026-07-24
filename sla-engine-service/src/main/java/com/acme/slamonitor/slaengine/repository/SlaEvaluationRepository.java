package com.acme.slamonitor.slaengine.repository;

import com.acme.slamonitor.slaengine.document.SlaEvaluationDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaEvaluationRepository extends MongoRepository<SlaEvaluationDocument, String> {

    Page<SlaEvaluationDocument> findByTenantCode(String tenantCode, Pageable pageable);

    Page<SlaEvaluationDocument> findByTenantCodeAndStatus(String tenantCode, String status, Pageable pageable);
}
