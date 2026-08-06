package com.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单权限表实体
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单ID，0为顶级 */
    private Long parentId;

    private String menuName;

    /** 0目录 1菜单 2按钮 */
    private Integer menuType;

    /** 路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    private String icon;

    /** 权限标识，如 system:user:add */
    private String perms;

    private Integer sortOrder;

    /** 0禁用 1正常 */
    private Integer status;

    private LocalDateTime createTime;
}
