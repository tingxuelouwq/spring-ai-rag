package com.kevin.springai.rag.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

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

    /**
     * 将 JSON 数组字符串解析为 List
     * <p>
     * 例如：{@code ["id1","id2"]} 解析为 {@code List<String>}。
     * </p>
     *
     * @param json  JSON 数组字符串
     * @param clazz 元素类型
     * @param <T>   元素泛型
     * @return 解析后的 List，解析失败或输入为空时返回空列表
     */
    public static <T> List<T> parseList(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException ex) {
            log.warn("JSON 数组解析失败，元素类型={}, json={}", clazz.getName(), json, ex);
            return Collections.emptyList();
        }
    }

    /**
     * 将 JSON 字符串反序列化为复杂泛型类型
     * <p>
     * 适用于 {@code List<User>}、{@code Map<String, List<User>>} 等带泛型的类型。
     * </p>
     *
     * @param json          JSON 字符串
     * @param typeReference 类型引用，如 {@code new TypeReference<List<User>>() {}}
     * @param <T>           泛型
     * @return 反序列化后的对象，失败时返回 null
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException ex) {
            log.warn("JSON 反序列化失败，type={}", typeReference.getType(), ex);
            return null;
        }
    }
}