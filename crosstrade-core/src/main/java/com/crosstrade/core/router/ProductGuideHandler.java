package com.crosstrade.core.router;

import com.crosstrade.common.constants.BusinessConstants;
import com.crosstrade.common.enums.IntentType;
import com.crosstrade.core.rag.RagRetriever;
import com.crosstrade.core.tool.ProductSearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品导购处理器
 * 反向澄清循环 + Function Calling产品检索 + RAG话术增强
 * 当用户需求参数不足时，最多追问3次，超限后降级为宽泛推荐
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class ProductGuideHandler implements IntentHandler {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ProductSearchTool productSearchTool;

    @Autowired
    private RagRetriever ragRetriever;

    @Override
    public IntentType supportIntent() {
        return IntentType.PRODUCT_GUID;
    }

    @Override
    public Flux<String> handle(ChatContext context) {
        String userMessage = context.getUserMessage();
        int clarifyRound = context.getClarifyRound();

        // 反向澄清 - LLM判断参数是否完整
        if (clarifyRound < BusinessConstants.MAX_CLARIFY_ROUND) {
            boolean needClarify = checkNeedClarify(userMessage);
            if (needClarify) {
                String clarifyQuestion = generateClarifyQuestion(userMessage, clarifyRound);
                log.info("反向澄清: sessionId={}, round={}", context.getSessionId(), clarifyRound);
                return Flux.just(clarifyQuestion);
            }
        }

        // Function Calling - 调用产品检索
        ProductSearchTool.ProductSearchResult searchResult = productSearchTool.search(
                new ProductSearchTool.ProductSearchRequest(null, null, null, null)
        );

        // RAG话术增强 - 检索销售话术
        List<RagRetriever.KnowledgeResult> salesScripts = ragRetriever.retrieveSalesScripts(userMessage);
        String salesScriptContext = salesScripts.stream()
                .map(RagRetriever.KnowledgeResult::getContent)
                .collect(Collectors.joining("\n"));

        // 组装Prompt调用LLM生成导购回复
        String guidePrompt = buildGuidePrompt(context, searchResult, salesScriptContext);
        return chatClient.prompt(guidePrompt).stream().content();
    }

    /**
     * LLM判断用户需求参数是否完整
     */
    private boolean checkNeedClarify(String userMessage) {
        String checkPrompt = """
                判断用户消息中是否包含足够的产品导购信息（至少包含需求/场景/预算/起订量中的2项）。
                只回答true或false。
                用户消息：%s
                """.formatted(userMessage);
        String result = chatClient.prompt(checkPrompt).call().content();
        return "true".equalsIgnoreCase(result != null ? result.trim() : "");
    }

    /**
     * LLM生成反向澄清追问
     */
    private String generateClarifyQuestion(String userMessage, int clarifyRound) {
        String clarifyPrompt = """
                作为跨境电商导购助手，用户的需求信息不够完整，请生成一个简短的追问（不超过50字），
                引导用户提供：使用场景、预算范围、起订量等关键信息。
                当前追问轮次：%d（最多3轮）
                用户消息：%s
                """.formatted(clarifyRound + 1, userMessage);
        String question = chatClient.prompt(clarifyPrompt).call().content();
        return question != null ? question : "为了给您推荐最合适的产品，能否详细描述一下您的需求？";
    }

    /**
     * 构建导购Prompt（含Function Calling结果和RAG话术）
     */
    private String buildGuidePrompt(ChatContext context,
                                    ProductSearchTool.ProductSearchResult searchResult,
                                    String salesScriptContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为跨境电商导购助手，根据以下信息推荐合适的产品。\n\n");

        prompt.append("【用户需求】").append(context.getUserMessage()).append("\n");

        if (context.getRegion() != null) {
            prompt.append("【用户地区】").append(context.getRegion()).append("\n");
        }

        if (searchResult != null && !searchResult.products().isEmpty()) {
            prompt.append("【产品检索结果】\n");
            for (var product : searchResult.products()) {
                prompt.append("- ").append(product.get("productName"))
                        .append(" | 价格: $").append(product.get("currentPrice"))
                        .append(" | 场景: ").append(product.get("scene"))
                        .append(" | 起订量: ").append(product.get("moq")).append("\n");
            }
        }

        if (!salesScriptContext.isEmpty()) {
            prompt.append("【销售话术参考】\n").append(salesScriptContext).append("\n");
        }

        if (context.getRelevantSummaries() != null && !context.getRelevantSummaries().isEmpty()) {
            prompt.append("【历史偏好】").append(String.join("；", context.getRelevantSummaries())).append("\n");
        }

        prompt.append("\n请基于以上信息给出专业的产品推荐，包括产品名称、核心优势、适用场景和参考报价。");
        return prompt.toString();
    }
}
