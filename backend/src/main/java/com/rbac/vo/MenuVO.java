package com.rbac.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单返回VO（树形结构）
 */
@Data
public class MenuVO {

    private Long id;

    private Long parentId;

    private String menuName;

    /** 0目录 1菜单 2按钮 */
    private Integer menuType;

    private String path;

    private String component;

    private String icon;

    private String perms;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    /** 子菜单列表 */
    private List<MenuVO> children;
}
