import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SlaMonitorService } from '../../services/sla-monitor.service';
import { AlertNotificationResponse, AlertDispatchRequest } from '../../models/sla-models';

@Component({
  selector: 'app-alert-center',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="glass-panel main-container">
      <div class="panel-header">
        <div>
          <h2><i class="fa-solid fa-bell"></i> Multi-Channel Alert Notification Center</h2>
          <p class="subtitle">Dispatched SLA warnings & breaches logged in MongoDB with recipient audit</p>
        </div>
        <button class="btn-primary" (click)="showModal = true">
          <i class="fa-solid fa-paper-plane"></i> Dispatch Alert
        </button>
      </div>

      <!-- Filters -->
      <div class="filter-bar">
        <div class="form-group">
          <label class="form-label">Tenant</label>
          <select class="form-control" [(ngModel)]="selectedTenant" (change)="loadAlerts()">
            <option value="ACME">ACME Corp (ACME)</option>
            <option value="GLOBEX">Globex Corp (GLOBEX)</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Severity Filter</label>
          <select class="form-control" [(ngModel)]="selectedSeverity" (change)="loadAlerts()">
            <option value="">All Severities</option>
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
              <th>Channel</th>
              <th>Recipient</th>
              <th>Severity</th>
              <th>Message</th>
              <th>Status</th>
              <th>Dispatched At</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let alert of alerts">
              <td class="mono font-semibold">{{ alert.jobId }}</td>
              <td><span class="tenant-tag">{{ alert.tenantCode }}</span></td>
              <td><span class="channel-tag">{{ alert.channel }}</span></td>
              <td class="mono text-sm">{{ alert.recipient }}</td>
              <td>
                <span class="badge" [ngClass]="{'badge-warning': alert.severity === 'WARNING', 'badge-breach': alert.severity === 'BREACH'}">
                  {{ alert.severity }}
                </span>
              </td>
              <td class="text-sm message-cell">{{ alert.message }}</td>
              <td><span class="badge badge-success">{{ alert.status }}</span></td>
              <td class="mono text-sm">{{ alert.sentAt | date:'medium' }}</td>
            </tr>
            <tr *ngIf="alerts.length === 0">
              <td colspan="8" class="empty-state">No dispatched alerts found for selected tenant.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Dispatch Modal -->
    <div class="modal-backdrop" *ngIf="showModal">
      <div class="glass-panel modal-card">
        <div class="modal-header">
          <h3>Dispatch Alert Notification</h3>
          <button class="close-btn" (click)="showModal = false">&times;</button>
        </div>
        <form (ngSubmit)="submitAlert()">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Tenant Code</label>
              <select class="form-control" [(ngModel)]="newAlert.tenantCode" name="tenantCode" required>
                <option value="ACME">ACME</option>
                <option value="GLOBEX">GLOBEX</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Severity</label>
              <select class="form-control" [(ngModel)]="newAlert.severity" name="severity" required>
                <option value="WARNING">WARNING</option>
                <option value="BREACH">BREACH</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Job Type</label>
              <input type="text" class="form-control" [(ngModel)]="newAlert.jobType" name="jobType" required/>
            </div>
            <div class="form-group">
              <label class="form-label">Job ID</label>
              <input type="text" class="form-control" [(ngModel)]="newAlert.jobId" name="jobId" required/>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Message</label>
            <textarea class="form-control" rows="3" [(ngModel)]="newAlert.message" name="message" required></textarea>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-secondary" (click)="showModal = false">Cancel</button>
            <button type="submit" class="btn-primary">Dispatch Alert</button>
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
    .channel-tag { background: rgba(121, 40, 202, 0.15); color: #C084FC; padding: 0.2rem 0.5rem; border-radius: 6px; font-weight: 600; font-size: 0.75rem; }
    .message-cell { max-width: 240px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
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
export class AlertCenterComponent implements OnInit {
  alerts: AlertNotificationResponse[] = [];
  selectedTenant = 'ACME';
  selectedSeverity = '';
  showModal = false;

  newAlert: AlertDispatchRequest = {
    tenantCode: 'ACME',
    jobType: 'PAYMENT_BATCH',
    jobId: `job-alert-${Math.floor(1000 + Math.random() * 9000)}`,
    severity: 'BREACH',
    durationMs: 360000,
    message: 'Manual Alert: SLA Breach detected on PAYMENT_BATCH'
  };

  constructor(private service: SlaMonitorService) {}

  ngOnInit(): void {
    this.loadAlerts();
  }

  loadAlerts(): void {
    this.service.getAlerts(this.selectedTenant, this.selectedSeverity).subscribe({
      next: (res) => this.alerts = res.content,
      error: (err) => console.error(err)
    });
  }

  submitAlert(): void {
    this.service.dispatchAlert(this.newAlert).subscribe({
      next: () => {
        this.showModal = false;
        this.loadAlerts();
      },
      error: (err) => console.error('Failed to dispatch alert:', err)
    });
  }
}
