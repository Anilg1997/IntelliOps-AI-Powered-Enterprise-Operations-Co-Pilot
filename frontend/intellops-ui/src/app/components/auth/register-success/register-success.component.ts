import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-register-success',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="auth-container">
      <div class="auth-card animate-fadeIn" style="text-align: center;">
        <div class="success-icon"><i class="fas fa-check-circle"></i></div>
        <h1>Account Created!</h1>
        <p>Your account has been successfully created. You can now sign in to access IntelliOps.</p>
        <a routerLink="/login" class="btn btn-primary" style="margin-top: 1.5rem; display: inline-flex;">Sign In</a>
      </div>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; align-items: center; justify-content: center; min-height: 100vh; background: linear-gradient(135deg, var(--gray-900), var(--gray-700)); }
    .auth-card { background: white; border-radius: 12px; padding: 2.5rem; max-width: 420px; box-shadow: var(--shadow-lg); }
    .success-icon { font-size: 4rem; color: var(--success); margin-bottom: 1rem; }
    h1 { font-size: 1.5rem; font-weight: 700; margin-bottom: 0.5rem; }
    p { color: var(--gray-500); font-size: 0.875rem; }
  `]
})
export class RegisterSuccessComponent {}
