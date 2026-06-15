package com.intellops.copilot.controller;

import com.intellops.copilot.model.ChatMessage;
import com.intellops.copilot.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Chat controller for the AI Co-Pilot.
 * <p>
 * Endpoints:
 * - POST /api/copilot/session — create a new chat session
 * - POST /api/copilot/chat — send a message (non-streaming)
 * - GET  /api/copilot/chat/stream — send a message via SSE (streaming)
 * - GET  /api/copilot/history/{sessionId} — get conversation history
 * - DELETE /api/copilot/session/{sessionId} — clear a session
 */
@RestController
@RequestMapping("/api/copilot")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> createSession() {
        String sessionId = chatService.createSession();
        log.info("Created new chat session: {}", sessionId);
        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "message", "Chat session created. Ask me anything about orders, inventory, or troubleshooting!"
        ));
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request) {
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "sessionId is required"));
        }
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message is required"));
        }

        String response = chatService.processMessage(request.sessionId(), request.message());
        return ResponseEntity.ok(Map.of(
                "sessionId", request.sessionId(),
                "response", response
        ));
    }

    /**
     * SSE streaming endpoint for real-time AI response generation.
     * <p>
     * Usage: GET /api/copilot/chat/stream?sessionId=xxx&message=Why+is+order+stuck
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam String sessionId,
            @RequestParam String message) {

        SseEmitter emitter = new SseEmitter(120_000L); // 2 minute timeout

        executor.execute(() -> {
            try {
                // Process the message and send the complete response as an SSE event
                String response = chatService.processMessage(sessionId, message);

                // Send the response in chunks for a streaming feel
                String[] words = response.split(" ");
                StringBuilder chunk = new StringBuilder();
                for (String word : words) {
                    chunk.append(word).append(" ");
                    if (chunk.length() > 50) {
                        emitter.send(SseEmitter.event()
                                .name("token")
                                .data(chunk.toString()));
                        chunk.setLength(0);
                        Thread.sleep(30); // Simulate streaming delay
                    }
                }
                // Send remaining text
                if (chunk.length() > 0) {
                    emitter.send(SseEmitter.event()
                            .name("token")
                            .data(chunk.toString()));
                }

                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("Error: " + e.getMessage()));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        List<ChatMessage> history = chatService.getHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Session cleared: " + sessionId));
    }

    /**
     * Request DTO for the chat endpoint.
     */
    public record ChatRequest(String sessionId, String message) {}
}
