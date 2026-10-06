package com.kevin.springai.rag.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * <p>
 * 标注在方法上，切面会在方法执行前记录调用信息。
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {

    /**
     * 需要记录的参数名，可指定多个
     * <p>
     * 留空时记录全部参数。参数名需与编译时保留的参数名一致。
     * </p>
     *
     * @return 参数名数组
     */
    String[] value() default {};
}