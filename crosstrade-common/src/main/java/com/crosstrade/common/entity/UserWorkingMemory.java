package com.crosstrade.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户工作记忆实体
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserWorkingMemory {
    private Long id;
    private String userId;
    private String summaryTags; // 标签矩阵json
    private String businessHabits; //自然语言习惯总结
    private String toolPreference; // 工具调用计数json
    private LocalDateTime updatedAt; // 更新时间
    private Long version = 0L; // 版本号，默认0
}
