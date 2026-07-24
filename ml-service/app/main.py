import os
import secrets
from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, HTTPException, Security, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security.api_key import APIKeyHeader

from app.model_pipeline import SlaBreachModel
from app.schemas import PredictionRequest, PredictionResponse, TrainResponse


# ---- Model Service -------------------------------------------------------
model_service = SlaBreachModel()


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load persisted model on startup; train fresh if none exists."""
    if not model_service.load_model():
        print("No serialized model found. Training initial SLA breach model...")
        model_service.train()
        print("Initial SLA breach model trained and persisted successfully.")
    yield


# ---- App -----------------------------------------------------------------
app = FastAPI(
    title="Enterprise Ops SLA Breach Risk ML Service",
    description="ML service hosting a Scikit-Learn model to predict SLA breach risk.",
    version="0.1.0",
    lifespan=lifespan,
)

# ✅ FIX 2: CORS restricted to configured dashboard origin only.
_ALLOWED_ORIGIN = os.getenv("CORS_ALLOWED_ORIGIN", "http://localhost:4200")
app.add_middleware(
    CORSMiddleware,
    allow_origins=[_ALLOWED_ORIGIN],
    allow_credentials=False,        # was True — now correctly False
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["Content-Type", "Accept", "X-API-Key"],
)

# ✅ FIX 3: API Key authentication via X-API-Key header.
_API_KEY_NAME = "X-API-Key"
_api_key_header = APIKeyHeader(name=_API_KEY_NAME, auto_error=False)
_EXPECTED_API_KEY = os.getenv("API_KEY", "")


async def verify_api_key(api_key: str = Security(_api_key_header)) -> str:
    """Dependency: validates the X-API-Key header using constant-time comparison."""
    if not _EXPECTED_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="API_KEY environment variable is not configured.",
        )
    provided = api_key or ""
    if not secrets.compare_digest(provided, _EXPECTED_API_KEY):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing or invalid X-API-Key header",
        )
    return api_key


# ---- Routes --------------------------------------------------------------

@app.get("/health", status_code=status.HTTP_200_OK)
def health_check():
    """Public endpoint — no API key required. Used by Docker healthcheck."""
    return {
        "status": "UP",
        "service": "ml-service",
        "model_loaded": model_service.pipeline is not None,
        "model_accuracy": round(model_service.accuracy, 4),
        "last_trained_at": model_service.last_trained_at,
    }


@app.post(
    "/predict",
    response_model=PredictionResponse,
    status_code=status.HTTP_200_OK,
    dependencies=[Depends(verify_api_key)],
)
def predict_breach_risk(request: PredictionRequest):
    """✅ FIX 3: Protected — requires valid X-API-Key header."""
    try:
        return model_service.predict(request)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Prediction error: {str(e)}",
        )


@app.post(
    "/train",
    response_model=TrainResponse,
    status_code=status.HTTP_200_OK,
    dependencies=[Depends(verify_api_key)],
)
def train_model():
    """✅ FIX 3: Protected — requires valid X-API-Key header."""
    try:
        return model_service.train()
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Model training error: {str(e)}",
        )
