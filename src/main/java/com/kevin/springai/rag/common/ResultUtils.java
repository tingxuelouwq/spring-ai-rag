package com.kevin.springai.rag.common;

import com.kevin.springai.rag.enums.ErrorCode;

/**
 * 响应结果构造工具
 */
public final class ResultUtils {

    private ResultUtils() {
        // 工具类禁止实例化
    }

    /**
     * 构造成功响应（使用默认提示信息 "ok"）
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功响应体
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), data, ErrorCode.SUCCESS.getMessage());
    }

    /**
     * 构造成功响应（自定义提示信息）
     *
     * @param data    业务数据
     * @param message 自定义提示信息
     * @param <T>     数据类型
     * @return 成功响应体
     */
    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), data, message);
    }

    /**
     * 构造失败响应（使用错误码自带的提示信息）
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return 失败响应体
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 构造失败响应（自定义 code 和提示信息）
     *
     * @param code    业务状态码
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败响应体
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 构造失败响应（使用错误码的 code，自定义提示信息）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义提示信息
     * @param <T>       数据类型
     * @return 失败响应体
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }

    /**
     * 构造失败响应（使用系统默认错误码）
     *
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败响应体
     */
    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(ErrorCode.SYSTEM_ERROR.getCode(), null, message);
    }
}