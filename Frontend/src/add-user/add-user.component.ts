import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {AuthService} from '../services/auth.service';

@Component({
  selector: 'app-add-user',
  standalone: true,
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css'],
  imports: [FormsModule, CommonModule],
})
export class AddUserComponent implements OnInit {
  user = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    isAdmin: false,
    permissions: [] as string[], // Selected permissions
  };
  allPermissions: string[] = []; // Available permissions
  isSaving: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(private http: HttpClient, private router: Router, private authService: AuthService) {}

  ngOnInit(): void {
    this.checkPermissions();
    this.fetchAllPermissions();
  }

  private checkPermissions(): void {
    const token = localStorage.getItem('authToken');
    if (!token) {
      this.errorMessage = 'Authentication token is missing. Please log in.';
      this.router.navigate(['/login']);
      return;
    }

    const userEmail = this.authService.getLoggedInUserEmail();
    if (!userEmail) {
      this.errorMessage = 'Logged-in user email is missing.';
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
            if (!userPermissions.includes('CAN_CREATE_USER')) {
              this.errorMessage = 'You do not have permission to create users.';
              this.router.navigate(['/users']);
            }
          } else {
            this.errorMessage = 'Failed to fetch user permissions.';
            this.router.navigate(['/users']);
          }
        },
        error: (err) => {
          console.error('Error checking permissions:', err);
          this.errorMessage = 'An error occurred while checking permissions.';
          this.router.navigate(['/users']);
        },
      });
  }

  private fetchAllPermissions(): void {
    const token = localStorage.getItem('authToken');
    if (!token) {
      this.errorMessage = 'Authentication token is missing. Please log in.';
      return;
    }

    this.http
      .get<{ success: boolean; data: { permission: string }[] }>(
        `http://localhost:2511/permissions/all`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.allPermissions = response.data.map((p) => p.permission);
          } else {
            this.errorMessage = 'Failed to fetch permissions.';
          }
        },
        error: (err) => {
          console.error('Error fetching permissions:', err);
          this.errorMessage = 'An error occurred while fetching permissions.';
        },
      });
  }

  get isFormValid(): boolean {
    const { firstName, lastName, email, password } = this.user;
    return (
      firstName.trim() !== '' &&
      lastName.trim() !== '' &&
      email.trim() !== '' &&
      this.validateEmail(email) &&
      password.trim() !== ''
    );
  }

  protected validateEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // Basic email format validation
    return emailRegex.test(email);
  }

  onSubmit(): void {
    if (!this.isFormValid) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    this.successMessage = '';
    this.errorMessage = '';
    this.isSaving = true;

    const token = localStorage.getItem('authToken');
    if (!token) {
      this.errorMessage = 'Authentication token is missing. Please log in.';
      this.isSaving = false;
      return;
    }

    // Step 1: Create the user
    this.http
      .post(
        `http://localhost:2511/users/create`,
        { ...this.user }, // Include the user object as-is
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: () => {
          this.router.navigate(['/users']);
        },
        error: (err) => {
          console.error('Error adding user:', err);
          this.errorMessage = 'Failed to add user. Please try again.';
          this.isSaving = false;
        },
      });
  }

  onPermissionChange(permission: string, event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;

    if (isChecked) {
      if (!this.user.permissions.includes(permission)) {
        this.user.permissions.push(permission);
      }
    } else {
      this.user.permissions = this.user.permissions.filter((p) => p !== permission);
    }
  }
}
