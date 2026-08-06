package com.rbac.controller;

import com.rbac.common.Result;
import com.rbac.dto.RoleDTO;
import com.rbac.entity.SysRole;
import com.rbac.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @ApiOperation("查询全部角色")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<SysRole>> list() {
        return Result.ok(roleService.listAll());
    }

    @ApiOperation("新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<?> create(@Valid @RequestBody RoleDTO dto) {
        roleService.createRole(dto);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改角色")
    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<?> update(@Valid @RequestBody RoleDTO dto) {
        roleService.updateRole(dto);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<?> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("查询角色已分配的菜单ID")
    @GetMapping("/{id}/menuIds")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<Long>> getMenuIds(@PathVariable Long id) {
        return Result.ok(roleService.getRoleMenuIds(id));
    }

    @ApiOperation("分配角色菜单权限")
    @PutMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<?> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.ok("权限分配成功");
    }
}
