package com.kevin.springai.rag.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举
 * <p>
 * code 采用 5 位数字设计：前 3 位对应 HTTP 语义，后 2 位为业务细分。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    UPDATE_ERROR(50002, "更新失败"),
    DELETE_ERROR(50004, "删除失败"),
    APIKEY_ERROR(50005, "apikey不存在"),
    PROMPT_ERROR(50006, "prompt为空"),
    MODEL_ERROR(50007, "模型为空"),
    DATABASE_ERROR(50009, "数据类型错误"),
    FILE_ERROR(50008, "请求文件为空"),
    API_REQUEST_ERROR(50010, "接口调用失败");

    /** 业务状态码 */
    private final int code;

    /** 错误提示信息 */
    private final String message;
}