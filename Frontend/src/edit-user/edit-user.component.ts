import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import {AuthService} from '../services/auth.service';

@Component({
  selector: 'app-edit-user',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.css'],
})
export class EditUserComponent implements OnInit {
  user: { firstName: string; lastName: string; email: string; permissions: string[] } | null = null;
  errorMessage: string = '';
  isLoading: boolean = true;
  isSaving: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    const userId = this.route.snapshot.paramMap.get('id');
    const token = localStorage.getItem('authToken');

    if (!userId || !token) {
      this.errorMessage = 'Invalid user ID or authentication token.';
      this.isLoading = false;
      return;
    }

    console.log(userId)
    // Fetch user data - get user
    this.http
      .get<{ firstName: string; lastName: string; email: string; permissions: string[] }>(
        `http://localhost:2511/users/${userId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: (user) => {
          this.user = user;
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Failed to fetch user details.';
          console.error(err);
          this.isLoading = false;
        },
      });
  }

  onSave(): void {
    if (!this.user) return;

    const token = localStorage.getItem('authToken');
    if (!token) {
      this.errorMessage = 'Authentication token missing. Please log in.';
      return;
    }

    this.isSaving = true;
    this.http
      .put(
        `http://localhost:2511/users/update/${this.user.email}`, // Assuming email is the unique identifier
        this.user,
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .subscribe({
        next: () => {
          this.isSaving = false;
          alert('User updated successfully!');
          this.router.navigate(['/users']);
        },
        error: (err) => {
          this.errorMessage = 'Failed to update user.';
          console.error(err);
          this.isSaving = false;
        },
      });
  }
  canCreate(): boolean {
    return this.authService.hasPermission('CAN_UPDATE_USERS');
  }
}
