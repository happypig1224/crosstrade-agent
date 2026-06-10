package com.crosstrade.core.memory;

import com.crosstrade.common.constants.BusinessConstants;
import com.crosstrade.common.constants.RedisKeyConstants;
import com.crosstrade.common.mq.CompressMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * L1短期记忆（Redis）
 * 使用Redis存储当前会话的热度上下文，包括：
 * - 最近8轮对话（LIST结构）
 * - 当前任务状态（HASH结构）
 * - 待办事项（LIST结构）
 * - 对话轮数（STRING计数器）
 * 关键设计：
 * - 滑动过期：每次对话交互时重置TTL为24小时
 * - 8轮窗口：超出8轮时通过LTRIM裁剪
 * - 所有Key共享相同的TTL，一起过期
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class ShortTermMemory {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 保存消息到短期记忆
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param role      角色
     * @param content   消息内容
     */
    public void saveMessage(String sessionId, String userId, String role, String content) {
        // RPUSH消息到List
        CompressMessage.MessageItem messageItem = new CompressMessage.MessageItem(
                role, content, System.currentTimeMillis()
        );
        try {
            String messageJson = objectMapper.writeValueAsString(messageItem);
            String messageKey = RedisKeyConstants.buildKey(RedisKeyConstants.MESSAGE_KEY, sessionId);
            redisTemplate.opsForList().rightPush(messageKey, messageJson);
        } catch (JsonProcessingException e) {
            log.error("序列化消息失败: sessionId={}, role={}", sessionId, role, e);
            return;
        }

        // 滑动过期 - 每次交互重置所有Key的TTL为24h
        expireAll(sessionId);

        // 维护8轮窗口 - 超出时LTRIM
        String roundKey = RedisKeyConstants.buildKey(RedisKeyConstants.ROUND_KEY, sessionId);
        Long round = redisTemplate.opsForValue().increment(roundKey);
        if (round != null && round > BusinessConstants.SHORT_MEMORY_ROUNDS) {
            // 保留最近8轮(16条消息：每轮含user+assistant两条)
            String messageKey = RedisKeyConstants.buildKey(RedisKeyConstants.MESSAGE_KEY, sessionId);
            redisTemplate.opsForList().trim(messageKey, -16, -1);
        }

        // 将用户加入48h活跃用户集合
        redisTemplate.opsForSet().add(RedisKeyConstants.ACTIVE_USERS_KEY, userId);
        redisTemplate.expire(RedisKeyConstants.ACTIVE_USERS_KEY,
                BusinessConstants.ACTIVE_USERS_TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * 加载最近对话
     *
     * @param sessionId 会话ID
     * @return 最近对话消息列表
     */
    public List<CompressMessage.MessageItem> loadRecentMessages(String sessionId) {
        String messageKey = RedisKeyConstants.buildKey(RedisKeyConstants.MESSAGE_KEY, sessionId);
        List<String> messageJsons = redisTemplate.opsForList().range(messageKey, 0, -1);
        if (messageJsons == null || messageJsons.isEmpty()) {
            return new ArrayList<>();
        }

        List<CompressMessage.MessageItem> messages = new ArrayList<>();
        for (String json : messageJsons) {
            try {
                CompressMessage.MessageItem item = objectMapper.readValue(json, CompressMessage.MessageItem.class);
                messages.add(item);
            } catch (JsonProcessingException e) {
                log.error("反序列化消息失败: sessionId={}, json={}", sessionId, json, e);
            }
        }
        return messages;
    }

    /**
     * 保存任务状态
     *
     * @param sessionId 会话ID
     * @param field     状态字段
     * @param value     状态值
     */
    public void saveState(String sessionId, String field, String value) {
        String stateKey = RedisKeyConstants.buildKey(RedisKeyConstants.STATE_KEY, sessionId);
        redisTemplate.opsForHash().put(stateKey, field, value);
        expireAll(sessionId);
    }

    /**
     * 加载任务状态
     *
     * @param sessionId 会话ID
     * @param field     状态字段
     * @return 状态值
     */
    public String loadState(String sessionId, String field) {
        String stateKey = RedisKeyConstants.buildKey(RedisKeyConstants.STATE_KEY, sessionId);
        Object value = redisTemplate.opsForHash().get(stateKey, field);
        return value != null ? value.toString() : null;
    }

    /**
     * 保存待办事项
     *
     * @param sessionId   会话ID
     * @param todoContent 待办内容
     */
    public void saveTodo(String sessionId, String todoContent) {
        String todoKey = RedisKeyConstants.buildKey(RedisKeyConstants.TODO_KEY, sessionId);
        redisTemplate.opsForList().rightPush(todoKey, todoContent);
        expireAll(sessionId);
    }

    /**
     * 加载待办事项
     *
     * @param sessionId 会话ID
     * @return 待办事项列表
     */
    public List<String> loadTodos(String sessionId) {
        String todoKey = RedisKeyConstants.buildKey(RedisKeyConstants.TODO_KEY, sessionId);
        List<String> todos = redisTemplate.opsForList().range(todoKey, 0, -1);
        return todos != null ? todos : new ArrayList<>();
    }

    /**
     * 获取对话轮数
     *
     * @param sessionId 会话ID
     * @return 对话轮数
     */
    public long getRound(String sessionId) {
        String roundKey = RedisKeyConstants.buildKey(RedisKeyConstants.ROUND_KEY, sessionId);
        String roundStr = redisTemplate.opsForValue().get(roundKey);
        return roundStr != null ? Long.parseLong(roundStr) : 0;
    }

    /**
     * 重置所有Key的TTL为24h（滑动过期）
     *
     * @param sessionId 会话ID
     */
    private void expireAll(String sessionId) {
        long ttlHours = BusinessConstants.SHORT_MEMORY_TTL_HOURS;
        redisTemplate.expire(RedisKeyConstants.buildKey(RedisKeyConstants.MESSAGE_KEY, sessionId), ttlHours, TimeUnit.HOURS);
        redisTemplate.expire(RedisKeyConstants.buildKey(RedisKeyConstants.STATE_KEY, sessionId), ttlHours, TimeUnit.HOURS);
        redisTemplate.expire(RedisKeyConstants.buildKey(RedisKeyConstants.TODO_KEY, sessionId), ttlHours, TimeUnit.HOURS);
        redisTemplate.expire(RedisKeyConstants.buildKey(RedisKeyConstants.ROUND_KEY, sessionId), ttlHours, TimeUnit.HOURS);
    }

    /**
     * 删除会话所有缓存（主动过期）
     *
     * @param sessionId 会话ID
     */
    public void deleteSession(String sessionId) {
        redisTemplate.delete(RedisKeyConstants.buildKey(RedisKeyConstants.MESSAGE_KEY, sessionId));
        redisTemplate.delete(RedisKeyConstants.buildKey(RedisKeyConstants.STATE_KEY, sessionId));
        redisTemplate.delete(RedisKeyConstants.buildKey(RedisKeyConstants.TODO_KEY, sessionId));
        redisTemplate.delete(RedisKeyConstants.buildKey(RedisKeyConstants.ROUND_KEY, sessionId));
    }

    /**
     * 检查会话缓存是否存在
     *
     * @param sessionId 会话ID
     * @return true=存在
     */
    public boolean exists(String sessionId) {
        String messageKey = RedisKeyConstants.buildKey(RedisKeyConstants.MESSAGE_KEY, sessionId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(messageKey));
    }
}