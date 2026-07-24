package com.acme.slamonitor.slaengine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sla_thresholds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlaThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "job_type", nullable = false, length = 100)
    private String jobType;

    @Column(name = "warning_threshold_ms", nullable = false)
    private Long warningThresholdMs;

    @Column(name = "critical_threshold_ms", nullable = false)
    private Long criticalThresholdMs;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
