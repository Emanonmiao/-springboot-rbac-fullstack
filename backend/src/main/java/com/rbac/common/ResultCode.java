package com.rbac.common;

import lombok.Getter;

/**
 * 统一返回状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "服务器内部错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有操作权限"),
    PARAM_ERROR(400, "参数校验失败"),
    ACCOUNT_LOCKED(423, "账号已锁定，请联系管理员"),
    PWD_EXPIRED(428, "密码已过期，请修改密码"),
    USER_NOT_FOUND(404, "用户不存在"),
    USERNAME_EXISTS(409, "用户名已存在"),
    PWD_WEAK(422, "密码强度不足"),
    LOGIN_FAIL(401, "用户名或密码错误");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
