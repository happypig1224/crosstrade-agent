package com.crosstrade.common.dto.response;

import lombok.*;

/**
 * 聊天响应实体
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponse {
    private String content;
    private int index; // token序号
    private String eventType; // 事件类型
    private String sessionId; // 会话ID
    private int totalTokens; // 总token消耗
    private String intentType; // 意图类型
    /**
     * 创建Token事件
     */
    public static ChatResponse token(String content, int index) {
        return ChatResponse.builder()
                .content(content)
                .index(index)
                .eventType("token")
                .build();
    }

    /**
     * 创建完成事件
     */
    public static ChatResponse done(String sessionId, int totalTokens, String intentType) {
        return ChatResponse.builder()
                .eventType("done")
                .sessionId(sessionId)
                .totalTokens(totalTokens)
                .intentType(intentType)
                .build();
    }
}
