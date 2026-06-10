package com.crosstrade.common.enums;

/**
 * 消息角色枚举
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public enum MessageRole {
    USER("user", "买家输入"), // 用户角色
    AGISTANT("assistant", "Agent回复"), // 助手角色
    SYSTEM("system", "系统提示词"),
    TOOL("tool", "工具执行结果"); // 工具角色
    private String value; // 枚举值，与LLM输出、数据库存储值一致
    private String description; // 枚举描述,中文描述

    //从字符串值反序列化为枚举值
    public static MessageRole fromValue(String value) {
        for (MessageRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return USER;
    }

    // 构造函数
    MessageRole(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
