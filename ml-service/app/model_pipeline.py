from datetime import datetime, timezone
import os
import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

from app.schemas import PredictionRequest, PredictionResponse, TrainResponse


FEATURE_NAMES = [
    "duration_ratio",
    "duration_warning_ratio",
    "record_count",
    "processing_rate",
    "hour_of_day",
    "day_of_week",
]


class SlaBreachModel:
    def __init__(self, model_path: str = "/app/models/model.joblib"):
        # ✅ FIX 5: Path points to named Docker volume — survives container restarts.
        self.model_path = model_path
        self.pipeline: Pipeline | None = None
        self.last_trained_at: str | None = None
        self.accuracy: float = 0.0

    def generate_synthetic_data(self, samples: int = 2500, random_state: int = 42) -> pd.DataFrame:
        np.random.seed(random_state)

        critical_thresholds = np.random.choice([300000.0, 600000.0, 900000.0], size=samples)
        warning_thresholds = critical_thresholds * 0.6

        # Generate realistic durations centered around warning & critical thresholds
        durations = np.random.uniform(low=10000.0, high=critical_thresholds * 1.3, size=samples)
        record_counts = np.random.randint(low=100, high=25000, size=samples)
        hours = np.random.randint(low=0, high=24, size=samples)
        days = np.random.randint(low=0, high=7, size=samples)

        # Ground truth rule with noise: breach if duration >= critical threshold or high duration + high records
        prob_breach = (
            (durations >= critical_thresholds).astype(float) * 0.85
            + (durations >= warning_thresholds).astype(float) * 0.15
            + (record_counts > 15000).astype(float) * 0.10
        )
        prob_breach = np.clip(prob_breach, 0.0, 1.0)
        breach = (np.random.binomial(n=1, p=prob_breach) == 1).astype(int)

        df = pd.DataFrame({
            "duration_ms": durations,
            "record_count": record_counts,
            "warning_threshold_ms": warning_thresholds,
            "critical_threshold_ms": critical_thresholds,
            "hour_of_day": hours,
            "day_of_week": days,
            "breach": breach,
        })
        return df

    def extract_features(self, df: pd.DataFrame) -> pd.DataFrame:
        features = pd.DataFrame()
        features["duration_ratio"] = df["duration_ms"] / df["critical_threshold_ms"]
        features["duration_warning_ratio"] = df["duration_ms"] / df["warning_threshold_ms"]
        features["record_count"] = df["record_count"]
        
        duration_sec = (df["duration_ms"] / 1000.0) + 1.0
        features["processing_rate"] = df["record_count"] / duration_sec
        features["hour_of_day"] = df["hour_of_day"]
        features["day_of_week"] = df["day_of_week"]
        return features[FEATURE_NAMES]

    def train(self, df: pd.DataFrame | None = None) -> TrainResponse:
        if df is None:
            df = self.generate_synthetic_data()

        X = self.extract_features(df)
        y = df["breach"]

        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

        self.pipeline = Pipeline([
            ("scaler", StandardScaler()),
            ("classifier", RandomForestClassifier(n_estimators=100, max_depth=8, random_state=42)),
        ])

        self.pipeline.fit(X_train, y_train)
        self.accuracy = float(self.pipeline.score(X_test, y_test))
        self.last_trained_at = datetime.now(timezone.utc).isoformat()

        self.save_model()

        return TrainResponse(
            status="SUCCESS",
            sample_count=len(df),
            accuracy=round(self.accuracy, 4),
            trained_at=self.last_trained_at,
        )

    def save_model(self, path: str | None = None) -> None:
        save_path = path or self.model_path
        if self.pipeline is not None:
            payload = {
                "pipeline": self.pipeline,
                "accuracy": self.accuracy,
                "last_trained_at": self.last_trained_at,
            }
            joblib.dump(payload, save_path)

    def load_model(self, path: str | None = None) -> bool:
        load_path = path or self.model_path
        if os.path.exists(load_path):
            try:
                payload = joblib.load(load_path)
                self.pipeline = payload["pipeline"]
                self.accuracy = payload.get("accuracy", 0.0)
                self.last_trained_at = payload.get("last_trained_at")
                return True
            except Exception:
                return False
        return False

    def predict(self, req: PredictionRequest) -> PredictionResponse:
        if self.pipeline is None:
            if not self.load_model():
                self.train()

        now = datetime.now(timezone.utc)
        hour = req.hour_of_day if req.hour_of_day is not None else now.hour
        day = req.day_of_week if req.day_of_week is not None else now.weekday()

        single_df = pd.DataFrame([{
            "duration_ms": req.duration_ms,
            "record_count": req.record_count,
            "warning_threshold_ms": req.warning_threshold_ms,
            "critical_threshold_ms": req.critical_threshold_ms,
            "hour_of_day": hour,
            "day_of_week": day,
        }])

        X = self.extract_features(single_df)
        prob_breach = float(self.pipeline.predict_proba(X)[0][1])

        if prob_breach >= 0.70:
            risk_level = "HIGH"
        elif prob_breach >= 0.30:
            risk_level = "MEDIUM"
        else:
            risk_level = "LOW"

        clf = self.pipeline.named_steps["classifier"]
        importances = clf.feature_importances_
        feature_importance_dict = {
            name: round(float(imp), 4) for name, imp in zip(FEATURE_NAMES, importances)
        }

        return PredictionResponse(
            breach_risk_score=round(prob_breach, 4),
            risk_level=risk_level,
            is_predicted_breach=(risk_level == "HIGH"),
            feature_importances=feature_importance_dict,
        )
