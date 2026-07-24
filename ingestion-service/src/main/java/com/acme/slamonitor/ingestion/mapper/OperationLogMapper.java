package com.acme.slamonitor.ingestion.mapper;

import com.acme.slamonitor.ingestion.document.OperationLogDocument;
import com.acme.slamonitor.ingestion.dto.OperationLogRequest;
import com.acme.slamonitor.ingestion.dto.OperationLogResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class OperationLogMapper {

    public OperationLogDocument toDocument(OperationLogRequest request) {
        long durationMs = Duration.between(request.getStartedAt(), request.getCompletedAt()).toMillis();

        return OperationLogDocument.builder()
                .tenantCode(request.getTenantCode())
                .jobType(request.getJobType())
                .jobId(request.getJobId())
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .durationMs(durationMs)
                .status(request.getStatus())
                .metadata(request.getMetadata())
                .build();
    }

    public OperationLogResponse toResponse(OperationLogDocument document) {
        return OperationLogResponse.builder()
                .id(document.getId())
                .tenantCode(document.getTenantCode())
                .jobType(document.getJobType())
                .jobId(document.getJobId())
                .startedAt(document.getStartedAt())
                .completedAt(document.getCompletedAt())
                .durationMs(document.getDurationMs())
                .status(document.getStatus())
                .ingestedAt(document.getIngestedAt())
                .build();
    }
}