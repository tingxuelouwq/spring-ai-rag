package com.kevin.springai.rag.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 * <p>
 * 基于 JJWT 0.12+ API 实现，提供 Token 的生成与解析能力。
 * 使用 HS256 对称加密算法，密钥由调用方传入，需保证长度不低于 32 字节。
 * </p>
 */
public class JwtUtil {

    /** HS256 算法要求的最小密钥长度（字节） */
    private static final int MIN_KEY_LENGTH = 32;

    private JwtUtil() {
        // 工具类禁止实例化
    }

    /**
     * 生成 JWT Token
     *
     * @param secretKey 签名密钥，长度必须 ≥ 32 字节，否则会抛出 WeakKeyException
     * @param ttlMillis Token 有效期（毫秒），从当前时间起算
     * @param claims    自定义声明信息，会被写入 Token 的 payload 部分
     * @return 签名后的 JWT 字符串
     * @throws IllegalArgumentException 密钥长度不足时抛出
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        SecretKey key = buildSecretKey(secretKey);
        Date expiration = new Date(System.currentTimeMillis() + ttlMillis);

        return Jwts.builder()
                // 先写入自定义声明，避免覆盖标准声明
                .claims(claims)
                // 签名算法由密钥类型自动推断为 HS256
                .signWith(key)
                // 设置过期时间
                .expiration(expiration)
                .compact();
    }

    /**
     * 解析并验证 JWT Token
     *
     * @param secretKey 签名密钥，必须与生成时使用的密钥一致
     * @param token     待解析的 JWT 字符串
     * @return Token 中的声明信息（payload）
     * @throws io.jsonwebtoken.JwtException 签名不匹配、Token 过期或格式非法时抛出
     */
    public static Claims parseJWT(String secretKey, String token) {
        SecretKey key = buildSecretKey(secretKey);

        return Jwts.parser()
                // 设置验签密钥
                .verifyWith(key)
                .build()
                // 解析已签名的 Token
                .parseSignedClaims(token)
                // 获取 payload
                .getPayload();
    }

    /**
     * 将原始字符串密钥转换为符合 HS256 要求的 SecretKey
     *
     * @param secretKey 原始密钥字符串
     * @return 可用于签名/验签的 SecretKey
     * @throws IllegalArgumentException 密钥为空或长度不足时抛出
     */
    private static SecretKey buildSecretKey(String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("JWT 密钥不能为空");
        }
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "JWT 密钥长度不足，HS256 要求至少 " + MIN_KEY_LENGTH + " 字节，当前为 " + keyBytes.length + " 字节");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}