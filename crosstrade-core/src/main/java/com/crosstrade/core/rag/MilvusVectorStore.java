package com.crosstrade.core.rag;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Milvus向量存储适配
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class MilvusVectorStore {

    @Autowired
    private MilvusClientV2 milvusClient;

    @Autowired
    private EmbeddingModel embeddingModel;

    private static final String KNOWLEDGE_COLLECTION = "knowledge_base";

    /**
     * 语义检索
     *
     * @param query 查询文本
     * @param topK  返回数量
     * @return 知识检索结果列表
     */
    public List<RagRetriever.KnowledgeResult> search(String query, int topK) {
        try {
            // 生成查询向量
            float[] queryVector = embeddingModel.embed(query);

            // Milvus语义检索
            SearchReq searchReq = SearchReq.builder()
                    .collectionName(KNOWLEDGE_COLLECTION)
                    .data(Collections.singletonList(new FloatVec(queryVector)))
                    .topK(topK)
                    .outputFields(Arrays.asList("content", "source"))
                    .build();

            SearchResp searchResp = milvusClient.search(searchReq);
            List<SearchResp.SearchResult> results = searchResp.getSearchResults().get(0);

            // 转换为KnowledgeResult
            List<RagRetriever.KnowledgeResult> knowledgeResults = new ArrayList<>();
            for (SearchResp.SearchResult result : results) {
                RagRetriever.KnowledgeResult kr = new RagRetriever.KnowledgeResult();
                Map<String, Object> entity = result.getEntity();
                kr.setContent(entity.get("content") != null ? entity.get("content").toString() : "");
                kr.setSimilarity(result.getScore());
                kr.setSource(entity.get("source") != null ? entity.get("source").toString() : "knowledge_base");
                knowledgeResults.add(kr);
            }
            return knowledgeResults;

        } catch (Exception e) {
            log.warn("Milvus语义检索失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}