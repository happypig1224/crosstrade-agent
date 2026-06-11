package com.crosstrade.common.repository;

import com.crosstrade.common.entity.SessionTodo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会话待办事项Repository
 * 基于Spring Data JDBC，提供对agent_session_todo表的CRUD操作。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface SessionTodoRepository extends CrudRepository<SessionTodo, Long> {

    /**
     * 查询会话的PENDING待办（冷启动恢复用）
     *
     * @param sessionId 会话ID
     * @param status    待办状态（PENDING/COMPLETED/CANCELED）
     * @return 该会话指定状态的待办列表
     */
    List<SessionTodo> findBySessionIdAndStatus(String sessionId, String status);
}