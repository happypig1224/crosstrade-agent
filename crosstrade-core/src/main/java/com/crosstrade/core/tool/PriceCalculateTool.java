package com.crosstrade.core.tool;

import com.crosstrade.common.dto.response.QuoteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * 价格计算工具
 * Spring AI Function Calling注册方式，通过@Bean注册为FunctionCallback。
 * 调用折扣矩阵计算最终报价，防薅羊毛核心：Agent仅提取参数，
 * 折扣矩阵Java业务代码控制价格决策。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Configuration
public class PriceCalculateTool {

    @Autowired
    private DiscountMatrix discountMatrix;

    /**
     * 价格计算请求
     */
    public record PriceCalculateRequest(
            String productId,
            BigDecimal expectedPrice,
            int quantity
    ) {}

    /**
     * 注册价格计算Function Calling
     */
    @Bean
    public FunctionToolCallback<PriceCalculateRequest, QuoteResult> priceCalculateFunction() {
        return FunctionToolCallback.<PriceCalculateRequest, QuoteResult>builder("priceCalculate", this::calculate)
                .description("根据产品ID、期望价格、采购量计算最终报价")
                .inputType(PriceCalculateRequest.class)
                .build();
    }

    /**
     * 价格计算逻辑
     *
     * @param request 计算请求
     * @return 报价结果
     */
    public QuoteResult calculate(PriceCalculateRequest request) {
        // 调用折扣矩阵Java业务代码（防薅羊毛：剥离LLM决策权）
        QuoteResult result = discountMatrix.calculate(
                request.productId(),
                request.expectedPrice(),
                request.quantity()
        );

        log.info("价格计算: productId={}, expectedPrice={}, quantity={}, accepted={}, finalPrice={}",
                request.productId(), request.expectedPrice(), request.quantity(),
                result.accepted(), result.finalPrice());

        return result;
    }
}