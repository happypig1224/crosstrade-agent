package com.crosstrade.core.memory;

import com.crosstrade.common.constants.BusinessConstants;
import com.crosstrade.common.constants.RedisKeyConstants;
import com.crosstrade.common.entity.ChatHistory;
import com.crosstrade.common.entity.SessionTodo;
import com.crosstrade.common.exception.MemoryRecoverException;
import com.crosstrade.common.repository.ChatHistoryRepository;
import com.crosstrade.common.repository.SessionTodoRepository;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 冷启动恢复
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class ColdStartRecovery {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private SessionTodoRepository sessionTodoRepository;

    @Autowired
    private ShortTermMemory shortTermMemory;

    /**
     * 冷启动恢复
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     */
    public void recoverIfNeeded(String sessionId, String userId) {
        // 检查Redis短期记忆是否存在
        String messageKey = RedisKeyConstants.buildKey(RedisKeyConstants.MESSAGE_KEY, sessionId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(messageKey))) {
            return; // 缓存命中，无需恢复
        }

        // 获取Redisson分布式锁
        String lockKey = RedisKeyConstants.buildKey(RedisKeyConstants.COLDSTART_LOCK_KEY, sessionId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，等待3s，锁自动释放10s
            boolean locked = lock.tryLock(
                    BusinessConstants.COLDSTART_LOCK_WAIT_SECONDS,
                    BusinessConstants.COLDSTART_LOCK_LEASE_SECONDS,
                    TimeUnit.SECONDS
            );
            if (!locked) {
                // 等待其他线程回填完成
                Thread.sleep(500);
                log.info("冷启动恢复等待其他线程回填: sessionId={}", sessionId);
                return;
            }

            // 双重检查（DCL）
            if (Boolean.TRUE.equals(redisTemplate.hasKey(messageKey))) {
                return; // 其他线程已完成回填
            }

            // 从MySQL回填最近8轮对话（时间正序）
            List<ChatHistory> messages = chatHistoryRepository.findTop16BySessionIdOrderByCreatedAtAsc(sessionId);
            for (ChatHistory msg : messages) {
                shortTermMemory.saveMessage(sessionId, userId, msg.getRole(), msg.getContent());
            }

            // 从MySQL回填PENDING待办事项
            List<SessionTodo> todos = sessionTodoRepository.findBySessionIdAndStatus(sessionId, "PENDING");
            for (SessionTodo todo : todos) {
                shortTermMemory.saveTodo(sessionId, todo.getTodoContent());
            }

            log.info("冷启动恢复完成: sessionId={}, messages={}, todos={}",
                    sessionId, messages.size(), todos.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("冷启动恢复获取锁超时: sessionId={}", sessionId, e);
            throw new MemoryRecoverException(sessionId, "冷启动恢复获取锁超时");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}