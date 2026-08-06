package com.rbac.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rbac.entity.SysMenu;
import com.rbac.entity.SysRole;
import com.rbac.entity.SysUser;
import com.rbac.entity.SysUserRole;
import com.rbac.mapper.SysMenuMapper;
import com.rbac.mapper.SysRoleMapper;
import com.rbac.mapper.SysUserMapper;
import com.rbac.mapper.SysUserRoleMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetailsService实现
 * 从数据库加载用户、角色、权限信息
 */
@Service
public class SecurityUserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    public SecurityUserDetailsServiceImpl(SysUserMapper userMapper,
                                          SysUserRoleMapper userRoleMapper,
                                          SysRoleMapper roleMapper,
                                          SysMenuMapper menuMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 2. 查询用户角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        Set<String> roleCodes = new HashSet<>();
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
            roleCodes = roles.stream()
                    .map(SysRole::getRoleCode).collect(Collectors.toSet());
        }

        // 3. 查询用户权限（菜单perms）
        Set<String> permissions = new HashSet<>();
        if (!roleCodes.isEmpty()) {
            List<Long> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysMenu> menus = menuMapper.selectMenusByRoleIds(roleIds);
            permissions = menus.stream()
                    .filter(m -> m.getPerms() != null && !m.getPerms().isEmpty())
                    .map(SysMenu::getPerms)
                    .collect(Collectors.toSet());
        }

        return new LoginUser(user, roleCodes, permissions);
    }
}
