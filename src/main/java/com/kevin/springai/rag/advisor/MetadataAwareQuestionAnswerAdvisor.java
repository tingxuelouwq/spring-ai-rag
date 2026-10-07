package com.kevin.springai.rag.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataAwareQuestionAnswerAdvisor implements BaseAdvisor {

    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("""
        {query}
        Context information is below.
        ---------------------
        {question_answer_context}
        ---------------------
        Given the context and provided history information and not prior knowledge,
        reply to the user comment. If the answer is not in the context, inform the user that you can't answer.
        """);

    @Override
    public ChatClientRequest before(ChatClientRequest baseRequest, AdvisorChain advisorChain) {
        // 1. 从上下文取出 QuestionAnswerAdvisor 已检索到的文档
        List<Document> documents = (List<Document>) baseRequest.context()
                .get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

        // 2. 取出原始用户问题（需要在 Controller 里显式 param 传入）
        String userMessage = (String) baseRequest.context().get("userMessage");

        if (!CollectionUtils.isEmpty(documents)) {
            // 3. 核心：把每段文本和它的来源文件名拼在一起
            String documentContext = documents.stream()
                    .map(doc -> doc.getText() + "\n来源文件："
                            + doc.getMetadata().getOrDefault("source", "unknown").toString())
                    .collect(Collectors.joining(System.lineSeparator()));

            // 4. 用模板渲染出新的用户消息
            String augmentedUserText = DEFAULT_PROMPT_TEMPLATE.render(Map.of(
                    "query", userMessage,
                    "question_answer_context", documentContext
            ));

            // 5. 替换原来的用户消息
            return baseRequest.mutate()
                    .prompt(baseRequest.prompt().augmentUserMessage(augmentedUserText))
                    .build();
        }
        return baseRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain advisorChain) {
        return response;
    }

    @Override
    public int getOrder() {
        // 必须在 QuestionAnswerAdvisor (order=0) 之后，
        // 但要在实际调用大模型的 Advisor (order=Integer.MAX_VALUE) 之前
        return Integer.MAX_VALUE - 1;
    }
}