import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  OperationLog,
  EvaluationRequest,
  EvaluationResponse,
  PredictionRequest,
  PredictionResponse,
  AlertDispatchRequest,
  AlertNotificationResponse,
  PageResponse,
} from '../models/sla-models';

@Injectable({
  providedIn: 'root'
})
export class SlaMonitorService {

  private ingestionUrl = 'http://localhost:8081';
  private slaEngineUrl = 'http://localhost:8082';
  private mlServiceUrl = 'http://localhost:8000';
  private alertingUrl = 'http://localhost:8083';

  constructor(private http: HttpClient) {}

  // Ingestion Service
  ingestLog(log: OperationLog): Observable<OperationLog> {
    return this.http.post<OperationLog>(`${this.ingestionUrl}/api/v1/logs`, log);
  }

  getLogs(tenantCode: string, jobType?: string, page: number = 0, size: number = 20): Observable<PageResponse<OperationLog>> {
    let params = new HttpParams()
      .set('tenantCode', tenantCode)
      .set('page', page.toString())
      .set('size', size.toString());
    if (jobType) {
      params = params.set('jobType', jobType);
    }
    return this.http.get<PageResponse<OperationLog>>(`${this.ingestionUrl}/api/v1/logs`, { params });
  }

  // SLA Engine Service
  evaluateSla(request: EvaluationRequest): Observable<EvaluationResponse> {
    return this.http.post<EvaluationResponse>(`${this.slaEngineUrl}/api/v1/evaluations/evaluate`, request);
  }

  getEvaluations(tenantCode: string, status?: string, page: number = 0, size: number = 20): Observable<PageResponse<EvaluationResponse>> {
    let params = new HttpParams()
      .set('tenantCode', tenantCode)
      .set('page', page.toString())
      .set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PageResponse<EvaluationResponse>>(`${this.slaEngineUrl}/api/v1/evaluations`, { params });
  }

  // ML Service
  predictRisk(request: PredictionRequest): Observable<PredictionResponse> {
    return this.http.post<PredictionResponse>(`${this.mlServiceUrl}/predict`, request);
  }

  retrainModel(): Observable<any> {
    return this.http.post(`${this.mlServiceUrl}/train`, {});
  }

  getMlHealth(): Observable<any> {
    return this.http.get(`${this.mlServiceUrl}/health`);
  }

  // Alerting Service
  dispatchAlert(request: AlertDispatchRequest): Observable<AlertNotificationResponse[]> {
    return this.http.post<AlertNotificationResponse[]>(`${this.alertingUrl}/api/v1/alerts/dispatch`, request);
  }

  getAlerts(tenantCode: string, severity?: string, page: number = 0, size: number = 20): Observable<PageResponse<AlertNotificationResponse>> {
    let params = new HttpParams()
      .set('tenantCode', tenantCode)
      .set('page', page.toString())
      .set('size', size.toString());
    if (severity) {
      params = params.set('severity', severity);
    }
    return this.http.get<PageResponse<AlertNotificationResponse>>(`${this.alertingUrl}/api/v1/alerts`, { params });
  }
}
