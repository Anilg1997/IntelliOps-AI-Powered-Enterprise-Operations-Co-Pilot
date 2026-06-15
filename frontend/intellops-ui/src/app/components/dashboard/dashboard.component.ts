import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface StatCard {
  label: string;
  value: string;
  trend: string;
  icon: string;
  color: string;
}

interface RecentOrder {
  number: string;
  customer: string;
  status: string;
  amount: string;
  date: string;
}

interface InventoryItem {
  sku: string;
  name: string;
  stock: number;
  threshold: number;
  status: string;
}

interface BillingSummary {
  totalInvoices: number;
  collected: string;
  pending: number;
  successRate: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="dashboard-container">
      <!-- Page Header -->
      <div class="page-header">
        <div>
          <h1>Dashboard</h1>
          <p class="subtitle">Enterprise Operations Overview</p>
        </div>
        <div class="header-actions">
          <span class="live-badge">
            <span class="live-dot"></span>
            Live
          </span>
          <button class="btn-refresh" (click)="refresh()">🔄 Refresh</button>
        </div>
      </div>

      <!-- Stats Cards -->
      <div class="stats-grid">
        <div class="stat-card" *ngFor="let stat of stats">
          <div class="stat-icon" [style.background]="stat.color + '20'">
            <span>{{ stat.icon }}</span>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
            <span class="stat-trend" [style.color]="stat.trend.startsWith('+') ? '#4caf50' : '#f44336'">
              {{ stat.trend }}
            </span>
          </div>
        </div>
      </div>

      <div class="content-grid">
        <!-- Recent Orders -->
        <div class="card orders-card">
          <div class="card-header">
            <h3>📋 Recent Orders</h3>
            <a routerLink="/orders" class="card-link">View All →</a>
          </div>
          <table>
            <thead>
              <tr><th>Order #</th><th>Customer</th><th>Status</th><th>Amount</th><th>Date</th></tr>
            </thead>
            <tbody>
              <tr *ngFor="let order of recentOrders">
                <td class="order-num">{{ order.number }}</td>
                <td>{{ order.customer }}</td>
                <td><span class="status-badge" [class]="order.status.toLowerCase()">{{ order.status }}</span></td>
                <td class="amount">{{ order.amount }}</td>
                <td class="date">{{ order.date }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Right Column -->
        <div class="right-column">
          <!-- Inventory Overview -->
          <div class="card inventory-card">
            <div class="card-header">
              <h3>📦 Inventory Overview</h3>
              <span class="badge">{{ inventory.length }} SKUs</span>
            </div>
            <div class="inventory-list">
              <div class="inventory-item" *ngFor="let item of inventory">
                <div class="item-info">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-sku">{{ item.sku }}</span>
                </div>
                <div class="item-stock">
                  <div class="stock-bar">
                    <div class="stock-fill" [style.width.%]="(item.stock / (item.threshold * 3)) * 100"
                         [style.background]="item.status === 'LOW' ? '#f44336' : item.status === 'MEDIUM' ? '#ff9800' : '#4caf50'"></div>
                  </div>
                  <span class="stock-count" [style.color]="item.status === 'LOW' ? '#f44336' : '#333'">{{ item.stock }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Billing Summary -->
          <div class="card billing-card">
            <div class="card-header">
              <h3>💰 Billing Summary</h3>
              <a href="#" class="card-link">Details →</a>
            </div>
            <div class="billing-grid">
              <div class="billing-stat">
                <span class="billing-value">{{ billing.totalInvoices }}</span>
                <span class="billing-label">Total Invoices</span>
              </div>
              <div class="billing-stat">
                <span class="billing-value" style="color:#2e7d32">{{ billing.collected }}</span>
                <span class="billing-label">Collected</span>
              </div>
              <div class="billing-stat">
                <span class="billing-value" style="color:#f57c00">{{ billing.pending }}</span>
                <span class="billing-label">Pending</span>
              </div>
              <div class="billing-stat">
                <span class="billing-value" style="color:#1976d2">{{ billing.successRate }}</span>
                <span class="billing-label">Success Rate</span>
              </div>
            </div>
          </div>

          <!-- AI Insights -->
          <div class="card insights-card">
            <div class="card-header">
              <h3>🤖 AI Insights</h3>
              <a routerLink="/copilot" class="card-link">Open Co-Pilot →</a>
            </div>
            <div class="insight-item">
              <span class="insight-dot" style="background:#f44336"></span>
              <span class="insight-text">3 orders on hold — stock verification needed</span>
            </div>
            <div class="insight-item">
              <span class="insight-dot" style="background:#ff9800"></span>
              <span class="insight-text">2 pending payments in billing system</span>
            </div>
            <div class="insight-item">
              <span class="insight-dot" style="background:#4caf50"></span>
              <span class="insight-text">SSL-WILD-1Y restock expected in 5 days</span>
            </div>
            <div class="insight-item">
              <span class="insight-dot" style="background:#2196f3"></span>
              <span class="insight-text">Peak order processing: 2-4 PM daily</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container { padding: 24px; max-width: 1400px; margin: 0 auto; }
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    h1 { margin: 0; font-size: 24px; color: #1a1a2e; font-weight: 700; }
    .subtitle { margin: 4px 0 0; font-size: 13px; color: #888; }
    .header-actions { display: flex; align-items: center; gap: 12px; }
    .live-badge { display: flex; align-items: center; gap: 6px; background: #e8f5e9; color: #2e7d32; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 600; }
    .live-dot { width: 8px; height: 8px; border-radius: 50%; background: #4caf50; animation: pulse 2s infinite; }
    @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
    .btn-refresh { padding: 6px 14px; border: 1px solid #e0e0e0; border-radius: 8px; background: white; cursor: pointer; font-size: 13px; color: #555; transition: all 0.2s; }
    .btn-refresh:hover { border-color: #7c4dff; color: #7c4dff; }

    .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .stat-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); display: flex; gap: 16px; align-items: flex-start; }
    .stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; flex-shrink: 0; }
    .stat-info { flex: 1; }
    .stat-value { display: block; font-size: 24px; font-weight: 700; color: #1a237e; }
    .stat-label { display: block; font-size: 12px; color: #888; margin-top: 2px; }
    .stat-trend { display: block; font-size: 11px; font-weight: 600; margin-top: 4px; }

    .content-grid { display: grid; grid-template-columns: 1fr 380px; gap: 20px; }
    .card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .card-header h3 { margin: 0; font-size: 15px; color: #333; font-weight: 600; }
    .card-link { font-size: 12px; color: #7c4dff; text-decoration: none; font-weight: 500; }
    .card-link:hover { text-decoration: underline; }
    .badge { background: #e8eaf6; color: #283593; padding: 3px 10px; border-radius: 10px; font-size: 11px; font-weight: 600; }

    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 10px 12px; font-size: 11px; color: #888; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 2px solid #f0f0f0; }
    td { padding: 10px 12px; font-size: 13px; color: #333; border-bottom: 1px solid #f5f5f5; }
    .order-num { font-weight: 600; color: #1a237e; font-size: 12px; }
    .amount { font-weight: 600; }
    .date { color: #888; font-size: 12px; }
    .status-badge { padding: 3px 10px; border-radius: 10px; font-size: 10px; font-weight: 600; text-transform: uppercase; white-space: nowrap; }
    .pending { background: #fff3e0; color: #f57c00; }
    .confirmed { background: #e3f2fd; color: #1976d2; }
    .processing { background: #e8eaf6; color: #3949ab; }
    .on_hold { background: #fef2f2; color: #d32f2f; }
    .shipped { background: #e8f5e9; color: #388e3c; }
    .delivered { background: #e8f5e9; color: #2e7d32; }
    .cancelled { background: #f5f5f5; color: #9e9e9e; }

    .right-column { display: flex; flex-direction: column; gap: 16px; }
    .inventory-list { display: flex; flex-direction: column; gap: 12px; }
    .inventory-item { display: flex; justify-content: space-between; align-items: center; }
    .item-info { display: flex; flex-direction: column; gap: 2px; }
    .item-name { font-size: 13px; font-weight: 500; color: #333; }
    .item-sku { font-size: 11px; color: #999; }
    .item-stock { display: flex; align-items: center; gap: 10px; }
    .stock-bar { width: 80px; height: 6px; background: #f0f0f0; border-radius: 3px; overflow: hidden; }
    .stock-fill { height: 100%; border-radius: 3px; transition: width 0.3s; }
    .stock-count { font-size: 13px; font-weight: 600; min-width: 30px; text-align: right; }

    .billing-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
    .billing-stat { text-align: center; padding: 12px; background: #f8f9fc; border-radius: 8px; }
    .billing-value { display: block; font-size: 20px; font-weight: 700; color: #1a237e; }
    .billing-label { display: block; font-size: 11px; color: #888; margin-top: 2px; }

    .insights-card { background: linear-gradient(135deg, #667eea08 0%, #764ba208 100%); border-left: 3px solid #667eea; }
    .insight-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; }
    .insight-item + .insight-item { border-top: 1px solid #f0f0f0; }
    .insight-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
    .insight-text { font-size: 13px; color: #555; }

    @media (max-width: 1024px) { .content-grid { grid-template-columns: 1fr; } .stats-grid { grid-template-columns: repeat(2, 1fr); } }
    @media (max-width: 600px) { .stats-grid { grid-template-columns: 1fr; } }
  `]
})
export class DashboardComponent {
  stats: StatCard[] = [
    { label: 'Total Orders', value: '156', trend: '+12%', icon: '📋', color: '#667eea' },
    { label: 'Total Revenue', value: '₹2.4M', trend: '+8.5%', icon: '💰', color: '#f093fb' },
    { label: 'Products in Stock', value: '842', trend: '+3.2%', icon: '📦', color: '#4facfe' },
    { label: 'Fulfillment Rate', value: '98.5%', trend: '+1.2%', icon: '📊', color: '#43e97b' },
  ];

  recentOrders: RecentOrder[] = [
    { number: 'ORD-20250101', customer: 'Acme Corporation', status: 'DELIVERED', amount: '₹1,24,500', date: 'Jan 15' },
    { number: 'ORD-20250102', customer: 'TechStart Inc', status: 'PENDING', amount: '₹67,800', date: 'Jan 14' },
    { number: 'ORD-20250103', customer: 'GlobalRetail Co', status: 'ON_HOLD', amount: '₹2,50,000', date: 'Jan 13' },
    { number: 'ORD-20250104', customer: 'DataFlow Ltd', status: 'CONFIRMED', amount: '₹89,200', date: 'Jan 12' },
    { number: 'ORD-20250105', customer: 'CloudBase Inc', status: 'SHIPPED', amount: '₹1,75,000', date: 'Jan 11' },
  ];

  inventory: InventoryItem[] = [
    { sku: 'SRV-RACK-42U', name: 'Server Rack 42U', stock: 22, threshold: 5, status: 'HIGH' },
    { sku: 'CLD-STO-1TB', name: 'Cloud Storage 1TB', stock: 90, threshold: 20, status: 'HIGH' },
    { sku: 'SSL-WILD-1Y', name: 'SSL Wildcard Cert', stock: 8, threshold: 30, status: 'LOW' },
    { sku: 'DB-LIC-STD', name: 'DB License Std', stock: 28, threshold: 5, status: 'HIGH' },
    { sku: 'FW-APPL-1U', name: 'Firewall Appliance', stock: 14, threshold: 3, status: 'MEDIUM' },
  ];

  billing: BillingSummary = {
    totalInvoices: 342,
    collected: '₹8.7M',
    pending: 12,
    successRate: '98%',
  };

  refresh(): void {
    console.log('Dashboard refreshed');
  }
}
