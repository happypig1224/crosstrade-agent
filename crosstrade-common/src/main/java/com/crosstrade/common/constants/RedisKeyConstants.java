package com.crosstrade.common.constants;

/**
 * Redis Key常量类
 * 统一管理所有Redis Key前缀，避免硬编码导致的拼写错误
 * Key模式中使用 %s 占位符，运行时通过 String.format() 替换为实际值
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {
        // 禁止实例化
    }

    // ==================== L1短期记忆 ====================

    /**
     * 对话消息（LIST结构，存储最近8轮对话）
     * %s = sessionId
     */
    public static final String MESSAGE_KEY = "session:messages:%s";

    /**
     * 任务状态（HASH结构，存储意图类型、追问轮数等）
     * %s = sessionId
     */
    public static final String STATE_KEY = "session:state:%s";

    /**
     * 待办事项（LIST结构，存储PENDING待办）
     * %s = sessionId
     */
    public static final String TODO_KEY = "session:todo:%s";

    /**
     * 对话轮数（STRING结构，计数器）
     * %s = sessionId
     */
    public static final String ROUND_KEY = "session:round:%s";

    // ==================== 活跃用户 ====================

    /**
     * 48小时活跃用户集合（SET结构，画像定时更新用）
     */
    public static final String ACTIVE_USERS_KEY = "active:users:48h";

    // ==================== 冷启动 ====================

    /**
     * 冷启动分布式锁（Redisson Lock）
     * %s = sessionId
     */
    public static final String COLDSTART_LOCK_KEY = "coldstart:lock:%s";

    /**
     * 构建完整的Redis Key
     *
     * @param pattern Key模式（含%s占位符）
     * @param args    替换参数
     * @return 完整的Redis Key
     */
    public static String buildKey(String pattern, Object... args) {
        return String.format(pattern, args);
    }
}