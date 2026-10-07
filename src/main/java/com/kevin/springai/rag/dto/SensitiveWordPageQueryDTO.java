package com.kevin.springai.rag.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 敏感词分页查询条件
 */
@Data
public class SensitiveWordPageQueryDTO implements Serializable {

    /** 页码，从 0 开始 */
    private int page = 0;

    /** 每页显示记录数 */
    private int size = 10;
}