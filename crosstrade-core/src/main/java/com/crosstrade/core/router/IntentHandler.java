package com.crosstrade.core.router;

import com.crosstrade.common.enums.IntentType;
import reactor.core.publisher.Flux;

/**
 * 意图处理器接口
 * 策略模式的核心接口，所有意图处理器均实现此接口。
 * 新增意图类型时只需新增Handler实现，不修改路由器代码。
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface IntentHandler {

    /**
     * 处理用户请求，返回流式响应
     *
     * @param context 聊天上下文
     * @return 流式响应文本
     */
    Flux<String> handle(ChatContext context);

    /**
     * 该Handler支持的意图类型
     *
     * @return 意图类型
     */
    IntentType supportIntent();
}