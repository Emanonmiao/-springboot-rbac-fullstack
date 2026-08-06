package com.rbac.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 菜单请求DTO（新增/修改）
 */
@Data
public class MenuDTO {

    private Long id;

    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /** 0目录 1菜单 2按钮 */
    private Integer menuType;

    private String path;

    private String component;

    private String icon;

    private String perms;

    private Integer sortOrder;

    private Integer status;
}
