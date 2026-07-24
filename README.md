# Enterprise Operations Anomaly & SLA Monitoring System

A production ready microservices ecosystem that ingests high volume operational logs, evaluates SLA thresholds, uses a Scikit-Learn machine learning pipeline to predict SLA breach probability, triggers multi-channel alerts, and displays real time glassmorphism visualizations on an Angular frontend dashboard.

---

## 🏗️ System Architecture

```
                                  ┌───────────────────────────┐
                                  │  Angular Admin Dashboard  │◀──┐
                                  │    (Nginx Host: 4200)     │   │
                                  └─────────────┬─────────────┘   │
                                                │                 │ HTTP / REST
                                                ▼                 │ (Authed via X-API-Key)
    ┌───────────────────────┐     ┌───────────────────────────┐   │
 ──▶│   Ingestion Service   │◀───▶│    SLA Engine Service     │◀──┘
    │  (Spring Boot: 8081)  │     │   (Spring Boot: 8082)     │
    └───────────┬───────────┘     └─────────────┬─────────────┘
                │                               │
                ▼ (Operation Logs)              ▼ (Evaluations)
        ┌───────────────┐               ┌───────────────┐
        │ MongoDB:27017 │               │ Postgres:5432 │ (Tenants, Thresholds, Profiles)
        └───────────────┘               └───────────────┘
                                                │
                                                ▼ (Breach events)
    ┌───────────────────────┐     ┌───────────────────────────┐
    │      ML Service       │◀───▶│     Alerting Service      │
    │    (FastAPI: 8000)    │     │   (Spring Boot: 8083)     │
    └───────────────────────┘     └───────────────────────────┘
```

---

## 🛠️ Build Status & Completed Phases

| Component | Status | Port (Host) | Description |
| :--- | :--- | :--- | :--- |
| **Ingestion Service** | Complete | `8081` | Spring Boot API that ingests logs to MongoDB. |
| **SLA Engine Service** | Complete | `8082` | Evaluates log durations against Postgres SLA thresholds. |
| **ML Service** | Complete | `8000` | FastAPI predictor estimating SLA breach probability. |
| **Alerting Service** | Complete | `8083` | Logs notifications (EMAIL/LOG) to database audits. |
| **Admin Dashboard** | Complete | `4200` | Glassmorphism dashboard visualizing streams and KPIs. |
| **Prometheus Telemetry**| Complete | `9090` | Scraping CPU/Memory/JVM stats (in `monitoring` profile). |
| **Grafana Dashboard** | Complete | `3000` | Pre-provisioned metrics display (in `monitoring` profile). |

---

## 🔒 Production Security Hardening

This ecosystem is fully hardened for a production deployment:
* **Secrets Management**: Plaintext credentials are fully parameterized. Values are loaded dynamically from the git ignored root `.env` file.
* **CORS Restrictions**: Standard wildcard `*` mappings are disabled. Services only permit connections originating from `CORS_ALLOWED_ORIGIN` (the dashboard at `http://localhost:4200`).
* **Microservice Authentication**: All non-public APIs require the `X-API-Key` header matching the key defined in the environmental variables.
* **Database Isolation**: Host mappings for PostgreSQL (`5432`) and MongoDB (`27017`) are closed. Databases reside exclusively within the isolated Docker bridge network.
* **Non-Root Execution**: Microservices execute inside their containers as a non-root system user (`appuser` with UID `10001`) to protect the host container runtime.

---

## 🚀 Getting Started

### 1. Environment Configurations
Copy `.env.example` to `.env` in the project root:
```bash
cp .env.example .env
```
*(Optionally change default database passwords and the global API Key in `.env` before running).*

### 2. Startup Commands

#### Default Profile (Application & Databases Only)
To launch the core microservices and databases:
```bash
docker compose up -d --build
```

#### Monitoring Profile (Optional Telemetry Stack)
To run Prometheus and Grafana telemetry side-by-side with the application stack:
```bash
docker compose --profile monitoring up -d
```
* **Prometheus**: Accessible at [http://localhost:9090/](http://localhost:9090/) (Targets: Ingestion, SLA Engine, Alerting).
* **Grafana**: Accessible at [http://localhost:3000/](http://localhost:3000/) (Pre-configured datasource + JVM Metrics dashboard, credentials: `admin` / `admin`).

---

## 🧪 Testing & Code Coverage

To run the automated test suites locally:

### Java Microservices
Run tests inside `ingestion-service/`, `sla-engine-service/`, or `alerting-service/`:
```bash
mvn clean test
```
* Coverage rules: **>85% line coverage** is strictly enforced via the JaCoCo plugin during maven test phases.
* View coverage reports at: `target/site/jacoco/index.html`.

### Python ML Service
Execute testing via pytest:
```bash
pip install -r requirements.txt
pytest
```

---

## 📡 API Reference

All requests must include the header `X-API-Key: <global_api_key>` (unless exempt, like `/actuator/health` or `/health`).

### Ingestion Service (`8081`)
* `POST /api/v1/logs` — Ingest a log payload.
* `GET /api/v1/logs?tenantCode=&jobType=&page=&size=` — Query paginated logs.
* `GET /actuator/health` — Public endpoint checking status.

### SLA Engine Service (`8082`)
* `POST /api/v1/evaluations` — Evaluates log durations and triggers breach predictions.
* `GET /api/v1/evaluations?tenantCode=&jobType=&page=&size=` — Retrieve evaluations.

### Alerting Service (`8083`)
* `POST /api/v1/alerts/dispatch` — Dispatch multi-channel notification payloads.
* `GET /api/v1/alerts?tenantCode=&severity=&page=&size=` — Retrieve paginated notifications feed.

### ML Service (`8000`)
* `POST /predict` — Estimating SLA breach probability.
* `POST /train` — Re-trains model on the MongoDB historical logs.
* `GET /health` — Public endpoint checking status.

---

## 📂 Repository Layout

```
enterprise-ops-sla-monitor/
├── docker-compose.yml
├── .env.example
├── infra/
│   ├── postgres-init/                    # Auto-seeds Postgres schemas/users
│   ├── prometheus/                       # Scraper configuration
│   └── grafana/                          # Provisioned datasources & dashboards
├── ingestion-service/                    # Ingestion Spring Boot microservice
├── sla-engine-service/                   # SLA verification Spring Boot microservice
├── alerting-service/                     # Dispatching and Auditing Spring Boot microservice
├── ml-service/                           # Python SLA breach probability predictor
├── admin-dashboard/                      # Angular Glassmorphism dashboard
└── docs/postman_collection.json
```
