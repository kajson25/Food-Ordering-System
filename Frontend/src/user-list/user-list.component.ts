import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../services/auth.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css'],
})
export class UserListComponent implements OnInit {
  users: Array<{ firstName: string; lastName: string; email: string; isAdmin: boolean; permissions: string[] }> = [];
  errorMessage: string = '';
  isLoading: boolean = true;

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit(): void {
    this.fetchUsersWithPermissions();
  }

  private fetchUsersWithPermissions(): void {
    const token = localStorage.getItem('authToken');
    const userEmail = this.authService.getLoggedInUserEmail();

    if (!token || !userEmail) {
      this.errorMessage = 'Authentication token or email is missing. Please log in.';
      this.isLoading = false;
      return;
    }

    // Fetch all users
    this.http
      .get<{
        success: boolean;
        error?: string;
        data?: { firstName: string; lastName: string; email: string; isAdmin: boolean }[];
      }>(`http://localhost:2511/users/all?email=${encodeURIComponent(userEmail)}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .subscribe({
        next: (response) => {
          if (response.success && response.data) {
            const users = response.data;
            const permissionRequests = users.map((user) =>
              this.http.get<{
                success: boolean;
                error?: string;
                data?: { permission: string }[];
              }>(`http://localhost:2511/permissions/${user.email}`, {
                headers: { Authorization: `Bearer ${token}` },
              })
            );

            // Fetch permissions for all users
            forkJoin(permissionRequests).subscribe({
              next: (permissionsResponses) => {
                this.users = users.map((user, index) => ({
                  ...user,
                  permissions: permissionsResponses[index]?.data?.map((p) => p.permission) || [],
                }));
                this.isLoading = false;
              },
              error: (err) => {
                this.errorMessage = 'Failed to fetch permissions for users.';
                console.error('Permission fetch error:', err);
                this.isLoading = false;
              },
            });
          } else {
            this.errorMessage = response.error || 'Failed to fetch users.';
            this.isLoading = false;
          }
        },
        error: (err) => {
          this.errorMessage = 'An error occurred while fetching users. Please try again.';
          console.error('Fetch users error:', err);
          this.isLoading = false;
        },
      });
  }

  canAdd(): boolean {
    return this.authService.hasPermission('CAN_CREATE_USER');
  }

  canEdit(): boolean {
    return this.authService.hasPermission('CAN_UPDATE_USER');
  }

  canDelete(): boolean {
    return this.authService.hasPermission('CAN_DELETE_USER');
  }

  onAddUser(): void {
    if (this.canAdd()) {
      window.location.href = `/add-user`;
    } else {
      alert('You do not have permission to add users.');
    }
  }

  onEditUser(email: string): void {
    if (this.canEdit()) {
      window.location.href = `/edit/${email}`;
    } else {
      alert('You do not have permission to edit users.');
    }
  }

  onDeleteUser(email: string): void {
    if (!this.canDelete()) {
      alert('You do not have permission to delete users.');
      return;
    }

    const confirmDeletion = confirm(`Are you sure you want to delete the user with email: ${email}?`);
    if (!confirmDeletion) {
      return;
    }

    const token = localStorage.getItem('authToken');
    if (!token) {
      alert('Authentication token missing. Please log in.');
      return;
    }

    this.http
      .delete(`http://localhost:2511/users/delete/${email}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .subscribe({
        next: () => {
          alert(`User with email ${email} deleted successfully.`);
          this.users = this.users.filter((user) => user.email !== email);
        },
        error: (err) => {
          console.error('Error deleting user:', err);
          alert('Failed to delete the user. Please try again.');
        },
      });
  }
}
