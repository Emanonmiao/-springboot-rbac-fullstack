package com.rbac.service;

import com.rbac.dto.RoleDTO;
import com.rbac.entity.SysRole;

import java.util.List;

/**
 * 角色管理服务接口
 */
public interface RoleService {

    /** 查询全部角色 */
    List<SysRole> listAll();

    /** 新增角色 */
    void createRole(RoleDTO dto);

    /** 修改角色 */
    void updateRole(RoleDTO dto);

    /** 删除角色 */
    void deleteRole(Long id);

    /** 为角色分配菜单权限 */
    void assignMenus(Long roleId, List<Long> menuIds);

    /** 查询角色已分配的菜单ID */
    List<Long> getRoleMenuIds(Long roleId);
}
