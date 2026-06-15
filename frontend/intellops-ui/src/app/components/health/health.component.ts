import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface ServiceStatus {
  name: string;
  version: string;
  status: 'operational' | 'degraded' | 'down';
  uptime: string;
  port: number;
  type: string;
}

@Component({
  selector: 'app-health',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="health-container">
      <div class="page-header">
        <div>
          <h1>🔧 System Health</h1>
          <p class="subtitle">Monitor the status of all IntelliOps microservices</p>
        </div>
        <div class="overall-status" [class]="overallStatus">
          <span class="status-dot"></span>
          <span>All Systems {{ overallStatus === 'operational' ? 'Operational' : 'Degraded' }}</span>
        </div>
      </div>

      <!-- Services Grid -->
      <div class="services-grid">
        <div class="service-card" *ngFor="let service of services">
          <div class="service-header">
            <div class="service-icon">{{ getIcon(service.type) }}</div>
            <div class="service-info">
              <span class="service-name">{{ service.name }}</span>
              <span class="service-type">{{ service.type }}</span>
            </div>
            <span class="service-status" [class]="service.status">
              <span class="status-dot"></span>
              {{ service.status }}
            </span>
          </div>
          <div class="service-details">
            <div class="detail-row">
              <span>Version</span>
              <span>{{ service.version }}</span>
            </div>
            <div class="detail-row">
              <span>Port</span>
              <span>{{ service.port }}</span>
            </div>
            <div class="detail-row">
              <span>Uptime</span>
              <span>{{ service.uptime }}</span>
            </div>
          </div>
          <div class="service-bar">
            <div class="bar-fill" [style.width.%]="service.status === 'operational' ? 100 : service.status === 'degraded' ? 60 : 0"></div>
          </div>
        </div>
      </div>

      <!-- Quick Stats -->
      <div class="stats-row">
        <div class="stat-card">
          <span class="stat-value" style="color:#4caf50">{{ getOperationalCount() }}</span>
          <span class="stat-label">Operational</span>
        </div>
        <div class="stat-card">
          <span class="stat-value" style="color:#ff9800">{{ getDegradedCount() }}</span>
          <span class="stat-label">Degraded</span>
        </div>
        <div class="stat-card">
          <span class="stat-value" style="color:#f44336">{{ getDownCount() }}</span>
          <span class="stat-label">Down</span>
        </div>
        <div class="stat-card">
          <span class="stat-value" style="color:#1a237e">{{ services.length }}</span>
          <span class="stat-label">Total Services</span>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="quick-actions">
        <a routerLink="/dashboard" class="action-btn">📊 Dashboard</a>
        <a routerLink="/orders" class="action-btn">📋 Orders</a>
        <a routerLink="/copilot" class="action-btn">🤖 AI Co-Pilot</a>
      </div>
    </div>
  `,
  styles: [`
    .health-container { padding: 24px; max-width: 1200px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    h1 { margin: 0; font-size: 24px; color: #1a1a2e; font-weight: 700; }
    .subtitle { margin: 4px 0 0; font-size: 13px; color: #888; }
    .overall-status { display: flex; align-items: center; gap: 8px; padding: 8px 16px; border-radius: 20px; font-size: 13px; font-weight: 600; }
    .overall-status.operational { background: #e8f5e9; color: #2e7d32; }
    .overall-status.degraded { background: #fff3e0; color: #f57c00; }
    .status-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
    .operational .status-dot { background: #4caf50; box-shadow: 0 0 6px rgba(76,175,80,0.5); }
    .degraded .status-dot { background: #ff9800; }
    .down .status-dot { background: #f44336; }

    .services-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .service-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .service-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
    .service-icon { font-size: 28px; }
    .service-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
    .service-name { font-size: 14px; font-weight: 600; color: #333; }
    .service-type { font-size: 11px; color: #999; }
    .service-status { display: flex; align-items: center; gap: 6px; font-size: 11px; font-weight: 600; padding: 4px 10px; border-radius: 10px; text-transform: capitalize; }
    .service-status.operational { background: #e8f5e9; color: #2e7d32; }
    .service-status.degraded { background: #fff3e0; color: #f57c00; }
    .service-status.down { background: #fef2f2; color: #d32f2f; }
    .service-details { display: flex; flex-direction: column; gap: 8px; margin-bottom: 12px; }
    .detail-row { display: flex; justify-content: space-between; font-size: 13px; }
    .detail-row span:first-child { color: #888; }
    .detail-row span:last-child { color: #333; font-weight: 500; }
    .service-bar { height: 4px; background: #f0f0f0; border-radius: 2px; overflow: hidden; }
    .bar-fill { height: 100%; background: linear-gradient(90deg, #4caf50, #66bb6a); border-radius: 2px; transition: width 1s ease; }

    .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .stat-card { background: white; padding: 16px; border-radius: 12px; text-align: center; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .stat-value { display: block; font-size: 28px; font-weight: 700; }
    .stat-label { font-size: 12px; color: #888; margin-top: 4px; }

    .quick-actions { display: flex; gap: 12px; }
    .action-btn {
      padding: 10px 20px; background: white; border: 1px solid #e0e0e0; border-radius: 8px;
      text-decoration: none; font-size: 13px; color: #555; transition: all 0.2s;
    }
    .action-btn:hover { border-color: #667eea; color: #667eea; }

    @media (max-width: 768px) { .services-grid { grid-template-columns: 1fr; } .stats-row { grid-template-columns: repeat(2, 1fr); } }
  `]
})
export class HealthComponent {
  overallStatus: 'operational' | 'degraded' = 'operational';

  services: ServiceStatus[] = [
    { name: 'Auth Service', version: '1.0.0', status: 'operational', uptime: '14d 6h', port: 8080, type: 'Spring Boot' },
    { name: 'Order Service', version: '1.0.0', status: 'operational', uptime: '14d 6h', port: 8081, type: 'Spring Boot' },
    { name: 'Inventory Service', version: '1.0.0', status: 'operational', uptime: '14d 6h', port: 8082, type: 'Spring Boot' },
    { name: 'AI Co-Pilot', version: '1.0.0', status: 'operational', uptime: '14d 6h', port: 8083, type: 'Spring Boot' },
    { name: 'Billing Service', version: '1.0.0', status: 'operational', uptime: '14d 6h', port: 8084, type: 'Spring Boot' },
    { name: 'PostgreSQL', version: '16', status: 'operational', uptime: '14d 6h', port: 5432, type: 'Database' },
    { name: 'MongoDB', version: '7.0', status: 'operational', uptime: '14d 6h', port: 27017, type: 'Database' },
    { name: 'Apache Kafka', version: '7.6.0', status: 'operational', uptime: '14d 6h', port: 9092, type: 'Message Queue' },
    { name: 'Ollama (AI)', version: 'llama3.1', status: 'operational', uptime: '14d 6h', port: 11434, type: 'AI Model' },
  ];

  getIcon(type: string): string {
    const icons: Record<string, string> = {
      'Spring Boot': '🌱', 'Database': '🗄️', 'Message Queue': '📨', 'AI Model': '🤖'
    };
    return icons[type] || '🔧';
  }

  getOperationalCount(): number { return this.services.filter(s => s.status === 'operational').length; }
  getDegradedCount(): number { return this.services.filter(s => s.status === 'degraded').length; }
  getDownCount(): number { return this.services.filter(s => s.status === 'down').length; }
}
