package com.crosstrade.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冷启动恢复请求实体
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ColdRecoverRequest {
    @NotBlank(message = "用户ID不能为空")
    private Long userId;
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
}
