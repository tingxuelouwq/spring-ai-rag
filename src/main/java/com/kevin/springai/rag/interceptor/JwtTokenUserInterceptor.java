package com.kevin.springai.rag.interceptor;

import com.kevin.springai.rag.config.JwtProperties;
import com.kevin.springai.rag.constant.JwtClaimsConstant;
import com.kevin.springai.rag.context.BaseContext;
import com.kevin.springai.rag.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 令牌校验拦截器
 * <p>
 * 拦截所有 Controller 方法请求，从请求头中提取 JWT 令牌并校验有效性。
 * 校验通过后，将用户 ID 写入 {@link BaseContext}，供后续业务逻辑使用。
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    /** Bearer 令牌前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProperties jwtProperties;

    /**
     * 请求前置处理：校验 JWT 令牌
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  被拦截的处理器
     * @return true 表示放行，false 表示拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 非 Controller 方法（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 从请求头中提取令牌
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("请求头中未携带 JWT 令牌，uri={}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        // 2. 校验令牌并解析用户信息
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.debug("JWT 校验通过，当前用户 id={}", userId);
            BaseContext.setCurrentId(userId);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT 令牌已过期，uri={}", request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT 令牌校验失败，uri={}，原因={}", request.getRequestURI(), ex.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    /**
     * 请求完成后清理 ThreadLocal，防止线程复用导致的数据污染
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  被拦截的处理器
     * @param ex       处理过程中抛出的异常，无异常时为 null
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }

    /**
     * 从请求头中提取 JWT 令牌，并去除 Bearer 前缀
     *
     * @param request 当前请求
     * @return 纯令牌字符串，若不存在则返回 null
     */
    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }
        return token;
    }
}