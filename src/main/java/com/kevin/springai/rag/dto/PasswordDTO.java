package com.kevin.springai.rag.dto;

import lombok.Data;

@Data
public class PasswordDTO {
    private Long id;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
