package com.crosstrade.common.enums;

/**
 * 代办状态枚举类
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public enum TodoStatus {
    TODO("todo", "待处理"),
    DONE("done", "已完成"),
    CANCELED("canceled", "已取消");
    private String value;
    private String description;

    public static TodoStatus fromValue(String value) {
        for (TodoStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return TODO;
    }

    TodoStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
