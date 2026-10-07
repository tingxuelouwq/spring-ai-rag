package com.kevin.springai.rag.advisor;

import com.kevin.springai.rag.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

/**
 * 对话日志 Advisor
 * <p>
 * 在请求前后打印用户输入、系统提示、可用工具、工具调用与模型输出。
 * 流式响应通过 {@link ChatClientMessageAggregator} 聚合后统一打印，
 * 避免逐 chunk 输出导致日志刷屏。
 * </p>
 */
@Slf4j
public class MyLoggingAdvisor implements BaseAdvisor {

    private final int order;
    private final boolean showSystemMessage;
    private final boolean showAvailableTools;

    private MyLoggingAdvisor(int order, boolean showSystemMessage, boolean showAvailableTools) {
        this.order = order;
        this.showSystemMessage = showSystemMessage;
        this.showAvailableTools = showAvailableTools;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        StringBuilder sb = new StringBuilder("\nUSER: ");

        if (this.showSystemMessage && chatClientRequest.prompt().getSystemMessage() != null) {
            sb.append("\n - SYSTEM: ")
                    .append(first(chatClientRequest.prompt().getSystemMessage().getText(), 60));
        }

        if (this.showAvailableTools) {
            Object tools = "No Tools";
            if (chatClientRequest.prompt().getOptions() instanceof ToolCallingChatOptions toolOptions
                    && toolOptions.getToolCallbacks() != null) {
                tools = toolOptions.getToolCallbacks().stream()
                        .map(tc -> tc.getToolDefinition().name())
                        .toList();
            }
            sb.append("\n - TOOLS: ").append(JsonUtils.toJson(tools));
        }

        Message lastMessage = chatClientRequest.prompt().getLastUserOrToolResponseMessage();
        if (lastMessage.getMessageType() == MessageType.TOOL) {
            ToolResponseMessage toolResponseMessage = (ToolResponseMessage) lastMessage;
            for (var toolResponse : toolResponseMessage.getResponses()) {
                sb.append("\n - TOOL-RESPONSE: ")
                        .append(toolResponse.name())
                        .append(": ")
                        .append(first(toolResponse.responseData(), 300));
            }
        } else if (lastMessage.getMessageType() == MessageType.USER) {
            if (StringUtils.hasText(lastMessage.getText())) {
                sb.append("\n - TEXT: ").append(first(lastMessage.getText(), 300));
            }
        }

        log.info(sb.toString());
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // 同步调用直接打印
        logResponse(chatClientResponse);
        return chatClientResponse;
    }

    /**
     * 流式调用：用 ChatClientMessageAggregator 聚合后统一打印
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain streamAdvisorChain) {
        // 请求前日志
        this.before(chatClientRequest, null);

        Flux<ChatClientResponse> responses = streamAdvisorChain.nextStream(chatClientRequest);

        // 聚合流式响应，流结束后统一打印
        return new ChatClientMessageAggregator()
                .aggregateChatClientResponse(responses, this::logResponse);
    }

    /**
     * 打印完整的模型响应
     */
    private void logResponse(ChatClientResponse chatClientResponse) {
        StringBuilder sb = new StringBuilder("\nASSISTANT: ");

        if (chatClientResponse.chatResponse() == null
                || chatClientResponse.chatResponse().getResults() == null) {
            sb.append(" No chat response ");
            log.info(sb.toString());
            return;
        }

        for (var generation : chatClientResponse.chatResponse().getResults()) {
            var message = generation.getOutput();

            // 工具调用
            if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
                for (var toolCall : message.getToolCalls()) {
                    sb.append("\n - TOOL-CALL: ")
                            .append(toolCall.name())
                            .append(" (")
                            .append(toolCall.arguments())
                            .append(")");
                }
            }

            // 完整文本输出
            if (StringUtils.hasText(message.getText())) {
                sb.append("\n - TEXT: ").append(message.getText());
            }
        }

        log.info(sb.toString());
    }

    /**
     * 截断字符串，超出长度时追加省略号
     */
    private String first(String text, int n) {
        if (text == null || text.length() <= n) {
            return text;
        }
        return text.substring(0, n) + "...";
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder
     */
    public static class Builder {

        private int order = 0;
        private boolean showSystemMessage = true;
        private boolean showAvailableTools = true;

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder showSystemMessage(boolean showSystemMessage) {
            this.showSystemMessage = showSystemMessage;
            return this;
        }

        public Builder showAvailableTools(boolean showAvailableTools) {
            this.showAvailableTools = showAvailableTools;
            return this;
        }

        public MyLoggingAdvisor build() {
            return new MyLoggingAdvisor(this.order, this.showSystemMessage, this.showAvailableTools);
        }
    }
}