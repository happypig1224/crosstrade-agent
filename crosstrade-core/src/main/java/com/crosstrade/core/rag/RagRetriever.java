package com.crosstrade.core.rag;

import com.crosstrade.common.constants.BusinessConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG检索入口
 * RAG（Retrieval-Augmented Generation）检索模块负责从知识库中检索相关信息，
 * 增强LLM的回复质量。支持：
 * - 知识库语义检索
 * - 产品销售话术检索
 * - 区域化售后政策匹配
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class RagRetriever {

    @Autowired
    private MilvusVectorStore milvusVectorStore;

    @Autowired
    private KnowledgeAugmentor knowledgeAugmentor;

    /**
     * 检索知识库
     *
     * @param query 查询文本
     * @param topK  返回数量
     * @return 高相关度知识结果列表
     */
    public List<KnowledgeResult> retrieve(String query, int topK) {
        // 向量语义检索
        List<KnowledgeResult> results = milvusVectorStore.search(query, topK);

        // 过滤低相关度结果
        return results.stream()
                .filter(r -> r.getSimilarity() >= BusinessConstants.RAG_SIMILARITY_THRESHOLD)
                .collect(Collectors.toList());
    }

    /**
     * 检索产品销售话术
     *
     * @param productKeywords 产品关键词（产品名称+SKU）
     * @return 销售话术列表
     */
    public List<KnowledgeResult> retrieveSalesScripts(String productKeywords) {
        return retrieve(productKeywords, BusinessConstants.RAG_TOP_K);
    }

    /**
     * 检索区域化售后政策
     *
     * @param region 用户地区
     * @return 售后政策文本
     */
    public String retrieveAfterSalesPolicy(String region) {
        return knowledgeAugmentor.getRegionalPolicy(region);
    }

    /**
     * 知识检索结果
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KnowledgeResult {
        /**
         * 知识内容
         */
        private String content;

        /**
         * 相似度分数
         */
        private double similarity;

        /**
         * 知识来源
         */
        private String source;
    }
}