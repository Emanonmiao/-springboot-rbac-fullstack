package com.rbac.vo;

import lombok.Data;

import java.util.Set;

/**
 * 登录返回VO
 * 注意：绝不返回密码等敏感字段
 */
@Data
public class LoginVO {

    /** JWT Token */
    private String token;

    private Long userId;

    private String username;

    private String realName;

    /** 角色编码集合 */
    private Set<String> roles;

    /** 权限标识集合 */
    private Set<String> permissions;

    /** 密码是否过期，true则前端强制跳转修改密码 */
    private boolean pwdExpired;
}
