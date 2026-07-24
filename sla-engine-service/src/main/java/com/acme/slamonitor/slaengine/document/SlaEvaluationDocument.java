package com.acme.slamonitor.slaengine.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Audit record stored in MongoDB for each SLA breach evaluation event.
 * Allows fast lookup of warning/breach history for operational dashboards.
 */
@Document(collection = "sla_evaluations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaEvaluationDocument {

    @Id
    private String id;

    @Indexed
    private String tenantCode;

    @Indexed
    private String jobType;

    private String jobId;

    private Long durationMs;

    /** SLA status: NORMAL, WARNING, BREACH */
    @Indexed
    private String status;

    private Long warningThresholdMs;

    private Long criticalThresholdMs;

    @Indexed
    @CreatedDate
    private Instant evaluatedAt;
}
