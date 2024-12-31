import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    // Check if the user is authenticated
    if (!this.authService.isAuthenticated()) {
      // Redirect unauthenticated users to the login page
      this.router.navigate(['/']);
      return false;
    }
    return true;
  }
}
