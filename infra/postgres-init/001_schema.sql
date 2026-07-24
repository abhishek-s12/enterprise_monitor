-- Schema for Enterprise Operations Anomaly & SLA Monitoring System

CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    tenant_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'OPERATOR',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sla_thresholds (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    job_type VARCHAR(100) NOT NULL,
    warning_threshold_ms BIGINT NOT NULL,
    critical_threshold_ms BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_job_type UNIQUE (tenant_id, job_type)
);

-- Seed Initial Demo Data
INSERT INTO tenants (tenant_code, name, active)
VALUES ('ACME', 'ACME Corporation', true)
ON CONFLICT (tenant_code) DO NOTHING;

INSERT INTO user_profiles (tenant_id, username, email, role)
SELECT id, 'admin_acme', 'admin@acme.com', 'ADMIN'
FROM tenants WHERE tenant_code = 'ACME'
ON CONFLICT DO NOTHING;

INSERT INTO sla_thresholds (tenant_id, job_type, warning_threshold_ms, critical_threshold_ms)
SELECT id, 'PAYMENT_BATCH', 180000, 300000
FROM tenants WHERE tenant_code = 'ACME'
ON CONFLICT DO NOTHING;
