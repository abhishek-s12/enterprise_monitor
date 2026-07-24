package com.acme.slamonitor.alerting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertDispatchRequest {

    @NotBlank(message = "tenantCode is required")
    private String tenantCode;

    @NotBlank(message = "jobType is required")
    private String jobType;

    @NotBlank(message = "jobId is required")
    private String jobId;

    /** WARNING or BREACH */
    @NotBlank(message = "severity is required")
    private String severity;

    private Long durationMs;

    @NotBlank(message = "message is required")
    private String message;
}
