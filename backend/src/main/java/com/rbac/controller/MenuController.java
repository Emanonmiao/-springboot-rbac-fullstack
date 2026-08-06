package com.rbac.controller;

import com.rbac.common.Result;
import com.rbac.dto.MenuDTO;
import com.rbac.service.MenuService;
import com.rbac.vo.MenuVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 菜单管理控制器
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @ApiOperation("查询全部菜单（树形）")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<MenuVO>> tree() {
        return Result.ok(menuService.listTree());
    }

    @ApiOperation("新增菜单")
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<?> create(@Valid @RequestBody MenuDTO dto) {
        menuService.createMenu(dto);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改菜单")
    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<?> update(@Valid @RequestBody MenuDTO dto) {
        menuService.updateMenu(dto);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public Result<?> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.ok("删除成功");
    }
}
