package com.crosstrade.common.dto.response;

import java.math.BigDecimal;

/**
 * 报价计算结果
 * 折扣矩阵Java业务代码的计算输出
 * accepted=true时finalPrice有值，accepted=false时minPrice有值用于反报价。
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public record QuoteResult(
        boolean accepted,
        BigDecimal finalPrice,
        BigDecimal minPrice
) {
}