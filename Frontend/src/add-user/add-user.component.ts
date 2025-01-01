import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-add-user',
  standalone: true,
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css'],
  imports: [
    FormsModule,
    CommonModule,
  ]
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

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.fetchAllPermissions();
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

  onSubmit(): void {
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
          // Step 2: Add permissions
          // this.addPermissions(this.user.email, this.user.permissions, token);
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
    const isChecked = (event.target as HTMLInputElement).checked; // Cast to HTMLInputElement to access `checked`

    if (isChecked) {
      // Add permission if it's checked and not already present
      if (!this.user.permissions.includes(permission)) {
        this.user.permissions.push(permission);
      }
    } else {
      // Remove permission if it's unchecked
      this.user.permissions = this.user.permissions.filter((p) => p !== permission);
    }
  }


}
