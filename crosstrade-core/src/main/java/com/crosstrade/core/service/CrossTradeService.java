package com.crosstrade.core.service;

import com.crosstrade.common.dto.request.ChatRequest;
import com.crosstrade.common.dto.response.IntentResult;
import com.crosstrade.common.enums.SecurityCheckResult;
import com.crosstrade.core.callback.StreamCompleteCallback;
import com.crosstrade.core.guardrail.InboundGuardrail;
import com.crosstrade.core.intent.IntentIdentifier;
import com.crosstrade.core.memory.MemoryManager;
import com.crosstrade.core.observe.MetricsCollector;
import com.crosstrade.core.observe.TraceContext;
import com.crosstrade.core.router.ChatContext;
import com.crosstrade.core.router.IntentRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 核心处理服务
 * 核心处理流水线是将前面所有模块串联起来的"总指挥"，
 * 从用户请求到响应的完整流程：
 * 接收用户请求 → 入口安全审查 → L1短期记忆加载(缓存击穿→冷启动) →
 * 意图识别与路由 → 业务处理(导购/咨询/报价/未知) →
 * LLM流式生成 → 出口安全审查 → 流式输出完成回调 → 返回结果
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Service
public class CrossTradeService {

    @Autowired
    private InboundGuardrail inboundGuardrail;

    @Autowired
    private MemoryManager memoryManager;

    @Autowired
    private IntentIdentifier intentIdentifier;

    @Autowired
    private IntentRouter intentRouter;

    @Autowired
    private StreamCompleteCallback streamCompleteCallback;

    @Autowired
    private MetricsCollector metricsCollector;

    /**
     * 核心处理方法（流式）
     *
     * @param request 聊天请求
     * @return 流式响应文本
     */
    public Flux<String> process(ChatRequest request) {
        // 设置链路追踪
        TraceContext.generateTraceId();
        long startTime = System.currentTimeMillis();

        // 入口安全审查
        SecurityCheckResult checkResult = inboundGuardrail.check(
                request.message(), request.sessionId(), request.userId()
        );
        if (checkResult == SecurityCheckResult.BLOCK) {
            metricsCollector.recordGuardrailBlock("inbound", "BLOCK");
            return Flux.just("您的消息包含敏感内容，请重新描述");
        }

        // 加载记忆上下文（缓存击穿时触发冷启动恢复）
        ChatContext context = memoryManager.buildContext(
                request.sessionId(), request.userId(), request.message()
        );
        if (request.region() != null) {
            context.setRegion(request.region());
        }

        // 意图识别与路由
        IntentResult intentResult = intentIdentifier.identify(request.message());
        context.setIntentType(intentResult.intentType().getValue());
        metricsCollector.recordIntentDistribution(intentResult.intentType());

        // 业务处理（流式生成）
        Flux<String> responseFlux = intentRouter.route(intentResult, context);

        // 流式输出 + 完成回调
        StringBuilder fullResponse = new StringBuilder();
        AtomicInteger tokenCount = new AtomicInteger(0);

        return responseFlux
                .doOnNext(token -> {
                    // 累积完整响应
                    fullResponse.append(token);
                    tokenCount.incrementAndGet();
                })
                .doOnComplete(() -> {
                    // 流式输出完成回调
                    try {
                        streamCompleteCallback.onComplete(
                                request.sessionId(), request.userId(),
                                request.message(), fullResponse.toString(),
                                tokenCount.get()
                        );

                        // 记录响应延迟
                        long duration = System.currentTimeMillis() - startTime;
                        metricsCollector.recordResponseLatency("qwen3.7-max", duration);
                    } catch (Exception e) {
                        log.error("流式输出完成回调异常: sessionId={}", request.sessionId(), e);
                    } finally {
                        TraceContext.clear();
                    }
                })
                .doOnError(e -> {
                    log.error("流式输出异常: sessionId={}", request.sessionId(), e);
                    TraceContext.clear();
                });
    }

    /**
     * 同步处理方法（调试用）
     *
     * @param request 聊天请求
     * @return 完整回复文本
     */
    public String processSync(ChatRequest request) {
        StringBuilder result = new StringBuilder();
        process(request)
                .doOnNext(result::append)
                .blockLast();
        return result.toString();
    }
}