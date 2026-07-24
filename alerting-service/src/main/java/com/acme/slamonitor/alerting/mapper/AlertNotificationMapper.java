package com.acme.slamonitor.alerting.mapper;

import com.acme.slamonitor.alerting.document.AlertNotificationDocument;
import com.acme.slamonitor.alerting.dto.AlertNotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class AlertNotificationMapper {

    public AlertNotificationResponse toResponse(AlertNotificationDocument doc) {
        return AlertNotificationResponse.builder()
                .id(doc.getId())
                .tenantCode(doc.getTenantCode())
                .jobType(doc.getJobType())
                .jobId(doc.getJobId())
                .severity(doc.getSeverity())
                .channel(doc.getChannel())
                .recipient(doc.getRecipient())
                .message(doc.getMessage())
                .durationMs(doc.getDurationMs())
                .status(doc.getStatus())
                .sentAt(doc.getSentAt())
                .build();
    }
}
