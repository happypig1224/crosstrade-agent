package com.crosstrade.common.enums;

import lombok.Getter;

/**
 * 意图识别类型枚举
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public enum IntentType {
    PRODUCT_GUID("product_guide", "产品导购"), // 用户描述需求、场景、预算，需要推荐相关产品
    PRODUCT_INFO("product_info", "产品咨询"), // 用户咨询产品对比/定制/售后政策
    QUOTE_NEGOTIATE("quote_negotiate", "报价谈判"),// 用户进入价格谈判流程
    UNKNOWN("unknown", "未知意图"); // 无法识别意图的兜底类型
    @Getter
    private String value; // 枚举值，与LLM输出、数据库存储值一致
    @Getter
    private String description; // 枚举描述,中文描述

    //从字符串值反序列化为枚举值
    public static IntentType fromValue(String value) {
        for (IntentType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    // 构造函数
    IntentType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
