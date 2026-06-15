import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService, Order } from '../../services/order.service';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="order-list-container">
      <div class="header">
        <h1>Orders</h1>
        <div class="header-actions">
          <input type="text" placeholder="Search orders..." class="search-input" />
          <span class="badge">{{ orders.length }} total</span>
        </div>
      </div>

      <div class="stats-row">
        <div class="stat-card">
          <span class="stat-value">{{ orders.length }}</span>
          <span class="stat-label">Total Orders</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ getStatusCount('PENDING') }}</span>
          <span class="stat-label">Pending</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ getStatusCount('ON_HOLD') }}</span>
          <span class="stat-label">On Hold</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ getStatusCount('DELIVERED') }}</span>
          <span class="stat-label">Delivered</span>
        </div>
      </div>

      <div class="table-card">
        <table>
          <thead>
            <tr>
              <th>Order #</th>
              <th>Customer</th>
              <th>Status</th>
              <th>Amount</th>
              <th>Created</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let order of orders">
              <td class="order-number">{{ order.orderNumber }}</td>
              <td>{{ order.customer.name || 'N/A' }}</td>
              <td>
                <span class="status-badge" [class]="order.status.toLowerCase()">
                  {{ order.status }}
                </span>
              </td>
              <td class="amount">₹{{ order.totalAmount | number:'1.2-2' }}</td>
              <td class="date">{{ order.createdAt | date:'medium' }}</td>
              <td>
                <a [routerLink]="['/orders', order.orderNumber]" class="view-link">View →</a>
              </td>
            </tr>
            <tr *ngIf="orders.length === 0">
              <td colspan="6" class="empty-state">No orders found. Create your first order!</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .order-list-container { padding: 24px; max-width: 1200px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    h1 { margin: 0; font-size: 24px; color: #1a1a2e; font-weight: 700; }
    .header-actions { display: flex; align-items: center; gap: 12px; }
    .search-input { padding: 8px 16px; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 14px; width: 240px; outline: none; }
    .search-input:focus { border-color: #7c4dff; }
    .badge { background: #e8eaf6; color: #283593; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; }
    .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .stat-card { background: white; padding: 16px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    .stat-value { display: block; font-size: 28px; font-weight: 700; color: #1a237e; }
    .stat-label { font-size: 12px; color: #666; margin-top: 4px; }
    .table-card { background: white; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); overflow: hidden; }
    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 16px; font-size: 11px; color: #666; text-transform: uppercase; letter-spacing: 1px; background: #fafafa; border-bottom: 2px solid #f0f0f0; }
    td { padding: 14px 16px; font-size: 13px; color: #333; border-bottom: 1px solid #f5f5f5; }
    tr:last-child td { border-bottom: none; }
    .order-number { font-weight: 600; color: #1a237e; }
    .amount { font-weight: 600; }
    .date { color: #888; font-size: 12px; }
    .status-badge { padding: 4px 12px; border-radius: 12px; font-size: 11px; font-weight: 600; text-transform: uppercase; }
    .pending { background: #fff3e0; color: #f57c00; }
    .confirmed { background: #e3f2fd; color: #1976d2; }
    .processing { background: #e8eaf6; color: #3949ab; }
    .on_hold { background: #fef2f2; color: #d32f2f; }
    .shipped { background: #e8f5e9; color: #388e3c; }
    .delivered { background: #e8f5e9; color: #2e7d32; }
    .cancelled { background: #f5f5f5; color: #9e9e9e; }
    .refunded { background: #fce4ec; color: #c62828; }
    .view-link { color: #7c4dff; text-decoration: none; font-weight: 500; }
    .view-link:hover { text-decoration: underline; }
    .empty-state { text-align: center; padding: 48px; color: #999; }
  `]
})
export class OrderListComponent implements OnInit {
  private orderService = inject(OrderService);
  orders: Order[] = [];

  ngOnInit() {
    this.orderService.getAllOrders().subscribe(data => this.orders = data);
  }

  getStatusCount(status: string): number {
    return this.orders.filter(o => o.status === status).length;
  }
}
