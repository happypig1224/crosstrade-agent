package com.crosstrade.common.exception;

import lombok.Getter;

/**
 * 意图识别异常
 * L2语义分析超时或返回无效结果时抛出
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Getter
public class IntentIdentifyException extends CrossTradeException {

    /**
     * 详细信息（超时/无效结果）
     */
    private final String detail;

    public IntentIdentifyException(String detail) {
        super("INTENT_FAIL", "意图识别失败: " + detail);
        this.detail = detail;
    }

    public IntentIdentifyException(String detail, Throwable cause) {
        super("INTENT_FAIL", "意图识别失败: " + detail, cause);
        this.detail = detail;
    }
}