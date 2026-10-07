package com.kevin.springai.rag.common;

import com.kevin.springai.rag.enums.ErrorCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应体
 *
 * @param <T> 业务数据类型
 */
@Data
@NoArgsConstructor
public class BaseResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 业务状态码，0 表示成功 */
    private int code;

    /** 业务数据，失败时为 null */
    private T data;

    /** 提示信息 */
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

    public BaseResponse(String message) {
        this(ErrorCode.SYSTEM_ERROR.getCode(), null, message);
    }
}