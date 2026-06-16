import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventoryService, InventoryProduct } from '../../services/inventory.service';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page animate-fadeIn">
      <div class="page-header">
        <h1><i class="fas fa-boxes-stacked"></i> Inventory</h1>
        <p>Product catalog and stock management</p>
      </div>

      <div class="toolbar">
        <select class="form-control" [(ngModel)]="selectedCategory" (change)="loadProducts()" style="max-width: 200px;">
          <option value="">All Categories</option>
          <option value="electronics">Electronics</option>
          <option value="furniture">Furniture</option>
          <option value="accessories">Accessories</option>
        </select>
      </div>

      <div class="products-grid">
        <div class="product-card" *ngFor="let product of products">
          <div class="product-header">
            <span class="category-badge">{{ product.category }}</span>
            <span class="stock-badge" [class.low]="product.stockQuantity <= product.reorderThreshold">
              {{ product.stockQuantity <= product.reorderThreshold ? '⚠️ Low Stock' : '✅ In Stock' }}
            </span>
          </div>
          <h3>{{ product.name }}</h3>
          <p class="sku">SKU: {{ product.sku }}</p>
          <p class="description">{{ product.description }}</p>
          <div class="product-footer">
            <span class="price">\${{ product.price | number:'1.2-2' }}</span>
            <div class="stock-info">
              <span>Stock: {{ product.stockQuantity }}</span>
              <span class="reorder">Reorder at: {{ product.reorderThreshold }}</span>
            </div>
          </div>
          <div class="stock-bar">
            <div class="stock-fill" [style.width.%]="getStockPercentage(product)"
                 [class.critical]="product.stockQuantity <= product.reorderThreshold * 0.5"
                 [class.warning]="product.stockQuantity <= product.reorderThreshold && product.stockQuantity > product.reorderThreshold * 0.5"></div>
          </div>
        </div>
      </div>

      <p class="empty-state" *ngIf="!products.length && !loading">No products found</p>
    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 1.5rem; h1 { font-size: 1.5rem; font-weight: 700; display: flex; align-items: center; gap: 0.5rem; } p { color: var(--gray-500); font-size: 0.875rem; } }
    .toolbar { margin-bottom: 1.5rem; display: flex; gap: 0.75rem; }
    .products-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.25rem; }
    .product-card { background: white; border-radius: var(--radius); box-shadow: var(--shadow); padding: 1.25rem; transition: box-shadow 0.2s;
      &:hover { box-shadow: var(--shadow-md); }
      h3 { font-size: 1rem; font-weight: 600; margin: 0.75rem 0 0.25rem; }
      .sku { font-size: 0.8125rem; color: var(--gray-500); font-family: monospace; }
      .description { font-size: 0.875rem; color: var(--gray-600); margin: 0.5rem 0; line-height: 1.5; } }
    .product-header { display: flex; justify-content: space-between; align-items: center; }
    .category-badge { background: var(--gray-100); color: var(--gray-600); padding: 0.25rem 0.75rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 500; text-transform: capitalize; }
    .stock-badge { font-size: 0.75rem; font-weight: 500; &.low { color: var(--warning); } }
    .product-footer { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 1rem; padding-top: 0.75rem; border-top: 1px solid var(--gray-100); }
    .price { font-size: 1.25rem; font-weight: 700; color: var(--primary); }
    .stock-info { text-align: right; font-size: 0.8125rem; color: var(--gray-600); display: flex; flex-direction: column; gap: 0.125rem;
      .reorder { font-size: 0.75rem; color: var(--gray-400); } }
    .stock-bar { height: 4px; background: var(--gray-100); border-radius: 2px; margin-top: 0.75rem; overflow: hidden;
      .stock-fill { height: 100%; background: var(--success); border-radius: 2px; transition: width 0.3s;
        &.warning { background: var(--warning); }
        &.critical { background: var(--danger); } } }
    .empty-state { text-align: center; padding: 3rem; color: var(--gray-400); }
  `]
})
export class InventoryComponent implements OnInit {
  products: InventoryProduct[] = [];
  selectedCategory = '';
  loading = false;

  constructor(private inventoryService: InventoryService) {}

  ngOnInit() { this.loadProducts(); }

  loadProducts() {
    this.loading = true;
    this.inventoryService.getProducts(this.selectedCategory || undefined).subscribe({
      next: (res) => { this.products = res.products || []; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  getStockPercentage(product: InventoryProduct): number {
    const max = Math.max(product.reorderThreshold * 3, product.stockQuantity, 1);
    return Math.min((product.stockQuantity / max) * 100, 100);
  }
}
