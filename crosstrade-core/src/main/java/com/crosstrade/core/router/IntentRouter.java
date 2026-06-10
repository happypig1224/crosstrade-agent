package com.crosstrade.core.router;

import com.crosstrade.common.dto.response.IntentResult;
import com.crosstrade.common.enums.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 意图路由器
 * 策略模式，根据IntentType分发到对应Handler，避免if-else膨胀
 * Spring自动注入所有IntentHandler实现，按IntentType分组
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class IntentRouter {

    /**
     * 意图类型--->处理器映射
     */
    private Map<IntentType, IntentHandler> handlerMap;

    /**
     * Spring自动注入所有IntentHandler实现，按IntentType分组
     */
    @Autowired
    public void setHandlers(List<IntentHandler> handlers) {
        handlerMap = new HashMap<>();
        for (IntentHandler handler : handlers) {
            handlerMap.put(handler.supportIntent(), handler);
            log.info("注册意图处理器: {} -> {}", handler.supportIntent(), handler.getClass().getSimpleName());
        }
    }

    /**
     * 根据意图分发到对应Handler
     *
     * @param intentResult 意图识别结果
     * @param context      聊天上下文
     * @return 流式响应
     */
    public Flux<String> route(IntentResult intentResult, ChatContext context) {
        IntentHandler handler = handlerMap.get(intentResult.intentType());
        if (handler == null) {
            // 没有对应的Handler，走unknown
            log.warn("未找到意图处理器: type={}, 降级为UNKNOWN", intentResult.intentType());
            handler = handlerMap.get(IntentType.UNKNOWN);
        }
        if (handler == null) {
            return Flux.just("抱歉，系统暂时无法处理您的请求，请稍后再试。");
        }
        return handler.handle(context);
    }
}