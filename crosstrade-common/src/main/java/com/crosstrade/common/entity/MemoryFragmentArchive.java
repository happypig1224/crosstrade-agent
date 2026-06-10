package com.crosstrade.common.entity;

/**
 * 记忆摘要归档实体
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * 记忆摘要归档实体
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("agent_memory_fragments_archive")
public class MemoryFragmentArchive {
    private Long id;
    private String userId; // 用户唯一标识
    private String sessionId; // 源会话ID
    private String fragmentText; // 脱水后的高密度事实文本
    private String category; // 记忆领域分类
    private LocalDateTime createdAt; // 原始片段生成时间
    private LocalDateTime archivedAt; // 归档迁移时间
}
