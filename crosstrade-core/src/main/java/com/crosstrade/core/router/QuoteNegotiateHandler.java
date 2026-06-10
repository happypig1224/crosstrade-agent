package com.crosstrade.core.router;

import com.crosstrade.common.dto.response.QuoteResult;
import com.crosstrade.common.enums.IntentType;
import com.crosstrade.core.tool.DiscountMatrix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 报价谈判处理器
 * 防薅羊毛核心：Agent仅提取参数，折扣矩阵Java业务代码控制价格决策
 * Agent提取结构化参数（期望价格、采购量），调用折扣矩阵计算，然后翻译为销售话术
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class QuoteNegotiateHandler implements IntentHandler {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private DiscountMatrix discountMatrix;

    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+\\.?\\d*)\\s*(美元|美金|刀|\\$|USD|元|块)");
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("(\\d+)\\s*(件|个|套|箱|pcs|pieces)");
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("(P00\\d)");

    @Override
    public IntentType supportIntent() {
        return IntentType.QUOTE_NEGOTIATE;
    }

    @Override
    public Flux<String> handle(ChatContext context) {
        String userMessage = context.getUserMessage();

        // Agent提取结构化参数
        Double expectedPrice = extractPrice(userMessage);
        Integer quantity = extractQuantity(userMessage);
        String productId = extractProductId(context);

        // 参数提取失败时主动追问
        if (expectedPrice == null || quantity == null) {
            return Flux.just("请问您的目标价格和采购数量是多少？这样我可以为您计算最优报价。");
        }
        if (productId == null) {
            return Flux.just("请问您想咨询哪个产品的报价？请提供产品编号（如P001）。");
        }

        // 调用折扣矩阵Java业务代码计算报价（防薅羊毛核心）
        QuoteResult quoteResult = discountMatrix.calculate(
                productId,
                BigDecimal.valueOf(expectedPrice),
                quantity
        );
        log.info("折扣矩阵计算: productId={}, expectedPrice={}, quantity={}, accepted={}, finalPrice={}, minPrice={}",
                productId, expectedPrice, quantity, quoteResult.accepted(),
                quoteResult.finalPrice(), quoteResult.minPrice());

        // 将业务结果翻译为销售话术
        String quotePrompt = buildQuotePrompt(context, productId, expectedPrice, quantity, quoteResult);
        return chatClient.prompt(quotePrompt).stream().content();
    }

    /**
     * 提取产品ID（优先从上下文获取，其次从消息提取）
     */
    private String extractProductId(ChatContext context) {
        if (context.getProductId() != null) {
            return context.getProductId();
        }
        Matcher matcher = PRODUCT_ID_PATTERN.matcher(context.getUserMessage());
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * 从用户消息中提取价格
     */
    private Double extractPrice(String message) {
        Matcher matcher = PRICE_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从用户消息中提取数量
     */
    private Integer extractQuantity(String message) {
        Matcher matcher = QUANTITY_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 构建报价Prompt（含折扣矩阵计算结果）
     */
    private String buildQuotePrompt(ChatContext context, String productId,
                                    Double expectedPrice, Integer quantity,
                                    QuoteResult quoteResult) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为跨境电商报价谈判助手，根据折扣矩阵的计算结果，以专业销售话术回复用户。\n\n");

        prompt.append("【用户消息】").append(context.getUserMessage()).append("\n");
        prompt.append("【产品编号】").append(productId).append("\n");
        prompt.append("【用户期望价格】").append(expectedPrice).append(" USD\n");
        prompt.append("【采购数量】").append(quantity).append(" 件\n");

        if (quoteResult.accepted()) {
            prompt.append("【折扣矩阵结果】报价已通过！最终价格：").append(quoteResult.finalPrice()).append(" USD\n");
            prompt.append("请以积极、专业的语气确认报价，强调性价比和批量优惠。\n");
        } else {
            prompt.append("【折扣矩阵结果】报价未通过。最低可接受价格：").append(quoteResult.minPrice()).append(" USD\n");
            prompt.append("请以委婉但坚定的语气说明无法接受该价格，并给出反报价和理由（如批量折扣、成本限制等），引导用户接受最低可接受价格。\n");
        }

        if (context.getRelevantSummaries() != null && !context.getRelevantSummaries().isEmpty()) {
            prompt.append("【历史谈判信息】").append(String.join("；", context.getRelevantSummaries())).append("\n");
        }

        prompt.append("\n注意：最终价格由系统计算，不可低于最低可接受价格，这是硬性限制。");
        return prompt.toString();
    }
}
