package com.crosstrade.common.repository;

import com.crosstrade.common.entity.MemoryFragment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 记忆摘要Repository
 * 基于Spring Data JDBC，提供对agent_memory_fragments表的CRUD操作。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface MemoryFragmentRepository extends CrudRepository<MemoryFragment, Long> {

    /**
     * 查询用户最近N天的摘要（画像增量更新用）
     *
     * @param userId 用户ID
     * @param since  起始时间
     * @return 该用户的增量摘要列表
     */
    List<MemoryFragment> findByUserIdAndCreatedAtAfter(String userId, LocalDateTime since);

    /**
     * 查询超过指定时间的摘要（归档用）
     *
     * @param before 截止时间
     * @return 超过保留期的摘要列表
     */
    List<MemoryFragment> findByCreatedAtBefore(LocalDateTime before);

    /**
     * 根据用户ID查询所有摘要（按时间倒序）
     *
     * @param userId 用户ID
     * @return 该用户的所有摘要列表
     */
    List<MemoryFragment> findByUserIdOrderByCreatedAtDesc(String userId);
}