package com.crosstrade.api.controller;

import com.crosstrade.common.dto.request.ChatRequest;
import com.crosstrade.common.dto.response.ChatResponse;
import com.crosstrade.core.router.ChatContext;
import com.crosstrade.core.service.CrossTradeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 核心聊天接口（SSE流式）
 * 提供SSE流式聊天接口和同步聊天接口（调试用）
 * SSE协议：Server-Sent Events，服务端向客户端单向推送数据，适合流式输出场景
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class CrossTradeController {

    @Autowired
    private CrossTradeService crossTradeService;

    /**
     * SSE流式聊天接口（核心接口）
     *
     * @param request 聊天请求
     * @return SSE流式事件流
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> streamChat(@RequestBody @Valid ChatRequest request) {
        log.info("SSE流式聊天请求: sessionId={}, userId={}, message={}",
                request.sessionId(), request.userId(), request.message());

        AtomicInteger tokenIndex = new AtomicInteger(0);

        // 调用核心处理服务获取流式响应
        return crossTradeService.process(request)
                .map(token -> {
                    // 将每个Token包装为ChatResponse
                    return ChatResponse.token(token, tokenIndex.getAndIncrement());
                })
                .concatWith(Flux.defer(() -> {
                    // 流式输出完成后发送done事件
                    return Flux.just(ChatResponse.done(
                            request.sessionId(),
                            tokenIndex.get(),
                            "unknown"
                    ));
                }));
    }

    /**
     * 同步聊天接口（调试用）
     *
     * @param request 聊天请求
     * @return 完整回复文本
     */
    @PostMapping("/chat/sync")
    public Result<String> syncChat(@RequestBody @Valid ChatRequest request) {
        log.info("同步聊天请求: sessionId={}, userId={}, message={}",
                request.sessionId(), request.userId(), request.message());

        String content = crossTradeService.processSync(request);
        return Result.success(content);
    }
}