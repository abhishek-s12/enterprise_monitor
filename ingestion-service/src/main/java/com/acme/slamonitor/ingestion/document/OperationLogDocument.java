package com.acme.slamonitor.ingestion.document;

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
 * Raw operational log entry for a single job/task execution, as reported
 * by a client system. Stored in MongoDB for high write throughput; the
 * SLA Engine service (Phase 2) consumes these to evaluate breach risk.
 */
@Document(collection = "operation_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDocument {

    @Id
    private String id;

    @Indexed
    private String tenantCode;

    @Indexed
    private String jobType;

    private String jobId;

    /** ISO-8601 timestamp the job started, as reported by the client system. */
    private Instant startedAt;

    /** ISO-8601 timestamp the job completed, as reported by the client system. */
    private Instant completedAt;

    /** Duration in milliseconds; derived if not supplied directly. */
    private Long durationMs;

    /** SUCCESS, FAILURE, TIMEOUT, PARTIAL */
    private String status;

    /** Free-form metadata from the source system (record counts, error codes, etc). */
    private Object metadata;

    @Indexed
    @CreatedDate
    private Instant ingestedAt;
}