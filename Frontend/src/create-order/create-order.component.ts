import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

interface Dish {
  id: number;
  name: string;
  description: string;
  price: number;
}

@Component({
  selector: 'app-create-order',
  standalone: true,
  templateUrl: './create-order.component.html',
  styleUrls: ['./create-order.component.css'],
  imports: [FormsModule, CommonModule],
})
export class CreateOrderComponent implements OnInit {
  dishes: Dish[] = []; // Catalog of dishes
  selectedDishes: { dish: Dish; quantity: number }[] = []; // Dishes selected by the user
  scheduledTime: string | null = null; // Scheduled time for the order in ISO 8601 format
  isLoading = true;
  errorMessage = '';
  successMessage = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchDishes();
  }

  private fetchDishes(): void {
    const token = localStorage.getItem('authToken');
    this.http
      .get<{ success: boolean; data: Dish[] }>('http://localhost:2511/dishes/all', {
        headers: { Authorization: `Bearer ${token}` },
      })
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.dishes = response.data;
          } else {
            this.errorMessage = 'Failed to fetch dishes.';
          }
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching dishes:', err);
          this.errorMessage = 'An error occurred while fetching dishes.';
          this.isLoading = false;
        },
      });
  }

  addDishToOrder(dish: Dish): void {
    const existingDish = this.selectedDishes.find((item) => item.dish.id === dish.id);
    if (existingDish) {
      existingDish.quantity++;
    } else {
      this.selectedDishes.push({ dish, quantity: 1 });
    }
  }

  removeDishFromOrder(dishId: number): void {
    this.selectedDishes = this.selectedDishes.filter((item) => item.dish.id !== dishId);
  }

  submitOrder(): void {
    const token = localStorage.getItem('authToken');
    if (!this.selectedDishes.length) {
      this.errorMessage = 'Please add at least one dish to your order.';
      return;
    }

    const orderRequest = {
      dishIds: this.selectedDishes.map((item) => item.dish.id),
      quantities: this.selectedDishes.map((item) => item.quantity),
      scheduledTime: this.scheduledTime || null, // Include scheduled time if provided
    };

    this.http
      .post<{ success: boolean; error?: string }>(
        'http://localhost:2511/orders/new-order',
        orderRequest,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.successMessage = 'Order created successfully!';
            this.selectedDishes = []; // Reset form
            this.scheduledTime = null;
          } else {
            this.errorMessage = response.error || 'Failed to create order.';
          }
        },
        error: (err) => {
          console.error('Error creating order:', err);
          this.errorMessage = 'An error occurred while creating the order.';
        },
      });
  }
}
