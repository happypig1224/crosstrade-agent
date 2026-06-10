package com.crosstrade.common.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 聊天请求DTO
 * 前端发送聊天消息时的请求体，携带校验注解在Controller层自动校验。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public record ChatRequest(
        @NotBlank(message = "会话ID不能为空")
        String sessionId,
        @NotBlank(message = "用户ID不能为空")
        String userId,
        @NotBlank(message = "用户消息不能为空")
        String message,
        String region
) {
}