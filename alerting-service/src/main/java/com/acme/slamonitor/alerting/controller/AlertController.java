package com.acme.slamonitor.alerting.controller;

import com.acme.slamonitor.alerting.dto.AlertDispatchRequest;
import com.acme.slamonitor.alerting.dto.AlertNotificationResponse;
import com.acme.slamonitor.alerting.service.AlertDispatcherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertDispatcherService alertDispatcherService;

    @PostMapping("/dispatch")
    public ResponseEntity<List<AlertNotificationResponse>> dispatchAlert(@Valid @RequestBody AlertDispatchRequest request) {
        List<AlertNotificationResponse> responses = alertDispatcherService.dispatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping
    public ResponseEntity<Page<AlertNotificationResponse>> findAlerts(
            @RequestParam String tenantCode,
            @RequestParam(required = false) String severity,
            Pageable pageable) {

        Page<AlertNotificationResponse> page = alertDispatcherService.findAlerts(tenantCode, severity, pageable);
        return ResponseEntity.ok(page);
    }
}
