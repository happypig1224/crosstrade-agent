package com.crosstrade.common.constants;

/**
 * 业务常量类
 * 集中管理所有业务常量，修改时只需改一处。
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public final class BusinessConstants {

    private BusinessConstants() {
        // 禁止实例化
    }

    // ==================== 反向澄清 ====================

    /**
     * 反向澄清最大追问次数（防止死循环）
     */
    public static final int MAX_CLARIFY_ROUND = 3;

    // ==================== 上下文压缩 ====================

    /**
     * 上下文压缩触发轮数（超过此值触发压缩）
     */
    public static final int COMPRESS_THRESHOLD = 12;

    /**
     * 每次压缩的对话条数（截取窗口外4条对话）
     */
    public static final int COMPRESS_BATCH_SIZE = 4;

    // ==================== L1短期记忆 ====================

    /**
     * L1短期记忆保留的对话轮数
     */
    public static final int SHORT_MEMORY_ROUNDS = 8;

    /**
     * L1短期记忆滑动过期时间（小时）
     */
    public static final int SHORT_MEMORY_TTL_HOURS = 24;

    // ==================== L2长期记忆 ====================

    /**
     * L2摘要保留天数（超过此值归档）
     */
    public static final int FRAGMENT_RETAIN_DAYS = 180;

    // ==================== 出口审查 ====================

    /**
     * 出口审查Token块大小（每20个Token为一块进行缓冲校验）
     */
    public static final int OUTPUT_BUFFER_TOKEN_SIZE = 20;

    // ==================== 活跃用户 ====================

    /**
     * 活跃用户集合过期时间（小时）
     */
    public static final int ACTIVE_USERS_TTL_HOURS = 48;

    // ==================== 冷启动 ====================

    /**
     * 冷启动分布式锁等待时间（秒）
     */
    public static final int COLDSTART_LOCK_WAIT_SECONDS = 3;

    /**
     * 冷启动分布式锁自动释放时间（秒）
     */
    public static final int COLDSTART_LOCK_LEASE_SECONDS = 10;

    // ==================== RAG检索 ====================

    /**
     * RAG检索相似度阈值（低于此值过滤掉）
     */
    public static final double RAG_SIMILARITY_THRESHOLD = 0.7;

    /**
     * RAG检索TopK（返回最相关的5条结果）
     */
    public static final int RAG_TOP_K = 5;
}