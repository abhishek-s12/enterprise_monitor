import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SlaMonitorService } from '../../services/sla-monitor.service';
import { PredictionRequest, PredictionResponse } from '../../models/sla-models';

@Component({
  selector: 'app-ml-predictor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="ml-grid">
      <!-- Prediction Form Panel -->
      <div class="glass-panel form-panel">
        <div class="panel-header">
          <div>
            <h2><i class="fa-solid fa-brain"></i> ML SLA Breach Risk Predictor</h2>
            <p class="subtitle">FastAPI Scikit-Learn Random Forest Classifier (Port 8000)</p>
          </div>
          <button class="btn-secondary btn-sm" (click)="retrainModel()" [disabled]="isRetraining">
            <i class="fa-solid fa-sync" [ngClass]="{'fa-spin': isRetraining}"></i>
            {{ isRetraining ? 'Retraining...' : 'Retrain Model' }}
          </button>
        </div>

        <form (ngSubmit)="predictRisk()">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Job Duration (ms)</label>
              <input type="number" class="form-control" [(ngModel)]="request.duration_ms" name="duration_ms" required/>
            </div>
            <div class="form-group">
              <label class="form-label">Record Count</label>
              <input type="number" class="form-control" [(ngModel)]="request.record_count" name="record_count" required/>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Warning Threshold (ms)</label>
              <input type="number" class="form-control" [(ngModel)]="request.warning_threshold_ms" name="warning_threshold_ms" required/>
            </div>
            <div class="form-group">
              <label class="form-label">Critical Threshold (ms)</label>
              <input type="number" class="form-control" [(ngModel)]="request.critical_threshold_ms" name="critical_threshold_ms" required/>
            </div>
          </div>

          <button type="submit" class="btn-primary w-full" [disabled]="isLoading">
            <i class="fa-solid fa-wand-magic-sparkles"></i> Predict SLA Breach Risk
          </button>
        </form>
      </div>

      <!-- Result Score Meter & Feature Importance Panel -->
      <div class="glass-panel result-panel" *ngIf="prediction">
        <div class="score-header">
          <span class="score-label">SLA Breach Risk Score</span>
          <span class="badge" [ngClass]="{
            'badge-low': prediction.risk_level === 'LOW',
            'badge-medium': prediction.risk_level === 'MEDIUM',
            'badge-high': prediction.risk_level === 'HIGH'
          }">
            {{ prediction.risk_level }} RISK
          </span>
        </div>

        <div class="score-display">
          <span class="score-value">{{ (prediction.breach_risk_score * 100).toFixed(1) }}%</span>
          <div class="progress-bar-bg">
            <div class="progress-bar-fill" [style.width.%]="prediction.breach_risk_score * 100" [ngClass]="{
              'fill-low': prediction.risk_level === 'LOW',
              'fill-medium': prediction.risk_level === 'MEDIUM',
              'fill-high': prediction.risk_level === 'HIGH'
            }"></div>
          </div>
        </div>

        <div class="importance-section">
          <h4><i class="fa-solid fa-chart-bar"></i> Feature Importances Breakdown</h4>
          <div class="feature-row" *ngFor="let item of featureList">
            <div class="feature-info">
              <span class="feature-name mono">{{ item.key }}</span>
              <span class="feature-val mono">{{ (item.value * 100).toFixed(1) }}%</span>
            </div>
            <div class="feature-bar-bg">
              <div class="feature-bar-fill" [style.width.%]="item.value * 100"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .ml-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); gap: 1.5rem; }
    .form-panel, .result-panel { padding: 1.75rem; }
    .panel-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
    .panel-header h2 { font-size: 1.25rem; font-weight: 700; display: flex; align-items: center; gap: 0.6rem; }
    .subtitle { font-size: 0.8rem; color: var(--text-muted); margin-top: 0.2rem; }
    
    .form-row { display: flex; gap: 1rem; margin-bottom: 1.2rem; }
    .form-group { flex: 1; }
    .w-full { width: 100%; margin-top: 0.5rem; }
    .btn-sm { padding: 0.4rem 0.8rem; font-size: 0.8rem; }

    .score-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .score-label { font-size: 0.9rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; }
    .score-display { margin-bottom: 1.75rem; text-align: center; }
    .score-value { font-size: 3rem; font-weight: 800; display: block; margin-bottom: 0.5rem; }
    
    .progress-bar-bg { width: 100%; height: 14px; background: rgba(255, 255, 255, 0.08); border-radius: 999px; overflow: hidden; }
    .progress-bar-fill { height: 100%; transition: width 0.6s ease; }
    .fill-low { background: linear-gradient(90deg, #00DFA2, #4FACFE); }
    .fill-medium { background: linear-gradient(90deg, #FFB800, #FF7B00); }
    .fill-high { background: linear-gradient(90deg, #FF0080, #FF3B30); }

    .importance-section h4 { font-size: 0.95rem; font-weight: 600; margin-bottom: 1rem; display: flex; align-items: center; gap: 0.5rem; }
    .feature-row { margin-bottom: 0.8rem; }
    .feature-info { display: flex; justify-content: space-between; font-size: 0.8rem; margin-bottom: 0.3rem; }
    .feature-name { color: var(--text-main); }
    .feature-val { color: var(--primary-cyan); font-weight: 600; }
    .feature-bar-bg { width: 100%; height: 6px; background: rgba(255, 255, 255, 0.06); border-radius: 999px; overflow: hidden; }
    .feature-bar-fill { height: 100%; background: var(--primary-cyan); }
  `]
})
export class MlPredictorComponent implements OnInit {
  request: PredictionRequest = {
    duration_ms: 350000,
    record_count: 15000,
    warning_threshold_ms: 180000,
    critical_threshold_ms: 300000
  };

  prediction: PredictionResponse | null = null;
  featureList: { key: string; value: number }[] = [];
  isLoading = false;
  isRetraining = false;

  constructor(private service: SlaMonitorService) {}

  ngOnInit(): void {
    this.predictRisk();
  }

  predictRisk(): void {
    this.isLoading = true;
    this.service.predictRisk(this.request).subscribe({
      next: (res) => {
        this.prediction = res;
        this.featureList = Object.entries(res.feature_importances).map(([key, value]) => ({ key, value }));
        this.isLoading = false;
      },
      error: (err) => {
        console.error('ML Prediction error:', err);
        this.isLoading = false;
      }
    });
  }

  retrainModel(): void {
    this.isRetraining = true;
    this.service.retrainModel().subscribe({
      next: () => {
        this.isRetraining = false;
        this.predictRisk();
      },
      error: () => this.isRetraining = false
    });
  }
}
