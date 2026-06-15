import { Injectable, inject } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';
import gql from 'graphql-tag';

export interface Customer {
  id: string;
  name: string;
  email: string;
  phoneNumber?: string;
  address?: string;
}

export interface Product {
  id: string;
  name: string;
  sku: string;
  price: number;
  category?: string;
}

export interface OrderLineItem {
  id: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  product: Product;
}

export interface Order {
  id: string;
  orderNumber: string;
  status: string;
  statusReason?: string;
  totalAmount: number;
  createdAt: string;
  updatedAt?: string;
  customer: Customer;
  lineItems?: OrderLineItem[];
}

const GET_ORDER = gql`
  query GetOrder($orderNumber: String!) {
    order(orderNumber: $orderNumber) {
      id
      orderNumber
      status
      statusReason
      totalAmount
      createdAt
      updatedAt
      customer { id name email phoneNumber address }
      lineItems {
        id quantity unitPrice subtotal
        product { id name sku price category }
      }
    }
  }
`;

const GET_ALL_ORDERS = gql`
  query GetAllOrders {
    allOrders {
      id
      orderNumber
      status
      totalAmount
      createdAt
      customer { id name email }
    }
  }
`;

@Injectable({ providedIn: 'root' })
export class OrderService {
  private apollo = inject(Apollo);

  getOrder(orderNumber: string): Observable<Order> {
    return this.apollo
      .watchQuery<{ order: Order }>({ query: GET_ORDER, variables: { orderNumber } })
      .valueChanges.pipe(map(result => result.data.order));
  }

  getAllOrders(): Observable<Order[]> {
    return this.apollo
      .watchQuery<{ allOrders: Order[] }>({ query: GET_ALL_ORDERS })
      .valueChanges.pipe(map(result => result.data.allOrders));
  }
}
