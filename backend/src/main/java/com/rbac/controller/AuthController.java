package com.rbac.controller;

import com.rbac.common.Result;
import com.rbac.dto.ChangePasswordDTO;
import com.rbac.dto.LoginDTO;
import com.rbac.security.LoginUser;
import com.rbac.service.AuthService;
import com.rbac.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器：登录、修改密码
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO,
                                 HttpServletRequest request) {
        LoginVO vo = authService.login(loginDTO, request);
        return Result.ok(vo);
    }

    @ApiOperation("修改密码")
    @PutMapping("/password")
    public Result<?> changePassword(@AuthenticationPrincipal LoginUser loginUser,
                                    @Valid @RequestBody ChangePasswordDTO dto) {
        authService.changePassword(loginUser.getUserId(), dto);
        return Result.ok("密码修改成功");
    }
}
