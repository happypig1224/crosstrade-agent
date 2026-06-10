package com.crosstrade.core.memory;

import com.crosstrade.common.constants.BusinessConstants;
import com.crosstrade.common.constants.MqTopicConstants;
import com.crosstrade.common.constants.RedisKeyConstants;
import com.crosstrade.common.mq.CompressMessage;
import com.crosstrade.core.mq.MqProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 上下文压缩
 * 当对话超过12轮时，Redis中存储的对话会越来越多，大模型的上下文窗口有限。
 * 需要对窗口外的对话进行压缩：
 * 1. 检测对话轮数是否超过12轮
 * 2. 截取窗口外4条对话
 * 3. 通过RocketMQ异步发送压缩消息（携带完整对话内容，不传ID）
 * 4. Redis LTRIM保留窗口内8轮对话
 * 5. MQ消费者调用小模型摘要压缩后存入Milvus和MySQL
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class ContextCompressor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 检测并触发上下文压缩
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     */
    public void checkAndCompress(String sessionId, String userId) {
        // 检查对话轮数是否超过阈值
        String roundKey = RedisKeyConstants.buildKey(RedisKeyConstants.ROUND_KEY, sessionId);
        String roundStr = redisTemplate.opsForValue().get(roundKey);
        if (roundStr == null) {
            return;
        }
        long round = Long.parseLong(roundStr);
        if (round <= BusinessConstants.COMPRESS_THRESHOLD) {
            return; // 不需要压缩
        }

        // 截取窗口外4条对话（最早4轮=8条消息）
        String messageKey = RedisKeyConstants.buildKey(RedisKeyConstants.MESSAGE_KEY, sessionId);
        List<String> windowOutJsons = redisTemplate.opsForList().range(messageKey, 0, 7);
        if (windowOutJsons == null || windowOutJsons.isEmpty()) {
            return;
        }

        List<CompressMessage.MessageItem> windowOutMessages = new ArrayList<>();
        for (String json : windowOutJsons) {
            try {
                CompressMessage.MessageItem item = objectMapper.readValue(json, CompressMessage.MessageItem.class);
                windowOutMessages.add(item);
            } catch (JsonProcessingException e) {
                log.error("反序列化消息失败: sessionId={}, json={}", sessionId, json, e);
            }
        }

        // 通过RocketMQ异步发送压缩消息（完整对话内容）
        CompressMessage compressMsg = new CompressMessage(sessionId, userId, windowOutMessages);
        try {
            mqProducer.sendCompress(compressMsg);
        } catch (Exception e) {
            log.error("发送压缩消息失败: sessionId={}", sessionId, e);
            return;
        }

        // Redis LTRIM保留窗口内8轮对话
        redisTemplate.opsForList().trim(messageKey, 8, -1);
        redisTemplate.opsForValue().set(roundKey, "8");

        log.info("上下文压缩触发: sessionId={}, 压缩前轮数={}, 压缩消息条数={}", sessionId, round, windowOutMessages.size());
    }
}