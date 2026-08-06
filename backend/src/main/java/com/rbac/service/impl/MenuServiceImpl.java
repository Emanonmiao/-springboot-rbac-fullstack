package com.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rbac.common.Constants;
import com.rbac.dto.MenuDTO;
import com.rbac.entity.SysMenu;
import com.rbac.exception.BusinessException;
import com.rbac.mapper.SysMenuMapper;
import com.rbac.service.MenuService;
import com.rbac.vo.MenuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单管理服务实现
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper menuMapper;

    public MenuServiceImpl(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public List<MenuVO> listTree() {
        List<SysMenu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder));
        return buildTree(convertToVOList(menus), 0L);
    }

    @Override
    public void createMenu(MenuDTO dto) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(dto, menu);
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        menu.setCreateTime(LocalDateTime.now());
        menuMapper.insert(menu);
    }

    @Override
    public void updateMenu(MenuDTO dto) {
        SysMenu menu = menuMapper.selectById(dto.getId());
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        BeanUtils.copyProperties(dto, menu);
        menuMapper.updateById(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        // 检查是否有子菜单
        Long childCount = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，无法删除");
        }
        menuMapper.deleteById(id);
    }

    @Override
    public List<MenuVO> getUserMenuTree(Long userId) {
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        // 只返回目录和菜单（不返回按钮），供前端路由使用
        List<MenuVO> voList = menus.stream()
                .filter(m -> m.getMenuType() != Constants.MENU_TYPE_BUTTON)
                .map(this::toVO)
                .collect(Collectors.toList());
        return buildTree(voList, 0L);
    }

    /** 构建菜单树 */
    private List<MenuVO> buildTree(List<MenuVO> allMenus, Long parentId) {
        return allMenus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .peek(m -> m.setChildren(buildTree(allMenus, m.getId())))
                .collect(Collectors.toList());
    }

    /** 实体列表转VO列表 */
    private List<MenuVO> convertToVOList(List<SysMenu> menus) {
        return menus.stream().map(this::toVO).collect(Collectors.toList());
    }

    /** 实体转VO */
    private MenuVO toVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(menu, vo);
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
