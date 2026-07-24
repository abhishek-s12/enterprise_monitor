package com.acme.slamonitor.alerting.repository;

import com.acme.slamonitor.alerting.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByTenantCodeAndActiveTrue(String tenantCode);
}
