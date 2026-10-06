package com.kevin.springai.rag.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 日志分页查询条件
 */
@Data
public class LogInfoPageQueryDTO implements Serializable {

    /** 页码，从 0 开始 */
    private int page = 0;

    /** 每页显示记录数 */
    private int size = 10;

    /** 方法名（模糊匹配） */
    private String methodName;

    /** 类名（模糊匹配） */
    private String className;

    /** 请求参数（模糊匹配） */
    private String requestParams;
}