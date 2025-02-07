import { Injectable } from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
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

  // canActivate(route: ActivatedRouteSnapshot): boolean {
  //   if (!this.authService.isAuthenticated()) {
  //     this.router.navigate(['/']); // Redirect to login
  //     return false;
  //   }
  //
  //   const requiredPermission = route.data['requiredPermission'];
  //   if (requiredPermission && !this.authService.hasPermission(requiredPermission)) {
  //     this.router.navigate(['/users']); // Redirect to unauthorized page
  //     return false;
  //   }
  //
  //   return true;
  // }
}
