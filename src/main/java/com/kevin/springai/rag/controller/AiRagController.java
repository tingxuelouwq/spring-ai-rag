package com.kevin.springai.rag.controller;

import com.kevin.springai.rag.advisor.MetadataAwareQuestionAnswerAdvisor;
import com.kevin.springai.rag.annotation.Loggable;
import com.kevin.springai.rag.constant.BizConstant;
import com.kevin.springai.rag.context.BaseContext;
import com.kevin.springai.rag.router.ModelRouter;
import com.kevin.springai.rag.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    private final VectorStore vectorStore;
    private final ModelRouter modelRouter;

    public AiRagController(SensitiveWordService sensitiveWordService,
                           ModelRouter modelRouter,
                           MilvusVectorStore milvusVectorStore) {
        this.sensitiveWordService = sensitiveWordService;
        this.modelRouter = modelRouter;
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
            @RequestParam(value = "message", defaultValue = "你好") String message,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "sources", required = false) List<String> sources) {

        log.info("rag请求start... 参数: message={}, sources={}", message, sources);

        // 敏感词过滤
        String hitWord = sensitiveWordService.findHitWord(message);
        if (hitWord != null) {
            return Flux.just("包含敏感词:" + hitWord);
        }

        ChatClient chatClient = modelRouter.getClient(model);

        boolean isSql = chatClient.prompt()
                .system("用户的查询是否涉及统计数据、求和、计数、平均值等聚合操作？")
                .user(message)
                .call()
                .entity(Boolean.class);

        if (isSql) {    // 如果是聚合操作，则使用SuperSQL生成SQL

            return null;
        } else {        // 如果不是聚合操作，则执行正常的RAG处理
            return processNormalRagQuery(message, sources, chatClient);
        }
    }

    private @NonNull Flux<String> processNormalRagQuery(String message, List<String> sources, ChatClient chatClient) {
        Long userId = BaseContext.getCurrentId();

        // 1. 构建动态的 SearchRequest
        SearchRequest.Builder searchRequestBuilder = SearchRequest.builder()
                .query(message)
                .similarityThreshold(0.1d)
                .topK(5);

        // 2. 如果传入了文件限制，添加 Milvus 过滤表达式
        if (sources != null && !sources.isEmpty()) {
            // 转义可能存在的双引号，防止表达式解析错误
            String filterExpr = sources.stream()
                    .map(s -> "\"" + s.replace("\"", "\\\"") + "\"")
                    .collect(Collectors.joining(", "));

            // 注意：这里的 "source" 需要与你在向量化写入数据库时设置的 metadata key 保持一致
            searchRequestBuilder.filterExpression("source in [" + filterExpr + "]");
            log.info("应用文件过滤条件: {}", searchRequestBuilder.build().getFilterExpression());
        }

        // 3. 构建请求
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt()
                .user(message)
                .system(p -> p.text(SYSTEM_PROMPT)
                        .param("current_date", LocalDate.now().toString()))
                // 传 userMessage，供 MetadataAwareQuestionAnswerAdvisor 使用
                .advisors(advisorSpec -> advisorSpec.param("userMessage", message))
                // 传会话 ID
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId));

        return spec
                // 4. 挂载 Advisor，使用动态构建的 SearchRequest
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(searchRequestBuilder.build())
                        .build())
                // 5. 拼装来源
                .advisors(new MetadataAwareQuestionAnswerAdvisor())
                .stream()
                .content();
    }
}