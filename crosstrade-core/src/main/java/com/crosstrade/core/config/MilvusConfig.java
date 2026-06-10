package com.crosstrade.core.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Milvus向量数据库配置
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Configuration
public class MilvusConfig {

    @Value("${milvus.host}")
    private String host;

    @Value("${milvus.port}")
    private int port;

    @Value("${milvus.token:}")
    private String token;

    /**
     * Milvus客户端Bean
     */
    @Bean
    public MilvusClientV2 milvusClient() {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri("http://" + host + ":" + port)
                .token(token.isEmpty() ? null : token)
                .build();
        return new MilvusClientV2(connectConfig);
    }

    /**
     * 应用启动时自动创建Collection和索引
     */
    @Bean
    public MilvusInitializer milvusInitializer(MilvusClientV2 milvusClient) {
        return new MilvusInitializer(milvusClient);
    }

    /**
     * Milvus初始化器
     */
    public static class MilvusInitializer {

        private static final String COLLECTION_NAME = "memory_fragments";
        private static final int VECTOR_DIMENSION = 1536;

        private final MilvusClientV2 milvusClient;

        public MilvusInitializer(MilvusClientV2 milvusClient) {
            this.milvusClient = milvusClient;
            initCollection();
        }

        /**
         * 初始化Collection
         */
        private void initCollection() {
            try {
                // Step 1: 检查Collection是否已存在
                HasCollectionReq hasCollectionReq = HasCollectionReq.builder()
                        .collectionName(COLLECTION_NAME)
                        .build();
                boolean hasCollection = milvusClient.hasCollection(hasCollectionReq);

                if (hasCollection) {
                    log.info("Milvus Collection [{}] 已存在，跳过创建", COLLECTION_NAME);
                    return;
                }

                // Step 2: 定义Schema
                CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder()
                        .build();
                schema.addField(AddFieldReq.builder()
                        .fieldName("id")
                        .dataType(DataType.Int64)
                        .isPrimaryKey(true)
                        .autoID(true)
                        .build());
                schema.addField(AddFieldReq.builder()
                        .fieldName("user_id")
                        .dataType(DataType.VarChar)
                        .maxLength(64)
                        .build());
                schema.addField(AddFieldReq.builder()
                        .fieldName("session_id")
                        .dataType(DataType.VarChar)
                        .maxLength(64)
                        .build());
                schema.addField(AddFieldReq.builder()
                        .fieldName("fragment_vector")
                        .dataType(DataType.FloatVector)
                        .dimension(VECTOR_DIMENSION)
                        .build());
                schema.addField(AddFieldReq.builder()
                        .fieldName("category")
                        .dataType(DataType.VarChar)
                        .maxLength(32)
                        .build());

                // Step 3: 创建索引
                List<IndexParam> indexParams = new ArrayList<>();
                indexParams.add(IndexParam.builder()
                        .fieldName("fragment_vector")
                        .indexType(IndexParam.IndexType.IVF_FLAT) // 倒排文件索引，适合中等规模数据集
                        .metricType(IndexParam.MetricType.COSINE) // 使用余弦相似度计算
                        .extraParams(new HashMap<String, Object>() {{
                            put("nlist", 1024); // 向量化空间划分为1024个单元
                        }})
                        .build());

                // Step 4: 创建Collection
                CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                        .collectionName(COLLECTION_NAME)
                        .collectionSchema(schema)
                        .indexParams(indexParams)
                        .build();
                milvusClient.createCollection(createCollectionReq);

                // Step 5: 加载Collection到内存
                LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                        .collectionName(COLLECTION_NAME)
                        .build();
                milvusClient.loadCollection(loadCollectionReq);

                log.info("Milvus Collection [{}] 创建并加载成功", COLLECTION_NAME);
            } catch (Exception e) {
                log.warn("Milvus Collection初始化失败，系统将以降级模式运行: {}", e.getMessage());
            }
        }
    }
}