package com.rbac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rbac.common.Result;
import com.rbac.dto.UserCreateDTO;
import com.rbac.dto.UserUpdateDTO;
import com.rbac.security.LoginUser;
import com.rbac.service.MenuService;
import com.rbac.service.UserService;
import com.rbac.vo.MenuVO;
import com.rbac.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户管理控制器
 * 仅接收参数，业务逻辑下沉Service
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final MenuService menuService;

    public UserController(UserService userService, MenuService menuService) {
        this.userService = userService;
        this.menuService = menuService;
    }

    @ApiOperation("分页查询用户")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<Page<UserVO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(required = false) String username) {
        return Result.ok(userService.pageUsers(pageNum, pageSize, username));
    }

    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/info")
    public Result<UserVO> currentUser(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.ok(userService.getUserById(loginUser.getUserId()));
    }

    @ApiOperation("获取当前用户菜单树（前端路由）")
    @GetMapping("/menus")
    public Result<List<MenuVO>> currentUserMenus(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.ok(menuService.getUserMenuTree(loginUser.getUserId()));
    }

    @ApiOperation("新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<?> create(@Valid @RequestBody UserCreateDTO dto) {
        userService.createUser(dto);
        return Result.ok("新增成功");
    }

    @ApiOperation("修改用户")
    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<?> update(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(dto);
        return Result.ok("修改成功");
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<?> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok("删除成功");
    }

    @ApiOperation("解锁用户")
    @PutMapping("/unlock/{id}")
    @PreAuthorize("hasAuthority('system:user:unlock')")
    public Result<?> unlock(@PathVariable Long id) {
        userService.unlockUser(id);
        return Result.ok("解锁成功");
    }

    @ApiOperation("根据ID查询用户")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.ok(userService.getUserById(id));
    }
}
