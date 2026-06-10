package com.crosstrade.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 记忆摘要片段实体
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("agent_memory_fragments")
public class MemoryFragment {
    private Long id;
    private String userId;
    private String sessionId;
    private String fragmentText;
    private String category; // 记忆领域分类
    private LocalDateTime createdAt;
    private boolean vectorFailed = false; // 向量写入是否失败，用于重试
}
