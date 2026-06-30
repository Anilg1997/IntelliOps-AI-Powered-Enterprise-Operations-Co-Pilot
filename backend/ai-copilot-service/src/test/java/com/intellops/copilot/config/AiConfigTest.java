package com.intellops.copilot.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AiConfig.class)
@TestPropertySource(properties = {
        "intellops.ai.ollama.base-url=http://localhost:11434",
        "intellops.ai.ollama.model=llama3.1",
        "intellops.ai.ollama.temperature=0.1",
        "intellops.ai.ollama.timeout=120"
})
class AiConfigTest {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Test
    void chatLanguageModel_shouldBeOllamaChatModel() {
        assertThat(chatLanguageModel).isInstanceOf(OllamaChatModel.class);
    }
}
