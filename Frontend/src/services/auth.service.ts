import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:2511/users/login';
  private permissions: string[] = [];
  private loggedInUserEmail: string | null = null; // Store the logged-in user's email

  constructor(private http: HttpClient, private router: Router) {
    this.reloadPermissionsFromToken(); // Reload permissions on service initialization
  }

  login(email: string, password: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(this.apiUrl, { email, password }).pipe(
      tap({
        next: (response) => {
          localStorage.setItem('authToken', response.token);
          this.decodeToken(response.token); // Decode and store permissions
        },
        error: (error) => {
          console.error('Error in tap:', error);
        },
      })
    );
  }

  logout(): void {
    localStorage.removeItem('authToken');
    this.permissions = []; // Clear permissions
    this.loggedInUserEmail = null;
    this.router.navigate(['/']);
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('authToken');
  }

  getPermissions(): string[] {
    return this.permissions;
  }

  hasPermission(permission: string): boolean {
    return this.permissions.includes(permission);
  }

  getLoggedInUserEmail(): string | null {
    return this.loggedInUserEmail;
  }

  private decodeToken(token: string): void {
    try {
      const decoded: { permissions: string[]; sub: string } = jwtDecode(token);
      this.permissions = decoded.permissions || [];
      this.loggedInUserEmail = decoded.sub || null;
    } catch (error) {
      console.error('Error decoding token:', error);
    }
  }

  private reloadPermissionsFromToken(): void {
    const token = localStorage.getItem('authToken');
    if (token) {
      this.decodeToken(token); // Decode and store permissions if token exists
    }
  }
}
