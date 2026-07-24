from typing import Dict, Optional
from pydantic import BaseModel, Field


class PredictionRequest(BaseModel):
    duration_ms: float = Field(..., description="Job duration or elapsed time in milliseconds", ge=0)
    record_count: int = Field(default=1000, description="Number of records processed by the job", ge=0)
    warning_threshold_ms: float = Field(default=180000.0, description="Warning SLA threshold in ms", gt=0)
    critical_threshold_ms: float = Field(default=300000.0, description="Critical SLA threshold in ms", gt=0)
    hour_of_day: Optional[int] = Field(default=None, description="Hour of day (0-23)", ge=0, le=23)
    day_of_week: Optional[int] = Field(default=None, description="Day of week (0=Monday, 6=Sunday)", ge=0, le=6)


class PredictionResponse(BaseModel):
    breach_risk_score: float = Field(..., description="Probability of SLA breach (0.0 to 1.0)")
    risk_level: str = Field(..., description="SLA Breach Risk Level: LOW, MEDIUM, HIGH")
    is_predicted_breach: bool = Field(..., description="Boolean flag if risk is high")
    feature_importances: Dict[str, float] = Field(default_factory=dict, description="Model feature importances")


class TrainResponse(BaseModel):
    status: str
    sample_count: int
    accuracy: float
    trained_at: str
