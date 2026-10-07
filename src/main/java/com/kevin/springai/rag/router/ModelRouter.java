package com.kevin.springai.rag.router;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ModelRouter {

    private final Map<String, ChatClient> clients = new HashMap<>();

    // Spring 会自动注入所有 ChatModel 类型的 Bean，key 为 Bean 名称
    public ModelRouter(Map<String, ChatModel> models, ChatMemory chatMemory) {
        for (Map.Entry<String, ChatModel> entry : models.entrySet()) {
            ChatClient client = ChatClient.builder(entry.getValue())
                    .defaultAdvisors(
                            MessageChatMemoryAdvisor.builder(chatMemory).build(),
                            SimpleLoggerAdvisor.builder().build()
                    )
                    .build();
            clients.put(entry.getKey(), client);
        }
        log.info("已注册模型: {}", clients.keySet());
    }

    public ChatClient getClient(String modelKey) {
        String key = (modelKey == null || modelKey.isBlank()) ? "qwenChatModel" : modelKey;
        ChatClient client = clients.get(key);
        if (client == null) {
            log.warn("未找到模型 Bean: {}，回退到 qwenChatModel", key);
            client = clients.get("qwenChatModel");
            key = "qwenChatModel";
        }
        log.info("【实际使用模型 Bean】{}", key);
        return client;
    }
}