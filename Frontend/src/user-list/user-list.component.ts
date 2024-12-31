import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import {AuthService} from '../services/auth.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css'],
})
export class UserListComponent implements OnInit {
  users: Array<{ firstName: string; lastName: string; email: string; permissions: string[] }> = [];
  errorMessage: string = '';
  isLoading: boolean = true;

  constructor(private http: HttpClient, public authService: AuthService) {}

  ngOnInit(): void {
    const token = localStorage.getItem('authToken');
    if (!token) {
      this.errorMessage = 'Authentication token missing. Please log in.';
      this.isLoading = false;
      return;
    }

    this.http
      .get<{ firstName: string; lastName: string; email: string; permissions: string[] }[]>('http://localhost:2511/users/all', {
        headers: { Authorization: `Bearer ${token}` },
      })
      .subscribe({
        next: (response) => {
          this.users = response;
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Failed to fetch users. Please try again later.';
          console.error(err);
          this.isLoading = false;
        },
      });
  }

  canAdd(): boolean {
    return this.authService.hasPermission('CAN_CREATE_USERS');
  }

  canEdit(): boolean {
    return this.authService.hasPermission('CAN_UPDATE_USERS');
  }

  canDelete(): boolean {
    return this.authService.hasPermission('CAN_DELETE_USERS');
  }

  onAddUser(): void {
    // Navigate to the add page for the selected user
    if (this.canAdd()) {
      window.location.href = `/add-user`;
    } else {
      alert('You do not have permission to add users.');
    }
  }


  onEditUser(userId: string): void {
    // Navigate to the edit page for the selected user
    if (this.canEdit()) {
      window.location.href = `/edit/${userId}`;
    } else {
      alert('You do not have permission to edit users.');
    }
  }

  onDeleteUser(userId: string): void {
    if (!this.canDelete()) {
      alert('You do not have permission to delete users.');
      return;
    }

    // Confirm deletion
    const confirmDeletion = confirm(`Are you sure you want to delete the user with ID: ${userId}?`);
    if (!confirmDeletion) {
      return;
    }

    const token = localStorage.getItem('authToken'); // Retrieve auth token
    if (!token) {
      alert('You are not authenticated. Please log in.');
      return;
    }

    // Call backend to delete user
    this.http
      .delete(`http://localhost:2511/users/delete/${userId}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .subscribe({
        next: () => {
          alert(`User ${userId} deleted successfully!`);
          // Refresh the user list after successful deletion
          this.users = this.users.filter(user => user.email !== userId);
        },
        error: (err) => {
          console.error('Error deleting user:', err);
          alert('Failed to delete user. Please try again.');
        },
      });
  }

}
