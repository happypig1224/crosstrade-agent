package com.crosstrade.common.util;

import java.util.*;

/**
 * DFA（确定有限自动机）敏感词过滤器
 * 基于字典树算法的敏感词过滤，时间复杂度O(n)，与敏感词数量无关。
 * 优于正则匹配的O(n*m)复杂度。
 * DFA算法原理：将敏感词库构建成一棵多叉树，每个节点是一个Map，
 * key是字符，value是子节点Map。当某个路径的叶子节点标记为"结束"时，
 * 表示匹配到一个敏感词。
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public class DfaSensitiveFilter {

    /**
     * DFA字典树根节点
     */
    private final Map<Character, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 敏感词结束标记Key
     */
    private static final String IS_END = "isEnd";

    /**
     * 初始化：从敏感词列表构建DFA树
     *
     * @param sensitiveWords 敏感词列表
     */
    public void initSensitiveWordMap(List<String> sensitiveWords) {
        if (sensitiveWords == null || sensitiveWords.isEmpty()) {
            return;
        }
        for (String word : sensitiveWords) {
            addWord(word);
        }
    }

    /**
     * 向DFA树中添加一个敏感词
     *
     * @param word 敏感词
     */
    @SuppressWarnings("unchecked")
    private void addWord(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        Map<Character, Object> currentNode = sensitiveWordMap;
        for (char ch : word.toCharArray()) {
            // 如果当前字符不在树中，创建新节点
            if (!currentNode.containsKey(ch)) {
                currentNode.put(ch, new HashMap<Character, Object>());
            }
            // 移动到子节点
            currentNode = (Map<Character, Object>) currentNode.get(ch);
        }
        // 标记词结束
        currentNode.put(IS_END.charAt(0), true);
    }

    /**
     * 检测文本是否包含敏感词
     * 逐字符遍历文本，找到起始字符后沿DFA树向下匹配完整敏感词。
     * @param text 待检测文本
     * @return true=包含敏感词，false=不包含
     */
    @SuppressWarnings("unchecked")
    public boolean containsSensitive(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (sensitiveWordMap.containsKey(ch)) {
                // 找到起始字符，沿DFA树向下匹配
                Map<Character, Object> currentNode = (Map<Character, Object>) sensitiveWordMap.get(ch);
                int j = i + 1;
                while (j < text.length()) {
                    char nextCh = text.charAt(j);
                    if (currentNode.containsKey(nextCh)) {
                        currentNode = (Map<Character, Object>) currentNode.get(nextCh);
                        // 检查是否到达敏感词结尾
                        if (currentNode.containsKey(IS_END.charAt(0))) {
                            return true; // 匹配到完整敏感词
                        }
                        j++;
                    } else {
                        break;
                    }
                }
                // 单字符敏感词的情况
                if (currentNode.containsKey(IS_END.charAt(0))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 替换文本中的敏感词为指定字符
     *
     * @param text        待处理文本
     * @param replaceChar 替换字符（如*）
     * @return 替换后的文本
     */
    @SuppressWarnings("unchecked")
    public String filter(String text, char replaceChar) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (sensitiveWordMap.containsKey(ch)) {
                Map<Character, Object> currentNode = (Map<Character, Object>) sensitiveWordMap.get(ch);
                int j = i + 1;
                int endIdx = -1;
                while (j < chars.length) {
                    char nextCh = chars[j];
                    if (currentNode.containsKey(nextCh)) {
                        currentNode = (Map<Character, Object>) currentNode.get(nextCh);
                        if (currentNode.containsKey(IS_END.charAt(0))) {
                            endIdx = j;
                        }
                        j++;
                    } else {
                        break;
                    }
                }
                // 单字符敏感词
                if (currentNode.containsKey(IS_END.charAt(0))) {
                    endIdx = i;
                }
                // 替换敏感词
                if (endIdx != -1) {
                    for (int k = i; k <= endIdx; k++) {
                        chars[k] = replaceChar;
                    }
                    i = endIdx;
                }
            }
        }
        return new String(chars);
    }
}