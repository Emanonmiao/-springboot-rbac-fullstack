package com.rbac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rbac.common.Constants;
import com.rbac.common.ResultCode;
import com.rbac.dto.LoginDTO;
import com.rbac.entity.SysLoginLog;
import com.rbac.entity.SysUser;
import com.rbac.exception.BusinessException;
import com.rbac.mapper.SysLoginLogMapper;
import com.rbac.mapper.SysUserMapper;
import com.rbac.security.LoginUser;
import com.rbac.service.impl.AuthServiceImpl;
import com.rbac.util.JwtUtil;
import com.rbac.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthService 登录逻辑单元测试
 * JUnit5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysLoginLogMapper loginLogMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest httpServletRequest;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AuthServiceImpl authService;

    private SysUser testUser;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setPassword(passwordEncoder.encode("Admin@2024"));
        testUser.setRealName("管理员");
        testUser.setStatus(Constants.USER_STATUS_NORMAL);
        testUser.setPwdWrongCount(0);
        testUser.setPwdExpireTime(LocalDateTime.now().plusDays(90));

        loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("Admin@2024");
    }

    @Test
    @DisplayName("登录成功：返回Token和用户信息")
    void testLoginSuccess() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        LoginUser loginUser = new LoginUser(testUser,
                new HashSet<String>() {{ add("ROLE_ADMIN"); }},
                new HashSet<String>() {{ add("system:user:list"); }});

        Authentication auth = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken("admin", 1L)).thenReturn("mock-jwt-token");
        when(userMapper.updateById(any())).thenReturn(1);
        when(loginLogMapper.insert(any(SysLoginLog.class))).thenReturn(1);

        LoginVO result = authService.login(loginDTO, httpServletRequest);

        assertNotNull(result);
        assertEquals("mock-jwt-token", result.getToken());
        assertEquals("admin", result.getUsername());
        assertFalse(result.isPwdExpired());
        verify(loginLogMapper, atLeastOnce()).insert(any(SysLoginLog.class));
    }

    @Test
    @DisplayName("用户不存在：抛出BusinessException")
    void testLoginUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(loginLogMapper.insert(any(SysLoginLog.class))).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(loginDTO, httpServletRequest));
        assertEquals(ResultCode.LOGIN_FAIL.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("账号已锁定：抛出ACCOUNT_LOCKED异常")
    void testLoginAccountLocked() {
        testUser.setStatus(Constants.USER_STATUS_LOCKED);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(loginLogMapper.insert(any(SysLoginLog.class))).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(loginDTO, httpServletRequest));
        assertEquals(ResultCode.ACCOUNT_LOCKED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("密码错误：累加错误次数")
    void testLoginWrongPassword() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("密码错误"));
        when(userMapper.updateById(any())).thenReturn(1);
        when(loginLogMapper.insert(any(SysLoginLog.class))).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(loginDTO, httpServletRequest));
        assertEquals(ResultCode.LOGIN_FAIL.getCode(), ex.getCode());

        // 验证错误次数+1
        assertEquals(1, testUser.getPwdWrongCount());
    }

    @Test
    @DisplayName("连续5次密码错误：账号被锁定")
    void testLoginLockedAfter5WrongAttempts() {
        testUser.setPwdWrongCount(4); // 已经错了4次
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("密码错误"));
        when(userMapper.updateById(any())).thenReturn(1);
        when(loginLogMapper.insert(any(SysLoginLog.class))).thenReturn(1);

        assertThrows(BusinessException.class,
                () -> authService.login(loginDTO, httpServletRequest));

        // 验证账号被锁定
        assertEquals(Constants.USER_STATUS_LOCKED, testUser.getStatus());
        assertEquals(5, testUser.getPwdWrongCount());
    }

    @Test
    @DisplayName("密码过期：登录成功但pwdExpired为true")
    void testLoginPasswordExpired() {
        testUser.setPwdExpireTime(LocalDateTime.now().minusDays(1)); // 已过期
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        LoginUser loginUser = new LoginUser(testUser, new HashSet<>(), new HashSet<>());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateToken("admin", 1L)).thenReturn("mock-token");
        when(userMapper.updateById(any())).thenReturn(1);
        when(loginLogMapper.insert(any(SysLoginLog.class))).thenReturn(1);

        LoginVO result = authService.login(loginDTO, httpServletRequest);

        assertNotNull(result);
        assertTrue(result.isPwdExpired());
    }
}
