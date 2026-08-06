package com.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rbac.common.Constants;
import com.rbac.common.ResultCode;
import com.rbac.dto.ChangePasswordDTO;
import com.rbac.dto.LoginDTO;
import com.rbac.entity.SysLoginLog;
import com.rbac.entity.SysUser;
import com.rbac.exception.BusinessException;
import com.rbac.mapper.SysLoginLogMapper;
import com.rbac.mapper.SysMenuMapper;
import com.rbac.mapper.SysUserMapper;
import com.rbac.security.LoginUser;
import com.rbac.service.AuthService;
import com.rbac.util.JwtUtil;
import com.rbac.util.PwdSecurityUtil;
import com.rbac.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 认证服务实现
 * 包含：登录前置校验、密码错误锁定、登录审计日志、JWT生成、密码过期检测
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final SysUserMapper userMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           SysUserMapper userMapper,
                           SysLoginLogMapper loginLogMapper,
                           JwtUtil jwtUtil,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public LoginVO login(LoginDTO loginDTO, HttpServletRequest request) {
        String username = loginDTO.getUsername();
        String ip = getClientIp(request);

        // 1. 查询用户（前置校验）
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));

        if (user == null) {
            recordLoginLog(username, ip, Constants.LOGIN_FAIL, "用户不存在");
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }

        // 2. 账号锁定判断
        if (user.getStatus() != null && user.getStatus() == Constants.USER_STATUS_LOCKED) {
            recordLoginLog(username, ip, Constants.LOGIN_FAIL, "账号已锁定");
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 3. 尝试认证（Spring Security）
        LoginUser loginUser;
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginDTO.getPassword()));
            loginUser = (LoginUser) authentication.getPrincipal();
        } catch (BadCredentialsException e) {
            // 密码错误：累加错误次数
            handleWrongPassword(user, ip);
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        } catch (LockedException e) {
            recordLoginLog(username, ip, Constants.LOGIN_FAIL, "账号已锁定");
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 4. 登录成功：重置错误计数
        user.setPwdWrongCount(0);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 5. 记录登录成功日志
        recordLoginLog(username, ip, Constants.LOGIN_SUCCESS, "登录成功");

        // 6. 生成JWT Token
        String token = jwtUtil.generateToken(username, user.getId());

        // 7. 检测密码是否过期
        boolean pwdExpired = false;
        if (user.getPwdExpireTime() != null && user.getPwdExpireTime().isBefore(LocalDateTime.now())) {
            pwdExpired = true;
        }

        // 8. 组装返回VO（绝不返回密码）
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoles(loginUser.getRoleCodes());
        vo.setPermissions(loginUser.getPermissions());
        vo.setPwdExpired(pwdExpired);

        return vo;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 校验新密码强度（PwdSecurityUtil）
        PwdSecurityUtil.validatePassword(dto.getNewPassword());

        // 更新密码 + 重置过期时间（当前时间+90天）
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPwdExpireTime(LocalDateTime.now().plusDays(Constants.PWD_EXPIRE_DAYS));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户[{}]修改密码成功", user.getUsername());
    }

    /** 处理密码错误：累加计数，达到5次锁定 */
    private void handleWrongPassword(SysUser user, String ip) {
        int count = (user.getPwdWrongCount() == null ? 0 : user.getPwdWrongCount()) + 1;
        user.setPwdWrongCount(count);
        user.setUpdateTime(LocalDateTime.now());

        if (count >= Constants.MAX_PWD_WRONG_COUNT) {
            // 连续输错5次，锁定账号
            user.setStatus(Constants.USER_STATUS_LOCKED);
            userMapper.updateById(user);
            recordLoginLog(user.getUsername(), ip, Constants.LOGIN_FAIL,
                    "连续输错" + count + "次，账号已锁定");
            log.warn("用户[{}]连续输错{}次密码，账号已锁定", user.getUsername(), count);
        } else {
            userMapper.updateById(user);
            recordLoginLog(user.getUsername(), ip, Constants.LOGIN_FAIL,
                    "密码错误，第" + count + "次");
        }
    }

    /** 记录登录审计日志 */
    private void recordLoginLog(String username, String ip, int status, String msg) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setLoginIp(ip);
        loginLog.setLoginStatus(status);
        loginLog.setMsg(msg);
        loginLog.setCreateTime(LocalDateTime.now());
        loginLogMapper.insert(loginLog);
    }

    /** 获取客户端IP */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
