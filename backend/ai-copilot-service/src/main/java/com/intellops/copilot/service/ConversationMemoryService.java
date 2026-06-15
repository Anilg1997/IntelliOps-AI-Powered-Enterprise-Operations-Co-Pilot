package com.intellops.copilot.service;

import com.intellops.copilot.model.ChatMessage;
import com.intellops.copilot.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Manages conversation history stored in MongoDB.
 * Each conversation is identified by a session ID (UUID).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationMemoryService {

    private final ConversationRepository conversationRepository;

    /**
     * Creates a new session and returns its ID.
     */
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        log.info("Created new conversation session: {}", sessionId);
        return sessionId;
    }

    /**
     * Stores a user message or assistant response in the conversation history.
     */
    public ChatMessage addMessage(String sessionId, String role, String content) {
        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        message = conversationRepository.save(message);
        log.debug("Saved {} message to session {} ({} chars)", role, sessionId, content.length());
        return message;
    }

    /**
     * Retrieves the full conversation history for a session.
     */
    public List<ChatMessage> getHistory(String sessionId) {
        return conversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * Builds a conversation context string from the recent history
     * (typically the last 10 messages) to include in the RAG + LLM prompt.
     */
    public String buildConversationContext(String sessionId, int maxMessages) {
        List<ChatMessage> history = getHistory(sessionId);
        if (history.isEmpty()) {
            return "";
        }

        // Take only the most recent messages
        int startIdx = Math.max(0, history.size() - maxMessages);
        List<ChatMessage> recent = history.subList(startIdx, history.size());

        StringBuilder context = new StringBuilder();
        context.append("--- Conversation History ---\n");
        for (ChatMessage msg : recent) {
            String prefix = msg.getRole().equals("user") ? "User" : "Assistant";
            context.append(prefix).append(": ").append(msg.getContent()).append("\n");
        }
        context.append("--- End Conversation History ---\n");
        return context.toString();
    }

    /**
     * Clears the conversation history for a session.
     */
    public void clearSession(String sessionId) {
        conversationRepository.deleteBySessionId(sessionId);
        log.info("Cleared conversation session: {}", sessionId);
    }
}
