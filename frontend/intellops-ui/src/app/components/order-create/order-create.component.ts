import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastService } from '../../services/notification/toast.service';

interface OrderItem {
  productName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
}

@Component({
  selector: 'app-order-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="create-container">
      <a routerLink="/orders" class="back-link">← Back to Orders</a>

      <div class="page-header">
        <div>
          <h1>Create New Order</h1>
          <p class="subtitle">Fill in the details below to create a new enterprise order</p>
        </div>
      </div>

      <div class="form-card">
        <!-- Customer Section -->
        <div class="form-section">
          <div class="section-header">
            <span class="section-icon">👤</span>
            <h3>Customer Information</h3>
          </div>
          <div class="form-grid">
            <div class="form-group">
              <label>Customer Name <span class="required">*</span></label>
              <input type="text" [(ngModel)]="customerName" placeholder="e.g. Acme Corporation" required />
            </div>
            <div class="form-group">
              <label>Email <span class="required">*</span></label>
              <input type="email" [(ngModel)]="customerEmail" placeholder="contact@company.com" required />
            </div>
            <div class="form-group">
              <label>Phone Number</label>
              <input type="tel" [(ngModel)]="customerPhone" placeholder="+1-555-0123" />
            </div>
            <div class="form-group">
              <label>Address</label>
              <input type="text" [(ngModel)]="customerAddress" placeholder="123 Business Ave, City" />
            </div>
          </div>
        </div>

        <!-- Order Items Section -->
        <div class="form-section">
          <div class="section-header">
            <span class="section-icon">📦</span>
            <h3>Order Items</h3>
            <span class="badge">{{ items.length }} item(s)</span>
          </div>

          <div class="items-table">
            <div class="items-header">
              <span style="flex:2">Product</span>
              <span style="flex:1.5">SKU</span>
              <span style="flex:0.8">Qty</span>
              <span style="flex:1">Unit Price</span>
              <span style="flex:1">Subtotal</span>
              <span style="width:40px"></span>
            </div>

            <div class="item-row" *ngFor="let item of items; let i = index">
              <input style="flex:2" type="text" [(ngModel)]="item.productName" [name]="'prod'+i" placeholder="Product name" />
              <input style="flex:1.5" type="text" [(ngModel)]="item.sku" [name]="'sku'+i" placeholder="SKU-001" />
              <input style="flex:0.8" type="number" [(ngModel)]="item.quantity" [name]="'qty'+i" min="1" />
              <input style="flex:1" type="number" [(ngModel)]="item.unitPrice" [name]="'price'+i" min="0" step="0.01" />
              <span style="flex:1;font-weight:600;color:#1a237e">₹{{ (item.quantity * item.unitPrice) | number:'1.2-2' }}</span>
              <button class="btn-remove" (click)="removeItem(i)" title="Remove item">✕</button>
            </div>
          </div>

          <button class="btn-add-item" (click)="addItem()">+ Add Item</button>
        </div>

        <!-- Summary -->
        <div class="form-section summary-section">
          <div class="summary-row">
            <span>Subtotal ({{ totalItems }} items)</span>
            <span class="summary-value">₹{{ subtotal | number:'1.2-2' }}</span>
          </div>
          <div class="summary-row">
            <span>Tax (18% GST)</span>
            <span class="summary-value">₹{{ tax | number:'1.2-2' }}</span>
          </div>
          <div class="summary-row total-row">
            <span>Total Amount</span>
            <span class="summary-value total">₹{{ total | number:'1.2-2' }}</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="form-actions">
          <button class="btn-cancel" routerLink="/orders">Cancel</button>
          <button class="btn-submit" (click)="submitOrder()" [disabled]="!isFormValid() || submitting">
            <span *ngIf="submitting" class="spinner"></span>
            {{ submitting ? 'Creating Order...' : '🚀 Create Order' }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .create-container { padding: 24px; max-width: 1000px; margin: 0 auto; }
    .back-link { color: #7c4dff; text-decoration: none; font-size: 14px; display: inline-block; margin-bottom: 16px; }
    .back-link:hover { text-decoration: underline; }
    .page-header { margin-bottom: 24px; }
    h1 { margin: 0; font-size: 24px; color: #1a1a2e; font-weight: 700; }
    .subtitle { margin: 4px 0 0; font-size: 13px; color: #888; }
    .form-card { background: white; border-radius: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); overflow: hidden; }
    .form-section { padding: 24px; border-bottom: 1px solid #f0f0f0; }
    .section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
    .section-icon { font-size: 20px; }
    .section-header h3 { margin: 0; font-size: 16px; color: #333; font-weight: 600; flex: 1; }
    .badge { background: #e8eaf6; color: #283593; padding: 3px 10px; border-radius: 10px; font-size: 11px; font-weight: 600; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .form-group { display: flex; flex-direction: column; gap: 6px; }
    label { font-size: 13px; font-weight: 600; color: #444; }
    .required { color: #f44336; }
    input, select {
      padding: 10px 14px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px;
      outline: none; transition: border-color 0.2s, box-shadow 0.2s;
    }
    input:focus { border-color: #7c4dff; box-shadow: 0 0 0 3px rgba(124,77,255,0.1); }
    .items-header {
      display: flex; gap: 12px; padding: 10px 12px; background: #f8f9fc; border-radius: 8px;
      font-size: 11px; font-weight: 600; color: #888; text-transform: uppercase; letter-spacing: 0.5px;
    }
    .item-row { display: flex; gap: 12px; align-items: center; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
    .item-row input { padding: 8px 10px; font-size: 13px; }
    .btn-remove {
      width: 32px; height: 32px; border: none; border-radius: 6px; background: #fef2f2;
      color: #f44336; cursor: pointer; font-size: 14px; transition: all 0.2s;
    }
    .btn-remove:hover { background: #f44336; color: white; }
    .btn-add-item {
      margin-top: 12px; padding: 8px 20px; background: #f0f2ff; color: #667eea; border: 1px dashed #667eea;
      border-radius: 8px; cursor: pointer; font-size: 13px; font-weight: 500; transition: all 0.2s;
    }
    .btn-add-item:hover { background: #667eea; color: white; }
    .summary-section { background: #f8f9fc; }
    .summary-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: #555; }
    .summary-value { font-weight: 600; color: #333; }
    .total-row { border-top: 2px solid #e0e0e0; padding-top: 16px; margin-top: 8px; }
    .total-row .summary-value { font-size: 20px; font-weight: 700; color: #1a237e; }
    .form-actions { display: flex; justify-content: flex-end; gap: 12px; padding: 20px 24px; }
    .btn-cancel {
      padding: 10px 24px; border: 1px solid #ddd; border-radius: 8px; background: white;
      color: #666; cursor: pointer; font-size: 14px; text-decoration: none; transition: all 0.2s;
    }
    .btn-cancel:hover { border-color: #999; }
    .btn-submit {
      padding: 10px 28px; background: linear-gradient(135deg, #667eea, #764ba2); color: white;
      border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer;
      transition: opacity 0.2s; display: flex; align-items: center; gap: 8px;
    }
    .btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-submit:not(:disabled):hover { opacity: 0.9; }
    .spinner { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
    @media (max-width: 768px) { .form-grid { grid-template-columns: 1fr; } }
  `]
})
export class OrderCreateComponent {
  private router = inject(Router);
  private toast = inject(ToastService);

  customerName = '';
  customerEmail = '';
  customerPhone = '';
  customerAddress = '';
  items: OrderItem[] = [
    { productName: '', sku: '', quantity: 1, unitPrice: 0 }
  ];
  submitting = false;

  get subtotal(): number {
    return this.items.reduce((sum, item) => sum + (item.quantity * item.unitPrice), 0);
  }
  get tax(): number { return this.subtotal * 0.18; }
  get total(): number { return this.subtotal + this.tax; }
  get totalItems(): number {
    return this.items.reduce((sum, item) => sum + item.quantity, 0);
  }

  addItem(): void {
    this.items.push({ productName: '', sku: '', quantity: 1, unitPrice: 0 });
  }

  removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.splice(index, 1);
    }
  }

  isFormValid(): boolean {
    return !!(
      this.customerName.trim() &&
      this.customerEmail.trim() &&
      this.items.length > 0 &&
      this.items.every(item => item.productName.trim() && item.sku.trim() && item.quantity > 0 && item.unitPrice > 0)
    );
  }

  submitOrder(): void {
    if (!this.isFormValid()) return;
    this.submitting = true;
    // Simulate order creation
    setTimeout(() => {
      this.toast.success('Order Created!', 'Your order has been submitted successfully. Order number: ORD-' + Date.now().toString(36).toUpperCase());
      this.submitting = false;
      setTimeout(() => this.router.navigate(['/orders']), 1500);
    }, 1500);
  }
}
