package com.crosstrade.core.schedule;

import com.crosstrade.common.constants.RedisKeyConstants;
import com.crosstrade.common.entity.MemoryFragment;
import com.crosstrade.common.entity.UserWorkingMemory;
import com.crosstrade.common.repository.MemoryFragmentRepository;
import com.crosstrade.common.repository.UserWorkingMemoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户画像增量更新定时任务
 * 凌晨1点执行，仅对48小时内活跃用户执行画像增量更新
 * 不定期全量轮询，而是通过定时任务仅对活跃用户执行增量更新
 * 使用小模型修正画像，有变化则更新（乐观锁）
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class UserPortraitUpdateJob {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserWorkingMemoryRepository userWorkingMemoryRepository;

    @Autowired
    private MemoryFragmentRepository fragmentRepository;

    @Autowired
    @Qualifier("chatClient")
    private ChatClient chatClient;

    /**
     * 凌晨1点执行用户画像增量更新
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void updateActiveUserPortraits() {
        log.info("用户画像增量更新任务开始");

        // 从Redis获取48小时内活跃用户集合
        Set<String> activeUsers = redisTemplate.opsForSet().members(RedisKeyConstants.ACTIVE_USERS_KEY);
        if (activeUsers == null || activeUsers.isEmpty()) {
            log.info("无活跃用户，跳过画像更新");
            return;
        }

        log.info("活跃用户数: {}", activeUsers.size());

        // 逐用户增量更新
        int successCount = 0;
        int failCount = 0;
        for (String userId : activeUsers) {
            try {
                if (updateSingleUser(userId)) {
                    successCount++;
                }
            } catch (Exception e) {
                failCount++;
                log.error("用户画像更新失败: userId={}", userId, e);
            }
        }

        log.info("用户画像增量更新任务完成: total={}, success={}, fail={}",
                activeUsers.size(), successCount, failCount);
    }

    /**
     * 更新单个用户的画像
     *
     * @param userId 用户ID
     * @return true=有更新，false=无更新
     */
    private boolean updateSingleUser(String userId) {
        // 获取L2摘要记录（增量事实，最近2天）
        List<MemoryFragment> fragments = fragmentRepository.findByUserIdAndCreatedAtAfter(
                userId, LocalDateTime.now().minusDays(2)
        );

        if (fragments.isEmpty()) {
            return false; // 没有新事实，跳过
        }

        // 获取旧画像
        Optional<UserWorkingMemory> oldProfileOpt = userWorkingMemoryRepository.findById(userId);
        if (oldProfileOpt.isEmpty()) {
            return false;
        }

        UserWorkingMemory oldProfile = oldProfileOpt.get();

        // 小模型修正画像
        String fragmentTexts = fragments.stream()
                .map(MemoryFragment::getFragmentText)
                .collect(Collectors.joining("\n"));

        String mergePrompt = "基于新事实修正用户画像:\n旧画像: " + oldProfile.getBusinessHabits()
                + "\n新事实: " + fragmentTexts
                + "\n请输出更新后的用户画像总结（自然语言）：";

        String newHabits = chatClient.prompt(mergePrompt).call().content();

        // 有变化则更新（乐观锁）
        if (newHabits != null && !newHabits.trim().equals(oldProfile.getBusinessHabits())) {
            oldProfile.setBusinessHabits(newHabits.trim());
            userWorkingMemoryRepository.save(oldProfile);
            log.info("用户画像更新: userId={}", userId);
            return true;
        }

        return false;
    }
}