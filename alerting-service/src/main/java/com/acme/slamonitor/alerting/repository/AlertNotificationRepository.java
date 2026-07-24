package com.acme.slamonitor.alerting.repository;

import com.acme.slamonitor.alerting.document.AlertNotificationDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertNotificationRepository extends MongoRepository<AlertNotificationDocument, String> {

    Page<AlertNotificationDocument> findByTenantCode(String tenantCode, Pageable pageable);

    Page<AlertNotificationDocument> findByTenantCodeAndSeverity(String tenantCode, String severity, Pageable pageable);
}
