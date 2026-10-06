package com.kevin.springai.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性
 * <p>
 * 对应 application.yml 中 {@code aliyun.alioss} 前缀下的配置项。
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "aliyun.alioss")
@Data
public class AliOssProperties {

    /** OSS 服务端点，如 oss-cn-hangzhou.aliyuncs.com */
    private String endpoint;

    /** 访问密钥 ID */
    private String accessKeyId;

    /** 访问密钥 Secret */
    private String accessKeySecret;

    /** Bucket 名称 */
    private String bucketName;

    /**
     * 重写 toString，对密钥进行脱敏，避免日志泄露
     */
    @Override
    public String toString() {
        return "AliOssProperties{" +
                "endpoint='" + endpoint + '\'' +
                ", accessKeyId='" + mask(accessKeyId) + '\'' +
                ", accessKeySecret='******'" +
                ", bucketName='" + bucketName + '\'' +
                '}';
    }

    /**
     * 对密钥进行脱敏，只保留前 4 位
     *
     * @param value 原始值
     * @return 脱敏后的字符串
     */
    private String mask(String value) {
        if (value == null || value.length() <= 4) {
            return "******";
        }
        return value.substring(0, 4) + "******";
    }
}