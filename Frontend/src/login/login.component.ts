import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { FormsModule } from '@angular/forms'; // Import FormsModule
import { CommonModule } from '@angular/common'; // Import CommonModule for structural directives

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule], // Add FormsModule here
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent {
  email = '';
  password = '';
  errorMessage = '';
  isLoading = false;

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    if (!this.email || !this.password) {
      this.errorMessage = 'Email and password are required.';
      return;
    }

    this.errorMessage = ''; // Reset error message on resubmission
    this.isLoading = true; // Start loading state

    this.authService.login(this.email, this.password).subscribe({
      next: (response) => {
        if (response.success) {
          this.isLoading = false;
          this.router.navigate(['/users']);
        } else {
          this.handleError(response.error || 'Login failed.');
        }
      },
      error: (err) => {
        console.error('Login error:', err);
        this.handleError('Invalid email or password.');
      },
    });
  }

  private handleError(message: string): void {
    this.isLoading = false;
    this.errorMessage = message;
  }
}
