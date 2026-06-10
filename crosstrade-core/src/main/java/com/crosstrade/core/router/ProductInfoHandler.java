package com.crosstrade.core.router;

import com.crosstrade.common.enums.IntentType;
import com.crosstrade.core.rag.RagRetriever;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品咨询处理器
 * RAG语义检索 + 区域化售后政策匹配 + LLM专业回复生成
 * 当知识库无高相关度结果时，返回兜底提示
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Slf4j
@Component
public class ProductInfoHandler implements IntentHandler {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private RagRetriever ragRetriever;

    @Override
    public IntentType supportIntent() {
        return IntentType.PRODUCT_INFO;
    }

    @Override
    public Flux<String> handle(ChatContext context) {
        String userMessage = context.getUserMessage();

        // RAG语义检索知识库
        List<RagRetriever.KnowledgeResult> knowledgeResults = ragRetriever.retrieve(userMessage, 5);
        String knowledgeContext = knowledgeResults.stream()
                .map(RagRetriever.KnowledgeResult::getContent)
                .collect(Collectors.joining("\n"));

        // 区域化售后政策匹配
        String afterSalesPolicy = null;
        if (context.getRegion() != null) {
            afterSalesPolicy = ragRetriever.retrieveAfterSalesPolicy(context.getRegion());
        }

        // 知识库无高相关度结果时兜底
        if (knowledgeResults.isEmpty() && afterSalesPolicy == null) {
            log.warn("RAG检索无结果，返回兜底提示: sessionId={}", context.getSessionId());
            return Flux.just("抱歉，暂时没有找到与您问题相关的信息。您可以换个方式描述，或联系人工客服获取帮助。");
        }

        // 组装Prompt调用LLM生成专业回复
        String infoPrompt = buildInfoPrompt(context, knowledgeContext, afterSalesPolicy);
        return chatClient.prompt(infoPrompt).stream().content();
    }

    /**
     * 构建产品咨询Prompt（含RAG知识库结果和售后政策）
     */
    private String buildInfoPrompt(ChatContext context, String knowledgeContext, String afterSalesPolicy) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为跨境电商产品咨询助手，请基于以下参考资料专业地回答用户的问题。\n\n");

        prompt.append("【用户问题】").append(context.getUserMessage()).append("\n");

        if (!knowledgeContext.isEmpty()) {
            prompt.append("【知识库参考】\n").append(knowledgeContext).append("\n");
        }

        if (afterSalesPolicy != null) {
            prompt.append("【售后政策】").append(afterSalesPolicy).append("\n");
        }

        if (context.getRelevantSummaries() != null && !context.getRelevantSummaries().isEmpty()) {
            prompt.append("【相关历史信息】").append(String.join("；", context.getRelevantSummaries())).append("\n");
        }

        prompt.append("\n请严格基于以上参考资料回答，如果参考资料中没有相关信息，请如实说明。");
        return prompt.toString();
    }
}
