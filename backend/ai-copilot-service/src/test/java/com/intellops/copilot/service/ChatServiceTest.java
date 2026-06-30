package com.intellops.copilot.service;

import com.intellops.copilot.config.AiConfig;
import com.intellops.copilot.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AiConfig.Assistant assistant;

    @Mock
    private RagService ragService;

    @Mock
    private ConversationMemoryService conversationMemory;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(assistant, ragService, conversationMemory);
    }

    @Test
    void createSession_shouldReturnSessionId() {
        when(conversationMemory.createSession()).thenReturn("test-session-id");

        String sessionId = chatService.createSession();

        assertThat(sessionId).isEqualTo("test-session-id");
    }

    @Test
    void processMessage_shouldReturnResponse() {
        String sessionId = "test-session";
        String userMessage = "What is the status of order 123?";
        String ragContext = "Relevant documentation about orders";
        String conversationHistory = "--- Conversation History ---\nUser: Hi\nAssistant: Hello\n--- End ---";
        String aiResponse = "Order 123 is in PROCESSING status.";

        when(ragService.retrieveContext(userMessage)).thenReturn(ragContext);
        when(conversationMemory.buildConversationContext(sessionId, 8)).thenReturn(conversationHistory);
        when(assistant.chat(anyString())).thenReturn(aiResponse);

        String response = chatService.processMessage(sessionId, userMessage);

        assertThat(response).isEqualTo(aiResponse);
        verify(conversationMemory).addMessage(sessionId, "user", userMessage);
        verify(conversationMemory).addMessage(sessionId, "assistant", aiResponse);
    }

    @Test
    void processMessage_whenAssistantThrows_shouldReturnErrorResponse() {
        when(ragService.retrieveContext(anyString())).thenReturn("context");
        when(conversationMemory.buildConversationContext(anyString(), anyInt())).thenReturn("history");
        when(assistant.chat(anyString())).thenThrow(new RuntimeException("Ollama not running"));

        String response = chatService.processMessage("session1", "Hello");

        assertThat(response).contains("I'm sorry");
        assertThat(response).contains("Ollama not running");
    }

    @Test
    void getHistory_shouldReturnMessages() {
        String sessionId = "test-session";
        ChatMessage msg = ChatMessage.builder()
                .sessionId(sessionId).role("user").content("Hello").build();
        when(conversationMemory.getHistory(sessionId)).thenReturn(List.of(msg));

        List<ChatMessage> history = chatService.getHistory(sessionId);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getContent()).isEqualTo("Hello");
    }

    @Test
    void clearSession_shouldDelegate() {
        chatService.clearSession("session1");

        verify(conversationMemory).clearSession("session1");
    }
}
