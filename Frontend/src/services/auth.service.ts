import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { jwtDecode } from 'jwt-decode'; // Correct import for jwt-decode

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:2511/auth/login';
  private permissions: string[] = [];
  private loggedInUserEmail: string | null = null;

  constructor(private http: HttpClient, private router: Router) {
    this.reloadPermissionsFromToken(); // Reload permissions on service initialization
  }

  login(email: string, password: string): Observable<{ success: boolean; data: string; error: string | null }> {
    return this.http.post<{ success: boolean; data: string; error: string | null }>(this.apiUrl, { email, password }).pipe(
      tap({
        next: (response) => {
          if (response.success && response.data) {
            localStorage.setItem('authToken', response.data); // Store the token from `data`
            this.decodeToken(response.data); // Decode and store permissions, email
          } else {
            console.error('Login failed:', response.error || 'Unknown error');
          }
        },
        error: (error) => {
          console.error('Error during login request:', error);
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
    if(!this.isAuthenticated())
      return false;
    return this.permissions.includes(permission);
  }

  getLoggedInUserEmail(): string {
    return <string>this.loggedInUserEmail;
  }

  private decodeToken(token: string): void {
    try {
      const decoded: { sub: string; id?: number; permissions: string[] } = jwtDecode(token);
      this.loggedInUserEmail = decoded.sub || null;
      this.permissions = decoded.permissions || [];
    } catch (error) {
      console.error('Error decoding token:', error);
      this.permissions = [];
      this.loggedInUserEmail = null;
    }
  }

  private reloadPermissionsFromToken(): void {
    const token = localStorage.getItem('authToken');
    if (token) {
      this.decodeToken(token); // Decode and store permissions and email if token exists
    }
  }
}
