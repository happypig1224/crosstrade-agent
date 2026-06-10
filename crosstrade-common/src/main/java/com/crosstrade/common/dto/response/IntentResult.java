package com.crosstrade.common.dto.response;

import com.crosstrade.common.enums.IntentType;

/**
 * 意图识别结果
 * 封装意图识别的输出，包含意图类型、置信度和识别来源。
 * source取值：L1_RULE / L2_LLM / L2_TIMEOUT / L2_LLM_DEGRADE / L2_ERROR
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public record IntentResult(
        IntentType intentType,
        double confidence,
        String source
) {
}