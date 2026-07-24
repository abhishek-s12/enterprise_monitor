package com.acme.slamonitor.slaengine.dto;

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
public class EvaluationResponse {

    private String id;
    private String tenantCode;
    private String jobType;
    private String jobId;
    private Long durationMs;
    private String status;
    private Long warningThresholdMs;
    private Long criticalThresholdMs;
    private Instant evaluatedAt;
}
