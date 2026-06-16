import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-billing',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page animate-fadeIn">
      <div class="page-header">
        <h1><i class="fas fa-file-invoice-dollar"></i> Billing</h1>
        <p>Legacy billing integration with Oracle + SOAP</p>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon" style="background: #dbeafe; color: #2563eb;"><i class="fas fa-file-invoice"></i></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalInvoices || 0 }}</span>
            <span class="stat-label">Total Invoices</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #fef3c7; color: #d97706;"><i class="fas fa-clock"></i></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.pendingInvoices || 0 }}</span>
            <span class="stat-label">Pending</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #fee2e2; color: #dc2626;"><i class="fas fa-exclamation-circle"></i></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.overdueInvoices || 0 }}</span>
            <span class="stat-label">Overdue</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #d1fae5; color: #059669;"><i class="fas fa-check-circle"></i></div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.paidInvoices || 0 }}</span>
            <span class="stat-label">Paid</span>
          </div>
        </div>
      </div>

      <div class="card">
        <h3><i class="fas fa-file-invoice"></i> Invoices</h3>
        <table class="table" *ngIf="invoices.length; else noInvoices">
          <thead>
            <tr><th>Invoice #</th><th>Order</th><th>Customer</th><th>Amount</th><th>Status</th><th>Payment</th><th>Due Date</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let inv of invoices">
              <td><strong>{{ inv.invoiceNumber }}</strong></td>
              <td>{{ inv.orderNumber }}</td>
              <td>{{ inv.customerName }}</td>
              <td>\${{ inv.totalAmount | number:'1.2-2' }}</td>
              <td><span class="badge" [ngClass]="'badge-' + inv.status.toLowerCase()">{{ inv.status }}</span></td>
              <td><span class="badge" [ngClass]="'badge-' + inv.paymentStatus.toLowerCase()">{{ inv.paymentStatus }}</span></td>
              <td>{{ inv.dueDate }}</td>
            </tr>
          </tbody>
        </table>
        <ng-template #noInvoices><p class="empty-state">No invoices found</p></ng-template>
      </div>

      <div class="card" style="margin-top: 1.5rem;">
        <h3><i class="fas fa-plug"></i> Legacy Integration</h3>
        <div class="integration-info">
          <div class="info-item"><span class="label">Database:</span><span class="value">Oracle XE (port 1521)</span></div>
          <div class="info-item"><span class="label">Protocol:</span><span class="value">SOAP/WSDL</span></div>
          <div class="info-item"><span class="label">Events:</span><span class="value">Apache Kafka</span></div>
          <div class="info-item"><span class="label">WSDL:</span><span class="value"><a href="http://localhost:8084/soap/billing.wsdl" target="_blank">View WSDL</a></span></div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 1.5rem; h1 { font-size: 1.5rem; font-weight: 700; display: flex; align-items: center; gap: 0.5rem; } p { color: var(--gray-500); font-size: 0.875rem; } }
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
    .stat-card { display: flex; align-items: center; gap: 1rem; background: white; padding: 1.25rem; border-radius: var(--radius); box-shadow: var(--shadow); }
    .stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.25rem; }
    .stat-info { display: flex; flex-direction: column; }
    .stat-value { font-size: 1.5rem; font-weight: 700; }
    .stat-label { font-size: 0.8125rem; color: var(--gray-500); }
    .card h3 { font-size: 1rem; font-weight: 600; margin-bottom: 1rem; display: flex; align-items: center; gap: 0.5rem; color: var(--gray-700); }
    .empty-state { text-align: center; padding: 2rem; color: var(--gray-400); }
    .integration-info { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1rem; }
    .info-item { display: flex; gap: 0.5rem; font-size: 0.875rem; .label { color: var(--gray-500); font-weight: 500; } .value { color: var(--gray-900); } }
  `]
})
export class BillingComponent implements OnInit {
  stats: any = {};
  invoices: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<any>('/api/v1/billing/stats').subscribe(stats => this.stats = stats);
    this.http.get<any>('/api/v1/billing/invoices').subscribe(res => this.invoices = Array.isArray(res) ? res : []);
  }
}
