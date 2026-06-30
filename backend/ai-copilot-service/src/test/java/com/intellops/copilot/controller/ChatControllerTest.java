package com.intellops.copilot.controller;

import com.intellops.copilot.model.ChatMessage;
import com.intellops.copilot.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void createSession_shouldReturnSessionId() throws Exception {
        when(chatService.createSession()).thenReturn("test-uuid");

        mockMvc.perform(post("/api/copilot/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("test-uuid"));
    }

    @Test
    void chat_withValidRequest_shouldReturnResponse() throws Exception {
        when(chatService.processMessage("s1", "Hello")).thenReturn("Hi there!");

        mockMvc.perform(post("/api/copilot/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sessionId": "s1", "message": "Hello"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Hi there!"));
    }

    @Test
    void chat_withMissingSessionId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/copilot/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "Hello"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getHistory_shouldReturnMessages() throws Exception {
        ChatMessage msg = ChatMessage.builder()
                .sessionId("s1").role("user").content("Hello").build();
        when(chatService.getHistory("s1")).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/copilot/history/s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("user"))
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    void clearSession_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/copilot/session/s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session cleared: s1"));
    }
}
