import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

interface OrderDTO {
  id: number;
  status: string;
  active: boolean;
  createdBy: string;
  items: ItemDTO[];
}

interface ItemDTO {
  id: number;
  dish: DishDTO;
  quantity: number;
}

interface DishDTO {
  id: number;
  name: string;
  description: string;
  price: number;
}

@Component({
  selector: 'app-orders',
  standalone: true,
  templateUrl: './search-orders.component.html',
  styleUrls: ['./search-orders.component.css'],
  imports: [FormsModule, CommonModule],
})
export class SearchOrdersComponent implements OnInit {
  orders: OrderDTO[] = [];
  allStatuses = ['ORDERED', 'PREPARING', 'IN_DELIVERY', 'DELIVERED', 'CANCELED'];
  isLoading = true;
  errorMessage = '';
  searchCriteria = {
    statuses: [] as string[],
    dateFrom: '',
    dateTo: '',
  };
  isAdmin = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.checkAdminStatus();
    this.fetchOrders();
  }

  private checkAdminStatus(): void {
    const token = localStorage.getItem('authToken');
    if (token) {
      const decoded = JSON.parse(atob(token.split('.')[1]));
      this.isAdmin = decoded.isAdmin || false;
    }
  }

  private fetchOrders(): void {
    const token = localStorage.getItem('authToken');

    this.isLoading = true;
    this.http
      .get<{ success: boolean; data: OrderDTO[]; error?: string }>(
        `http://localhost:2511/orders/all`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.orders = response.data;
          } else {
            this.errorMessage = response.error || 'Failed to fetch orders.';
          }
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching orders:', err);
          this.errorMessage = 'An error occurred while fetching orders.';
          this.isLoading = false;
        },
      });
  }

  searchOrders(): void {
    const token = localStorage.getItem('authToken');

    this.isLoading = true;
    this.http
      .get<{ success: boolean; data: OrderDTO[]; error?: string }>(
        `http://localhost:2511/orders/search`,
        {
          headers: { Authorization: `Bearer ${token}` },
          params: {
            statuses: this.searchCriteria.statuses.join(','), // Convert to comma-separated string
            dateFrom: this.searchCriteria.dateFrom,
            dateTo: this.searchCriteria.dateTo,
          },
        }
      )
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.orders = response.data;
          } else {
            this.errorMessage = response.error || 'Failed to search orders.';
          }
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error searching orders:', err);
          this.errorMessage = 'An error occurred while searching orders.';
          this.isLoading = false;
        },
      });
  }
}
