import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/notification/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div *ngFor="let toast of toastService.activeToasts()" class="toast" [class]="toast.type">
        <div class="toast-icon">
          <ng-container [ngSwitch]="toast.type">
            <span *ngSwitchCase="'success'">✅</span>
            <span *ngSwitchCase="'error'">❌</span>
            <span *ngSwitchCase="'warning'">⚠️</span>
            <span *ngSwitchCase="'info'">ℹ️</span>
          </ng-container>
        </div>
        <div class="toast-content">
          <span class="toast-title">{{ toast.title }}</span>
          <span class="toast-message">{{ toast.message }}</span>
        </div>
        <button class="toast-close" (click)="toastService.remove(toast.id)">✕</button>
        <div class="toast-progress"></div>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed; top: 16px; right: 16px; z-index: 9999;
      display: flex; flex-direction: column; gap: 8px; max-width: 400px;
    }
    .toast {
      display: flex; align-items: flex-start; gap: 12px;
      padding: 14px 16px; border-radius: 10px; background: white;
      box-shadow: 0 8px 32px rgba(0,0,0,0.15);
      animation: slideIn 0.3s ease; overflow: hidden;
      position: relative;
    }
    @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
    .toast.success { border-left: 4px solid #4caf50; }
    .toast.error { border-left: 4px solid #f44336; }
    .toast.warning { border-left: 4px solid #ff9800; }
    .toast.info { border-left: 4px solid #2196f3; }
    .toast-icon { font-size: 20px; flex-shrink: 0; }
    .toast-content { flex: 1; display: flex; flex-direction: column; gap: 2px; }
    .toast-title { font-size: 13px; font-weight: 600; color: #333; }
    .toast-message { font-size: 12px; color: #666; }
    .toast-close {
      background: none; border: none; color: #999; cursor: pointer;
      font-size: 14px; padding: 2px; flex-shrink: 0; transition: color 0.2s;
    }
    .toast-close:hover { color: #333; }
    .toast-progress {
      position: absolute; bottom: 0; left: 0; height: 3px;
      background: linear-gradient(90deg, transparent, currentColor);
      animation: progress 4s linear forwards;
    }
    .toast.success .toast-progress { color: #4caf50; }
    .toast.error .toast-progress { color: #f44336; }
    .toast.warning .toast-progress { color: #ff9800; }
    .toast.info .toast-progress { color: #2196f3; }
    @keyframes progress { from { width: 100%; } to { width: 0%; } }
  `]
})
export class ToastContainerComponent {
  toastService = inject(ToastService);
}
