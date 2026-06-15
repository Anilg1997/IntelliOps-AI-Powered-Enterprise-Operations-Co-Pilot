package com.intellops.copilot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * A single chat message stored in MongoDB for conversation history.
 * Messages are grouped by a conversation/session ID.
 */
@Document(collection = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    private String id;

    @Field("session_id")
    private String sessionId;

    @Field("role")
    private String role;  // "user" or "assistant"

    @Field("content")
    private String content;

    @Field("created_at")
    private LocalDateTime createdAt;
}
