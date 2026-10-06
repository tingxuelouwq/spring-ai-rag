package com.kevin.springai.rag.aop;

import com.kevin.springai.rag.annotation.Loggable;
import com.kevin.springai.rag.entity.LogInfo;
import com.kevin.springai.rag.service.LogInfoService;
import com.kevin.springai.rag.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日志记录切面
 * <p>
 * 拦截标注了 {@link Loggable} 的方法，在方法执行前记录调用信息。
 * </p>
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private final LogInfoService logInfoService;

    /**
     * 切点：所有标注了 @Loggable 的方法
     */
    @Pointcut("@annotation(com.kevin.springai.rag.annotation.Loggable)")
    public void loggableMethods() {
    }

    /**
     * 方法执行前记录日志
     *
     * @param joinPoint 连接点
     * @param loggable  方法上的 @Loggable 注解
     */
    @Before(value = "loggableMethods() && @annotation(loggable)", argNames = "joinPoint,loggable")
    public void logBefore(JoinPoint joinPoint, Loggable loggable) {
        try {
            LogInfo logInfo = new LogInfo();
            logInfo.setMethodName(joinPoint.getSignature().getName());
            logInfo.setClassName(joinPoint.getTarget().getClass().getName());
            logInfo.setRequestTime(LocalDateTime.now());
            logInfo.setRequestParams(buildRequestParams(joinPoint, loggable));

            logInfoService.save(logInfo);
        } catch (Exception ex) {
            // 日志记录失败不应影响业务主流程
            log.error("记录操作日志失败", ex);
        }
    }

    /**
     * 构建请求参数字符串
     * <p>
     * 若注解指定了参数名，则只记录指定参数；否则记录全部参数。
     * </p>
     *
     * @param joinPoint 连接点
     * @param loggable  注解实例
     * @return JSON 格式的参数字符串
     */
    private String buildRequestParams(JoinPoint joinPoint, Loggable loggable) {
        String[] targetParams = loggable.value();
        Object[] args = joinPoint.getArgs();

        // 未指定参数名，记录全部参数
        if (targetParams == null || targetParams.length == 0) {
            return JsonUtils.toJson(args);
        }

        // 指定了参数名，筛选出目标参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        // 编译时未保留参数名信息，退化为记录全部参数
        if (parameterNames == null || parameterNames.length == 0) {
            log.warn("无法获取方法参数名，退化为记录全部参数，method={}", signature.getName());
            return JsonUtils.toJson(args);
        }

        Map<String, Object> selectedParams = new HashMap<>(targetParams.length);
        List<String> targetList = Arrays.asList(targetParams);
        for (int i = 0; i < parameterNames.length; i++) {
            if (targetList.contains(parameterNames[i])) {
                selectedParams.put(parameterNames[i], args[i]);
            }
        }
        return JsonUtils.toJson(selectedParams);
    }
}