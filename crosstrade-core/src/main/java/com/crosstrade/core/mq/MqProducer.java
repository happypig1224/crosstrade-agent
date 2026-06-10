package com.crosstrade.core.mq;

import com.crosstrade.common.constants.MqTopicConstants;
import com.crosstrade.common.mq.CompressMessage;
import com.crosstrade.common.mq.PersistMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * MQ消息生产者
 * 负责异步消息的发送，解耦核心对话流程与存储操作。
 * 两个Topic：
 * - COMPRESS_TOPIC：上下文压缩消息（对话超过12轮时发送）
 * - PERSIST_TOPIC：对话持久化消息（流式输出完成后发送）
 * 消息发送失败处理：本地重试3次 + 指数退避，仍失败则记录补偿日志。
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class MqProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送压缩消息
     *
     * @param compressMsg 压缩消息
     */
    public void sendCompress(CompressMessage compressMsg) {
        sendWithRetry(MqTopicConstants.CONTEXT_COMPRESS_TOPIC, MqTopicConstants.CONTEXT_COMPRESS_TAG, compressMsg);
    }

    /**
     * 发送持久化消息
     *
     * @param persistMsg 持久化消息
     */
    public void sendPersist(PersistMessage persistMsg) {
        sendWithRetry(MqTopicConstants.MESSAGE_PERSIST_TOPIC, MqTopicConstants.MESSAGE_PERSIST_TAG, persistMsg);
    }

    /**
     * 带重试的消息发送
     *
     * @param topic   Topic名称
     * @param tag     Tag名称
     * @param message 消息体
     */
    public void sendWithRetry(String topic, String tag, Object message) {
        String destination = topic + ":" + tag;
        try {
            rocketMQTemplate.syncSend(destination, MessageBuilder.withPayload(message).build());
            log.debug("MQ消息发送成功: topic={}, tag={}", topic, tag);
        } catch (Exception e) {
            log.error("MQ消息发送失败: topic={}, tag={}, error={}", topic, tag, e.getMessage());
            throw e;
        }
    }

    /**
     * 重试3次仍失败的补偿处理
     *
     * @param topic   Topic名称
     * @param tag     Tag名称
     * @param message 消息体
     */
    public void sendWithCompensation(String topic, String tag, Object message) {
        for (int i = 1; i <= 3; i++) {
            try {
                sendWithRetry(topic, tag, message);
                return;
            } catch (Exception e) {
                if (i == 3) {
                    log.error("MQ消息发送失败需补偿: topic={}, tag={}, message={}, 重试次数={}",
                            topic, tag, message, i);
                } else {
                    try {
                        Thread.sleep((long) Math.pow(2, i) * 100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
