package com.kevin.springai.rag.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词实体
 */
@Entity
@Table(name = "sensitive_word")
@Data
public class SensitiveWord {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 敏感词内容
     */
    private String word;

    /**
     * 敏感词类别
     */
    private String category;

    /**
     * 敏感词状态
     */
    private String status;

    /**
     * 创建时间戳
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间戳
     */
    private LocalDateTime updatedAt;
}