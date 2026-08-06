package com.rbac.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 角色请求DTO（新增/修改）
 */
@Data
public class RoleDTO {

    private Long id;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private String remark;

    /** 菜单ID列表（分配权限时使用） */
    private List<Long> menuIds;
}
