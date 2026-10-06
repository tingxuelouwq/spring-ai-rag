package com.kevin.springai.rag.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 */
public final class DateTimeUtil {

    /** 紧凑格式：yyyyMMddHHmmss，例如 20261007231633 */
    private static final DateTimeFormatter COMPACT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 标准格式：yyyy-MM-dd HH:mm:ss，例如 2026-10-07 23:16:33 */
    private static final DateTimeFormatter STANDARD_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
        // 工具类禁止实例化
    }

    /**
     * 将 LocalDateTime 格式化为紧凑字符串（yyyyMMddHHmmss）
     *
     * @param dateTime 待格式化的时间
     * @return 格式化后的字符串，dateTime 为 null 时返回空字符串
     */
    public static String formatCompact(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(COMPACT_FORMATTER);
    }

    /**
     * 获取当前时间的紧凑格式字符串（yyyyMMddHHmmss）
     *
     * @return 当前时间格式化后的字符串
     */
    public static String nowCompact() {
        return formatCompact(LocalDateTime.now());
    }

    /**
     * 将 LocalDateTime 格式化为标准字符串（yyyy-MM-dd HH:mm:ss）
     *
     * @param dateTime 待格式化的时间
     * @return 格式化后的字符串，dateTime 为 null 时返回空字符串
     */
    public static String formatStandard(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(STANDARD_FORMATTER);
    }

    /**
     * 获取当前时间的标准格式字符串（yyyy-MM-dd HH:mm:ss）
     *
     * @return 当前时间格式化后的字符串
     */
    public static String nowStandard() {
        return formatStandard(LocalDateTime.now());
    }
}