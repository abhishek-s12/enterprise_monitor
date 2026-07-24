package com.acme.slamonitor.alerting.document;

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
 * Audit record of dispatched alert notifications stored in MongoDB collection `alert_notifications`.
 */
@Document(collection = "alert_notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertNotificationDocument {

    @Id
    private String id;

    @Indexed
    private String tenantCode;

    @Indexed
    private String jobType;

    private String jobId;

    /** WARNING, BREACH */
    @Indexed
    private String severity;

    /** LOG, EMAIL, WEBHOOK */
    private String channel;

    private String recipient;

    private String message;

    private Long durationMs;

    private String status;

    @Indexed
    @CreatedDate
    private Instant sentAt;
}
