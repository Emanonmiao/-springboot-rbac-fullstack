package com.rbac.controller;

import com.rbac.common.Result;
import com.rbac.mapper.SysLoginLogMapper;
import com.rbac.mapper.SysMenuMapper;
import com.rbac.mapper.SysRoleMapper;
import com.rbac.mapper.SysUserMapper;
import com.rbac.vo.DashboardStatVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页统计控制器
 */
@Api(tags = "首页统计")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysLoginLogMapper loginLogMapper;

    public DashboardController(SysUserMapper userMapper,
                               SysRoleMapper roleMapper,
                               SysMenuMapper menuMapper,
                               SysLoginLogMapper loginLogMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.loginLogMapper = loginLogMapper;
    }

    @ApiOperation("获取首页统计数据")
    @GetMapping("/stat")
    public Result<DashboardStatVO> stat() {
        DashboardStatVO vo = new DashboardStatVO();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setRoleCount(roleMapper.selectCount(null));
        vo.setMenuCount(menuMapper.selectCount(null));
        vo.setLogCount(loginLogMapper.selectCount(null));
        return Result.ok(vo);
    }
}
