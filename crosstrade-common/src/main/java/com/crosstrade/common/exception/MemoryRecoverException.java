package com.crosstrade.common.exception;

import lombok.Getter;

/**
 * 记忆恢复异常
 * 冷启动恢复MySQL查询超时时抛出
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Getter
public class MemoryRecoverException extends CrossTradeException {

    /**
     * 关联的会话ID
     */
    private final String sessionId;

    public MemoryRecoverException(String sessionId, String message) {
        super("MEMORY_RECOVER_FAIL", message);
        this.sessionId = sessionId;
    }

    public MemoryRecoverException(String sessionId, String message, Throwable cause) {
        super("MEMORY_RECOVER_FAIL", message, cause);
        this.sessionId = sessionId;
    }
}