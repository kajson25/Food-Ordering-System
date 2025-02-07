import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {AuthService} from '../services/auth.service';

@Component({
  selector: 'app-edit-user',
  standalone: true,
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.css'],
  imports: [FormsModule, CommonModule],
})
export class EditUserComponent implements OnInit {
  user: { firstName: string; lastName: string; email: string; password: string; isAdmin: boolean } | null = null;
  assignedPermissions: string[] = []; // Permissions already assigned to the user
  allPermissions: string[] = []; // All available permissions in the system
  isLoading: boolean = true;
  isSaving: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.checkPermissions();
    this.fetchUserDetails();
    this.fetchAllPermissions();
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
            if (!userPermissions.includes('CAN_UPDATE_USER')) {
              this.errorMessage = 'You do not have permission to edit users.';
              this.router.navigate(['/users']);
            }
          } else {
            this.errorMessage = 'Failed to verify permissions.';
            this.router.navigate(['/users']);
          }
        },
        error: (err) => {
          console.error('Permission check failed:', err);
          this.errorMessage = 'An error occurred while verifying permissions.';
          this.router.navigate(['/users']);
        },
      });
  }

  private fetchUserDetails(): void {
    const email = this.route.snapshot.paramMap.get('email');
    const token = localStorage.getItem('authToken');

    if (!email || !token) {
      this.errorMessage = 'Invalid request or authentication token is missing.';
      this.isLoading = false;
      return;
    }

    // Fetch user details
    this.http
      .get<{
        success: boolean;
        data: { firstName: string; lastName: string; email: string; password: string; isAdmin: boolean };
      }>(`http://localhost:2511/users/${email}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.user = response.data;
            this.fetchUserPermissions(email);
          } else {
            this.errorMessage = 'Failed to fetch user details.';
            this.isLoading = false;
          }
        },
        error: (err) => {
          console.error('Error fetching user details:', err);
          this.errorMessage = 'An error occurred while fetching user details.';
          this.isLoading = false;
        },
      });
  }

  private fetchUserPermissions(email: string): void {
    const token = localStorage.getItem('authToken');
    this.http
      .get<{ success: boolean; data: { permission: string }[] }>(
        `http://localhost:2511/permissions/${email}`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.assignedPermissions = response.data.map((p) => p.permission);
          } else {
            this.errorMessage = 'Failed to fetch user permissions.';
          }
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching user permissions:', err);
          this.errorMessage = 'An error occurred while fetching user permissions.';
          this.isLoading = false;
        },
      });
  }

  private fetchAllPermissions(): void {
    const token = localStorage.getItem('authToken');
    this.http
      .get<{ success: boolean; data: { permission: string }[] }>(
        `http://localhost:2511/permissions/all`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.allPermissions = response.data.map((p) => p.permission);
          } else {
            this.errorMessage = 'Failed to fetch all permissions.';
          }
        },
        error: (err) => {
          console.error('Error fetching all permissions:', err);
          this.errorMessage = 'An error occurred while fetching all permissions.';
        },
      });
  }

  togglePermission(permission: string): void {
    if (this.assignedPermissions.includes(permission)) {
      // Remove permission
      this.assignedPermissions = this.assignedPermissions.filter((p) => p !== permission);
    } else {
      // Add permission
      this.assignedPermissions.push(permission);
    }
  }

  saveChanges(): void {
    if (!this.user) return;

    this.successMessage = '';
    this.errorMessage = '';
    this.isSaving = true;

    const token = localStorage.getItem('authToken');
    const email = this.user.email;

    // Prepare the payload for the request
    const updatedUser = {
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      email: this.user.email,
      password: this.user.password, // Ensure password is included
      isAdmin: this.user.isAdmin,
      permissions: this.assignedPermissions, // Include permissions
    };

    this.http
      .put<{ success: boolean; data?: any; error?: string }>(
        `http://localhost:2511/users/update/${email}`,
        updatedUser,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (response) => {
          if (response.success) {
            this.successMessage = 'Changes saved successfully!';
            this.router.navigate(['/users']);
          } else {
            this.errorMessage = response.error || 'Failed to save changes.';
          }
          this.isSaving = false;
        },
        error: (err) => {
          console.error('Error saving changes:', err);
          this.errorMessage = 'An error occurred while saving changes.';
          this.isSaving = false;
        },
      });
  }
}
