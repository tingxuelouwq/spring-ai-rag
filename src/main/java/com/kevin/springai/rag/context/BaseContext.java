package com.kevin.springai.rag.context;

/**
 * 基于 ThreadLocal 的当前登录用户上下文
 * <p>
 * 用于在同一次请求的线程内传递用户 ID。
 * 请求结束后必须调用 {@link #removeCurrentId()} 清理，避免线程池复用导致的脏数据。
 * </p>
 */
public class BaseContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private BaseContext() {
        // 工具类禁止实例化
    }

    /**
     * 设置当前登录用户 ID
     *
     * @param id 用户 ID
     */
    public static void setCurrentId(Long id) {
        CURRENT_USER_ID.set(id);
    }

    /**
     * 获取当前登录用户 ID
     *
     * @return 用户 ID，未设置时返回 null
     */
    public static Long getCurrentId() {
        return CURRENT_USER_ID.get();
    }

    /**
     * 清除当前登录用户 ID
     * <p>务必在请求结束时调用，防止线程复用造成数据污染。</p>
     */
    public static void removeCurrentId() {
        CURRENT_USER_ID.remove();
    }
}