package com.crosstrade.core.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Configuration
public class ChatClientConfig {
    /**
     * 主模型系统提示词
     */
    private static final String SYSTEM_PROMPT = """
            你是一个专业的跨境电商助手，你的任务是根据用户的问题，评估用户的需求。
            评估用户的需求包括但不限于：用户的需求是否符合跨境电商的交易规则，
            用户的需求是否符合跨境电商的交易流程，用户的需求是否符合跨境电商的交易成本等。
            """;
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}