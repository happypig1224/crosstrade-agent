package com.crosstrade.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 上下文压缩MQ消息体
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompressMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;

    private String userId;

    private List<MessageItem> messages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String role;
        private String content;
        private long timestamp;
    }
}