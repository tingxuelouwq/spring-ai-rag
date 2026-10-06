package com.kevin.springai.rag.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPageQueryDTO implements Serializable {

    /** 用户姓名 */
    private String name;

    /** 页码，从 0 开始 */
    private int page = 0;

    /** 每页显示记录数 */
    private int pageSize = 10;
}
