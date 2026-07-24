# Enterprise Operations Anomaly & SLA Monitoring System

An operational monitoring engine that ingests high-volume transactional logs,
detects SLA breach risk with an ML model, and exposes microservice endpoints
for operational dashboards.

## Architecture

```
                     ┌───────────────────────┐
   client systems ─▶ │   Ingestion Service    │──▶ MongoDB (operation_logs)
                     │   (Spring Boot, 8081)  │
                     └──────────┬────────────┘
                                │ reads thresholds
                                ▼
                     ┌───────────────────────┐
                     │      PostgreSQL        │  tenants, user_profiles,
                     │                        │  sla_thresholds
                     └──────────┬────────────┘
                                │
                     ┌──────────▼────────────┐      ┌────────────────────┐
                     │   SLA Engine Service   │◀────▶│   ML Service        │
                     │   (Spring Boot)        │      │ (FastAPI + sklearn) │
                     └──────────┬────────────┘      └────────────────────┘
                                │ breach events
                                ▼
                     ┌───────────────────────┐
                     │  Alerting Service      │
                     │  (Spring Boot)         │
                     └──────────┬────────────┘
                                │
                                ▼
                     ┌───────────────────────┐
                     │  Angular Admin Dash    │
                     └───────────────────────┘
```

## Build Status

| Component            | Status                                   |
|-----------------------|-------------------------------------------|
| Infra (docker-compose)| ✅ Phase 1                                 |
| Postgres schema       | ✅ Phase 1                                 |
| Ingestion Service      | ✅ Phase 1 — build/test/run instructions below |
| SLA Engine Service     | ⏳ Phase 2                                 |
| ML Service (FastAPI)   | ⏳ Phase 3                                 |
| Alerting Service       | ⏳ Phase 4                                 |
| Angular Admin Dashboard| ⏳ Phase 5                                 |
| Postman collection     | 🚧 Growing each phase — see `docs/postman_collection.json` |

## Tech Stack

- **Java 17 / Spring Boot 3.3** — Ingestion Service, SLA Engine, Alerting Service
- **PostgreSQL 16** — tenants, user profiles, SLA thresholds (relational)
- **MongoDB 7** — high-throughput unstructured operation log stream
- **Python / FastAPI + scikit-learn** — SLA breach-risk prediction
- **Angular** — admin dashboard
- **JUnit 5 + Mockito** — unit/slice testing, target >85% line coverage (enforced via JaCoCo)

## Running Phase 1 locally

### 1. Start infrastructure + ingestion service

```bash
docker compose up --build
```

This starts:
- Postgres on `5432` (auto-seeded with a demo `ACME` tenant + `PAYMENT_BATCH` SLA threshold)
- MongoDB on `27017`
- Ingestion Service on `8081`

### 2. Try it

```bash
curl -X POST http://localhost:8081/api/v1/logs \
  -H "Content-Type: application/json" \
  -d '{
    "tenantCode": "ACME",
    "jobType": "PAYMENT_BATCH",
    "jobId": "job-12345",
    "startedAt": "2026-07-24T08:00:00Z",
    "completedAt": "2026-07-24T08:04:30Z",
    "status": "SUCCESS",
    "metadata": { "recordCount": 5400 }
  }'

curl "http://localhost:8081/api/v1/logs?tenantCode=ACME"
```

Or import `docs/postman_collection.json` into Postman.

### 3. Run tests + coverage (inside `ingestion-service/`)

```bash
mvn clean test
# HTML coverage report: target/site/jacoco/index.html
```

The build enforces >85% line coverage via the JaCoCo Maven plugin
(`jacoco-check` execution bound to the `test` phase).

## Repository Layout

```
enterprise-ops-sla-monitor/
├── docker-compose.yml
├── infra/
│   └── postgres-init/001_schema.sql      # tenants, user_profiles, sla_thresholds
├── ingestion-service/                    # Spring Boot — Phase 1 (complete)
│   ├── src/main/java/com/acme/slamonitor/ingestion/
│   │   ├── controller/   OperationLogController
│   │   ├── service/      OperationLogService(+Impl)
│   │   ├── repository/   TenantRepository (JPA), OperationLogRepository (Mongo)
│   │   ├── entity/       Tenant (JPA)
│   │   ├── document/     OperationLogDocument (Mongo)
│   │   ├── dto/          OperationLogRequest/Response
│   │   ├── mapper/       OperationLogMapper
│   │   └── exception/    GlobalExceptionHandler, TenantNotFoundException
│   └── src/test/java/...                 # JUnit5 + Mockito + MockMvc tests
├── sla-engine-service/                   # Phase 2
├── ml-service/                           # Phase 3
├── alerting-service/                     # Phase 4
├── admin-dashboard/                      # Phase 5
└── docs/postman_collection.json
```

## API Reference (Ingestion Service, Phase 1)

| Method | Path                                             | Description                              |
|--------|---------------------------------------------------|-------------------------------------------|
| POST   | `/api/v1/logs`                                    | Ingest a single operation log entry       |
| GET    | `/api/v1/logs?tenantCode=&jobType=&page=&size=`   | Query logs, optionally filtered by job type |
| GET    | `/actuator/health`                                | Health check                              |

## Next Steps

Reply to continue with **Phase 2 (SLA Engine Service)**: it will read `sla_thresholds`
from Postgres, poll/consume new `operation_logs` from Mongo, compute breach/warning
status, and expose an alerts read API — laying the groundwork for the ML risk model
in Phase 3.