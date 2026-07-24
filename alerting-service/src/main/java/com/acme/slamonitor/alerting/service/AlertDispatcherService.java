package com.acme.slamonitor.alerting.service;

import com.acme.slamonitor.alerting.dto.AlertDispatchRequest;
import com.acme.slamonitor.alerting.dto.AlertNotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AlertDispatcherService {
    List<AlertNotificationResponse> dispatch(AlertDispatchRequest request);
    Page<AlertNotificationResponse> findAlerts(String tenantCode, String severity, Pageable pageable);
}
