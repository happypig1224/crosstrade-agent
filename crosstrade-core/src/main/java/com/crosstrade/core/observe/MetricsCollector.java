package com.crosstrade.core.observe;

import com.crosstrade.common.enums.IntentType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Micrometer指标采集器
 * 负责采集系统运行指标，帮助运维人员监控系统运行状态。
 * 指标包括：意图识别耗时/分布、安全审查拦截、Token消耗、
 * 首Token延迟、工具调用频次、MQ发送失败等。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class MetricsCollector {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 记录意图识别耗时
     *
     * @param source 识别来源（L1/L2/L2_TIMEOUT）
     * @param type   意图类型
     */
    public void recordIntentIdentify(String source, IntentType type) {
        Timer.builder("crosstrade.intent.identify")
                .tag("source", source)
                .tag("type", type.getValue())
                .register(meterRegistry)
                .record(1, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录意图分布
     *
     * @param type 意图类型
     */
    public void recordIntentDistribution(IntentType type) {
        Counter.builder("crosstrade.intent.distribution")
                .tag("type", type.getValue())
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录安全审查拦截
     *
     * @param direction 方向（inbound/outbound）
     * @param reason    拦截原因
     */
    public void recordGuardrailBlock(String direction, String reason) {
        Counter.builder("crosstrade.guardrail.block")
                .tag("direction", direction)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录Token消耗
     *
     * @param model  模型名称
     * @param tokens Token数量
     */
    public void recordTokenConsume(String model, int tokens) {
        Counter.builder("crosstrade.chat.token.consume")
                .tag("model", model)
                .register(meterRegistry)
                .increment(tokens);
    }

    /**
     * 记录首Token延迟
     *
     * @param model      模型名称
     * @param durationMs 延迟毫秒数
     */
    public void recordFirstTokenLatency(String model, long durationMs) {
        Timer.builder("crosstrade.chat.first.token.latency")
                .tag("model", model)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录完整响应延迟
     *
     * @param model      模型名称
     * @param durationMs 延迟毫秒数
     */
    public void recordResponseLatency(String model, long durationMs) {
        Timer.builder("crosstrade.chat.response.latency")
                .tag("model", model)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录工具调用频次
     *
     * @param toolName 工具名称
     */
    public void recordToolCall(String toolName) {
        Counter.builder("crosstrade.tool.call")
                .tag("toolName", toolName)
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录上下文压缩耗时
     *
     * @param durationMs 延迟毫秒数
     */
    public void recordMemoryCompress(long durationMs) {
        Timer.builder("crosstrade.memory.compress")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录冷启动恢复耗时
     *
     * @param durationMs 延迟毫秒数
     */
    public void recordMemoryColdStart(long durationMs) {
        Timer.builder("crosstrade.memory.coldstart")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录MQ发送失败
     *
     * @param topic Topic名称
     */
    public void recordMqProduceFail(String topic) {
        Counter.builder("crosstrade.mq.produce.fail")
                .tag("topic", topic)
                .register(meterRegistry)
                .increment();
    }
}