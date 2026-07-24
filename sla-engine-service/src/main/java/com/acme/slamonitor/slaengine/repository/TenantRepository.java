package com.acme.slamonitor.slaengine.repository;

import com.acme.slamonitor.slaengine.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByTenantCodeAndActiveTrue(String tenantCode);
}
