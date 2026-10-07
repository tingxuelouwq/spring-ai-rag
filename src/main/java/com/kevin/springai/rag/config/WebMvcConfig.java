package com.kevin.springai.rag.config;

import com.kevin.springai.rag.constant.BizConstant;
import com.kevin.springai.rag.interceptor.JwtTokenUserInterceptor;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 全局配置
 * <p>
 * 负责注册拦截器、声明 ETL 组件等基础 Bean。
 * </p>
 *
 * @author xushu
 * @date 2025/2/8
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 无需 JWT 校验的白名单路径 */
    private static final String[] EXCLUDE_PATHS = {
            BizConstant.API_VERSION + "/user/login",
            BizConstant.API_VERSION + "/user/register",
            BizConstant.API_VERSION + "/knowledge/download/**",
            BizConstant.API_VERSION + "/knowledge/preview/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**"
    };

    private final JwtTokenUserInterceptor jwtTokenUserInterceptor;

    public WebMvcConfig(JwtTokenUserInterceptor jwtTokenUserInterceptor) {
        this.jwtTokenUserInterceptor = jwtTokenUserInterceptor;
    }

    /**
     * ETL 流程中的文档转换器，将长文本切分为多个语义段落
     *
     * @return TokenTextSplitter 实例
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return TokenTextSplitter.builder().build();
    }

    /**
     * 注册 JWT 用户拦截器
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }
}