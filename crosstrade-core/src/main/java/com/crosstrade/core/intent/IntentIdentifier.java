package com.crosstrade.core.intent;

import com.crosstrade.common.dto.response.IntentResult;
import com.crosstrade.common.enums.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 意图识别入口
 * 双层识别机制：L1规则引擎快速匹配 → L2 LLM语义分析
 * L1速度快、成本低但只能识别明确关键词；
 * L2能理解语义但速度慢、成本高且有超时风险
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class IntentIdentifier {

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private LlmIntentAnalyzer llmIntentAnalyzer;

    /**
     * 意图识别
     *
     * @param userMessage 用户消息
     * @return 意图识别结果
     */
    public IntentResult identify(String userMessage) {
        // L1规则引擎快速匹配
        IntentResult result = ruleEngine.match(userMessage);
        if (result != null) {
            log.debug("意图识别-L1命中: type={}, confidence={}", result.intentType(), result.confidence());
            return result;
        }

        // L2 LLM语义分析
        try {
            result = llmIntentAnalyzer.analyze(userMessage);
            log.debug("意图识别-L2命中: type={}, confidence={}", result.intentType(), result.confidence());
            return result;
        } catch (Exception e) {
            log.warn("L2语义分析异常，降级为unknown: {}", e.getMessage());
            return new IntentResult(IntentType.UNKNOWN, 0.0, "L2_ERROR");
        }
    }
}