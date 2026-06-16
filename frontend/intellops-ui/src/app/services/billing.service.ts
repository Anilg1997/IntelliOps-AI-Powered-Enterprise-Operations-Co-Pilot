import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Invoice {
  id: number;
  invoiceNumber: string;
  orderNumber: string;
  customerName: string;
  totalAmount: number;
  taxAmount: number;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  transactionId: string;
  issueDate: string;
  dueDate: string;
  paidDate: string;
}

@Injectable({ providedIn: 'root' })
export class BillingService {
  private readonly API_URL = '/api/v1/billing';

  constructor(private http: HttpClient) {}

  getInvoices(status?: string): Observable<Invoice[]> {
    let url = `${this.API_URL}/invoices`;
    if (status) url += `?status=${status}`;
    return this.http.get<Invoice[]>(url);
  }

  getInvoice(invoiceNumber: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.API_URL}/invoices/${invoiceNumber}`);
  }

  getInvoiceByOrder(orderNumber: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.API_URL}/invoices/order/${orderNumber}`);
  }

  getStats(): Observable<any> {
    return this.http.get(`${this.API_URL}/stats`);
  }

  processPayment(invoiceNumber: string, paymentMethod: string, transactionId: string): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.API_URL}/invoices/${invoiceNumber}/pay`, { paymentMethod, transactionId });
  }
}
