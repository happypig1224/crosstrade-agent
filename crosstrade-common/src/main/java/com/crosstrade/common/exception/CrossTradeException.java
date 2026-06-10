package com.crosstrade.common.exception;

import lombok.Getter;

/**
 * 跨境电商系统基础异常
 * 三层结构：errorCode + message + cause
 * 所有业务异常均继承此类，携带错误码方便前端做差异化处理。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Getter
public class CrossTradeException extends RuntimeException {

    /**
     * 错误码（如SECURITY_BLOCK/INTENT_FAIL/MEMORY_RECOVER_FAIL）
     */
    private final String errorCode;

    public CrossTradeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CrossTradeException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}