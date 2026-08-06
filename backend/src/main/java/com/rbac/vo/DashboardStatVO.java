package com.rbac.vo;

import lombok.Data;

/**
 * 首页统计数据VO
 */
@Data
public class DashboardStatVO {

    /** 用户总数 */
    private long userCount;

    /** 角色总数 */
    private long roleCount;

    /** 菜单总数 */
    private long menuCount;

    /** 登录日志总数 */
    private long logCount;
}
