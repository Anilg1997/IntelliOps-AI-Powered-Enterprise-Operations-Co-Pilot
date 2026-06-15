import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <div class="navbar-brand">
        <span class="logo">⚡</span>
        <span class="brand-text">IntelliOps</span>
      </div>
      <div class="navbar-links">
        <a routerLink="/orders" routerLinkActive="active">Orders</a>
        <a routerLink="/orders/new" routerLinkActive="active">New Order</a>
        <a routerLink="/copilot" routerLinkActive="active">AI Co-Pilot</a>
      </div>
      <div class="navbar-status">
        <span class="status-dot online"></span>
        <span class="status-text">Ollama Ready</span>
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
    .navbar-status { display: flex; align-items: center; gap: 8px; font-size: 12px; }
    .status-dot { width: 8px; height: 8px; border-radius: 50%; }
    .online { background: #4caf50; box-shadow: 0 0 8px rgba(76,175,80,0.6); }
    .status-text { opacity: 0.9; }
  `]
})
export class NavbarComponent {}
