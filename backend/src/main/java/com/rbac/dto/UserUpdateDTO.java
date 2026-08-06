package com.rbac.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 修改用户请求DTO
 */
@Data
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String realName;

    private String phone;

    private Integer status;

    /** 角色ID列表 */
    private List<Long> roleIds;
}
