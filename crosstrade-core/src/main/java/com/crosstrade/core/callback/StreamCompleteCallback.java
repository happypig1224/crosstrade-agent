package com.crosstrade.core.callback;

import com.crosstrade.common.constants.MqTopicConstants;
import com.crosstrade.common.mq.PersistMessage;
import com.crosstrade.core.memory.ContextCompressor;
import com.crosstrade.core.memory.ShortTermMemory;
import com.crosstrade.core.mq.MqProducer;
import com.crosstrade.core.observe.MetricsCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 流式输出完成回调
 * 挂载到Spring AI流式输出的doOnComplete生命周期，确保存储时序与输出完整性一致。
 * AI响应是逐Token吐露的，Redis和MySQL的存储必须等到输出完成后才能执行，
 * 否则可能存储不完整的内容。
 * 回调执行流程：
 * 1. Redis RPUSH短期记忆（同步，保证下次对话可用）
 * 2. RocketMQ异步发送持久化消息（异步，不阻塞响应）
 * 3. 检测是否需要上下文压缩
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class StreamCompleteCallback {

    @Autowired
    private ShortTermMemory shortTermMemory;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ContextCompressor contextCompressor;

    @Autowired
    private MetricsCollector metricsCollector;

    /**
     * 流式输出完成回调
     *
     * @param sessionId   会话ID
     * @param userId      用户ID
     * @param userMessage 用户消息
     * @param aiResponse  AI完整回复
     * @param tokens      Token消耗数
     */
    public void onComplete(String sessionId, String userId,
                           String userMessage, String aiResponse, int tokens) {
        log.info("流式输出完成回调: sessionId={}, userId={}, tokens={}", sessionId, userId, tokens);

        // Redis RPUSH短期记忆（同步，保证下次对话可用）
        shortTermMemory.saveMessage(sessionId, userId, "user", userMessage);
        shortTermMemory.saveMessage(sessionId, userId, "assistant", aiResponse);

        // RocketMQ异步发送持久化消息（异步，不阻塞响应）
        PersistMessage persistMsg = new PersistMessage(sessionId, userId, userMessage, aiResponse, tokens);
        try {
            mqProducer.sendPersist(persistMsg);
        } catch (Exception e) {
            // 发送失败，走补偿重试
            mqProducer.sendWithCompensation(MqTopicConstants.MESSAGE_PERSIST_TOPIC, MqTopicConstants.MESSAGE_PERSIST_TAG, persistMsg);
        }

        // 检测是否需要上下文压缩
        contextCompressor.checkAndCompress(sessionId, userId);

        // 记录指标
        metricsCollector.recordTokenConsume("qwen3.7-max", tokens);
    }
}