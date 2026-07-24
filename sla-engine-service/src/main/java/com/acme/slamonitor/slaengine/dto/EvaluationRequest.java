package com.acme.slamonitor.slaengine.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequest {

    @NotBlank(message = "tenantCode is required")
    private String tenantCode;

    @NotBlank(message = "jobType is required")
    private String jobType;

    @NotBlank(message = "jobId is required")
    private String jobId;

    @NotNull(message = "durationMs is required")
    @Min(value = 0, message = "durationMs cannot be negative")
    private Long durationMs;
}
