package com.rbac.security;

import com.rbac.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 用户详情对象
 * 包装SysUser实体，实现UserDetails接口
 */
@Data
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    private String password;
    private Integer status;
    /** 角色编码集合，如 ROLE_ADMIN */
    private Set<String> roleCodes;
    /** 权限标识集合，如 system:user:add */
    private Set<String> permissions;

    public LoginUser(SysUser user, Set<String> roleCodes, Set<String> permissions) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.status = user.getStatus();
        this.roleCodes = roleCodes;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 添加角色权限 ROLE_xxx
        if (roleCodes != null) {
            for (String roleCode : roleCodes) {
                authorities.add(new SimpleGrantedAuthority(roleCode));
            }
        }
        // 添加菜单/按钮权限 system:xxx:xxx
        if (permissions != null) {
            for (String perm : permissions) {
                authorities.add(new SimpleGrantedAuthority(perm));
            }
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // status=1正常，0锁定
        return status != null && status == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
