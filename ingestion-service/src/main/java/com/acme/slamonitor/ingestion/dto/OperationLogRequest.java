package com.acme.slamonitor.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogRequest {

    @NotBlank(message = "tenantCode is required")
    private String tenantCode;

    @NotBlank(message = "jobType is required")
    private String jobType;

    @NotBlank(message = "jobId is required")
    private String jobId;

    @NotNull(message = "startedAt is required")
    @PastOrPresent(message = "startedAt cannot be in the future")
    private Instant startedAt;

    @NotNull(message = "completedAt is required")
    @PastOrPresent(message = "completedAt cannot be in the future")
    private Instant completedAt;

    @NotBlank(message = "status is required")
    private String status;

    private Object metadata;
}