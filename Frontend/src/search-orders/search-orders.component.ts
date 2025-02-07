import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {AuthService} from '../services/auth.service';

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
    userId: '',
  };
  isAdmin = false;

  constructor(private http: HttpClient, private router: Router, public authService: AuthService) {}

  ngOnInit(): void {
    this.checkPermissions();
  }

  private checkPermissions(): void {
    const token = localStorage.getItem('authToken');
    const userEmail = this.authService.getLoggedInUserEmail();

    if (!token || !userEmail) {
      this.errorMessage = 'Authentication token or user email is missing.';
      this.router.navigate(['/login']);
      return;
    }

    this.http
      .get<{ success: boolean; data?: { permission: string }[] }>(
        `http://localhost:2511/permissions/${userEmail}`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success) {
            const userPermissions = response.data?.map((p) => p.permission) || [];
            if (!userPermissions.includes('CAN_SEARCH_ORDER')) {
              this.errorMessage = 'You do not have permission to search orders.';
              this.router.navigate(['/home']); // Redirect to a fallback page
            } else {
              this.fetchOrders(); // Fetch orders only if the permission exists
            }
          } else {
            this.errorMessage = 'Failed to verify permissions.';
            this.router.navigate(['/home']);
          }
        },
        error: (err) => {
          console.error('Permission check failed:', err);
          this.errorMessage = 'An error occurred while verifying permissions.';
          this.router.navigate(['/home']);
        },
      });
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
            userId: this.searchCriteria.userId,
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

  cancelOrder(orderId: number): void {
    const token = localStorage.getItem('authToken');

    if (!token) {
      this.errorMessage = 'Authentication token is missing. Please log in.';
      this.router.navigate(['/']);
      return;
    }

    if (!confirm(`Are you sure you want to cancel order #${orderId}?`)) {
      return; // Stop if user cancels
    }

    this.http
      .post<{ success: boolean; data?: OrderDTO; error?: string }>(
        `http://localhost:2511/orders/${orderId}/cancel`,
        {}, // Empty body required for POST
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            // Update the order list with the new status
            this.orders = this.orders.map((order) =>
              order.id === orderId ? { ...order, status: 'CANCELED' } : order
            );
          } else {
            this.errorMessage = response.error || 'Failed to cancel order.';
          }
        },
        error: (err) => {
          console.error('Error canceling order:', err);
          this.errorMessage = 'An error occurred while canceling the order.';
        },
      });
  }

}
