package com.kevin.springai.rag.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultiModelConfig {

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private OpenAiChatModel buildModel(String modelName) {
        return OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(baseUrl)      // 你的百炼端点
                        .apiKey(apiKey)        // 直接传 String
                        .model(modelName)      // 模型名
                        .build())
                .build();
    }

    @Bean("qwenChatModel")
    public ChatModel qwenChatModel() {
        return buildModel("qwen3.8-max-0902");
    }

    @Bean("deepseekChatModel")
    public ChatModel deepseekChatModel() {
        return buildModel("deepseek-v4.1-flash");
    }

    @Bean("glmChatModel")
    public ChatModel glmChatModel() {
        return buildModel("glm-5.3");
    }

    @Bean("kimiChatModel")
    public ChatModel kimiChatModel() {
        return buildModel("kimi-k3");
    }
}