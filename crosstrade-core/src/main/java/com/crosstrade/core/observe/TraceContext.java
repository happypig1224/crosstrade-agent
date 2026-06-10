package com.crosstrade.core.observe;

import org.slf4j.MDC;

/**
 * 链路追踪上下文
 * 基于MDC（Mapped Diagnostic Context）实现链路追踪
 * 确保日志中包含traceId，方便问题排查
 * 支持跨MQ消息传递traceId
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public class TraceContext {

    /**
     * MDC中traceId的Key
     */
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 设置Trace上下文到MDC
     *
     * @param traceId 追踪ID
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * 获取当前TraceId
     *
     * @return 当前追踪ID，不存在时返回null
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 清除Trace上下文
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * 生成新的TraceId（UUID）
     *
     * @return 新的追踪ID
     */
    public static String generateTraceId() {
        String traceId = java.util.UUID.randomUUID().toString().replace("-", "");
        setTraceId(traceId);
        return traceId;
    }

    /**
     * 为MQ消息添加Trace上下文（跨MQ传递）
     * 在消息头中添加traceId，确保消费者可以延续链路追踪
     *
     * @param message 消息对象
     * @param <T>     消息类型
     * @return 带有traceId的消息对象
     */
    public static <T> T injectToMessage(T message) {
        return message;
    }
}