package com.intellops.copilot.service;

import com.intellops.copilot.model.ChatMessage;
import com.intellops.copilot.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationMemoryServiceTest {

    @Mock
    private ConversationRepository repository;

    private ConversationMemoryService service;

    @BeforeEach
    void setUp() {
        service = new ConversationMemoryService(repository);
    }

    @Test
    void createSession_shouldReturnUuid() {
        String sessionId = service.createSession();
        assertThat(sessionId).isNotBlank();
        assertThat(sessionId).contains("-");
    }

    @Test
    void addMessage_shouldSaveAndReturn() {
        ChatMessage saved = ChatMessage.builder()
                .sessionId("s1").role("user").content("Hi").build();
        when(repository.save(any(ChatMessage.class))).thenReturn(saved);

        ChatMessage result = service.addMessage("s1", "user", "Hi");

        assertThat(result.getRole()).isEqualTo("user");
        assertThat(result.getContent()).isEqualTo("Hi");
        verify(repository).save(any(ChatMessage.class));
    }

    @Test
    void getHistory_shouldReturnOrderedMessages() {
        ChatMessage m1 = ChatMessage.builder().sessionId("s1").role("user").content("Hi").build();
        ChatMessage m2 = ChatMessage.builder().sessionId("s1").role("assistant").content("Hello").build();
        when(repository.findBySessionIdOrderByCreatedAtAsc("s1")).thenReturn(List.of(m1, m2));

        List<ChatMessage> history = service.getHistory("s1");

        assertThat(history).hasSize(2);
    }

    @Test
    void buildConversationContext_withNoHistory_shouldReturnEmpty() {
        when(repository.findBySessionIdOrderByCreatedAtAsc("s1")).thenReturn(List.of());

        String context = service.buildConversationContext("s1", 10);

        assertThat(context).isEmpty();
    }

    @Test
    void buildConversationContext_withHistory_shouldFormat() {
        ChatMessage m1 = ChatMessage.builder().sessionId("s1").role("user").content("Order status?").build();
        ChatMessage m2 = ChatMessage.builder().sessionId("s1").role("assistant").content("Processing.").build();
        when(repository.findBySessionIdOrderByCreatedAtAsc("s1")).thenReturn(List.of(m1, m2));

        String context = service.buildConversationContext("s1", 10);

        assertThat(context).contains("User: Order status?");
        assertThat(context).contains("Assistant: Processing.");
    }

    @Test
    void clearSession_shouldDelete() {
        service.clearSession("s1");
        verify(repository).deleteBySessionId("s1");
    }
}
