import pytest
from fastapi.testclient import TestClient
from app.main import app


@pytest.fixture
def client():
    with TestClient(app) as c:
        yield c


def test_health_check(client):
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"
    assert data["service"] == "ml-service"
    assert data["model_loaded"] is True


def test_predict_endpoint_low_risk(client):
    payload = {
        "duration_ms": 60000.0,
        "record_count": 1000,
        "warning_threshold_ms": 180000.0,
        "critical_threshold_ms": 300000.0,
    }
    response = client.post("/predict", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "breach_risk_score" in data
    assert "risk_level" in data
    assert "feature_importances" in data
    assert data["risk_level"] in ["LOW", "MEDIUM"]


def test_predict_endpoint_high_risk(client):
    payload = {
        "duration_ms": 360000.0,
        "record_count": 20000,
        "warning_threshold_ms": 180000.0,
        "critical_threshold_ms": 300000.0,
    }
    response = client.post("/predict", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["risk_level"] == "HIGH"
    assert data["is_predicted_breach"] is True


def test_train_endpoint(client):
    response = client.post("/train")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "SUCCESS"
    assert data["sample_count"] > 0
    assert data["accuracy"] > 0.70
