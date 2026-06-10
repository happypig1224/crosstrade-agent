package com.crosstrade.common.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 冷启动恢复请求DTO
 * 用户恢复历史会话时的请求体。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public record SessionRecoverRequest(
        @NotBlank(message = "会话ID不能为空")
        String sessionId,
        @NotBlank(message = "用户ID不能为空")
        String userId
) {
}