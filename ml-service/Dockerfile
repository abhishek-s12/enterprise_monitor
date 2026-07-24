FROM python:3.11-slim

WORKDIR /app

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# ✅ FIX 5: Create mount point for the named volume that persists the ML model
RUN mkdir -p /app/models

# ✅ Create non-root system user/group and assign directory rights
RUN groupadd -g 10001 appgroup && \
    useradd -u 10001 -g appgroup -r -m appuser && \
    chown -R appuser:appgroup /app /app/models

COPY --chown=appuser:appgroup . .

# ✅ Run as non-root user
USER appuser

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
