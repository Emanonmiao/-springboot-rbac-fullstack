package com.rbac.common;

/**
 * 系统常量
 */
public final class Constants {

    private Constants() {}

    /** 用户状态：正常 */
    public static final int USER_STATUS_NORMAL = 1;

    /** 用户状态：锁定 */
    public static final int USER_STATUS_LOCKED = 0;

    /** 密码连续错误最大次数，超过则锁定 */
    public static final int MAX_PWD_WRONG_COUNT = 5;

    /** 密码有效期天数 */
    public static final int PWD_EXPIRE_DAYS = 90;

    /** 密码最小长度 */
    public static final int PWD_MIN_LENGTH = 8;

    /** JWT请求头 */
    public static final String AUTH_HEADER = "Authorization";

    /** JWT Token前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 登录成功 */
    public static final int LOGIN_SUCCESS = 1;

    /** 登录失败 */
    public static final int LOGIN_FAIL = 0;

    /** 菜单类型：目录 */
    public static final int MENU_TYPE_DIR = 0;

    /** 菜单类型：菜单 */
    public static final int MENU_TYPE_MENU = 1;

    /** 菜单类型：按钮 */
    public static final int MENU_TYPE_BUTTON = 2;
}
