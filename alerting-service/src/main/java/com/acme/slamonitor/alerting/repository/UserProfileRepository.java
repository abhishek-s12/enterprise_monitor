package com.acme.slamonitor.alerting.repository;

import com.acme.slamonitor.alerting.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    List<UserProfile> findByTenantTenantCode(String tenantCode);
}
