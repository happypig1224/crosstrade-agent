package com.crosstrade.common.repository;

import com.crosstrade.common.entity.ChatHistory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * 对话历史Repository
 * 基于Spring Data JDBC，提供对agent_chat_history表的CRUD操作
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface ChatHistoryRepository extends CrudRepository<ChatHistory, Long> {

    /**
     * 根据会话ID查询最近N轮对话（按时间正序）
     * 16条消息 = 8轮对话（每轮含user+assistant两条）
     *
     * @param sessionId 会话ID
     * @return 最近16条对话记录
     */
    List<ChatHistory> findTop16BySessionIdOrderByCreatedAtAsc(String sessionId);

    /**
     * 根据用户ID查询所有对话（按时间倒序）
     *
     * @param userId 用户ID
     * @return 该用户的所有对话记录
     */
    List<ChatHistory> findByUserIdOrderByCreatedAtDesc(String userId);
}