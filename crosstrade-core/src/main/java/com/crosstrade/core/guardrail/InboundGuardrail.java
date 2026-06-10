package com.crosstrade.core.guardrail;

import com.crosstrade.common.enums.SecurityCheckResult;
import com.crosstrade.common.util.DfaSensitiveFilter;
import com.crosstrade.common.util.PromptInjectionDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 入口安全审查
 * 用户消息进入系统时，执行敏感词DFA过滤 + 防Prompt注入正则检测
 * 同步执行，不调用LLM，保证低延迟
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class InboundGuardrail {

    private final DfaSensitiveFilter sensitiveFilter;
    private final PromptInjectionDetector injectionDetector;

    public InboundGuardrail() {
        this.sensitiveFilter = new DfaSensitiveFilter();
        this.injectionDetector = new PromptInjectionDetector();
        // TODO: 从数据库或配置文件加载敏感词库
        // sensitiveFilter.initSensitiveWordMap(sensitiveWords);
    }

    /**
     * 入口安全审查
     *
     * @param userMessage 用户消息
     * @param sessionId   会话ID
     * @param userId      用户ID
     * @return 审查结果
     */
    public SecurityCheckResult check(String userMessage, String sessionId, String userId) {
        // DFA敏感词过滤
        if (sensitiveFilter.containsSensitive(userMessage)) {
            log.warn("安全审查-敏感词拦截: sessionId={}, userId={}, message={}", sessionId, userId, userMessage);
            return SecurityCheckResult.BLOCK;
        }

        // 防Prompt注入正则检测
        if (injectionDetector.detect(userMessage)) {
            log.warn("安全审查-Prompt注入拦截: sessionId={}, userId={}, message={}", sessionId, userId, userMessage);
            return SecurityCheckResult.BLOCK;
        }

        // 审查通过
        return SecurityCheckResult.PASS;
    }
}