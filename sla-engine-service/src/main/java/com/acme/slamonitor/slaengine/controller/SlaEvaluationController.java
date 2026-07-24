package com.acme.slamonitor.slaengine.controller;

import com.acme.slamonitor.slaengine.dto.EvaluationRequest;
import com.acme.slamonitor.slaengine.dto.EvaluationResponse;
import com.acme.slamonitor.slaengine.service.SlaEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/evaluations")
@RequiredArgsConstructor
public class SlaEvaluationController {

    private final SlaEvaluationService slaEvaluationService;

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluationResponse> evaluate(@Valid @RequestBody EvaluationRequest request) {
        EvaluationResponse response = slaEvaluationService.evaluate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EvaluationResponse>> findEvaluations(
            @RequestParam String tenantCode,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        Page<EvaluationResponse> page = slaEvaluationService.findEvaluations(tenantCode, status, pageable);
        return ResponseEntity.ok(page);
    }
}
