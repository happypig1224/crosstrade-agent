package com.crosstrade.api.advice;

import com.crosstrade.api.controller.Result;
import com.crosstrade.common.exception.CrossTradeException;
import com.crosstrade.common.exception.IntentIdentifyException;
import com.crosstrade.common.exception.MemoryRecoverException;
import com.crosstrade.common.exception.SecurityBlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理所有Controller抛出的异常，转换为标准Result响应格式。
 * 每种异常类型对应不同的错误码和用户提示信息。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 安全审查拦截异常
     */
    @ExceptionHandler(SecurityBlockException.class)
    public Result<Void> handleSecurityBlock(SecurityBlockException e) {
        log.warn("安全审查拦截: blockReason={}, message={}", e.getBlockReason(), e.getMessage());
        return Result.fail("SECURITY_BLOCK", "您的消息包含敏感内容，请重新描述");
    }

    /**
     * 意图识别异常
     */
    @ExceptionHandler(IntentIdentifyException.class)
    public Result<Void> handleIntentIdentify(IntentIdentifyException e) {
        log.warn("意图识别失败: detail={}", e.getDetail());
        return Result.fail("INTENT_FAIL", "抱歉，我暂时无法理解您的问题，请换个方式描述");
    }

    /**
     * 记忆恢复异常
     */
    @ExceptionHandler(MemoryRecoverException.class)
    public Result<Void> handleMemoryRecover(MemoryRecoverException e) {
        log.error("记忆恢复失败: sessionId={}", e.getSessionId(), e);
        return Result.fail("MEMORY_RECOVER_FAIL", "会话历史暂时无法加载，将以新会话开始");
    }

    /**
     * 通用业务异常
     */
    @ExceptionHandler(CrossTradeException.class)
    public Result<Void> handleCrossTrade(CrossTradeException e) {
        log.error("业务异常: errorCode={}, message={}", e.getErrorCode(), e.getMessage());
        return Result.fail(e.getErrorCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return Result.fail("PARAM_ERROR", "请求参数不合法: " + e.getMessage());
    }

    /**
     * 通用异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleGeneral(Exception e) {
        log.error("系统异常", e);
        return Result.fail("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}