import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = '/api/auth';
  private currentUser = signal<User | null>(null);

  user = this.currentUser.asReadonly();
  isAuthenticated = computed(() => !!this.getToken());

  constructor(private http: HttpClient, private router: Router) {
    this.loadStoredUser();
  }

  private loadStoredUser(): void {
    const userJson = localStorage.getItem('intellops_user');
    if (userJson) {
      try {
        this.currentUser.set(JSON.parse(userJson));
      } catch {
        localStorage.removeItem('intellops_user');
      }
    }
  }

  getToken(): string | null {
    return localStorage.getItem('intellops_token');
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, { email, password })
      .pipe(tap(res => this.handleAuthResponse(res)));
  }

  register(data: { email: string; password: string; firstName: string; lastName: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, data)
      .pipe(tap(res => this.handleAuthResponse(res)));
  }

  logout(): void {
    localStorage.removeItem('intellops_token');
    localStorage.removeItem('intellops_refresh_token');
    localStorage.removeItem('intellops_user');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getProfile(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/me`)
      .pipe(tap(user => {
        this.currentUser.set(user);
        localStorage.setItem('intellops_user', JSON.stringify(user));
      }));
  }

  private handleAuthResponse(res: AuthResponse): void {
    localStorage.setItem('intellops_token', res.token);
    localStorage.setItem('intellops_refresh_token', res.refreshToken);
    localStorage.setItem('intellops_user', JSON.stringify(res.user));
    this.currentUser.set(res.user);
  }
}
