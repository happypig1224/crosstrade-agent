package com.crosstrade.common.repository;

import com.crosstrade.common.entity.UserWorkingMemory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户画像Repository
 * 基于Spring Data JDBC，提供对user_working_memory表的CRUD操作。
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public interface UserWorkingMemoryRepository extends CrudRepository<UserWorkingMemory, String> {

}