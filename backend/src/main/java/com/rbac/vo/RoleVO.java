package com.rbac.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色返回VO
 */
@Data
public class RoleVO {

    private Long id;

    private String roleName;

    private String roleCode;

    private String remark;

    private LocalDateTime createTime;
}
