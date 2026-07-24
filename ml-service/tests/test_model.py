import os
import pytest
from app.model_pipeline import SlaBreachModel
from app.schemas import PredictionRequest


@pytest.fixture
def temp_model_path(tmp_path):
    return str(tmp_path / "test_model.joblib")


def test_synthetic_data_generation():
    model = SlaBreachModel()
    df = model.generate_synthetic_data(samples=100)
    assert len(df) == 100
    assert "duration_ms" in df.columns
    assert "breach" in df.columns


def test_train_and_predict(temp_model_path):
    model = SlaBreachModel(model_path=temp_model_path)
    train_res = model.train()
    
    assert train_res.status == "SUCCESS"
    assert train_res.accuracy > 0.70
    assert os.path.exists(temp_model_path)

    # Low risk request
    low_req = PredictionRequest(
        duration_ms=50000.0,
        record_count=500,
        warning_threshold_ms=180000.0,
        critical_threshold_ms=300000.0,
    )
    low_res = model.predict(low_req)
    assert low_res.risk_level in ["LOW", "MEDIUM"]
    assert 0.0 <= low_res.breach_risk_score <= 1.0

    # High risk request (duration exceeds critical threshold)
    high_req = PredictionRequest(
        duration_ms=350000.0,
        record_count=15000,
        warning_threshold_ms=180000.0,
        critical_threshold_ms=300000.0,
    )
    high_res = model.predict(high_req)
    assert high_res.risk_level == "HIGH"
    assert high_res.breach_risk_score >= 0.70
    assert high_res.is_predicted_breach is True


def test_save_and_load_model(temp_model_path):
    model1 = SlaBreachModel(model_path=temp_model_path)
    model1.train()

    model2 = SlaBreachModel(model_path=temp_model_path)
    loaded = model2.load_model()
    assert loaded is True
    assert model2.pipeline is not None
    assert model2.accuracy == model1.accuracy
