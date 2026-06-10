create database if not exists `crosstrade_agent`;
use `crosstrade_agent`;

-- 1. 全量历史对话表
CREATE TABLE `agent_chat_history`
(
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '自增主键或雪花ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话唯一标识',
    `user_id`    VARCHAR(64) NOT NULL COMMENT '用户唯一标识',
    `role`       VARCHAR(20) NOT NULL COMMENT '角色: user(买家) / assistant(Agent) / system(系统提示词) / tool(工具执行结果)',
    `content`    TEXT        NULL COMMENT '聊天原始文本（如果是tool角色则存入参或出参JSON）',
    `tool_calls` JSON        NULL COMMENT '记录当前轮次大模型决定调用哪些工具的原始结构化JSON',
    `raw_tokens` INT         NOT NULL DEFAULT 0 COMMENT '当前单条消息消耗的Token数，用于成本审计与财务统计',
    `created_at` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息产生绝对时间',
    PRIMARY KEY (`id`),
    INDEX `idx_session_time` (`session_id`, `created_at` DESC),
    INDEX `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='全量历史对话表';

-- 2. L2摘要压缩表（保留180天）
CREATE TABLE `agent_memory_fragments`
(
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `user_id`       VARCHAR(64)   NOT NULL COMMENT '用户唯一标识',
    `session_id`    VARCHAR(64)   NOT NULL COMMENT '产生该记忆的源会话ID',
    `fragment_text` VARCHAR(1024) NOT NULL COMMENT '脱水后的高密度事实文本，例如：[2026-06-09]用户表达了100件Tent-X1的采购意向，对价格敏感',
    `category`      VARCHAR(32)   NOT NULL DEFAULT 'COMMERCE' COMMENT '记忆领域分类: COMMERCE(商务) / AFTER_SALES(售后) / PERSONAL(个人习惯)',
    `created_at`    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '片段生成时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_time` (`user_id`, `created_at`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='L2-摘要压缩表(保留180天，过期滚动归档)';

-- 3. L3用户画像表（一户一行）
CREATE TABLE `user_working_memory`
(
    `user_id`         VARCHAR(64) NOT NULL COMMENT '用户唯一标识',
    `summary_tags`    JSON        NOT NULL COMMENT '强结构化标签矩阵，例：{"region": "US", "price_sensitivity": "HIGH"}',
    `business_habits` TEXT        NOT NULL COMMENT '大模型动态合并的自然语言习惯总结，描绘客户性格、谈判倾向、起订量癖好',
    `tool_preference` JSON        NOT NULL COMMENT '工具调用计数器，用于辅助Agent判断用户的行为偏好',
    `updated_at`      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '离线反思任务最终更新时间',
    PRIMARY KEY (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='L3-用户画像表(一户一行，主键查)';

-- 4. 待办事项状态表
CREATE TABLE `agent_session_todo`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `session_id`   VARCHAR(64)  NOT NULL COMMENT '会话唯一标识',
    `todo_content` VARCHAR(255) NOT NULL COMMENT '具体待办事项，例：等待客户确认起订量 / 待计算退款运费',
    `status`       VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING(待办) / COMPLETED(已办) / CANCELED(取消)',
    `created_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_session_status` (`session_id`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='待办事项状态表(缓存冷启动恢复)';

-- 5. L2摘要压缩归档表
CREATE TABLE `agent_memory_fragments_archive`
(
    `id`            BIGINT        NOT NULL COMMENT '保留原热表ID',
    `user_id`       VARCHAR(64)   NOT NULL,
    `session_id`    VARCHAR(64)   NOT NULL,
    `fragment_text` VARCHAR(1024) NOT NULL,
    `category`      VARCHAR(32)   NOT NULL,
    `created_at`    TIMESTAMP     NOT NULL,
    `archived_at`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档迁移时间',
    PRIMARY KEY (`id`),
    INDEX `idx_archive_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='L2-摘要压缩归档表(数据分流防膨胀)';