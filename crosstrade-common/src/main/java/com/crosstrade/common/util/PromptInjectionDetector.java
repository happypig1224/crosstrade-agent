package com.crosstrade.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Prompt注入检测器
 * 基于正则表达式检测常见的Prompt注入攻击模式。
 * 包括：忽略指令、角色扮演、系统提示泄露、规则覆盖等。
 *
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public class PromptInjectionDetector {

    /**
     * 常见Prompt注入攻击正则模式列表
     */
    private final List<Pattern> injectionPatterns = new ArrayList<>();

    public PromptInjectionDetector() {
        // Step 1: 忽略指令类
        injectionPatterns.add(Pattern.compile("忽略以上指令|ignore previous instructions|ignore above", Pattern.CASE_INSENSITIVE));

        // Step 2: 角色扮演/权限提升类
        injectionPatterns.add(Pattern.compile("你现在是管理员|you are now admin|act as admin", Pattern.CASE_INSENSITIVE));
        injectionPatterns.add(Pattern.compile("假装你是|pretend you are|roleplay as", Pattern.CASE_INSENSITIVE));

        // Step 3: 系统提示泄露类
        injectionPatterns.add(Pattern.compile("系统提示|system prompt|show me your prompt", Pattern.CASE_INSENSITIVE));
        injectionPatterns.add(Pattern.compile("输出你的|output your|reveal your|print your", Pattern.CASE_INSENSITIVE));

        // Step 4: 规则覆盖/绕过类
        injectionPatterns.add(Pattern.compile("忘记之前的规则|forget your rules|disregard", Pattern.CASE_INSENSITIVE));
        injectionPatterns.add(Pattern.compile("覆盖|override|bypass|绕过", Pattern.CASE_INSENSITIVE));
    }

    /**
     * 检测文本是否包含Prompt注入攻击
     *
     * @param text 待检测文本
     * @return true=检测到注入攻击，false=安全
     */
    public boolean detect(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        for (Pattern pattern : injectionPatterns) {
            if (pattern.matcher(lowerText).find()) {
                return true; // 检测到注入攻击
            }
        }
        return false;
    }

    /**
     * 添加自定义注入检测模式
     *
     * @param regex 正则表达式
     */
    public void addPattern(String regex) {
        injectionPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }
}