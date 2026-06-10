package com.crosstrade.core.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 知识增强器
 * 提供区域化售后政策缓存，启动时从MySQL加载到内存
 * 不同国家/地区的退换货规则和流程不同，需要根据用户地区匹配对应政策
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class KnowledgeAugmentor {

    /**
     * 区域化售后政策缓存
     * Key: region, Value: 售后政策文本
     */
    private final Map<String, String> regionalPolicyCache = new HashMap<>();

    /**
     * 初始化售后政策缓存
     * 实际项目中从MySQL加载所有售后政策到内存缓存
     */
    public KnowledgeAugmentor() {
        // 默认售后政策
        regionalPolicyCache.put("US", "美国地区售后政策：收货后30天内可申请退换货，运费由买方承担。退款将在收到退货后5个工作日内处理。");
        regionalPolicyCache.put("EU", "欧盟地区售后政策：根据欧盟消费者保护法，收货后14天内可无理由退换货，运费由卖方承担。退款将在收到退货后10个工作日内处理。");
        regionalPolicyCache.put("UK", "英国地区售后政策：收货后30天内可申请退换货，运费由买方承担。退款将在收到退货后7个工作日内处理。");
        regionalPolicyCache.put("default", "通用售后政策：收货后15天内可申请退换货，具体运费承担方视退换货原因而定。退款将在收到退货后7个工作日内处理。");

        log.info("售后政策缓存初始化完成: regions={}", regionalPolicyCache.keySet());
    }

    /**
     * 获取区域化售后政策
     *
     * @param region 用户地区
     * @return 售后政策文本，未找到时返回默认政策
     */
    public String getRegionalPolicy(String region) {
        if (region == null || region.isEmpty()) {
            return regionalPolicyCache.get("default");
        }
        return regionalPolicyCache.getOrDefault(region, regionalPolicyCache.get("default"));
    }
}