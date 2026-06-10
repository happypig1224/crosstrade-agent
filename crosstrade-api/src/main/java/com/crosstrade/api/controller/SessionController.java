package com.crosstrade.api.controller;

import com.crosstrade.common.dto.request.SessionRecoverRequest;
import com.crosstrade.core.memory.ColdStartRecovery;
import com.crosstrade.core.memory.ShortTermMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 会话管理接口
 * 提供会话创建、恢复（冷启动）、关闭等管理功能。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/session")
public class SessionController {

    @Autowired
    private ShortTermMemory shortTermMemory;

    @Autowired
    private ColdStartRecovery coldStartRecovery;

    /**
     * 新建会话
     *
     * @param request 创建请求（包含userId和可选的previousSessionId）
     * @return 新会话ID
     */
    @PostMapping("/create")
    public Result<SessionCreateResponse> create(@RequestBody SessionCreateRequest request) {
        // 清理旧会话Redis缓存（主动过期）
        if (request.previousSessionId() != null && !request.previousSessionId().isEmpty()) {
            shortTermMemory.deleteSession(request.previousSessionId());
            log.info("清理旧会话缓存: previousSessionId={}", request.previousSessionId());
        }

        // 生成新会话ID
        String sessionId = UUID.randomUUID().toString().replace("-", "");

        log.info("新建会话: sessionId={}, userId={}", sessionId, request.userId());
        return Result.success(new SessionCreateResponse(sessionId));
    }

    /**
     * 恢复会话（冷启动）
     *
     * @param request 恢复请求
     * @return 恢复结果
     */
    @PostMapping("/recover")
    public Result<SessionRecoverResponse> recover(@RequestBody SessionRecoverRequest request) {
        log.info("恢复会话: sessionId={}, userId={}", request.sessionId(), request.userId());

        // 执行冷启动恢复
        coldStartRecovery.recoverIfNeeded(request.sessionId(), request.userId());

        // 统计恢复的消息数和待办数
        int messageCount = shortTermMemory.loadRecentMessages(request.sessionId()).size();
        int todoCount = shortTermMemory.loadTodos(request.sessionId()).size();

        return Result.success(new SessionRecoverResponse(
                request.sessionId(), true, messageCount, todoCount
        ));
    }

    /**
     * 结束会话
     *
     * @param request 关闭请求
     * @return 操作结果
     */
    @PostMapping("/close")
    public Result<Void> close(@RequestBody SessionCloseRequest request) {
        shortTermMemory.deleteSession(request.sessionId());
        log.info("关闭会话: sessionId={}", request.sessionId());
        return Result.success(null);
    }

    /**
     * 会话创建请求
     */
    public record SessionCreateRequest(String userId, String previousSessionId) {}

    /**
     * 会话创建响应
     */
    public record SessionCreateResponse(String sessionId) {}

    /**
     * 会话恢复响应
     */
    public record SessionRecoverResponse(String sessionId, boolean recovered, int messageCount, int todoCount) {}

    /**
     * 会话关闭请求
     */
    public record SessionCloseRequest(String sessionId) {}
}