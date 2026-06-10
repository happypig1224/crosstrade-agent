package com.crosstrade.core.intent;

import com.crosstrade.common.dto.response.IntentResult;
import com.crosstrade.common.enums.IntentType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * L1规则引擎
 * 基于关键词的快速匹配，O(1)时间复杂度，不调用LLM。
 * 启动时从配置加载关键词映射到内存，运行时逐关键词contains匹配。
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Component
public class RuleEngine {

    /**
     * 关键词→意图映射表（LinkedHashMap保持插入顺序）
     */
    private final Map<String, IntentType> keywordMap = new LinkedHashMap<>();

    /**
     * 初始化关键词映射表
     */
    @PostConstruct
    public void initKeywordMap() {
        // 导购关键词
        keywordMap.put("推荐", IntentType.PRODUCT_GUID);
        keywordMap.put("适合", IntentType.PRODUCT_GUID);
        keywordMap.put("想要", IntentType.PRODUCT_GUID);
        keywordMap.put("需要", IntentType.PRODUCT_GUID);
        keywordMap.put("找", IntentType.PRODUCT_GUID);
        keywordMap.put("求购", IntentType.PRODUCT_GUID);

        // 咨询关键词
        keywordMap.put("优势", IntentType.PRODUCT_INFO);
        keywordMap.put("对比", IntentType.PRODUCT_INFO);
        keywordMap.put("退货", IntentType.PRODUCT_INFO);
        keywordMap.put("售后", IntentType.PRODUCT_INFO);
        keywordMap.put("定制", IntentType.PRODUCT_INFO);
        keywordMap.put("区别", IntentType.PRODUCT_INFO);

        // 报价关键词
        keywordMap.put("价格", IntentType.QUOTE_NEGOTIATE);
        keywordMap.put("报价", IntentType.QUOTE_NEGOTIATE);
        keywordMap.put("降价", IntentType.QUOTE_NEGOTIATE);
        keywordMap.put("折扣", IntentType.QUOTE_NEGOTIATE);
        keywordMap.put("便宜", IntentType.QUOTE_NEGOTIATE);
        keywordMap.put("砍价", IntentType.QUOTE_NEGOTIATE);
    }

    /**
     * 关键词匹配
     *
     * @param userMessage 用户消息
     * @return 匹配结果，L1未命中时返回null
     */
    public IntentResult match(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            return null;
        }
        for (String keyword : keywordMap.keySet()) {
            if (userMessage.contains(keyword)) {
                return new IntentResult(keywordMap.get(keyword), 1.0, "L1_RULE");
            }
        }
        return null; // L1未命中，降级到L2
    }
}