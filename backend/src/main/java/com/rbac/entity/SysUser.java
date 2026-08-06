package com.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表实体
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt加密密码，禁止明文存储 */
    private String password;

    private String realName;

    private String phone;

    /** 0锁定 1正常 */
    private Integer status;

    /** 密码过期时间（90天有效期） */
    private LocalDateTime pwdExpireTime;

    /** 连续密码错误次数，5次锁定 */
    private Integer pwdWrongCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
