package com.acme.slamonitor.slaengine.repository;

import com.acme.slamonitor.slaengine.entity.SlaThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaThresholdRepository extends JpaRepository<SlaThreshold, Long> {

    Optional<SlaThreshold> findByTenantTenantCodeAndJobType(String tenantCode, String jobType);
}
