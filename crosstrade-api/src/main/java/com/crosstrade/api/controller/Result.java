package com.crosstrade.api.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应包装类
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 状态码：200成功，其他失败
     */
    private int code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功响应
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 失败响应
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @return 失败Result
     */
    public static <T> Result<T> fail(String errorCode, String message) {
        return new Result<>(500, message, null);
    }
}