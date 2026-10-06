package com.kevin.springai.rag.config;

import com.kevin.springai.rag.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 阿里云 OSS 配置类
 * <p>
 * 根据 {@link AliOssProperties} 创建 {@link AliOssUtil} Bean。
 * </p>
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class AliOssConfiguration {

    private final AliOssProperties aliOssProperties;

    /**
     * 创建阿里云 OSS 工具类
     *
     * @return AliOssUtil 实例
     * @throws IllegalStateException 必要配置缺失时抛出
     */
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil() {
        // 校验必要配置项
        if (!StringUtils.hasText(aliOssProperties.getEndpoint())
                || !StringUtils.hasText(aliOssProperties.getAccessKeyId())
                || !StringUtils.hasText(aliOssProperties.getAccessKeySecret())
                || !StringUtils.hasText(aliOssProperties.getBucketName())) {
            throw new IllegalStateException(
                    "阿里云 OSS 配置不完整，请检查 aliyun.alioss 下的 endpoint、accessKeyId、accessKeySecret、bucketName");
        }

        log.info("初始化阿里云 OSS 工具类，配置：{}", aliOssProperties);
        return new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}