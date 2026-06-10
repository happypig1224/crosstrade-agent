package com.crosstrade.core.guardrail;

import com.crosstrade.common.constants.BusinessConstants;

/**
 * 流式输出Token块缓冲器
 * 以20个Token为一块进行缓冲，每满一块触发出口审查校验。
 * 解决逐Token校验延迟过高的问题。
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public class OutputBuffer {

    /**
     * 每块Token数量
     */
    private final int tokenSize;

    /**
     * 缓冲区
     */
    private final StringBuilder buffer;

    /**
     * 当前Token计数
     */
    private int currentTokenCount;

    public OutputBuffer() {
        this.tokenSize = BusinessConstants.OUTPUT_BUFFER_TOKEN_SIZE;
        this.buffer = new StringBuilder();
        this.currentTokenCount = 0;
    }

    /**
     * 追加Token到缓冲区
     *
     * @param token Token文本
     */
    public void append(String token) {
        buffer.append(token);
        currentTokenCount++;
    }

    /**
     * 缓冲区是否已满
     *
     * @return true=已满
     */
    public boolean isFull() {
        return currentTokenCount >= tokenSize;
    }

    /**
     * 清空缓冲区并返回内容
     *
     * @return 缓冲区内容
     */
    public String flush() {
        String content = buffer.toString();
        buffer.setLength(0);
        currentTokenCount = 0;
        return content;
    }

    /**
     * 获取缓冲区剩余内容（流式输出结束时）
     *
     * @return 剩余内容，无内容时返回空字符串
     */
    public String drain() {
        if (currentTokenCount == 0) {
            return "";
        }
        return flush();
    }

    /**
     * 缓冲区是否有内容
     *
     * @return true=有内容
     */
    public boolean hasContent() {
        return currentTokenCount > 0;
    }
}