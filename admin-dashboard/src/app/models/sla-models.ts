export interface OperationLog {
  id?: string;
  tenantCode: string;
  jobType: string;
  jobId: string;
  startedAt: string;
  completedAt: string;
  durationMs?: number;
  status: string;
  metadata?: Record<string, any>;
  createdAt?: string;
}

export interface EvaluationRequest {
  tenantCode: string;
  jobType: string;
  jobId: string;
  durationMs: number;
}

export interface EvaluationResponse {
  id: string;
  tenantCode: string;
  jobType: string;
  jobId: string;
  durationMs: number;
  warningThresholdMs: number;
  criticalThresholdMs: number;
  status: 'NORMAL' | 'WARNING' | 'BREACH';
  evaluatedAt: string;
}

export interface PredictionRequest {
  duration_ms: number;
  record_count: number;
  warning_threshold_ms: number;
  critical_threshold_ms: number;
  hour_of_day?: number;
  day_of_week?: number;
}

export interface PredictionResponse {
  breach_risk_score: number;
  risk_level: 'LOW' | 'MEDIUM' | 'HIGH';
  is_predicted_breach: boolean;
  feature_importances: Record<string, number>;
}

export interface AlertDispatchRequest {
  tenantCode: string;
  jobType: string;
  jobId: string;
  severity: 'WARNING' | 'BREACH';
  durationMs: number;
  message: string;
}

export interface AlertNotificationResponse {
  id: string;
  tenantCode: string;
  jobType: string;
  jobId: string;
  severity: string;
  channel: string;
  recipient: string;
  message: string;
  durationMs: number;
  status: string;
  sentAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
