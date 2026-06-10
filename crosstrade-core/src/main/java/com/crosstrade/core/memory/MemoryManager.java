package com.crosstrade.core.memory;

import com.crosstrade.common.entity.MemoryFragment;
import com.crosstrade.common.entity.UserWorkingMemory;
import com.crosstrade.common.mq.CompressMessage;
import com.crosstrade.core.router.ChatContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 记忆管理入口
 * MemoryManager是三层记忆的协调入口，负责：
 * - 组装完整上下文（压缩摘要 + 短期记忆 + RAG结果 + 工具结果）
 * - 各层记忆的加载优先级和降级策略
 * - 统一对外提供记忆读写接口
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class MemoryManager {

    @Autowired
    private ShortTermMemory shortTermMemory;

    @Autowired
    private LongTermMemory longTermMemory;

    @Autowired
    private WorkingMemory workingMemory;

    @Autowired
    private ColdStartRecovery coldStartRecovery;

    /**
     * 组装完整上下文
     *
     * @param sessionId   会话ID
     * @param userId      用户ID
     * @param userMessage 用户消息
     * @return 聊天上下文
     */
    public ChatContext buildContext(String sessionId, String userId, String userMessage) {
        // 加载L1短期记忆（缓存击穿时触发冷启动恢复）
        List<CompressMessage.MessageItem> recentMessages = shortTermMemory.loadRecentMessages(sessionId);
        if (recentMessages.isEmpty()) {
            coldStartRecovery.recoverIfNeeded(sessionId, userId);
            recentMessages = shortTermMemory.loadRecentMessages(sessionId);
        }

        // 加载L2长期记忆（语义检索相关摘要）
        List<MemoryFragment> relevantFragments = longTermMemory.searchRelevantSummary(userId, userMessage, 5);
        List<String> relevantSummaries = relevantFragments.stream()
                .map(MemoryFragment::getFragmentText)
                .collect(Collectors.toList());

        // 加载L3工作记忆（用户画像）
        UserWorkingMemory userProfile = workingMemory.getUserProfile(userId);

        // 加载会话状态和待办
        String intentType = shortTermMemory.loadState(sessionId, "intentType");
        String clarifyRoundStr = shortTermMemory.loadState(sessionId, "clarifyRound");
        int clarifyRound = clarifyRoundStr != null ? Integer.parseInt(clarifyRoundStr) : 0;
        List<String> todos = shortTermMemory.loadTodos(sessionId);

        // 组装ChatContext
        return ChatContext.builder()
                .sessionId(sessionId)
                .userId(userId)
                .userMessage(userMessage)
                .recentMessages(recentMessages)
                .relevantSummaries(relevantSummaries)
                .userProfile(userProfile)
                .intentType(intentType)
                .clarifyRound(clarifyRound)
                .todos(todos)
                .build();
    }

    /**
     * 保存对话到记忆
     *
     * @param sessionId   会话ID
     * @param userId      用户ID
     * @param userMessage 用户消息
     * @param aiMessage   AI回复
     */
    public void saveConversation(String sessionId, String userId, String userMessage, String aiMessage) {
        // 保存到L1短期记忆
        shortTermMemory.saveMessage(sessionId, userId, "user", userMessage);
        shortTermMemory.saveMessage(sessionId, userId, "assistant", aiMessage);
    }

    /**
     * 保存会话状态
     *
     * @param sessionId 会话ID
     * @param field     状态字段
     * @param value     状态值
     */
    public void saveState(String sessionId, String field, String value) {
        shortTermMemory.saveState(sessionId, field, value);
    }
}