import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatMessage {
  response: string;
  conversationId: string;
}

export interface Conversation {
  id: string;
  userId: string;
  title: string;
  messages: { role: string; content: string; timestamp: string }[];
  createdAt: string;
  updatedAt: string;
}

@Injectable({ providedIn: 'root' })
export class CopilotService {
  private readonly API_URL = '/api/v1/copilot';

  constructor(private http: HttpClient) {}

  chat(message: string, conversationId?: string): Observable<ChatMessage> {
    const body: any = { message, userId: 'web-user' };
    if (conversationId) body.conversationId = conversationId;
    return this.http.post<ChatMessage>(`${this.API_URL}/chat`, body);
  }

  streamChat(message: string, conversationId?: string): EventSource {
    const body = JSON.stringify({ message, conversationId, userId: 'web-user' });
    // SSE via fetch for POST
    return new EventSource(`${this.API_URL}/stream?message=${encodeURIComponent(message)}`);
  }

  getConversations(): Observable<Conversation[]> {
    return this.http.get<Conversation[]>(`${this.API_URL}/conversations?userId=web-user`);
  }

  getConversation(id: string): Observable<Conversation> {
    return this.http.get<Conversation>(`${this.API_URL}/conversations/${id}`);
  }
}
