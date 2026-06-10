package com.crosstrade.core.mq;

import com.crosstrade.common.constants.MqTopicConstants;
import com.crosstrade.common.entity.MemoryFragment;
import com.crosstrade.common.mq.CompressMessage;
import com.crosstrade.common.repository.MemoryFragmentRepository;
import com.crosstrade.core.memory.LongTermMemory;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 上下文压缩MQ消费者
 * 消费压缩Topic消息，调用小模型摘要压缩后存入Milvus和MySQL。
 * 消息体携带完整对话内容，消费者无需回查数据库。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = MqTopicConstants.CONTEXT_COMPRESS_TOPIC,
        consumerGroup = "compress-consumer-group"
)
public class CompressConsumer implements RocketMQListener<CompressMessage> {

    @Autowired
    @Qualifier("chatClient")
    private ChatClient chatClient;

    @Autowired
    private LongTermMemory longTermMemory;

    @Autowired
    private MemoryFragmentRepository fragmentRepository;

    /**
     * 消费压缩消息
     *
     * @param compressMsg 压缩消息
     */
    @Override
    public void onMessage(CompressMessage compressMsg) {
        log.info("收到压缩消息: sessionId={}, userId={}, 消息条数={}",
                compressMsg.getSessionId(), compressMsg.getUserId(),
                compressMsg.getMessages() != null ? compressMsg.getMessages().size() : 0);

        // 小模型摘要压缩
        String messagesText = compressMsg.getMessages().stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        String summaryPrompt = "将以下对话压缩为高密度事实摘要，保留关键商务信息（产品、价格、数量、偏好）:\n" + messagesText;
        String fragmentText = chatClient.prompt(summaryPrompt).call().content();

        if (fragmentText == null || fragmentText.trim().isEmpty()) {
            log.warn("摘要压缩结果为空: sessionId={}", compressMsg.getSessionId());
            return;
        }

        // 分类记忆类别
        String category = classifyCategory(fragmentText);

        // 存入Milvus向量 + MySQL摘要表
        MemoryFragment fragment = new MemoryFragment();
        fragment.setUserId(compressMsg.getUserId());
        fragment.setSessionId(compressMsg.getSessionId());
        fragment.setFragmentText(fragmentText.trim());
        fragment.setCategory(category);

        longTermMemory.saveFragment(fragment);

        log.info("上下文压缩完成: sessionId={}, fragmentText长度={}, category={}",
                compressMsg.getSessionId(), fragmentText.length(), category);
    }

    /**
     * 根据摘要内容分类记忆类别
     *
     * @param fragmentText 摘要文本
     * @return 记忆类别
     */
    private String classifyCategory(String fragmentText) {
        if (fragmentText.contains("退货") || fragmentText.contains("退款") || fragmentText.contains("售后")) {
            return "AFTER_SALES";
        } else if (fragmentText.contains("偏好") || fragmentText.contains("习惯") || fragmentText.contains("倾向")) {
            return "PERSONAL";
        }
        return "COMMERCE";
    }
}