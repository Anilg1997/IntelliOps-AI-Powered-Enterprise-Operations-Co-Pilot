import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <div class="navbar-brand">
        <span class="logo">⚡</span>
        <span class="brand-text">IntelliOps</span>
      </div>
      <div class="navbar-links" *ngIf="authService.isAuthenticated()">
        <a routerLink="/orders" routerLinkActive="active">Orders</a>
        <a routerLink="/copilot" routerLinkActive="active">AI Co-Pilot</a>
      </div>
      <div class="navbar-right">
        <div class="navbar-status" *ngIf="authService.isAuthenticated()">
          <span class="status-dot online"></span>
          <span class="status-text">Ollama Ready</span>
        </div>
        <div class="user-menu" *ngIf="authService.isAuthenticated()">
          <span class="user-avatar">{{ (authService.user()?.fullName || 'U')[0] }}</span>
          <span class="user-name">{{ authService.user()?.fullName }}</span>
          <button class="btn-logout" (click)="logout()" title="Sign out">🚪</button>
        </div>
        <div class="auth-links" *ngIf="!authService.isAuthenticated()">
          <a routerLink="/login" class="nav-link" routerLinkActive="active">Sign In</a>
          <a routerLink="/register" class="nav-link btn-signup">Get Started</a>
        </div>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      display: flex; align-items: center; padding: 0 24px;
      height: 56px; background: linear-gradient(135deg, #1a237e, #283593);
      color: white; gap: 32px;
    }
    .navbar-brand { display: flex; align-items: center; gap: 8px; }
    .logo { font-size: 24px; }
    .brand-text { font-size: 20px; font-weight: 700; letter-spacing: -0.5px; }
    .navbar-links { display: flex; gap: 20px; flex: 1; }
    .navbar-links a { color: rgba(255,255,255,0.8); text-decoration: none; font-size: 14px; padding: 4px 0; border-bottom: 2px solid transparent; transition: all 0.2s; }
    .navbar-links a:hover, .navbar-links a.active { color: white; border-bottom-color: #7c4dff; }
    .navbar-right { display: flex; align-items: center; gap: 20px; }
    .navbar-status { display: flex; align-items: center; gap: 8px; font-size: 12px; }
    .status-dot { width: 8px; height: 8px; border-radius: 50%; }
    .online { background: #4caf50; box-shadow: 0 0 8px rgba(76,175,80,0.6); }
    .status-text { opacity: 0.9; }
    .user-menu { display: flex; align-items: center; gap: 8px; }
    .user-avatar { width: 32px; height: 32px; border-radius: 50%; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 600; }
    .user-name { font-size: 13px; opacity: 0.9; }
    .btn-logout { background: none; border: 1px solid rgba(255,255,255,0.3); border-radius: 6px; padding: 4px 8px; cursor: pointer; font-size: 14px; transition: background 0.2s; }
    .btn-logout:hover { background: rgba(255,255,255,0.1); }
    .auth-links { display: flex; align-items: center; gap: 12px; }
    .nav-link { color: rgba(255,255,255,0.8); text-decoration: none; font-size: 14px; transition: color 0.2s; }
    .nav-link:hover { color: white; }
    .btn-signup { background: rgba(255,255,255,0.15); padding: 6px 16px; border-radius: 6px; font-weight: 500; }
    .btn-signup:hover { background: rgba(255,255,255,0.25); color: white; }
  `]
})
export class NavbarComponent {
  authService = inject(AuthService);
  private router = inject(Router);

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
