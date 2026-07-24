package com.acme.slamonitor.ingestion.dto;

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
public class OperationLogResponse {

    private String id;
    private String tenantCode;
    private String jobType;
    private String jobId;
    private Instant startedAt;
    private Instant completedAt;
    private Long durationMs;
    private String status;
    private Instant ingestedAt;
}