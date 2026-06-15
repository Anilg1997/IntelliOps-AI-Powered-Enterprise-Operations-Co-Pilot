package com.intellops.copilot.service;

import com.intellops.copilot.config.AiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates the AI co-pilot chat flow:
 * <p>
 * 1. Retrieve relevant RAG context from knowledge base (pgvector)
 * 2. Build enriched prompt with conversation history + RAG context
 * 3. Call LangChain4j AI Service (which handles tool calling)
 * 4. Store conversation in MongoDB
 * 5. Return synthesized answer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final AiConfig.Assistant assistant;
    private final RagService ragService;
    private final ConversationMemoryService conversationMemory;

    // In-memory session tracking: sessionId -> active
    private final Map<String, Boolean> activeSessions = new ConcurrentHashMap<>();

    /**
     * Creates a new chat session.
     */
    public String createSession() {
        String sessionId = conversationMemory.createSession();
        activeSessions.put(sessionId, true);
        return sessionId;
    }

    /**
     * Processes a user message and returns the AI response.
     * Uses RAG to augment the prompt with relevant knowledge base context.
     */
    public String processMessage(String sessionId, String userMessage) {
        // 1. Store user message in history
        conversationMemory.addMessage(sessionId, "user", userMessage);

        // 2. Retrieve relevant RAG context
        String ragContext = ragService.retrieveContext(userMessage);

        // 3. Build enriched prompt with context and conversation history
        String conversationHistory = conversationMemory.buildConversationContext(sessionId, 8);
        String enrichedPrompt = buildEnrichedPrompt(userMessage, ragContext, conversationHistory);

        // 4. Call AI assistant (handles tool calling automatically)
        log.info("🤖 Processing message in session {} ({} chars, RAG: {} chars)",
                sessionId, userMessage.length(), ragContext.length());
        String response;
        try {
            response = assistant.chat(enrichedPrompt);
        } catch (Exception e) {
            log.error("AI assistant error: {}", e.getMessage());
            response = "I'm sorry, I encountered an error processing your request. "
                    + "Please make sure Ollama is running (ollama pull llama3.1) and try again.\n\n"
                    + "Error: " + e.getMessage();
        }

        // 5. Store assistant response in history
        conversationMemory.addMessage(sessionId, "assistant", response);

        return response;
    }

    /**
     * Processes a message and returns the AI response via a streaming callback.
     * For SSE streaming support.
     */
    public String processMessageStream(String sessionId, String userMessage) {
        // For streaming, we use the same logic but the controller handles SSE
        return processMessage(sessionId, userMessage);
    }

    /**
     * Returns the conversation history for a session.
     */
    public List<com.intellops.copilot.model.ChatMessage> getHistory(String sessionId) {
        return conversationMemory.getHistory(sessionId);
    }

    /**
     * Clears a conversation session.
     */
    public void clearSession(String sessionId) {
        conversationMemory.clearSession(sessionId);
        activeSessions.remove(sessionId);
    }

    /**
     * Builds the enriched prompt with RAG context and conversation history.
     */
    private String buildEnrichedPrompt(String userMessage, String ragContext, String conversationHistory) {
        return """
                You are IntelliOps, an AI-powered Enterprise Operations Co-Pilot.
                You help support engineers troubleshoot orders, check inventory, and resolve issues.
                
                ## System Context
                You have access to the following backend systems:
                1. Order Service — order lookup, status tracking, customer info
                2. Inventory Service — stock levels, product catalog, warehouse info
                3. Knowledge Base — runbooks and FAQs for troubleshooting
                
                ## Instructions
                - Use the tools available to you (getOrderDetails, checkStockBySku, etc.) to gather information.
                - Always answer in a clear, structured format.
                - If you don't know something, say so — don't make up information.
                - Be concise but thorough. A support engineer needs actionable answers.
                
                """ +
                (ragContext.isEmpty() ? "" : ragContext + "\n") +
                (conversationHistory.isEmpty() ? "" : conversationHistory + "\n") +
                "## User Question\n" + userMessage;
    }
}
