package com.crosstrade.common.enums;

/**
 * 安全检查结果枚举类
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public enum SecurityCheckResult {
    PASS("PASS", "审查通过"),     // 放行，进入下一环节
    BLOCK("BLOCK", "审查拦截"),     // 拦截，返回标准化拒绝回复
    DEGRADE("DEGRADE", "降级放行"); // 小LLM不可用时降级为规则过滤通过
    private String value;
    private String description;
    public static SecurityCheckResult fromValue(String value) {
        for (SecurityCheckResult result : values()) {
            if (result.value.equals(value)) {
                return result;
            }
        }
        return BLOCK;
    }
    SecurityCheckResult(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
