package com.rbac.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rbac.dto.UserCreateDTO;
import com.rbac.dto.UserUpdateDTO;
import com.rbac.vo.UserVO;

/**
 * 用户管理服务接口
 */
public interface UserService {

    /** 分页查询用户列表 */
    Page<UserVO> pageUsers(int pageNum, int pageSize, String username);

    /** 新增用户 */
    void createUser(UserCreateDTO dto);

    /** 修改用户 */
    void updateUser(UserUpdateDTO dto);

    /** 删除用户 */
    void deleteUser(Long id);

    /** 解锁用户（重置密码错误次数，恢复状态） */
    void unlockUser(Long id);

    /** 根据ID获取用户VO */
    UserVO getUserById(Long id);
}
