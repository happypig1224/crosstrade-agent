package com.crosstrade.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("agent_session_todo")
public class SessionTodo {
    private Long id;
    private String sessionId;
    private String todoContent; // 待办事项描述
    private String status = "PENDING"; // 状态：PENDING/COMPLETED/CANCELED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
