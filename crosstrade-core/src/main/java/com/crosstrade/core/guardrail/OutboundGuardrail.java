package com.crosstrade.core.guardrail;

import com.crosstrade.common.dto.response.ChatResponse;
import com.crosstrade.common.util.DfaSensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxSink;

/**
 * 出口安全审查
 * AI生成结果返回用户前，以20个Token为一块进行异步缓冲 + 小LLM校验
 * 当小LLM熔断时降级为DFA规则过滤
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class OutboundGuardrail {

    @Autowired
    @Qualifier("chatClient")
    private ChatClient chatClient;

    private final DfaSensitiveFilter sensitiveFilter;
    private final OutputBuffer outputBuffer;

    public OutboundGuardrail() {
        this.sensitiveFilter = new DfaSensitiveFilter();
        this.outputBuffer = new OutputBuffer();
    }

    /**
     * 检查并发射Token块
     *
     * @param tokenChunk Token文本
     * @param emitter    SSE发射器
     * @param tokenIndex Token序号
     */
    public void checkAndEmit(String tokenChunk, FluxSink<ChatResponse> emitter, int tokenIndex) {
        // 将token加入缓冲区
        outputBuffer.append(tokenChunk);

        // 缓冲区满20个Token时触发校验
        if (outputBuffer.isFull()) {
            String block = outputBuffer.flush();

            // 尝试小LLM校验（Resilience4j熔断保护）
            boolean isValid;
            try {
                isValid = smallLlmCheck(block);
            } catch (Exception e) {
                // 降级：仅执行规则过滤
                isValid = !sensitiveFilter.containsSensitive(block);
                log.warn("出口审查降级为规则过滤: {}", e.getMessage());
            }

            // 校验结果处理
            if (isValid) {
                emitter.next(ChatResponse.token(block, tokenIndex));
            } else {
                emitter.next(ChatResponse.token("[内容已过滤]", tokenIndex));
                log.warn("出口审查拦截: {}", block);
            }
        }
    }

    /**
     * 流式输出结束时，处理缓冲区剩余内容
     *
     * @param emitter    SSE发射器
     * @param tokenIndex Token序号
     */
    public void flushRemaining(FluxSink<ChatResponse> emitter, int tokenIndex) {
        String remaining = outputBuffer.drain();
        if (!remaining.isEmpty()) {
            // 同样需要校验
            boolean isValid;
            try {
                isValid = smallLlmCheck(remaining);
            } catch (Exception e) {
                isValid = !sensitiveFilter.containsSensitive(remaining);
                log.warn("出口审查降级为规则过滤(剩余): {}", e.getMessage());
            }

            if (isValid) {
                emitter.next(ChatResponse.token(remaining, tokenIndex));
            } else {
                emitter.next(ChatResponse.token("[内容已过滤]", tokenIndex));
                log.warn("出口审查拦截(剩余): {}", remaining);
            }
        }
    }

    /**
     * 小LLM校验Prompt
     * 判断内容是否包含违禁或不当信息
     *
     * @param block 待校验文本
     * @return true=合规，false=违规
     */
    private boolean smallLlmCheck(String block) {
        String prompt = "判断以下内容是否包含违禁或不当信息，仅回答YES或NO: " + block;
        String result = chatClient.prompt(prompt).call().content();
        if (result == null) {
            return true;
        }
        return "YES".equalsIgnoreCase(result.trim());
    }
}