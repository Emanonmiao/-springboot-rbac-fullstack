package com.rbac.service;

import com.rbac.dto.ChangePasswordDTO;
import com.rbac.dto.LoginDTO;
import com.rbac.vo.LoginVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginDTO 登录请求
     * @param request  HTTP请求（获取IP）
     * @return 登录信息（含Token）
     */
    LoginVO login(LoginDTO loginDTO, HttpServletRequest request);

    /**
     * 修改密码
     * @param userId   当前用户ID
     * @param dto      修改密码请求
     */
    void changePassword(Long userId, ChangePasswordDTO dto);
}
