import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SlaMonitorService } from '../../services/sla-monitor.service';
import { EvaluationResponse, EvaluationRequest } from '../../models/sla-models';

@Component({
  selector: 'app-sla-engine',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="glass-panel main-container">
      <div class="panel-header">
        <div>
          <h2><i class="fa-solid fa-stopwatch-20"></i> SLA Breach Threshold Engine</h2>
          <p class="subtitle">Real-time evaluation against PostgreSQL SLA thresholds & audit persistence</p>
        </div>
        <button class="btn-primary" (click)="showModal = true">
          <i class="fa-solid fa-play"></i> Evaluate SLA
        </button>
      </div>

      <!-- Filters -->
      <div class="filter-bar">
        <div class="form-group">
          <label class="form-label">Tenant</label>
          <select class="form-control" [(ngModel)]="selectedTenant" (change)="loadEvaluations()">
            <option value="ACME">ACME Corp (ACME)</option>
            <option value="GLOBEX">Globex Corp (GLOBEX)</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Status Filter</label>
          <select class="form-control" [(ngModel)]="selectedStatus" (change)="loadEvaluations()">
            <option value="">All Statuses</option>
            <option value="NORMAL">NORMAL</option>
            <option value="WARNING">WARNING</option>
            <option value="BREACH">BREACH</option>
          </select>
        </div>
      </div>

      <!-- Table -->
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>Job ID</th>
              <th>Tenant</th>
              <th>Job Type</th>
              <th>Duration (ms)</th>
              <th>Warning Threshold</th>
              <th>Critical Threshold</th>
              <th>SLA Status</th>
              <th>Evaluated At</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let ev of evaluations">
              <td class="mono font-semibold">{{ ev.jobId }}</td>
              <td><span class="tenant-tag">{{ ev.tenantCode }}</span></td>
              <td>{{ ev.jobType }}</td>
              <td class="mono">{{ ev.durationMs | number }} ms</td>
              <td class="mono text-muted">{{ ev.warningThresholdMs | number }} ms</td>
              <td class="mono text-muted">{{ ev.criticalThresholdMs | number }} ms</td>
              <td>
                <span class="badge" [ngClass]="{
                  'badge-normal': ev.status === 'NORMAL',
                  'badge-warning': ev.status === 'WARNING',
                  'badge-breach': ev.status === 'BREACH'
                }">
                  {{ ev.status }}
                </span>
              </td>
              <td class="mono text-sm">{{ ev.evaluatedAt | date:'medium' }}</td>
            </tr>
            <tr *ngIf="evaluations.length === 0">
              <td colspan="8" class="empty-state">No SLA evaluations found.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Evaluation Modal -->
    <div class="modal-backdrop" *ngIf="showModal">
      <div class="glass-panel modal-card">
        <div class="modal-header">
          <h3>Trigger SLA Evaluation</h3>
          <button class="close-btn" (click)="showModal = false">&times;</button>
        </div>
        <form (ngSubmit)="submitEvaluation()">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Tenant Code</label>
              <select class="form-control" [(ngModel)]="newEval.tenantCode" name="tenantCode" required>
                <option value="ACME">ACME</option>
                <option value="GLOBEX">GLOBEX</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Job Type</label>
              <input type="text" class="form-control" [(ngModel)]="newEval.jobType" name="jobType" required/>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Job ID</label>
            <input type="text" class="form-control" [(ngModel)]="newEval.jobId" name="jobId" required/>
          </div>
          <div class="form-group">
            <label class="form-label">Duration (ms)</label>
            <input type="number" class="form-control" [(ngModel)]="newEval.durationMs" name="durationMs" required/>
            <small class="hint">Warning threshold: 180,000 ms | Critical threshold: 300,000 ms</small>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-secondary" (click)="showModal = false">Cancel</button>
            <button type="submit" class="btn-primary">Evaluate SLA</button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .main-container { padding: 1.75rem; }
    .panel-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; }
    .panel-header h2 { font-size: 1.3rem; font-weight: 700; display: flex; align-items: center; gap: 0.6rem; }
    .subtitle { font-size: 0.85rem; color: var(--text-muted); margin-top: 0.2rem; }
    
    .filter-bar { display: flex; gap: 1rem; margin-bottom: 1.5rem; }
    .form-group { flex: 1; }
    .tenant-tag { background: rgba(0, 242, 254, 0.1); color: var(--primary-cyan); padding: 0.2rem 0.5rem; border-radius: 6px; font-weight: 600; font-size: 0.8rem; }
    .empty-state { text-align: center; padding: 2rem; color: var(--text-muted); }
    .hint { display: block; font-size: 0.75rem; color: var(--text-muted); margin-top: 0.3rem; }

    .modal-backdrop {
      position: fixed; top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.7); backdrop-filter: blur(8px);
      display: flex; align-items: center; justify-content: center; z-index: 1000;
    }
    .modal-card { width: 100%; max-width: 520px; padding: 1.75rem; }
    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; }
    .close-btn { background: none; border: none; color: var(--text-muted); font-size: 1.5rem; cursor: pointer; }
    .form-row { display: flex; gap: 1rem; }
    .modal-footer { display: flex; justify-content: flex-end; gap: 0.8rem; margin-top: 1.5rem; }
  `]
})
export class SlaEngineComponent implements OnInit {
  evaluations: EvaluationResponse[] = [];
  selectedTenant = 'ACME';
  selectedStatus = '';
  showModal = false;

  newEval: EvaluationRequest = {
    tenantCode: 'ACME',
    jobType: 'PAYMENT_BATCH',
    jobId: `job-eval-${Math.floor(1000 + Math.random() * 9000)}`,
    durationMs: 350000
  };

  constructor(private service: SlaMonitorService) {}

  ngOnInit(): void {
    this.loadEvaluations();
  }

  loadEvaluations(): void {
    this.service.getEvaluations(this.selectedTenant, this.selectedStatus).subscribe({
      next: (res) => this.evaluations = res.content,
      error: (err) => console.error(err)
    });
  }

  submitEvaluation(): void {
    this.service.evaluateSla(this.newEval).subscribe({
      next: () => {
        this.showModal = false;
        this.loadEvaluations();
      },
      error: (err) => console.error('Failed evaluation:', err)
    });
  }
}
