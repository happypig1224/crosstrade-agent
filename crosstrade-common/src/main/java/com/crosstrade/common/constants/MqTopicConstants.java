package com.crosstrade.common.constants;

/**
 * 消息队列常量类
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public class MqTopicConstants {
    // 上下文压缩Topic（会话超过12轮，截取窗口对话异步提取压缩）
    public static final String CONTEXT_COMPRESS_TOPIC = "crosstrade-compress-topic";
    public static final String CONTEXT_COMPRESS_TAG = "CONTEXT_COMPRESS";

    // 消息持久化Topic（流式输出完成，将对话记录持久化到数据库）
    public static final String MESSAGE_PERSIST_TOPIC = "crosstrade-persist-topic";
    public static final String MESSAGE_PERSIST_TAG = "MESSAGE_PERSIST";
}
