package com.crosstrade.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Resilience4j熔断/重试/限流配置
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Configuration
public class Resilience4jConfig {

    /**
     * Milvus熔断降级为仅使用MySQL
     * 返回空结果，由调用方走MySQL降级逻辑
     */
    public Object milvusFallback(Throwable throwable) {
        log.warn("Milvus熔断降级，仅使用MySQL: {}", throwable.getMessage());
        return Collections.emptyList();
    }
}