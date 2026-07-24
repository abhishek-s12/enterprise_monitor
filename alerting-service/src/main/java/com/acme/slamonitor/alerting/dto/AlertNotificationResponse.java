package com.acme.slamonitor.alerting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertNotificationResponse {

    private String id;
    private String tenantCode;
    private String jobType;
    private String jobId;
    private String severity;
    private String channel;
    private String recipient;
    private String message;
    private Long durationMs;
    private String status;
    private Instant sentAt;
}
