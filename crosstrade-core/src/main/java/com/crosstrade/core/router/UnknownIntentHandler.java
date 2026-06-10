package com.crosstrade.core.router;

import com.crosstrade.common.entity.UserWorkingMemory;
import com.crosstrade.common.enums.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 未知意图处理器
 * 组合状态引导，结合上下文和用户画像生成个性化引导。
 * 优先级：上下文引导 > 画像引导 > 通用引导
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class UnknownIntentHandler implements IntentHandler {

    @Override
    public IntentType supportIntent() {
        return IntentType.UNKNOWN;
    }

    @Override
    public Flux<String> handle(ChatContext context) {
        // 获取会话上下文(最近讨论话题)
        String recentTopic = context.getRecentTopic();

        // 获取用户画像(采购偏好)
        UserWorkingMemory userProfile = context.getUserProfile();

        // 生成组合状态引导回复
        String reply;
        if (recentTopic != null) {
            // 基于上下文引导
            reply = "注意到您之前在讨论" + recentTopic + "，是否需要查看相关报价或产品详情？";
        } else if (userProfile != null && userProfile.getBusinessHabits() != null
                && !userProfile.getBusinessHabits().isEmpty()) {
            // 基于画像引导
            reply = "根据您的采购偏好，是否需要推荐相关类产品？我可以帮您找到最合适的商品。";
        } else {
            // 通用引导
            reply = "您好！我可以帮您推荐产品、解答产品问题或提供报价，请问您需要哪方面的帮助？";
        }

        return Flux.just(reply);
    }
}