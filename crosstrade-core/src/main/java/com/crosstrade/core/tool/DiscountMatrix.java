package com.crosstrade.core.tool;

import com.crosstrade.common.dto.response.QuoteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 折扣矩阵业务逻辑
 * 防薅羊毛核心：Agent仅提取参数，折扣矩阵Java业务代码控制价格决策
 * 基于订购量的阶梯折扣率，结合产品最低价限制，计算最终可接受价格
 * LLM无法绕过此Java逻辑直接给出更低价格
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class DiscountMatrix {

    /**
     * 折扣率阶梯表（基于订购量）
     * Key: 起订量阈值, Value: 折扣率
     */
    private static final Map<Integer, Double> DISCOUNT_RATE_TABLE = new LinkedHashMap<>();

    static {
        DISCOUNT_RATE_TABLE.put(50, 0.05);    // 50件起 5%折扣
        DISCOUNT_RATE_TABLE.put(100, 0.10);   // 100件起 10%折扣
        DISCOUNT_RATE_TABLE.put(500, 0.15);   // 500件起 15%折扣
        DISCOUNT_RATE_TABLE.put(1000, 0.20);  // 1000件起 20%折扣
    }

    /**
     * 模拟产品价格信息
     */
    private static final Map<String, ProductPriceInfo> PRODUCT_PRICE_MAP = Map.of(
            "P001", new ProductPriceInfo("P001", new BigDecimal("89.99"), new BigDecimal("65.00")),
            "P002", new ProductPriceInfo("P002", new BigDecimal("29.99"), new BigDecimal("18.00")),
            "P003", new ProductPriceInfo("P003", new BigDecimal("3.99"), new BigDecimal("2.50")),
            "P004", new ProductPriceInfo("P004", new BigDecimal("15.99"), new BigDecimal("9.00")),
            "P005", new ProductPriceInfo("P005", new BigDecimal("24.99"), new BigDecimal("15.00"))
    );

    /**
     * 计算报价
     *
     * @param productId     产品ID
     * @param expectedPrice 期望价格
     * @param quantity      采购数量
     * @return 报价结果
     */
    public QuoteResult calculate(String productId, BigDecimal expectedPrice, int quantity) {
        // 获取产品价格信息（模拟从产品系统获取）
        ProductPriceInfo product = PRODUCT_PRICE_MAP.get(productId);
        if (product == null) {
            log.warn("产品不存在: productId={}", productId);
            return new QuoteResult(false, null, BigDecimal.ZERO);
        }

        BigDecimal currentPrice = product.currentPrice;
        BigDecimal minPrice = product.minPrice;

        // 计算批量折扣率
        double discountRate = getDiscountRate(quantity);

        // 计算可接受价格
        BigDecimal acceptablePrice = currentPrice.multiply(
                BigDecimal.ONE.subtract(BigDecimal.valueOf(discountRate))
        ).setScale(2, RoundingMode.HALF_UP);

        // 不低于最低价
        acceptablePrice = acceptablePrice.max(minPrice);

        // 判定结果
        if (expectedPrice.compareTo(acceptablePrice) >= 0) {
            BigDecimal finalPrice = expectedPrice.min(acceptablePrice);
            return new QuoteResult(true, finalPrice, minPrice);
        } else {
            return new QuoteResult(false, null, acceptablePrice);
        }
    }

    /**
     * 获取折扣率
     *
     * @param quantity 采购数量
     * @return 折扣率（0.0~0.2）
     */
    private double getDiscountRate(int quantity) {
        double rate = 0.0;
        for (Map.Entry<Integer, Double> entry : DISCOUNT_RATE_TABLE.entrySet()) {
            if (quantity >= entry.getKey()) {
                rate = Math.max(rate, entry.getValue());
            }
        }
        return rate;
    }

    /**
     * 产品价格信息
     */
    private record ProductPriceInfo(String productId, BigDecimal currentPrice, BigDecimal minPrice) {}
}