package com.crosstrade.core.router;

import com.crosstrade.common.entity.UserWorkingMemory;
import com.crosstrade.common.mq.CompressMessage;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 聊天上下文
 * 封装一次对话请求的所有上下文信息，在各Handler之间传递。
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
public class ChatContext {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户消息
     */
    private String userMessage;

    /**
     * 用户所在地区
     */
    private String region;

    /**
     * 最近对话消息
     */
    private List<CompressMessage.MessageItem> recentMessages;

    /**
     * 相关摘要
     */
    private List<String> relevantSummaries;

    /**
     * 用户画像
     */
    private UserWorkingMemory userProfile;

    /**
     * 当前意图类型
     */
    private String intentType;

    /**
     * 追问轮数
     */
    private int clarifyRound;

    /**
     * 待办事项
     */
    private List<String> todos;

    /**
     * 产品ID（报价谈判时使用）
     */
    private String productId;

    /**
     * 获取最近讨论话题
     */
    public String getRecentTopic() {
        if (recentMessages == null || recentMessages.isEmpty()) {
            return null;
        }
        // 取最近一条assistant消息作为最近话题
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            if ("assistant".equals(recentMessages.get(i).getRole())) {
                String content = recentMessages.get(i).getContent();
                if (content != null && content.length() > 50) {
                    return content.substring(0, 50) + "...";
                }
                return content;
            }
        }
        return null;
    }
}