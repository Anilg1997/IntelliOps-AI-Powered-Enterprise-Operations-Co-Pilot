import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface UserProfile {
  id: number;
  email: string;
  fullName: string;
  phoneNumber: string;
  role: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: UserProfile;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'intellops_token';

  private userSignal = signal<UserProfile | null>(null);
  user = this.userSignal.asReadonly();
  isAuthenticated = computed(() => this.userSignal() !== null);

  constructor(private http: HttpClient) {
    this.loadUserFromToken();
  }

  register(email: string, password: string, fullName: string, phoneNumber?: string): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API_URL}/register`, {
      email, password, fullName, phoneNumber
    }).pipe(
      tap(res => this.handleAuthResponse(res.data))
    );
  }

  login(email: string, password: string): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.API_URL}/login`, {
      email, password
    }).pipe(
      tap(res => this.handleAuthResponse(res.data))
    );
  }

  getProfile(): Observable<ApiResponse<UserProfile>> {
    return this.http.get<ApiResponse<UserProfile>>(`${this.API_URL}/me`);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.userSignal.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private handleAuthResponse(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    this.userSignal.set(response.user);
  }

  private loadUserFromToken(): void {
    const token = this.getToken();
    if (token) {
      // Decode JWT to get basic user info without server call
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.userSignal.set({
          id: parseInt(payload.sub),
          email: payload.email,
          fullName: payload.email?.split('@')[0] || 'User',
          phoneNumber: '',
          role: payload.role?.replace('ROLE_', '') || 'USER',
          createdAt: ''
        });
        // Refresh profile from server
        this.getProfile().subscribe({
          next: (res) => this.userSignal.set(res.data),
          error: () => this.logout()
        });
      } catch {
        this.logout();
      }
    }
  }
}
