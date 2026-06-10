package com.crosstrade.core.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品检索工具
 * Spring AI Function Calling注册方式，通过@Bean注册为FunctionCallback
 * 根据需求/场景/预算/起订量检索匹配产品
 * 实际项目中通过HTTP/RPC调用产品/ERP系统接口
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Configuration
public class ProductSearchTool {

    /**
     * 模拟产品数据库
     */
    private static final List<Map<String, Object>> MOCK_PRODUCT_DATABASE = List.of(
            Map.of("productId", "P001", "productName", "户外露营帐篷X1", "skuId", "TENT-X1",
                    "currentPrice", 89.99, "minPrice", 65.0, "category", "户外用品",
                    "scene", "露营 户外 旅行", "moq", 10),
            Map.of("productId", "P002", "productName", "便携式太阳能充电宝", "skuId", "SOLAR-CB",
                    "currentPrice", 29.99, "minPrice", 18.0, "category", "电子配件",
                    "scene", "旅行 户外 应急", "moq", 50),
            Map.of("productId", "P003", "productName", "跨境电商专用打包箱", "skuId", "BOX-L",
                    "currentPrice", 3.99, "minPrice", 2.5, "category", "包装耗材",
                    "scene", "物流 仓储 发货", "moq", 100),
            Map.of("productId", "P004", "productName", "LED应急灯", "skuId", "LED-EL",
                    "currentPrice", 15.99, "minPrice", 9.0, "category", "电子配件",
                    "scene", "应急 家用 户外", "moq", 30),
            Map.of("productId", "P005", "productName", "多功能工具钳", "skuId", "TOOL-MP",
                    "currentPrice", 24.99, "minPrice", 15.0, "category", "工具",
                    "scene", "维修 户外 露营", "moq", 20)
    );

    /**
     * 产品检索请求
     */
    public record ProductSearchRequest(
            String need,
            String scene,
            Double budget,
            Integer moq
    ) {}

    /**
     * 产品检索结果
     */
    public record ProductSearchResult(
            List<Map<String, Object>> products
    ) {}

    /**
     * 注册产品检索Function Calling
     */
    @Bean
    public FunctionToolCallback<ProductSearchRequest, ProductSearchResult> productSearchFunction() {
        return FunctionToolCallback.<ProductSearchRequest, ProductSearchResult>builder("productSearch", this::search)
                .description("根据需求、场景、预算、起订量检索匹配产品")
                .inputType(ProductSearchRequest.class)
                .build();
    }

    /**
     * 产品检索逻辑
     *
     * @param request 检索请求
     * @return 检索结果
     */
    public ProductSearchResult search(ProductSearchRequest request) {
        // 过滤匹配产品
        List<Map<String, Object>> products = MOCK_PRODUCT_DATABASE.stream()
                .filter(p -> matchNeed(p, request.need()))
                .filter(p -> matchScene(p, request.scene()))
                .filter(p -> matchBudget(p, request.budget()))
                .filter(p -> matchMoq(p, request.moq()))
                .collect(Collectors.toList());

        log.info("产品检索: need={}, scene={}, budget={}, moq={}, 结果数={}",
                request.need(), request.scene(), request.budget(), request.moq(), products.size());

        return new ProductSearchResult(products);
    }

    /**
     * 匹配需求关键词
     */
    private boolean matchNeed(Map<String, Object> product, String need) {
        if (need == null || need.isEmpty()) {
            return true;
        }
        String productName = (String) product.get("productName");
        return productName != null && productName.contains(need);
    }

    /**
     * 匹配使用场景
     */
    private boolean matchScene(Map<String, Object> product, String scene) {
        if (scene == null || scene.isEmpty()) {
            return true;
        }
        String productScene = (String) product.get("scene");
        if (productScene == null) {
            return false;
        }
        for (String keyword : scene.split("[\\s,，]+")) {
            if (productScene.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 匹配预算
     */
    private boolean matchBudget(Map<String, Object> product, Double budget) {
        if (budget == null) {
            return true;
        }
        Double price = (Double) product.get("currentPrice");
        return price != null && price <= budget;
    }

    /**
     * 匹配起订量
     */
    private boolean matchMoq(Map<String, Object> product, Integer moq) {
        if (moq == null) {
            return true;
        }
        Integer productMoq = (Integer) product.get("moq");
        return productMoq != null && moq >= productMoq;
    }
}