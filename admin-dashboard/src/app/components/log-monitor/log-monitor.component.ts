import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SlaMonitorService } from '../../services/sla-monitor.service';
import { OperationLog } from '../../models/sla-models';

@Component({
  selector: 'app-log-monitor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="glass-panel main-container">
      <div class="panel-header">
        <div>
          <h2><i class="fa-solid fa-server"></i> Operation Logs Ingestion Monitor</h2>
          <p class="subtitle">High-throughput log auditing persisted across MongoDB and PostgreSQL</p>
        </div>
        <button class="btn-primary" (click)="showModal = true">
          <i class="fa-solid fa-plus"></i> Ingest New Log
        </button>
      </div>

      <!-- Filters -->
      <div class="filter-bar">
        <div class="form-group">
          <label class="form-label">Tenant</label>
          <select class="form-control" [(ngModel)]="selectedTenant" (change)="loadLogs()">
            <option value="ACME">ACME Corp (ACME)</option>
            <option value="GLOBEX">Globex Corp (GLOBEX)</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Job Type Filter</label>
          <input type="text" class="form-control" placeholder="e.g. PAYMENT_BATCH" [(ngModel)]="selectedJobType" (input)="loadLogs()"/>
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
              <th>Status</th>
              <th>Completed At</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let log of logs">
              <td class="mono font-semibold">{{ log.jobId }}</td>
              <td><span class="tenant-tag">{{ log.tenantCode }}</span></td>
              <td>{{ log.jobType }}</td>
              <td class="mono">{{ log.durationMs | number }} ms</td>
              <td>
                <span class="badge" [ngClass]="{'badge-success': log.status === 'SUCCESS', 'badge-failed': log.status === 'FAILED'}">
                  {{ log.status }}
                </span>
              </td>
              <td class="mono text-sm">{{ log.completedAt | date:'medium' }}</td>
            </tr>
            <tr *ngIf="logs.length === 0">
              <td colspan="6" class="empty-state">No operation logs found for tenant filter.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Ingestion Modal -->
    <div class="modal-backdrop" *ngIf="showModal">
      <div class="glass-panel modal-card">
        <div class="modal-header">
          <h3>Ingest Operation Log</h3>
          <button class="close-btn" (click)="showModal = false">&times;</button>
        </div>
        <form (ngSubmit)="submitLog()">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Tenant Code</label>
              <select class="form-control" [(ngModel)]="newLog.tenantCode" name="tenantCode" required>
                <option value="ACME">ACME</option>
                <option value="GLOBEX">GLOBEX</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Job Type</label>
              <input type="text" class="form-control" [(ngModel)]="newLog.jobType" name="jobType" required/>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Job ID</label>
            <input type="text" class="form-control" [(ngModel)]="newLog.jobId" name="jobId" required/>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Started At</label>
              <input type="datetime-local" class="form-control" [(ngModel)]="startedAtLocal" name="startedAt" required/>
            </div>
            <div class="form-group">
              <label class="form-label">Completed At</label>
              <input type="datetime-local" class="form-control" [(ngModel)]="completedAtLocal" name="completedAt" required/>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-secondary" (click)="showModal = false">Cancel</button>
            <button type="submit" class="btn-primary">Submit Log</button>
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
export class LogMonitorComponent implements OnInit {
  logs: OperationLog[] = [];
  selectedTenant = 'ACME';
  selectedJobType = '';
  showModal = false;

  startedAtLocal = new Date(Date.now() - 240000).toISOString().slice(0, 16);
  completedAtLocal = new Date().toISOString().slice(0, 16);

  newLog: OperationLog = {
    tenantCode: 'ACME',
    jobType: 'PAYMENT_BATCH',
    jobId: `job-${Math.floor(1000 + Math.random() * 9000)}`,
    startedAt: '',
    completedAt: '',
    status: 'SUCCESS'
  };

  constructor(private service: SlaMonitorService) {}

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.service.getLogs(this.selectedTenant, this.selectedJobType).subscribe({
      next: (res) => this.logs = res.content,
      error: (err) => console.error(err)
    });
  }

  submitLog(): void {
    this.newLog.startedAt = new Date(this.startedAtLocal).toISOString();
    this.newLog.completedAt = new Date(this.completedAtLocal).toISOString();

    this.service.ingestLog(this.newLog).subscribe({
      next: () => {
        this.showModal = false;
        this.loadLogs();
      },
      error: (err) => console.error('Failed to ingest log:', err)
    });
  }
}
