package com.kevin.springai.rag.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * OSS 文件实体
 */
@Entity
@Table(name = "ali_oss_file")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AliOssFile {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 链接地址
     */
    private String url;

    /**
     * 该文件分割出的多段向量文本 ID
     */
    private String vectorId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}