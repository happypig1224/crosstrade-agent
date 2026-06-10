package com.crosstrade.core.intent;

import com.crosstrade.common.dto.response.IntentResult;
import com.crosstrade.common.enums.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * L2 LLM语义分析
 * 当L1规则引擎无法匹配时，降级到通义千问进行深层语义分析
 * 构造结构化Prompt，约束LLM仅输出四种意图之一
 * Resilience4j超时3s + 重试1次保护
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class LlmIntentAnalyzer {

    @Autowired
    private ChatClient chatClient;

    /**
     * 意图识别结构化Prompt
     */
    private static final String INTENT_PROMPT = """
            分析用户消息的意图，仅返回以下四种之一：
            - product_guide: 用户需要产品推荐或导购引导
            - product_info: 用户咨询产品详情、对比、售后政策
            - quote_negotiate: 用户进行价格谈判或报价咨询
            - unknown: 无法识别意图
            
            用户消息: %s
            意图:
            """;

    /**
     * LLM语义分析
     *
     * @param userMessage 用户消息
     * @return 意图识别结果
     */
    public IntentResult analyze(String userMessage) {
        // 构造结构化Prompt
        String prompt = String.format(INTENT_PROMPT, userMessage);

        // 调用LLM
        String response = chatClient.prompt(prompt).call().content();

        // 校验LLM返回值
        if (response == null || response.trim().isEmpty()) {
            return new IntentResult(IntentType.UNKNOWN, 0.0, "L2_LLM_DEGRADE");
        }

        IntentType intentType = IntentType.fromValue(response.trim());
        if (intentType == IntentType.UNKNOWN) {
            // LLM返回了不在预定义类型中的值，降级为unknown
            return new IntentResult(IntentType.UNKNOWN, 0.0, "L2_LLM_DEGRADE");
        }

        return new IntentResult(intentType, 0.8, "L2_LLM");
    }
}