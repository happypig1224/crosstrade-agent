package com.crosstrade.common.repository;

import com.crosstrade.common.entity.MemoryFragmentArchive;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * 记忆摘要归档Repository
 * 基于Spring Data JDBC，提供对agent_memory_fragments_archive表的CRUD操作。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface MemoryFragmentArchiveRepository extends CrudRepository<MemoryFragmentArchive, Long> {

    /**
     * 按用户查询归档记录
     *
     * @param userId 用户ID
     * @return 该用户的归档记录列表
     */
    List<MemoryFragmentArchive> findByUserId(String userId);
}