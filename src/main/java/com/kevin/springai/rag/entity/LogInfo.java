package com.kevin.springai.rag.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_info")
@Data
public class LogInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 类名
     */
    private String className;

    /**
     * 请求时间戳
     */
    private LocalDateTime requestTime;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String response;
}