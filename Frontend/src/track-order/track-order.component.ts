import {Component, OnDestroy, OnInit} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

interface DishDTO {
  id: number;
  name: string;
  description: string;
  price: number;
}

interface OrderItemDTO {
  id: number;
  dish: DishDTO;
  quantity: number;
}

interface OrderDTO {
  id: number;
  status: string;
  active: boolean;
  createdBy: string;
  items: OrderItemDTO[];
}

@Component({
  selector: 'app-track-order',
  standalone: true,
  templateUrl: './track-order.component.html',
  styleUrls: ['./track-order.component.css'],
  imports: [FormsModule, CommonModule],
})
export class TrackOrderComponent implements OnInit, OnDestroy {
  order: OrderDTO | null = null;
  isLoading: boolean = true;
  errorMessage: string = '';
  private refreshInterval: any; // Stores the interval reference

  constructor(private http: HttpClient, private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.fetchMostRecentOrder();
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval); // Stop polling when component is destroyed
    }
  }

  private fetchMostRecentOrder(): void {
    const token = localStorage.getItem('authToken');
    if (!token) {
      this.errorMessage = 'Authentication token is missing. Please log in.';
      this.router.navigate(['/']);
      return;
    }

    this.http
      .get<{ success: boolean; data?: OrderDTO; error?: string }>(
        `http://localhost:2511/orders/recent`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.order = response.data;
            this.startAutoRefresh(); // Start polling for status updates
          } else {
            this.errorMessage = response.error || 'No recent orders found.';
          }
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching recent order:', err);
          this.errorMessage = 'An error occurred while fetching recent order.';
          this.isLoading = false;
        },
      });
  }

  private startAutoRefresh(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval); // Prevent multiple intervals
    }

    this.refreshInterval = setInterval(() => {
      if (this.order && this.order.status !== 'DELIVERED') {
        this.trackOrder();
      } else {
        clearInterval(this.refreshInterval); // Stop polling if order is delivered
      }
    }, 6000); // Poll every 6 seconds
  }

  private trackOrder(): void {
    if (!this.order) return;

    const token = localStorage.getItem('authToken');
    this.http
      .get<{ success: boolean; data?: string; error?: string }>(
        `http://localhost:2511/orders/${this.order.id}/track`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.order!.status = response.data;

            // Stop auto-refresh when order is delivered
            if (this.order!.status === 'DELIVERED') {
              clearInterval(this.refreshInterval);
            }
          } else {
            this.errorMessage = response.error || 'Failed to track order.';
          }
        },
        error: (err) => {
          console.error('Error tracking order:', err);
          this.errorMessage = 'An error occurred while tracking the order.';
        },
      });
  }

  refreshStatus(): void {
    if (!this.order) return;

    const token = localStorage.getItem('authToken');
    this.http
      .get<{ success: boolean; data?: string; error?: string }>(
        `http://localhost:2511/orders/${this.order.id}/track`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.order!.status = response.data; // Update status in order object
          } else {
            this.errorMessage = response.error || 'Failed to track order.';
          }
        },
        error: (err) => {
          console.error('Error tracking order:', err);
          this.errorMessage = 'An error occurred while tracking the order.';
        },
      });
  }
}
