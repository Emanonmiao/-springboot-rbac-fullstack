package com.rbac.service;

import com.rbac.dto.MenuDTO;
import com.rbac.vo.MenuVO;

import java.util.List;

/**
 * 菜单管理服务接口
 */
public interface MenuService {

    /** 查询全部菜单（树形结构） */
    List<MenuVO> listTree();

    /** 新增菜单 */
    void createMenu(MenuDTO dto);

    /** 修改菜单 */
    void updateMenu(MenuDTO dto);

    /** 删除菜单 */
    void deleteMenu(Long id);

    /** 查询当前用户的菜单树（前端路由用） */
    List<MenuVO> getUserMenuTree(Long userId);
}
