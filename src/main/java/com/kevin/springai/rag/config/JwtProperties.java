package com.kevin.springai.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 相关配置属性
 * <p>
 * 对应 application.yml 中 {@code jwt} 前缀下的配置项，
 * 分别管理「管理端」和「用户端」的密钥、有效期和令牌请求头名称。
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    /** 管理端签名密钥 */
    private String adminSecretKey;

    /** 管理端令牌有效期（毫秒） */
    private long adminTtl;

    /** 管理端令牌所在的请求头名称 */
    private String adminTokenName;

    /** 用户端签名密钥 */
    private String userSecretKey;

    /** 用户端令牌有效期（毫秒） */
    private long userTtl;

    /** 用户端令牌所在的请求头名称 */
    private String userTokenName;
}