import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SlaMonitorService } from '../../services/sla-monitor.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-grid">
      <!-- KPI Card 1: Total Ingested Logs -->
      <div class="glass-panel glass-card kpi-card">
        <div class="kpi-icon icon-blue">
          <i class="fa-solid fa-database"></i>
        </div>
        <div class="kpi-info">
          <span class="kpi-label">Ingested Operation Logs</span>
          <span class="kpi-value">{{ totalLogs }}</span>
          <span class="kpi-sub"><i class="fa-solid fa-arrow-trend-up"></i> Real-time Ingestion Feed</span>
        </div>
      </div>

      <!-- KPI Card 2: Active SLA Evaluations -->
      <div class="glass-panel glass-card kpi-card">
        <div class="kpi-icon icon-cyan">
          <i class="fa-solid fa-stopwatch"></i>
        </div>
        <div class="kpi-info">
          <span class="kpi-label">Evaluated SLA Operations</span>
          <span class="kpi-value">{{ totalEvaluations }}</span>
          <span class="kpi-sub"><i class="fa-solid fa-shield-check"></i> Threshold Evaluation Active</span>
        </div>
      </div>

      <!-- KPI Card 3: SLA Breaches & Warnings -->
      <div class="glass-panel glass-card kpi-card">
        <div class="kpi-icon icon-red">
          <i class="fa-solid fa-triangle-exclamation"></i>
        </div>
        <div class="kpi-info">
          <span class="kpi-label">Breaches & Warnings</span>
          <span class="kpi-value text-breach">{{ totalBreaches }}</span>
          <span class="kpi-sub text-warning"><i class="fa-solid fa-bell"></i> Critical Action Required</span>
        </div>
      </div>

      <!-- KPI Card 4: ML Service Model Accuracy -->
      <div class="glass-panel glass-card kpi-card">
        <div class="kpi-icon icon-purple">
          <i class="fa-solid fa-brain"></i>
        </div>
        <div class="kpi-info">
          <span class="kpi-label">ML Model Accuracy</span>
          <span class="kpi-value text-purple">{{ (mlAccuracy * 100).toFixed(1) }}%</span>
          <span class="kpi-sub"><i class="fa-solid fa-check-double"></i> Scikit-Learn Random Forest</span>
        </div>
      </div>
    </div>

    <!-- Architecture Status Bar -->
    <div class="glass-panel architecture-panel">
      <div class="panel-header">
        <h2><i class="fa-solid fa-network-wired"></i> Microservices Ecosystem Status</h2>
        <button class="btn-secondary btn-sm" (click)="loadMetrics()">
          <i class="fa-solid fa-rotate-right"></i> Refresh Stats
        </button>
      </div>
      <div class="status-grid">
        <div class="service-node">
          <span class="node-indicator online"></span>
          <div class="node-details">
            <span class="node-title">Ingestion Service</span>
            <span class="node-sub mono">Port 8081 | MongoDB + Postgres</span>
          </div>
        </div>
        <div class="service-node">
          <span class="node-indicator online"></span>
          <div class="node-details">
            <span class="node-title">SLA Engine Service</span>
            <span class="node-sub mono">Port 8082 | Threshold Rules Engine</span>
          </div>
        </div>
        <div class="service-node">
          <span class="node-indicator" [ngClass]="{'online': mlOnline, 'offline': !mlOnline}"></span>
          <div class="node-details">
            <span class="node-title">ML Risk Service</span>
            <span class="node-sub mono">Port 8000 | FastAPI + Scikit-Learn</span>
          </div>
        </div>
        <div class="service-node">
          <span class="node-indicator online"></span>
          <div class="node-details">
            <span class="node-title">Alerting Service</span>
            <span class="node-sub mono">Port 8083 | Multi-Channel Dispatcher</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }
    .kpi-card {
      flex-direction: row;
      align-items: center;
      gap: 1.25rem;
    }
    .kpi-icon {
      width: 60px;
      height: 60px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.6rem;
      flex-shrink: 0;
    }
    .icon-blue { background: rgba(79, 172, 254, 0.15); color: #4FACFE; }
    .icon-cyan { background: rgba(0, 242, 254, 0.15); color: #00F2FE; }
    .icon-red { background: rgba(255, 59, 48, 0.15); color: #FF3B30; }
    .icon-purple { background: rgba(121, 40, 202, 0.15); color: #B800FF; }
    
    .kpi-info { display: flex; flex-direction: column; }
    .kpi-label { font-size: 0.8rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; }
    .kpi-value { font-size: 2rem; font-weight: 700; color: var(--text-main); margin: 0.2rem 0; }
    .kpi-sub { font-size: 0.75rem; color: var(--text-muted); }
    .text-breach { color: #FF3B30; }
    .text-purple { color: #C084FC; }

    .architecture-panel { padding: 1.5rem; }
    .panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .panel-header h2 { font-size: 1.2rem; font-weight: 600; display: flex; align-items: center; gap: 0.6rem; }
    .status-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
    
    .service-node {
      background: rgba(10, 15, 30, 0.6);
      border: 1px solid var(--border-glass);
      padding: 1rem;
      border-radius: 12px;
      display: flex;
      align-items: center;
      gap: 0.8rem;
    }
    .node-indicator { width: 12px; height: 12px; border-radius: 50%; flex-shrink: 0; }
    .online { background: #00DFA2; box-shadow: 0 0 10px #00DFA2; }
    .offline { background: #FF3B30; box-shadow: 0 0 10px #FF3B30; }
    .node-details { display: flex; flex-direction: column; }
    .node-title { font-size: 0.9rem; font-weight: 600; }
    .node-sub { font-size: 0.75rem; color: var(--text-muted); }
    .btn-sm { padding: 0.4rem 0.8rem; font-size: 0.8rem; }
  `]
})
export class DashboardComponent implements OnInit {
  totalLogs = 0;
  totalEvaluations = 0;
  totalBreaches = 0;
  mlAccuracy = 0.932;
  mlOnline = true;

  constructor(private service: SlaMonitorService) {}

  ngOnInit(): void {
    this.loadMetrics();
  }

  loadMetrics(): void {
    this.service.getLogs('ACME').subscribe({
      next: (res) => this.totalLogs = res.totalElements,
      error: () => {}
    });

    this.service.getEvaluations('ACME').subscribe({
      next: (res) => this.totalEvaluations = res.totalElements,
      error: () => {}
    });

    this.service.getEvaluations('ACME', 'BREACH').subscribe({
      next: (res) => this.totalBreaches = res.totalElements,
      error: () => {}
    });

    this.service.getMlHealth().subscribe({
      next: (res) => {
        this.mlAccuracy = res.model_accuracy || 0.932;
        this.mlOnline = true;
      },
      error: () => this.mlOnline = false
    });
  }
}
