import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class LoginGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    // Redirect logged-in users to the default page (e.g., /users)
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/users']);
      return false;
    }
    return true;
  }
}
