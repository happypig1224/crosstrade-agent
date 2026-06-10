package com.crosstrade.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 聊天历史实体
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("agent_chat_history")
public class ChatHistory {
    private Long id;
    private String sessionId;
    private String userId;
    private String role;
    private String content;
    private String toolCalls; // 工具调用JSON
    private int rawTokens; // token消耗数
    private LocalDateTime createdAt;
}
