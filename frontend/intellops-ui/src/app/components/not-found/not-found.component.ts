import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="not-found-container">
      <div class="not-found-card">
        <div class="error-code">404</div>
        <div class="error-icon">🔍</div>
        <h1>Page Not Found</h1>
        <p class="error-description">
          The page you're looking for doesn't exist or has been moved.
          Please check the URL or navigate back to a known page.
        </p>
        <div class="error-actions">
          <a routerLink="/dashboard" class="btn-primary">Go to Dashboard</a>
          <a routerLink="/orders" class="btn-secondary">View Orders</a>
        </div>
        <div class="error-help">
          <p>Need help? Try these:</p>
          <div class="help-links">
            <a routerLink="/copilot">🤖 Ask AI Co-Pilot</a>
            <a routerLink="/login">🔐 Sign In</a>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .not-found-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
      padding: 20px;
    }
    .not-found-card {
      background: white;
      border-radius: 20px;
      padding: 60px 48px;
      text-align: center;
      max-width: 520px;
      width: 100%;
      box-shadow: 0 20px 60px rgba(0,0,0,0.1);
    }
    .error-code {
      font-size: 120px;
      font-weight: 800;
      background: linear-gradient(135deg, #667eea, #764ba2);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      line-height: 1;
      margin-bottom: -10px;
    }
    .error-icon { font-size: 48px; margin-bottom: 16px; }
    h1 { margin: 0 0 12px; font-size: 24px; color: #1a1a2e; font-weight: 700; }
    .error-description { color: #666; font-size: 14px; line-height: 1.6; margin: 0 0 32px; }
    .error-actions { display: flex; gap: 12px; justify-content: center; margin-bottom: 32px; }
    .btn-primary {
      padding: 12px 28px; background: linear-gradient(135deg, #667eea, #764ba2);
      color: white; border-radius: 8px; text-decoration: none; font-size: 14px; font-weight: 600;
      transition: opacity 0.2s;
    }
    .btn-primary:hover { opacity: 0.9; }
    .btn-secondary {
      padding: 12px 28px; background: white; color: #667eea; border: 2px solid #667eea;
      border-radius: 8px; text-decoration: none; font-size: 14px; font-weight: 600;
      transition: all 0.2s;
    }
    .btn-secondary:hover { background: #f0f2ff; }
    .error-help { border-top: 1px solid #eee; padding-top: 24px; }
    .error-help p { font-size: 13px; color: #888; margin: 0 0 12px; }
    .help-links { display: flex; gap: 20px; justify-content: center; }
    .help-links a { font-size: 13px; color: #667eea; text-decoration: none; font-weight: 500; }
    .help-links a:hover { text-decoration: underline; }
  `]
})
export class NotFoundComponent {}
