import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService, Order } from '../../services/order.service';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="detail-container" *ngIf="order; else loading">
      <a routerLink="/orders" class="back-link">← Back to Orders</a>

      <div class="order-header">
        <div>
          <h1>{{ order.orderNumber }}</h1>
          <span class="status-badge" [class]="order.status.toLowerCase()">{{ order.status }}</span>
          <span *ngIf="order.statusReason" class="reason">— {{ order.statusReason }}</span>
        </div>
        <div class="amount-display">
          <span class="amount-label">Total Amount</span>
          <span class="amount-value">₹{{ order.totalAmount | number:'1.2-2' }}</span>
        </div>
      </div>

      <div class="content-grid">
        <div class="card customer-card">
          <h3>Customer</h3>
          <p class="customer-name">{{ order.customer.name }}</p>
          <p class="customer-detail">📧 {{ order.customer.email }}</p>
          <p class="customer-detail" *ngIf="order.customer.phoneNumber">📞 {{ order.customer.phoneNumber }}</p>
          <p class="customer-detail" *ngIf="order.customer.address">📍 {{ order.customer.address }}</p>
        </div>

        <div class="card timeline-card">
          <h3>Timeline</h3>
          <p class="timeline-item"><span class="label">Created:</span> {{ order.createdAt | date:'medium' }}</p>
          <p class="timeline-item" *ngIf="order.updatedAt"><span class="label">Updated:</span> {{ order.updatedAt | date:'medium' }}</p>
        </div>
      </div>

      <div class="card items-card">
        <h3>Line Items ({{ order.lineItems?.length || 0 }})</h3>
        <table>
          <thead>
            <tr><th>Product</th><th>SKU</th><th>Category</th><th>Qty</th><th>Unit Price</th><th>Subtotal</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of order.lineItems">
              <td class="product-name">{{ item.product.name }}</td>
              <td>{{ item.product.sku }}</td>
              <td>{{ item.product.category || '-' }}</td>
              <td>{{ item.quantity }}</td>
              <td>₹{{ item.unitPrice | number:'1.2-2' }}</td>
              <td class="subtotal">₹{{ item.subtotal | number:'1.2-2' }}</td>
            </tr>
          </tbody>
          <tfoot>
            <tr><td colspan="5" class="total-label">Total</td><td class="total-value">₹{{ order.totalAmount | number:'1.2-2' }}</td></tr>
          </tfoot>
        </table>
      </div>
    </div>

    <ng-template #loading>
      <div class="loading">Loading order details...</div>
    </ng-template>
  `,
  styles: [`
    .detail-container { padding: 24px; max-width: 1200px; margin: 0 auto; }
    .back-link { color: #7c4dff; text-decoration: none; font-size: 14px; display: inline-block; margin-bottom: 16px; }
    .back-link:hover { text-decoration: underline; }
    .order-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    h1 { margin: 0 0 8px 0; font-size: 22px; color: #1a1a2e; font-weight: 700; }
    .status-badge { padding: 4px 12px; border-radius: 12px; font-size: 11px; font-weight: 600; text-transform: uppercase; }
    .pending { background: #fff3e0; color: #f57c00; }
    .confirmed { background: #e3f2fd; color: #1976d2; }
    .processing { background: #e8eaf6; color: #3949ab; }
    .on_hold { background: #fef2f2; color: #d32f2f; }
    .shipped { background: #e8f5e9; color: #388e3c; }
    .delivered { background: #e8f5e9; color: #2e7d32; }
    .cancelled { background: #f5f5f5; color: #9e9e9e; }
    .refunded { background: #fce4ec; color: #c62828; }
    .reason { font-size: 13px; color: #666; margin-left: 8px; }
    .amount-display { text-align: right; }
    .amount-label { display: block; font-size: 12px; color: #666; }
    .amount-value { font-size: 28px; font-weight: 700; color: #1a237e; }
    .content-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; }
    .card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
    h3 { margin: 0 0 16px 0; font-size: 14px; color: #333; font-weight: 600; }
    .customer-name { font-size: 16px; font-weight: 600; color: #1a1a2e; margin: 0 0 8px 0; }
    .customer-detail { font-size: 13px; color: #666; margin: 4px 0; }
    .timeline-item { font-size: 13px; color: #555; margin: 8px 0; }
    .timeline-item .label { color: #999; }
    .items-card { margin-bottom: 24px; }
    table { width: 100%; border-collapse: collapse; }
    th { text-align: left; padding: 12px; font-size: 11px; color: #666; text-transform: uppercase; background: #fafafa; border-bottom: 2px solid #f0f0f0; }
    td { padding: 12px; font-size: 13px; color: #333; border-bottom: 1px solid #f5f5f5; }
    .product-name { font-weight: 500; }
    .subtotal { font-weight: 600; }
    tfoot td { padding: 12px; border-top: 2px solid #e0e0e0; }
    .total-label { font-weight: 600; text-align: right; }
    .total-value { font-weight: 700; color: #1a237e; font-size: 16px; }
    .loading { text-align: center; padding: 48px; color: #999; font-size: 14px; }
  `]
})
export class OrderDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);
  order?: Order;

  ngOnInit() {
    const orderNumber = this.route.snapshot.paramMap.get('orderNumber')!;
    this.orderService.getOrder(orderNumber).subscribe(data => this.order = data);
  }
}
