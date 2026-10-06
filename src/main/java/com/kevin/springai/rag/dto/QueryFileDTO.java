package com.kevin.springai.rag.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件分页查询条件
 */
@Data
public class QueryFileDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 页码，从 0 开始 */
    private Integer page = 0;

    /** 每页显示记录数 */
    private Integer pageSize = 10;

    /** 文件名（模糊匹配） */
    private String fileName;
}