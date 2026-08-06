package com.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rbac.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据角色ID列表查询菜单权限
     * @param roleIds 角色ID列表
     * @return 菜单列表
     */
    List<SysMenu> selectMenusByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 查询用户拥有权限的菜单（通过userId关联查询）
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}
