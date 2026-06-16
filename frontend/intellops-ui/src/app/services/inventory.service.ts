import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface InventoryProduct {
  id: string;
  sku: string;
  name: string;
  description: string;
  price: number;
  category: string;
  stockQuantity: number;
  reorderThreshold: number;
  active: boolean;
}

export interface InventoryResponse {
  products: InventoryProduct[];
  totalCount: number;
  page: number;
  pageSize: number;
}

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly API_URL = '/api/v1/inventory';

  constructor(private http: HttpClient) {}

  getProducts(category?: string, page = 0, pageSize = 20): Observable<InventoryResponse> {
    let url = `${this.API_URL}/products?page=${page}&pageSize=${pageSize}`;
    if (category) url += `&category=${category}`;
    return this.http.get<InventoryResponse>(url);
  }

  getProduct(productId: string): Observable<InventoryProduct> {
    return this.http.get<InventoryProduct>(`${this.API_URL}/products/${productId}`);
  }

  checkStock(productId: string, quantity = 1): Observable<any> {
    return this.http.get(`${this.API_URL}/stock/${productId}?quantity=${quantity}`);
  }
}
