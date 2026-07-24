package com.acme.slamonitor.alerting.service;

import com.acme.slamonitor.alerting.document.AlertNotificationDocument;
import com.acme.slamonitor.alerting.dto.AlertDispatchRequest;
import com.acme.slamonitor.alerting.dto.AlertNotificationResponse;
import com.acme.slamonitor.alerting.entity.UserProfile;
import com.acme.slamonitor.alerting.exception.TenantNotFoundException;
import com.acme.slamonitor.alerting.mapper.AlertNotificationMapper;
import com.acme.slamonitor.alerting.repository.AlertNotificationRepository;
import com.acme.slamonitor.alerting.repository.TenantRepository;
import com.acme.slamonitor.alerting.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertDispatcherServiceImpl implements AlertDispatcherService {

    private final TenantRepository tenantRepository;
    private final UserProfileRepository userProfileRepository;
    private final AlertNotificationRepository alertNotificationRepository;
    private final AlertNotificationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<AlertNotificationResponse> dispatch(AlertDispatchRequest request) {
        tenantRepository.findByTenantCodeAndActiveTrue(request.getTenantCode())
                .orElseThrow(() -> new TenantNotFoundException(request.getTenantCode()));

        List<UserProfile> profiles = userProfileRepository.findByTenantTenantCode(request.getTenantCode());

        List<AlertNotificationDocument> docsToSave = new ArrayList<>();

        // Always log structured system alert
        log.info("ALERT DISPATCH [{}] Tenant='{}' JobType='{}' JobId='{}' Message='{}'",
                request.getSeverity(), request.getTenantCode(), request.getJobType(),
                request.getJobId(), request.getMessage());

        docsToSave.add(AlertNotificationDocument.builder()
                .tenantCode(request.getTenantCode())
                .jobType(request.getJobType())
                .jobId(request.getJobId())
                .severity(request.getSeverity().toUpperCase())
                .channel("LOG")
                .recipient("SYSTEM_LOG")
                .message(request.getMessage())
                .durationMs(request.getDurationMs())
                .status("SENT")
                .build());

        // Dispatch simulated EMAIL notifications to tenant user profiles
        if (!profiles.isEmpty()) {
            for (UserProfile profile : profiles) {
                log.info("Simulating EMAIL alert to {} ({}) for SLA {}",
                        profile.getUsername(), profile.getEmail(), request.getSeverity());

                docsToSave.add(AlertNotificationDocument.builder()
                        .tenantCode(request.getTenantCode())
                        .jobType(request.getJobType())
                        .jobId(request.getJobId())
                        .severity(request.getSeverity().toUpperCase())
                        .channel("EMAIL")
                        .recipient(profile.getEmail())
                        .message(request.getMessage())
                        .durationMs(request.getDurationMs())
                        .status("SENT")
                        .build());
            }
        }

        List<AlertNotificationDocument> savedDocs = alertNotificationRepository.saveAll(docsToSave);
        return savedDocs.stream().map(mapper::toResponse).toList();
    }

    @Override
    public Page<AlertNotificationResponse> findAlerts(String tenantCode, String severity, Pageable pageable) {
        Page<AlertNotificationDocument> page = (severity == null || severity.isBlank())
                ? alertNotificationRepository.findByTenantCode(tenantCode, pageable)
                : alertNotificationRepository.findByTenantCodeAndSeverity(tenantCode, severity.toUpperCase(), pageable);

        return page.map(mapper::toResponse);
    }
}
