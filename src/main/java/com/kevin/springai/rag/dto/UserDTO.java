package com.kevin.springai.rag.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserDTO implements Serializable {

    /** 用户 ID */
    private Long id;

    /** 登录用户名 */
    private String userName;

    /** 用户姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 性别 */
    private String sex;

    /** 身份证号 */
    private String idNumber;
}