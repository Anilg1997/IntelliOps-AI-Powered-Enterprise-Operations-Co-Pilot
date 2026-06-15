import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CopilotService, ChatMessage } from '../../services/copilot.service';
import { Subscription } from 'rxjs';

interface Message {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  isStreaming?: boolean;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="chat-container">
      <!-- Header -->
      <div class="chat-header">
        <div class="header-content">
          <span class="header-icon">🤖</span>
          <div>
            <h2>AI Co-Pilot</h2>
            <p class="subtitle">Enterprise Operations Assistant</p>
          </div>
        </div>
        <div class="header-actions">
          <span class="status-indicator" [class.online]="sessionId !== null">
            {{ sessionId ? 'Connected' : 'Disconnected' }}
          </span>
          <button *ngIf="sessionId" class="btn-clear" (click)="clearChat()" title="Clear chat">
            🗑️ Clear
          </button>
        </div>
      </div>

      <!-- Messages -->
      <div class="messages-container" #messagesContainer>
        <div *ngIf="messages.length === 0" class="welcome-message">
          <div class="welcome-icon">🤖</div>
          <h3>Welcome to IntelliOps Co-Pilot</h3>
          <p>Ask me anything about orders, inventory, or troubleshooting.</p>
          <div class="suggested-prompts">
            <button class="prompt-chip" (click)="suggestPrompt('Why is order stuck?')">
              🔍 Check order status
            </button>
            <button class="prompt-chip" (click)="suggestPrompt('Show me low stock products')">
              📦 Low stock items
            </button>
            <button class="prompt-chip" (click)="suggestPrompt('Get order statistics')">
              📊 Order stats
            </button>
          </div>
        </div>

        <div *ngFor="let msg of messages" class="message" [class.user-message]="msg.role === 'user'"
             [class.assistant-message]="msg.role === 'assistant'">
          <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
          <div class="bubble">
            <div class="message-content" [class.streaming]="msg.isStreaming">
              {{ msg.content }}
              <span *ngIf="msg.isStreaming" class="cursor-blink">|</span>
            </div>
            <div class="timestamp">{{ msg.timestamp | date:'HH:mm:ss' }}</div>
          </div>
        </div>

        <div *ngIf="isLoading" class="message assistant-message">
          <div class="avatar">🤖</div>
          <div class="bubble">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- Input -->
      <div class="input-container">
        <textarea
          [(ngModel)]="userInput"
          (keydown.enter)="!isLoading && sendMessage(); $event.preventDefault()"
          placeholder="Ask about orders, inventory, or troubleshooting..."
          [disabled]="isLoading"
          rows="1"
          class="chat-input"
        ></textarea>
        <button class="btn-send" (click)="sendMessage()" [disabled]="!userInput.trim() || isLoading">
          {{ isLoading ? '⏳' : '➡️' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .chat-container {
      display: flex;
      flex-direction: column;
      height: calc(100vh - 56px);
      max-width: 900px;
      margin: 0 auto;
      background: white;
      box-shadow: 0 0 20px rgba(0,0,0,0.05);
    }

    .chat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 0;
    }

    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .header-icon { font-size: 32px; }

    .header-content h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
    }

    .subtitle {
      margin: 2px 0 0;
      font-size: 12px;
      opacity: 0.85;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .status-indicator {
      font-size: 12px;
      padding: 4px 10px;
      border-radius: 12px;
      background: rgba(255,255,255,0.2);
    }

    .status-indicator.online {
      background: rgba(76, 175, 80, 0.3);
    }

    .btn-clear {
      background: rgba(255,255,255,0.2);
      border: none;
      color: white;
      padding: 6px 12px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 13px;
      transition: background 0.2s;
    }

    .btn-clear:hover { background: rgba(255,255,255,0.3); }

    .messages-container {
      flex: 1;
      overflow-y: auto;
      padding: 20px 24px;
      display: flex;
      flex-direction: column;
      gap: 16px;
      background: #f8f9fc;
    }

    .welcome-message {
      text-align: center;
      padding: 40px 20px;
      color: #666;
    }

    .welcome-icon { font-size: 64px; margin-bottom: 16px; }

    .welcome-message h3 {
      margin: 0 0 8px;
      color: #333;
      font-size: 20px;
    }

    .welcome-message p {
      margin: 0 0 20px;
      color: #888;
    }

    .suggested-prompts {
      display: flex;
      gap: 10px;
      justify-content: center;
      flex-wrap: wrap;
    }

    .prompt-chip {
      background: white;
      border: 1px solid #e0e0e0;
      padding: 8px 16px;
      border-radius: 20px;
      cursor: pointer;
      font-size: 13px;
      transition: all 0.2s;
      color: #555;
    }

    .prompt-chip:hover {
      border-color: #667eea;
      color: #667eea;
      background: #f0f2ff;
    }

    .message {
      display: flex;
      gap: 12px;
      animation: fadeIn 0.3s ease;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(10px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .message.user-message {
      flex-direction: row-reverse;
    }

    .avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      flex-shrink: 0;
    }

    .user-message .avatar {
      background: #667eea20;
    }

    .assistant-message .avatar {
      background: #764ba220;
    }

    .bubble {
      max-width: 75%;
      padding: 12px 16px;
      border-radius: 12px;
      font-size: 14px;
      line-height: 1.5;
    }

    .user-message .bubble {
      background: #667eea;
      color: white;
      border-bottom-right-radius: 4px;
    }

    .assistant-message .bubble {
      background: white;
      color: #333;
      border: 1px solid #e8e8e8;
      border-bottom-left-radius: 4px;
    }

    .message-content {
      white-space: pre-wrap;
      word-break: break-word;
    }

    .message-content.streaming {
      border-right: 2px solid transparent;
    }

    .cursor-blink {
      animation: blink 1s step-end infinite;
      color: #667eea;
    }

    @keyframes blink {
      50% { opacity: 0; }
    }

    .timestamp {
      font-size: 11px;
      color: #999;
      margin-top: 4px;
    }

    .user-message .timestamp { color: rgba(255,255,255,0.7); }

    .typing-indicator {
      display: flex;
      gap: 4px;
      padding: 4px 0;
    }

    .typing-indicator span {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #ccc;
      animation: typing 1.4s infinite;
    }

    .typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
    .typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

    @keyframes typing {
      0%, 60%, 100% { opacity: 0.3; transform: scale(0.8); }
      30% { opacity: 1; transform: scale(1); }
    }

    .input-container {
      display: flex;
      gap: 8px;
      padding: 16px 24px;
      background: white;
      border-top: 1px solid #eee;
    }

    .chat-input {
      flex: 1;
      padding: 12px 16px;
      border: 1px solid #ddd;
      border-radius: 8px;
      font-size: 14px;
      resize: none;
      outline: none;
      transition: border-color 0.2s;
      font-family: inherit;
      line-height: 1.4;
    }

    .chat-input:focus {
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102,126,234,0.1);
    }

    .chat-input:disabled {
      background: #f5f5f5;
    }

    .btn-send {
      width: 44px;
      height: 44px;
      border: none;
      border-radius: 8px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      font-size: 20px;
      cursor: pointer;
      transition: opacity 0.2s;
      flex-shrink: 0;
    }

    .btn-send:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .btn-send:not(:disabled):hover {
      opacity: 0.9;
    }

    @media (max-width: 768px) {
      .chat-container {
        height: calc(100vh - 56px);
      }

      .bubble {
        max-width: 85%;
      }

      .suggested-prompts {
        flex-direction: column;
        align-items: center;
      }
    }
  `]
})
export class ChatComponent implements OnInit, OnDestroy {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  messages: Message[] = [];
  userInput = '';
  sessionId: string | null = null;
  isLoading = false;
  private sessionSub?: Subscription;

  constructor(private copilotService: CopilotService) {}

  ngOnInit(): void {
    this.createSession();
  }

  ngOnDestroy(): void {
    this.sessionSub?.unsubscribe();
  }

  private createSession(): void {
    this.copilotService.createSession().subscribe({
      next: (res) => {
        this.sessionId = res.sessionId;
        console.log('Chat session created:', res.sessionId);
      },
      error: (err) => {
        console.error('Failed to create session:', err);
        this.messages.push({
          role: 'assistant',
          content: '⚠️ Could not connect to the AI Co-Pilot service. Make sure the service is running on port 8083.',
          timestamp: new Date()
        });
      }
    });
  }

  sendMessage(): void {
    const message = this.userInput.trim();
    if (!message || !this.sessionId || this.isLoading) return;

    this.userInput = '';
    this.messages.push({ role: 'user', content: message, timestamp: new Date() });

    this.isLoading = true;
    this.scrollToBottom();

    // Try SSE streaming first
    const streamingMsg: Message = {
      role: 'assistant',
      content: '',
      timestamp: new Date(),
      isStreaming: true
    };
    this.messages.push(streamingMsg);
    this.isLoading = false;

    this.copilotService.sendMessageStream(this.sessionId, message).subscribe({
      next: (token) => {
        streamingMsg.content += token;
        this.scrollToBottom();
      },
      error: () => {
        // Fallback to non-streaming
        this.messages = this.messages.filter(m => m !== streamingMsg);
        this.isLoading = true;
        this.copilotService.sendMessage(this.sessionId!, message).subscribe({
          next: (res) => {
            this.messages.push({ role: 'assistant', content: res.response, timestamp: new Date() });
            this.isLoading = false;
            this.scrollToBottom();
          },
          error: (err) => {
            this.messages.push({
              role: 'assistant',
              content: '❌ Error: ' + (err.error?.error || 'Could not reach the AI service'),
              timestamp: new Date()
            });
            this.isLoading = false;
            this.scrollToBottom();
          }
        });
      },
      complete: () => {
        streamingMsg.isStreaming = false;
        this.scrollToBottom();
      }
    });
  }

  suggestPrompt(prompt: string): void {
    this.userInput = prompt;
    this.sendMessage();
  }

  clearChat(): void {
    if (this.sessionId) {
      this.copilotService.clearSession(this.sessionId).subscribe();
    }
    this.messages = [];
    this.createSession();
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.messagesContainer) {
        this.messagesContainer.nativeElement.scrollTop =
          this.messagesContainer.nativeElement.scrollHeight;
      }
    }, 100);
  }
}
