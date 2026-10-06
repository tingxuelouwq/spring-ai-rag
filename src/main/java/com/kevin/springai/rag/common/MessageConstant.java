package com.kevin.springai.rag.common;

/**
 * 信息提示常量
 * <p>
 * 集中管理业务层返回给前端的提示信息，避免硬编码字符串散落在各处。
 * </p>
 */
public final class MessageConstant {

    private MessageConstant() {
        // 常量类禁止实例化
    }

    // ==================== 用户相关 ====================

    /** 密码错误 */
    public static final String PASSWORD_ERROR = "密码错误";

    /** 账号不存在 */
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";

    /** 账号被锁定 */
    public static final String ACCOUNT_LOCKED = "账号被锁定";

    /** 用户未登录 */
    public static final String USER_NOT_LOGIN = "用户未登录";

    /** 登录失败 */
    public static final String LOGIN_FAILED = "登录失败";

    /** 密码修改失败 */
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";

    // ==================== 通用 ====================

    /** 数据已存在 */
    public static final String ALREADY_EXISTS = "已存在";

    /** 未知错误 */
    public static final String UNKNOWN_ERROR = "未知错误";

    /** 文件上传失败 */
    public static final String UPLOAD_FAILED = "文件上传失败";
}