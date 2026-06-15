import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subject, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface ChatResponse {
  sessionId: string;
  response: string;
}

export interface ChatMessage {
  id?: string;
  sessionId: string;
  role: 'user' | 'assistant';
  content: string;
  createdAt?: string;
}

/**
 * Service for communicating with the AI Co-Pilot backend.
 * Supports both regular HTTP and SSE streaming modes.
 */
@Injectable({
  providedIn: 'root'
})
export class CopilotService {

  private baseUrl = 'http://localhost:8083/api/copilot';

  constructor(private http: HttpClient) {}

  /**
   * Creates a new chat session.
   */
  createSession(): Observable<{ sessionId: string; message: string }> {
    return this.http.post<{ sessionId: string; message: string }>(
      `${this.baseUrl}/session`, {}
    );
  }

  /**
   * Sends a message to the AI co-pilot and receives a complete response.
   */
  sendMessage(sessionId: string, message: string): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.baseUrl}/chat`, {
      sessionId,
      message
    });
  }

  /**
   * Sends a message and receives the response as an SSE stream of tokens.
   * Returns an Observable that emits each token event.
   */
  sendMessageStream(sessionId: string, message: string): Observable<string> {
    return new Observable<string>(observer => {
      const eventSource = new EventSource(
        `${this.baseUrl}/chat/stream?sessionId=${encodeURIComponent(sessionId)}&message=${encodeURIComponent(message)}`
      );

      eventSource.addEventListener('token', (event: any) => {
        observer.next(event.data);
      });

      eventSource.addEventListener('done', () => {
        eventSource.close();
        observer.complete();
      });

      eventSource.addEventListener('error', (event: any) => {
        eventSource.close();
        observer.error('SSE connection error');
      });

      return () => eventSource.close();
    });
  }

  /**
   * Retrieves the conversation history for a session.
   */
  getHistory(sessionId: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.baseUrl}/history/${sessionId}`);
  }

  /**
   * Clears a chat session.
   */
  clearSession(sessionId: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/session/${sessionId}`);
  }
}
