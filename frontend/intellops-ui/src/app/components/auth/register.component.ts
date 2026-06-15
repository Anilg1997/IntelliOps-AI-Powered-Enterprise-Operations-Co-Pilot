import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <span class="logo">⚡</span>
          <h1>Create Account</h1>
          <p class="subtitle">Join IntelliOps Platform</p>
        </div>

        <form (ngSubmit)="onSubmit()" class="auth-form">
          <div class="form-group">
            <label for="fullName">Full Name</label>
            <input
              id="fullName"
              type="text"
              [(ngModel)]="fullName"
              name="fullName"
              placeholder="Enter your full name"
              required
              [disabled]="loading"
            />
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input
              id="email"
              type="email"
              [(ngModel)]="email"
              name="email"
              placeholder="Enter your email"
              required
              autocomplete="email"
              [disabled]="loading"
            />
          </div>

          <div class="form-group">
            <label for="phoneNumber">Phone Number (optional)</label>
            <input
              id="phoneNumber"
              type="tel"
              [(ngModel)]="phoneNumber"
              name="phoneNumber"
              placeholder="Enter your phone number"
              [disabled]="loading"
            />
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <input
              id="password"
              type="password"
              [(ngModel)]="password"
              name="password"
              placeholder="Create a strong password"
              required
              autocomplete="new-password"
              [disabled]="loading"
            />
          </div>

          <div class="form-group">
            <label for="confirmPassword">Confirm Password</label>
            <input
              id="confirmPassword"
              type="password"
              [(ngModel)]="confirmPassword"
              name="confirmPassword"
              placeholder="Confirm your password"
              required
              autocomplete="new-password"
              [disabled]="loading"
            />
          </div>

          <div *ngIf="error" class="error-message">{{ error }}</div>

          <button type="submit" class="btn-primary" [disabled]="loading || !isFormValid()">
            <span *ngIf="loading" class="spinner"></span>
            {{ loading ? 'Creating account...' : 'Create Account' }}
          </button>
        </form>

        <div class="auth-footer">
          Already have an account? <a routerLink="/login">Sign in</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #1a237e 0%, #283593 50%, #3949ab 100%);
      padding: 20px;
    }

    .auth-card {
      background: white;
      border-radius: 16px;
      padding: 40px;
      width: 100%;
      max-width: 440px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    }

    .auth-header {
      text-align: center;
      margin-bottom: 28px;
    }

    .logo {
      font-size: 48px;
      display: block;
      margin-bottom: 8px;
    }

    h1 {
      margin: 0;
      font-size: 26px;
      color: #1a237e;
      font-weight: 700;
    }

    .subtitle {
      color: #666;
      font-size: 13px;
      margin: 6px 0 0;
    }

    .auth-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 5px;
    }

    label {
      font-size: 13px;
      font-weight: 600;
      color: #444;
    }

    input {
      padding: 11px 14px;
      border: 1px solid #ddd;
      border-radius: 8px;
      font-size: 14px;
      outline: none;
      transition: border-color 0.2s, box-shadow 0.2s;
    }

    input:focus {
      border-color: #7c4dff;
      box-shadow: 0 0 0 3px rgba(124, 77, 255, 0.1);
    }

    input:disabled {
      background: #f5f5f5;
    }

    .error-message {
      background: #fef2f2;
      color: #dc2626;
      padding: 10px 14px;
      border-radius: 8px;
      font-size: 13px;
      border: 1px solid #fecaca;
    }

    .btn-primary {
      padding: 12px 24px;
      background: linear-gradient(135deg, #7c4dff, #651fff);
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      transition: opacity 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      margin-top: 4px;
    }

    .btn-primary:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .btn-primary:not(:disabled):hover {
      opacity: 0.9;
    }

    .spinner {
      width: 18px;
      height: 18px;
      border: 2px solid rgba(255,255,255,0.3);
      border-top-color: white;
      border-radius: 50%;
      animation: spin 0.6s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .auth-footer {
      text-align: center;
      margin-top: 20px;
      font-size: 13px;
      color: #666;
    }

    .auth-footer a {
      color: #7c4dff;
      text-decoration: none;
      font-weight: 600;
    }

    .auth-footer a:hover {
      text-decoration: underline;
    }
  `]
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  fullName = '';
  email = '';
  phoneNumber = '';
  password = '';
  confirmPassword = '';
  loading = false;
  error = '';

  isFormValid(): boolean {
    return !!(
      this.fullName &&
      this.email &&
      this.password &&
      this.password === this.confirmPassword &&
      this.password.length >= 6
    );
  }

  onSubmit(): void {
    if (!this.isFormValid()) return;

    this.loading = true;
    this.error = '';

    this.authService.register(this.email, this.password, this.fullName, this.phoneNumber || undefined).subscribe({
      next: () => {
        this.router.navigate(['/orders']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Registration failed. Please try again.';
        this.loading = false;
      }
    });
  }
}
