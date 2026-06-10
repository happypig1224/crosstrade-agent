package com.crosstrade.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 记忆分类枚举类
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Getter
@AllArgsConstructor
public enum MemoryCategory {
    COMMERCE("commerce", "商务"),
    AFTER_SALES("after_sales", "售后"),
    PERSONAL("personal", "个人习惯");

    private final String value;
    private final String description;

    public static MemoryCategory fromValue(String value) {
        for (MemoryCategory category : values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        return COMMERCE;
    }
}
