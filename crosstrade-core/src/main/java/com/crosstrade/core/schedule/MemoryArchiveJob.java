package com.crosstrade.core.schedule;

import com.crosstrade.common.constants.BusinessConstants;
import com.crosstrade.common.entity.MemoryFragment;
import com.crosstrade.common.entity.MemoryFragmentArchive;
import com.crosstrade.common.repository.MemoryFragmentArchiveRepository;
import com.crosstrade.common.repository.MemoryFragmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 记忆摘要180天归档定时任务
 * 凌晨2点执行，将超过180天的摘要记录从热表迁移到归档表，
 * 防止热表膨胀影响查询性能。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class MemoryArchiveJob {

    @Autowired
    private MemoryFragmentRepository fragmentRepository;

    @Autowired
    private MemoryFragmentArchiveRepository archiveRepository;

    /**
     * 凌晨2点执行记忆摘要归档
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void archiveOldFragments() {
        log.info("记忆摘要归档任务开始");

        // 查询超过180天的摘要记录
        LocalDateTime threshold = LocalDateTime.now().minusDays(BusinessConstants.FRAGMENT_RETAIN_DAYS);
        List<MemoryFragment> oldFragments = fragmentRepository.findByCreatedAtBefore(threshold);

        if (oldFragments.isEmpty()) {
            log.info("无过期摘要，跳过归档");
            return;
        }

        log.info("待归档摘要数: {}", oldFragments.size());

        // 迁移到归档表
        int archiveCount = 0;
        for (MemoryFragment fragment : oldFragments) {
            try {
                MemoryFragmentArchive archive = new MemoryFragmentArchive();
                archive.setId(fragment.getId());
                archive.setUserId(fragment.getUserId());
                archive.setSessionId(fragment.getSessionId());
                archive.setFragmentText(fragment.getFragmentText());
                archive.setCategory(fragment.getCategory());
                archive.setCreatedAt(fragment.getCreatedAt());
                archive.setArchivedAt(LocalDateTime.now());

                archiveRepository.save(archive);
                archiveCount++;
            } catch (Exception e) {
                log.error("归档单条摘要失败: fragmentId={}", fragment.getId(), e);
            }
        }

        // 删除热表记录
        if (archiveCount > 0) {
            fragmentRepository.deleteAll(oldFragments);
        }

        log.info("记忆摘要归档任务完成: total={}, archived={}", oldFragments.size(), archiveCount);
    }
}