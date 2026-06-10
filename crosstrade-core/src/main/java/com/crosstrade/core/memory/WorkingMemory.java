package com.crosstrade.core.memory;

import com.crosstrade.common.entity.UserWorkingMemory;
import com.crosstrade.common.repository.UserWorkingMemoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * L3工作记忆（MySQL用户画像）
 * <p>
 * 使用MySQL存储用户画像，每个用户一条记录（一户一行），包含：
 * - summaryTags：结构化标签矩阵（地区、价格敏感度等）
 * - businessHabits：自然语言习惯总结（性格、谈判倾向等）
 * - toolPreference：工具调用计数器（各工具的调用频次）
 * <p>
 * 增量更新：不定期全量轮询，而是通过定时任务仅对48小时内活跃用户执行增量更新。
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class WorkingMemory {

    @Autowired
    private UserWorkingMemoryRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取用户画像
     *
     * @param userId 用户ID
     * @return 用户画像，不存在时返回null
     */
    public UserWorkingMemory getUserProfile(String userId) {
        Optional<UserWorkingMemory> optional = repository.findById(userId);
        return optional.orElse(null);
    }

    /**
     * 获取用户地区
     *
     * @param userId 用户ID
     * @return 用户地区，默认"US"
     */
    public String getUserRegion(String userId) {
        UserWorkingMemory profile = getUserProfile(userId);
        if (profile != null && profile.getSummaryTags() != null) {
            try {
                JsonNode tagsNode = objectMapper.readTree(profile.getSummaryTags());
                JsonNode regionNode = tagsNode.get("region");
                if (regionNode != null && !regionNode.isNull()) {
                    return regionNode.asText();
                }
            } catch (JsonProcessingException e) {
                log.error("解析用户画像标签失败: userId={}", userId, e);
            }
        }
        return "US"; // 默认美国
    }

    /**
     * 增量更新工具调用计数器
     *
     * @param userId   用户ID
     * @param toolName 工具名称
     */
    public void updateToolPreference(String userId, String toolName) {
        // 读取当前画像
        Optional<UserWorkingMemory> optional = repository.findById(userId);
        if (optional.isEmpty()) {
            return;
        }

        UserWorkingMemory profile = optional.get();

        try {
            // 增量更新工具调用计数器
            ObjectNode toolPref = (ObjectNode) objectMapper.readTree(profile.getToolPreference());
            int currentCount = 0;
            JsonNode countNode = toolPref.get(toolName);
            if (countNode != null && !countNode.isNull()) {
                currentCount = countNode.asInt();
            }
            toolPref.put(toolName, currentCount + 1);
            profile.setToolPreference(objectMapper.writeValueAsString(toolPref));

            // 乐观锁更新
            repository.save(profile);
        } catch (JsonProcessingException e) {
            log.error("更新工具调用计数器失败: userId={}, toolName={}", userId, toolName, e);
        }
    }

    /**
     * 创建或更新用户画像
     *
     * @param profile 用户画像
     */
    public void saveProfile(UserWorkingMemory profile) {
        repository.save(profile);
    }

    /**
     * 检查用户是否有偏好信息
     *
     * @param userId 用户ID
     * @return true=有偏好信息
     */
    public boolean hasPreferences(String userId) {
        UserWorkingMemory profile = getUserProfile(userId);
        return profile != null
                && profile.getBusinessHabits() != null
                && !profile.getBusinessHabits().isEmpty();
    }
}