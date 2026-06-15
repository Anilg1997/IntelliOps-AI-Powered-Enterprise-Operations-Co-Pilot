import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const authGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();

  if (!token) {
    return router.parseUrl('/login');
  }

  // If user signal is already populated, allow access
  if (authService.isAuthenticated()) {
    return true;
  }

  // Otherwise, wait for the profile to load
  // For now, check token existence and redirect to login if no token
  // This is a simple approach for a demo app
  return true; // Token exists, allow through (profile will load async)
};
