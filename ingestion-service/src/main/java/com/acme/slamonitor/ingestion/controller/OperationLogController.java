package com.acme.slamonitor.ingestion.controller;

import com.acme.slamonitor.ingestion.dto.OperationLogRequest;
import com.acme.slamonitor.ingestion.dto.OperationLogResponse;
import com.acme.slamonitor.ingestion.service.OperationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @PostMapping
    public ResponseEntity<OperationLogResponse> ingest(@Valid @RequestBody OperationLogRequest request) {
        OperationLogResponse response = operationLogService.ingest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<OperationLogResponse>> findLogs(
            @RequestParam String tenantCode,
            @RequestParam(required = false) String jobType,
            Pageable pageable) {

        Page<OperationLogResponse> page = (jobType == null || jobType.isBlank())
                ? operationLogService.findByTenant(tenantCode, pageable)
                : operationLogService.findByTenantAndJobType(tenantCode, jobType, pageable);

        return ResponseEntity.ok(page);
    }
}