package com.crosstrade.core.memory;

import com.crosstrade.common.entity.MemoryFragment;
import com.crosstrade.common.repository.MemoryFragmentRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * L2长期记忆（Milvus + MySQL）
 * 采用Milvus + MySQL双存储架构：
 * - Milvus：存储压缩摘要的语义向量，支持跨会话语义检索
 * - MySQL：存储压缩摘要的文本内容，支持历史回溯
 * 降级策略：当Milvus不可用时，降级为仅使用MySQL按时间倒序查询
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class LongTermMemory {

    @Autowired
    private MilvusClientV2 milvusClient;

    @Autowired
    private MemoryFragmentRepository fragmentRepository;

    @Autowired
    private EmbeddingModel embeddingModel;

    private static final String COLLECTION_NAME = "memory_fragments";
    private static final Gson gson = new Gson();

    /**
     * 语义检索相关摘要
     *
     * @param userId 用户ID
     * @param query  查询文本
     * @param topK   返回数量
     * @return 相关摘要列表
     */
    public List<MemoryFragment> searchRelevantSummary(String userId, String query, int topK) {
        // 向量语义检索Milvus
        try {
            float[] queryVector = embeddingModel.embed(query);

            SearchReq searchReq = SearchReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .data(Collections.singletonList(new FloatVec(queryVector)))
                    .topK(topK)
                    .filter("user_id == \"" + userId + "\"")
                    .outputFields(Arrays.asList("user_id", "session_id", "category"))
                    .build();

            SearchResp searchResp = milvusClient.search(searchReq);
            List<SearchResp.SearchResult> results = searchResp.getSearchResults().get(0);

            if (!results.isEmpty()) {
                // 从MySQL获取完整文本
                List<MemoryFragment> fragments = new ArrayList<>();
                for (SearchResp.SearchResult result : results) {
                    Long fragmentId = (Long) result.getId();
                    fragmentRepository.findById(fragmentId).ifPresent(fragments::add);
                }
                if (!fragments.isEmpty()) {
                    return fragments;
                }
            }
        } catch (Exception e) {
            log.warn("Milvus检索失败，降级到MySQL: {}", e.getMessage());
        }

        // Milvus不可用时降级到MySQL
        return fragmentRepository.findByUserIdAndCreatedAtAfter(
                userId, LocalDateTime.now().minusDays(30)
        );
    }

    /**
     * 保存记忆摘要片段
     *
     * @param fragment 记忆摘要片段
     */
    public void saveFragment(MemoryFragment fragment) {
        // 向量写入Milvus
        try {
            float[] vector = embeddingModel.embed(fragment.getFragmentText());
            List<Float> vectorList = new ArrayList<>();
            for (float v : vector) {
                vectorList.add(v);
            }

            JsonObject data = new JsonObject();
            data.addProperty("user_id", fragment.getUserId());
            data.addProperty("session_id", fragment.getSessionId());
            data.add("fragment_vector", gson.toJsonTree(vectorList));
            data.addProperty("category", fragment.getCategory());

            InsertReq insertReq = InsertReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .data(Collections.singletonList(data))
                    .build();
            milvusClient.insert(insertReq);
        } catch (Exception e) {
            log.warn("Milvus写入失败，标记待补偿: {}", e.getMessage());
            fragment.setVectorFailed(true);
        }

        // 文本写入MySQL（保证最终一致性）
        fragmentRepository.save(fragment);
    }
}