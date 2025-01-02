import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

interface ErrorMessage {
  id: number;
  date: string;
  orderId: number;
  operation: string;
  message: string;
}

@Component({
  selector: 'app-error-history',
  standalone: true,
  templateUrl: './error-history.component.html',
  styleUrls: ['./error-history.component.css'],
  imports: [CommonModule],
})
export class ErrorHistoryComponent implements OnInit {
  errors: ErrorMessage[] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  isLoading = true;
  errorMessage = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchErrors();
  }

  fetchErrors(): void {
    const token = localStorage.getItem('authToken');
    this.isLoading = true;

    this.http
      .get<{ success: boolean; data: { content: ErrorMessage[]; totalPages: number } }>(
        `http://localhost:2511/errors`,
        {
          headers: { Authorization: `Bearer ${token}` },
          params: {
            page: this.currentPage,
            size: this.pageSize,
          },
        }
      )
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.errors = response.data.content;
            this.totalPages = response.data.totalPages;
          } else {
            this.errorMessage = 'Failed to fetch errors.';
          }
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching errors:', err);
          this.errorMessage = 'An error occurred while fetching errors.';
          this.isLoading = false;
        },
      });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) {
      return;
    }
    this.currentPage = page;
    this.fetchErrors();
  }
}
