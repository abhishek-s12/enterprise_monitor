package com.acme.slamonitor.ingestion.exception;

public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(String tenantCode) {
        super("No active tenant found for tenantCode='" + tenantCode + "'");
    }
}