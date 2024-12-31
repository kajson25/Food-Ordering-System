import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {AuthService} from '../services/auth.service';

@Component({
  selector: 'app-add-user',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css'],
})
export class AddUserComponent {
  user = { firstName: '', lastName: '', email: '', password: '', permissions: '' };
  errorMessage: string = '';
  successMessage: string = '';
  isSaving: boolean = false;

  constructor(private http: HttpClient, private router: Router, private authService: AuthService) {}

  onSubmit(): void {
    // Check if any field is empty
    if (!this.user.firstName || !this.user.lastName || !this.user.email || !this.user.password || !this.user.permissions) {
      this.errorMessage = 'All fields are required.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.isSaving = true;

    const token = localStorage.getItem('authToken');
    if (!token) {
      this.errorMessage = 'Authentication token missing. Please log in.';
      this.isSaving = false;
      return;
    }

    // Send user data to the backend
    this.http
      .post('http://localhost:2511/users/create', this.user, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .subscribe({
        next: () => {
          this.isSaving = false;
          this.successMessage = 'User added successfully!';
          setTimeout(() => {
            this.router.navigate(['/users']);
          }, 2000);
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = 'Failed to add user. Please try again.';
          console.error(err);
        },
      });
  }
  canCreate(): boolean {
    return this.authService.hasPermission('CAN_CREATE_USERS');
  }

}
