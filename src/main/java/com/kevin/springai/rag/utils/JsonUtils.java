package com.kevin.springai.rag.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON 序列化工具类
 * <p>
 * 基于 Jackson 实现，提供对象与 JSON 字符串之间的转换。
 * </p>
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtils() {
        // 工具类禁止实例化
    }

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param obj 待序列化对象
     * @return JSON 字符串，序列化失败时返回 null
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.warn("对象序列化为 JSON 失败，类型={}", obj.getClass().getName(), ex);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return 反序列化后的对象，失败时返回 null
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException ex) {
            log.warn("JSON 反序列化失败，目标类型={}", clazz.getName(), ex);
            return null;
        }
    }
}