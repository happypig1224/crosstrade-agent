package com.crosstrade.common.exception;

import lombok.Getter;

/**
 * 安全审查拦截异常
 * 入口/出口审查不通过时抛出
 * blockReason: SENSITIVE_WORD / PROMPT_INJECTION / OUTPUT_VIOLATION
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Getter
public class SecurityBlockException extends CrossTradeException {

    /**
     * 拦截原因：SENSITIVE_WORD / PROMPT_INJECTION / OUTPUT_VIOLATION
     */
    private final String blockReason;

    public SecurityBlockException(String blockReason, String message) {
        super("SECURITY_BLOCK", message);
        this.blockReason = blockReason;
    }

    public SecurityBlockException(String blockReason, String message, Throwable cause) {
        super("SECURITY_BLOCK", message, cause);
        this.blockReason = blockReason;
    }
}