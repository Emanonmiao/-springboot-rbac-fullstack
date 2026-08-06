-- ====================================================
-- RBAC权限管理系统 - MySQL 8.0 数据库初始化脚本
-- 适用于 MySQL 8.0+，使用Navicat执行本脚本
-- 字符集 utf8mb4，排序规则 utf8mb4_unicode_ci
-- ====================================================

CREATE DATABASE IF NOT EXISTS rbac_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE rbac_db;

-- ====================================================
-- 建表
-- ====================================================

-- 1. 用户表
CREATE TABLE sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(64)  NOT NULL COMMENT '用户名',
    password      VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码，禁止明文',
    real_name     VARCHAR(64)  DEFAULT NULL COMMENT '真实姓名',
    phone         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    status        INT          NOT NULL DEFAULT 1 COMMENT '0-锁定 1-正常',
    pwd_expire_time DATETIME   DEFAULT NULL COMMENT '密码过期时间（90天有效期）',
    pwd_wrong_count INT        NOT NULL DEFAULT 0 COMMENT '连续密码错误次数，5次锁定',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 角色表
CREATE TABLE sys_role (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_name   VARCHAR(64) NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(64) NOT NULL COMMENT '角色编码',
    remark      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 3. 用户-角色关联表（多对多）
CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 4. 菜单权限表
CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID，0为顶级',
    menu_name   VARCHAR(64)  NOT NULL COMMENT '菜单名称',
    menu_type   INT          NOT NULL DEFAULT 0 COMMENT '0-目录 1-菜单 2-按钮',
    path        VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
    component   VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径',
    icon        VARCHAR(64)  DEFAULT NULL COMMENT '图标',
    perms       VARCHAR(128) DEFAULT NULL COMMENT '权限标识，如 system:user:add',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      INT          NOT NULL DEFAULT 1 COMMENT '0-禁用 1-正常',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';

-- 5. 角色-菜单权限关联表
CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 6. 登录审计日志表
CREATE TABLE sys_login_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username     VARCHAR(64)  DEFAULT NULL COMMENT '用户名',
    login_ip     VARCHAR(64)  DEFAULT NULL COMMENT '登录IP',
    login_status INT          NOT NULL DEFAULT 0 COMMENT '0-失败 1-成功',
    msg          VARCHAR(255) DEFAULT NULL COMMENT '提示信息',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_username (username),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录审计日志表';

-- ====================================================
-- 初始化数据
-- ====================================================

-- 角色数据
INSERT INTO sys_role (role_name, role_code, remark) VALUES
('超级管理员', 'ROLE_ADMIN', '拥有系统全部权限'),
('普通用户',   'ROLE_USER',  '普通操作用户');

-- 菜单数据 —— 一级目录
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, perms, sort_order) VALUES
(1, 0, '系统管理', 0, '/system', 'Layout', 'el-icon-setting', NULL, 1);

-- 菜单数据 —— 二级菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, perms, sort_order) VALUES
(2, 1, '用户管理', 1, 'user', 'system/user/index', 'el-icon-user',     'system:user:list', 1),
(3, 1, '角色管理', 1, 'role', 'system/role/index', 'el-icon-s-custom', 'system:role:list', 2),
(4, 1, '菜单管理', 1, 'menu', 'system/menu/index', 'el-icon-menu',     'system:menu:list', 3);

-- 三级按钮权限 —— 用户管理
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort_order) VALUES
(5,  2, '用户新增', 2, 'system:user:add',    1),
(6,  2, '用户修改', 2, 'system:user:edit',   2),
(7,  2, '用户删除', 2, 'system:user:delete', 3),
(8,  2, '用户解锁', 2, 'system:user:unlock', 4);

-- 三级按钮权限 —— 角色管理
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort_order) VALUES
(9,  3, '角色新增', 2, 'system:role:add',    1),
(10, 3, '角色修改', 2, 'system:role:edit',   2),
(11, 3, '角色删除', 2, 'system:role:delete', 3);

-- 三级按钮权限 —— 菜单管理
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort_order) VALUES
(12, 4, '菜单新增', 2, 'system:menu:add',    1),
(13, 4, '菜单修改', 2, 'system:menu:edit',   2),
(14, 4, '菜单删除', 2, 'system:menu:delete', 3);

-- 重置AUTO_INCREMENT，确保后续插入从15开始
ALTER TABLE sys_menu AUTO_INCREMENT = 15;

-- 管理员角色关联（user_id=1 -> role_id=1 超级管理员）
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 超级管理员分配全部菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 普通用户只分配查看权限（用户列表、角色列表、菜单列表）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 2), (2, 3), (2, 4);

-- ====================================================
-- 注意事项：
-- admin用户的密码为 Admin@2024（BCrypt加密后的密文）
-- 请在Java启动后手动执行以下SQL插入管理员账号：
-- INSERT INTO sys_user (username, password, real_name, status, pwd_expire_time)
-- VALUES ('admin', '$2a$10$BCrypt密文', '超级管理员', 1,
--         DATE_ADD(NOW(), INTERVAL 90 DAY));
-- ====================================================
