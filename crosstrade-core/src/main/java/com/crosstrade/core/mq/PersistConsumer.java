package com.crosstrade.core.mq;

import com.crosstrade.common.constants.MqTopicConstants;
import com.crosstrade.common.entity.ChatHistory;
import com.crosstrade.common.mq.PersistMessage;
import com.crosstrade.common.repository.ChatHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 对话持久化MQ消费者
 * 消费持久化Topic消息，将对话记录异步写入MySQL
 * 消息体携带完整内容，消费者无需回查数据库
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = MqTopicConstants.MESSAGE_PERSIST_TOPIC,
        consumerGroup = "persist-consumer-group"
)
public class PersistConsumer implements RocketMQListener<PersistMessage> {

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    /**
     * 消费持久化消息
     *
     * @param persistMsg 持久化消息
     */
    @Override
    public void onMessage(PersistMessage persistMsg) {
        log.info("收到持久化消息: sessionId={}, userId={}", persistMsg.getSessionId(), persistMsg.getUserId());

        // 写入用户消息
        ChatHistory userHistory = new ChatHistory();
        userHistory.setSessionId(persistMsg.getSessionId());
        userHistory.setUserId(persistMsg.getUserId());
        userHistory.setRole("user");
        userHistory.setContent(persistMsg.getUserMessage());
        userHistory.setRawTokens(0);
        userHistory.setCreatedAt(LocalDateTime.now());
        chatHistoryRepository.save(userHistory);

        // 写入AI回复
        ChatHistory aiHistory = new ChatHistory();
        aiHistory.setSessionId(persistMsg.getSessionId());
        aiHistory.setUserId(persistMsg.getUserId());
        aiHistory.setRole("assistant");
        aiHistory.setContent(persistMsg.getAiMessage());
        aiHistory.setRawTokens(persistMsg.getTokens());
        aiHistory.setCreatedAt(LocalDateTime.now());
        chatHistoryRepository.save(aiHistory);

        log.info("对话持久化完成: sessionId={}", persistMsg.getSessionId());
    }
}