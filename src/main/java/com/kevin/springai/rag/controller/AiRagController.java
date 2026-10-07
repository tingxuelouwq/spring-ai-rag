package com.kevin.springai.rag.controller;

import com.kevin.springai.rag.advisor.MyLoggingAdvisor;
import com.kevin.springai.rag.annotation.Loggable;
import com.kevin.springai.rag.constant.BizConstant;
import com.kevin.springai.rag.context.BaseContext;
import com.kevin.springai.rag.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDate;

/**
 * RAG 对话接口
 */
@Tag(name = "AiRagController", description = "Rag接口")
@Slf4j
@RestController
@RequestMapping(BizConstant.API_VERSION + "/ai")
public class AiRagController {

    /** 系统提示词 */
    private static final String SYSTEM_PROMPT = """
        ## 角色
        您是AI知识库系统的对话助手。请以友好、乐于助人且愉快的方式来回复。
        今天的日期：{current_date}

        ## 回答规则
        1. 你只能基于系统提供的「知识库内容」回答用户问题，禁止使用任何知识库之外的信息。
        2. 如果知识库内容不足以回答问题，请直接回复「知识库中未找到相关内容」，不要自行编造。
        3. 回答时请标注信息来源，格式为：在引用内容后加上 [来源: 文件名]。

        ## 要求
        1. 请讲中文。
        """;

    private final SensitiveWordService sensitiveWordService;
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AiRagController(SensitiveWordService sensitiveWordService,
                           ChatModel chatModel,
                           ChatMemory chatMemory,
                           MilvusVectorStore milvusVectorStore) {
        this.sensitiveWordService = sensitiveWordService;

        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        MyLoggingAdvisor.builder().showAvailableTools(false).showSystemMessage(false).build()
                )
                .build();

        this.vectorStore = milvusVectorStore;
    }

    /**
     * RAG 对话接口（POST 版本）
     *
     * @param message 用户消息
     * @return 流式响应
     */
    @Operation(summary = "rag post", description = "Rag对话接口POST版本")
    @PostMapping(value = "/rag")
    @Loggable
    public Flux<String> generatePost(
            @RequestParam(value = "message", defaultValue = "你好") String message) throws IOException {
        log.info("rag请求start...");

        // 敏感词过滤
        String hitWord = sensitiveWordService.findHitWord(message);
        if (hitWord != null) {
            return Flux.just("包含敏感词:" + hitWord);
        }

        Long userId = BaseContext.getCurrentId();

        return chatClient.prompt()
                .user(message)
                .system(p -> p.text(SYSTEM_PROMPT)
                        .param("current_date", LocalDate.now().toString()))
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId))
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .query(message)
                                .similarityThreshold(0.1d)
                                .topK(5)
                                .build())
                        .build())
                .stream()
                .content();
    }

//    /**
//     * 处理正常的 RAG 查询
//     *
//     * @param sources 数据源列表
//     * @param message 用户消息
//     * @return 响应流
//     */
//    private Flux<String> processNormalRagQuery(List<String> sources, String message) {
//        Long userId = BaseContext.getCurrentId();
//
//        ChatClient.ChatClientRequestSpec clientRequestSpec = chatClient.prompt()
//                .user(message)
//                .system(a -> a.param("current_data", LocalDate.now().toString()))
//                .advisors(a -> a.param("userMessage", message))
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId));
//
//        // 如果提供了 sources 参数，使用向量数据库查询
//        if (sources != null && !sources.isEmpty()) {
//            SearchRequest searchRequest = SearchRequest.builder()
//                    .query(message)
//                    .similarityThreshold(0.1d)
//                    .topK(5)
//                    .filterExpression("source in " + JSON.toJSONString(sources))
//                    .build();
//
//            clientRequestSpec = clientRequestSpec
//                    .system(a -> a.param("rag_message", RAG_SYSTEM_PROMPT))
//                    .advisors(QuestionAnswerAdvisor.builder(vectorStore)
//                            .searchRequest(searchRequest)
//                            .build());
//        }
//
//        return clientRequestSpec.stream().content();
//    }
}