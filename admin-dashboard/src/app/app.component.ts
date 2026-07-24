import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { LogMonitorComponent } from './components/log-monitor/log-monitor.component';
import { SlaEngineComponent } from './components/sla-engine/sla-engine.component';
import { MlPredictorComponent } from './components/ml-predictor/ml-predictor.component';
import { AlertCenterComponent } from './components/alert-center/alert-center.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    DashboardComponent,
    LogMonitorComponent,
    SlaEngineComponent,
    MlPredictorComponent,
    AlertCenterComponent
  ],
  template: `
    <div class="app-wrapper">
      <!-- Navbar -->
      <header class="app-header glass-panel">
        <div class="header-brand">
          <div class="brand-logo">
            <i class="fa-solid fa-shield-halved"></i>
          </div>
          <div>
            <h1 class="brand-title">Enterprise Ops SLA & Anomaly Monitor</h1>
            <p class="brand-subtitle">Distributed SLA Monitoring & Machine Learning Anomaly Platform</p>
          </div>
        </div>

        <div class="header-status">
          <span class="status-indicator"></span>
          <span class="status-text">SYSTEM OPERATIONAL</span>
        </div>
      </header>

      <!-- Navigation Tabs -->
      <nav class="nav-tabs glass-panel">
        <button class="tab-btn" [ngClass]="{'active': activeTab === 'overview'}" (click)="activeTab = 'overview'">
          <i class="fa-solid fa-chart-pie"></i> Overview
        </button>
        <button class="tab-btn" [ngClass]="{'active': activeTab === 'logs'}" (click)="activeTab = 'logs'">
          <i class="fa-solid fa-server"></i> Ingestion Logs
        </button>
        <button class="tab-btn" [ngClass]="{'active': activeTab === 'sla'}" (click)="activeTab = 'sla'">
          <i class="fa-solid fa-stopwatch-20"></i> SLA Engine
        </button>
        <button class="tab-btn" [ngClass]="{'active': activeTab === 'ml'}" (click)="activeTab = 'ml'">
          <i class="fa-solid fa-brain"></i> ML Risk Predictor
        </button>
        <button class="tab-btn" [ngClass]="{'active': activeTab === 'alerts'}" (click)="activeTab = 'alerts'">
          <i class="fa-solid fa-bell"></i> Alert Center
        </button>
      </nav>

      <!-- Active Tab Content -->
      <main class="main-content">
        <app-dashboard *ngIf="activeTab === 'overview'"></app-dashboard>
        <app-log-monitor *ngIf="activeTab === 'logs'"></app-log-monitor>
        <app-sla-engine *ngIf="activeTab === 'sla'"></app-sla-engine>
        <app-ml-predictor *ngIf="activeTab === 'ml'"></app-ml-predictor>
        <app-alert-center *ngIf="activeTab === 'alerts'"></app-alert-center>
      </main>
    </div>
  `,
  styles: [`
    .app-wrapper {
      max-width: 1400px;
      margin: 0 auto;
      padding: 1.5rem;
    }
    .app-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.25rem 2rem;
      margin-bottom: 1.25rem;
    }
    .header-brand { display: flex; align-items: center; gap: 1rem; }
    .brand-logo {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      background: linear-gradient(135deg, var(--primary-cyan), var(--accent-purple));
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      color: #070913;
    }
    .brand-title { font-size: 1.35rem; font-weight: 700; background: linear-gradient(90deg, #FFF, var(--primary-cyan)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    .brand-subtitle { font-size: 0.8rem; color: var(--text-muted); }

    .header-status {
      display: flex;
      align-items: center;
      gap: 0.6rem;
      background: rgba(0, 223, 162, 0.1);
      border: 1px solid rgba(0, 223, 162, 0.3);
      padding: 0.5rem 1rem;
      border-radius: 9999px;
    }
    .status-indicator { width: 10px; height: 10px; border-radius: 50%; background: #00DFA2; box-shadow: 0 0 10px #00DFA2; }
    .status-text { font-size: 0.75rem; font-weight: 700; color: #00DFA2; letter-spacing: 0.05em; }

    .nav-tabs {
      display: flex;
      gap: 0.5rem;
      padding: 0.5rem;
      margin-bottom: 1.5rem;
      overflow-x: auto;
    }
    .tab-btn {
      background: transparent;
      border: none;
      color: var(--text-muted);
      padding: 0.75rem 1.25rem;
      border-radius: 10px;
      font-family: var(--font-family);
      font-weight: 600;
      font-size: 0.9rem;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      transition: all 0.2s ease;
      white-space: nowrap;
    }
    .tab-btn:hover { color: var(--text-main); background: rgba(255, 255, 255, 0.05); }
    .tab-btn.active {
      background: rgba(0, 242, 254, 0.15);
      color: var(--primary-cyan);
      border: 1px solid rgba(0, 242, 254, 0.3);
    }

    .main-content { min-height: 500px; }
  `]
})
export class AppComponent {
  activeTab: 'overview' | 'logs' | 'sla' | 'ml' | 'alerts' = 'overview';
}
