package com.rbac.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户返回VO
 * 注意：绝不包含password字段
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    /** 0锁定 1正常 */
    private Integer status;

    private LocalDateTime pwdExpireTime;

    private LocalDateTime createTime;

    /** 角色列表 */
    private List<RoleVO> roles;
}
