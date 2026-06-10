package com.crosstrade.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 对话持久化MQ消息体
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersistMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;

    private String userId;

    private String userMessage;

    private String aiMessage;

    private int tokens;
}