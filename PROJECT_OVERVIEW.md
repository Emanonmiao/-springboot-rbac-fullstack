# SpringBoot-RBAC-Fullstack 项目技术介绍

> 文档版本：1.0.0 · 适用于项目代码快照（MySQL 8.0 版本）

---

## 一、项目概述

`springboot-rbac-fullstack` 是一个**前后端分离的企业级权限管理全栈项目**，以 RBAC（Role-Based Access Control）模型为核心，参照**等保2.0**安全规范实现账号安全体系。项目可直接作为中小型后台管理系统的权限模块脚手架，也可作为秋招 GitHub 展示项目。

- **后端**：SpringBoot 2.7.18 + Spring Security 5.7 + MyBatis-Plus 3.5.3.2，提供 RESTful API
- **前端**：Vue 2.6 + Element-UI 2.15 + Vuex + Vue Router，实现路由/按钮级权限控制
- **数据库**：MySQL 8.0，utf8mb4 字符集
- **认证方式**：JWT 无状态令牌（JJWT 0.11.5 / HMAC-SHA256）

---

## 二、仓库目录结构

```
springboot-rbac-fullstack/
├── README.md                          # 快速上手文档
├── PROJECT_OVERVIEW.md                # 本项目技术介绍（当前文件）
│
├── backend/                           # ── 后端模块（SpringBoot） ──
│   ├── pom.xml                        # Maven 依赖，所有版本均已锁定
│   ├── application-dev-example.yml    # 配置文件示例（不含真实密码）
│   ├── .gitignore                     # 屏蔽 application.yml 等敏感文件
│   ├── sql/
│   │   └── init.sql                   # MySQL 建表 + 初始数据脚本
│   └── src/
│       ├── main/java/com/rbac/
│       │   ├── RbacApplication.java           # 启动类 + @MapperScan
│       │   ├── common/
│       │   │   ├── Result.java                # 统一响应体 {code, msg, data}
│       │   │   ├── ResultCode.java            # HTTP 状态码枚举
│       │   │   └── Constants.java             # 系统常量（状态值、阈值等）
│       │   ├── config/
│       │   │   ├── SecurityConfig.java        # Security 过滤链 + BCrypt Bean
│       │   │   ├── JwtConfig.java             # JWT 参数注入
│       │   │   ├── MybatisPlusConfig.java     # 分页插件（MySQL）
│       │   │   └── WebMvcConfig.java          # 跨域 CORS 配置
│       │   ├── security/
│       │   │   ├── JwtAuthenticationFilter.java   # OncePerRequestFilter，解析 Token
│       │   │   ├── LoginUser.java                 # UserDetails 实现，持有角色+权限集合
│       │   │   └── SecurityUserDetailsServiceImpl.java  # UserDetailsService 实现
│       │   ├── controller/
│       │   │   ├── AuthController.java        # /api/auth/login  /api/auth/password
│       │   │   ├── UserController.java        # /api/user/**
│       │   │   ├── RoleController.java        # /api/role/**
│       │   │   └── MenuController.java        # /api/menu/**
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── UserService.java
│       │   │   ├── RoleService.java
│       │   │   ├── MenuService.java
│       │   │   └── impl/
│       │   │       ├── AuthServiceImpl.java   # 核心登录逻辑（等保安全全流程）
│       │   │       ├── UserServiceImpl.java
│       │   │       ├── RoleServiceImpl.java
│       │   │       └── MenuServiceImpl.java
│       │   ├── mapper/
│       │   │   ├── SysUserMapper.java
│       │   │   ├── SysRoleMapper.java
│       │   │   ├── SysMenuMapper.java
│       │   │   ├── SysUserRoleMapper.java
│       │   │   ├── SysRoleMenuMapper.java
│       │   │   └── SysLoginLogMapper.java
│       │   ├── entity/                        # 与数据库表一一对应的 PO 实体
│       │   ├── dto/                           # 请求入参（带 @Valid 校验注解）
│       │   ├── vo/                            # 返回出参（所有 VO 均不含 password）
│       │   ├── util/
│       │   │   ├── JwtUtil.java               # Token 生成 / 解析 / 刷新
│       │   │   └── PwdSecurityUtil.java       # 等保2.0 密码安全工具类
│       │   └── exception/
│       │       ├── BusinessException.java     # 自定义业务异常
│       │       └── GlobalExceptionHandler.java  # @RestControllerAdvice 全局处理
│       ├── main/resources/
│       │   └── mapper/SysMenuMapper.xml       # 菜单树递归自定义 SQL
│       └── test/java/com/rbac/
│           ├── util/PwdSecurityUtilTest.java  # 13 个 JUnit5 单元测试
│           └── service/AuthServiceTest.java   # 6 个 Mockito 集成测试
│
└── web-vue/                           # ── 前端模块（Vue 2） ──
    ├── package.json
    ├── vue.config.js                  # devServer 代理到后端 :8080
    ├── .gitignore
    ├── public/index.html
    └── src/
        ├── main.js                    # Vue 实例 + Element-UI 全局注册
        ├── App.vue
        ├── api/
        │   ├── request.js             # Axios 实例 + 请求/响应拦截器
        │   └── index.js               # 所有业务接口封装
        ├── router/index.js            # 路由表 + 全局路由守卫（权限控制）
        ├── store/index.js             # Vuex：token / userInfo / roles / perms
        ├── layout/Layout.vue          # 整体布局：侧边菜单 + 顶部导航
        └── views/
            ├── Login.vue              # 登录页
            ├── Dashboard.vue          # 首页统计卡片
            ├── ChangePassword.vue     # 修改密码（密码过期自动跳转）
            └── system/
                ├── UserManage.vue     # 用户管理（分页、新增、编辑、解锁）
                ├── RoleManage.vue     # 角色管理（CRUD + 菜单权限分配树）
                └── MenuManage.vue     # 菜单管理（树形展示）
```

---

## 三、技术选型一览

| 维度       | 技术 / 框架                    | 版本           | 说明                                 |
| ---------- | ------------------------------ | -------------- | ------------------------------------ |
| 后端基础   | Spring Boot                    | **2.7.18**     | 禁止升级至 SpringBoot 3              |
| 安全框架   | Spring Security                | **5.7**        | 组件化 FilterChain，不使用已废弃 Adapter |
| JWT 库     | JJWT (jjwt-api / impl / jackson) | **0.11.5**   | 使用新版 `parserBuilder()` API       |
| ORM        | MyBatis-Plus                   | **3.5.3.2**    | LambdaQueryWrapper + 分页插件        |
| 接口文档   | Knife4j (OpenAPI2)             | **4.1.0**      | 访问 `/doc.html`                     |
| 参数校验   | spring-boot-starter-validation | 随 Boot        | `@Valid` + `@NotBlank` 等            |
| 数据库驱动 | mysql-connector-java           | 8.0.x (BOM)    | MySQL 8.0+，utf8mb4                  |
| 单元测试   | JUnit 5 + Mockito              | 随 Boot        | `@ExtendWith(MockitoExtension.class)` |
| 前端框架   | Vue 2 + Vue Router 3 + Vuex 3  | 2.6.14         | 不使用 Vue 3 / Composition API       |
| UI 组件库  | Element-UI                     | **2.15.13**    |                                      |
| HTTP 客户端 | Axios                         | 1.4.0          | 请求/响应双拦截器                    |
| 构建工具   | Maven（后端） / Vue CLI（前端） | 3.6+ / 5.0.8  |                                      |
| JDK        | JDK 8                          | 1.8            | 不兼容 JDK 17+                       |

---

## 四、数据库设计（6 张核心表）

```
rbac_db
├── sys_user          用户表（账号安全字段：status / pwd_expire_time / pwd_wrong_count）
├── sys_role          角色表（role_code 唯一索引）
├── sys_user_role     用户-角色 多对多关联
├── sys_menu          菜单权限表（支持 目录/菜单/按钮 三类，树形结构）
├── sys_role_menu     角色-菜单 多对多关联
└── sys_login_log     登录审计日志（IP / 状态 / 失败原因 / 时间）
```

### 关键字段设计（sys_user）

| 字段名           | 类型      | 说明                                   |
| ---------------- | --------- | -------------------------------------- |
| `status`         | INT       | 0 = 锁定，1 = 正常                     |
| `pwd_expire_time`| DATETIME  | 密码过期时间，新建/改密时写入 NOW()+90d |
| `pwd_wrong_count`| INT       | 连续错误次数，登录成功后重置为 0        |
| `password`       | VARCHAR   | BCrypt 加密后密文，禁止明文入库        |

---

## 五、核心模块详解

### 5.1 认证流程（登录）

```
POST /api/auth/login  (username + password)
         │
         ▼
AuthServiceImpl.login()
   ① 查询用户是否存在                          → 不存在：返回"用户名或密码错误"
   ② 判断账号是否已锁定（status == 0）         → 锁定：返回 423 状态码
   ③ 调用 Spring Security AuthenticationManager
      ├─ 密码正确 → 继续
      └─ 密码错误 → pwd_wrong_count +1
                    ├─ < 5次：返回"密码错误，剩余N次"
                    └─ ≥ 5次：status 置 0（锁定），返回 423
   ④ 登录成功：pwd_wrong_count 重置为 0
   ⑤ 判断密码是否过期（pwd_expire_time < NOW()）
      └─ 过期：pwdExpired = true，返回 428 状态码（前端跳转改密页）
   ⑥ 生成 JWT Token（HMAC-SHA256，有效期 24h）
   ⑦ 记录登录审计日志（IP / 状态 / 原因）
   ⑧ 返回 { token, userInfo, roles, permissions }
```

### 5.2 鉴权流程（每次请求）

```
HTTP Request  →  JwtAuthenticationFilter（OncePerRequestFilter）
                    │
                    ├─ 提取 Authorization: Bearer <token>
                    ├─ JwtUtil 解析并验证签名 + 过期时间
                    ├─ 从 token subject 中取 userId
                    ├─ 加载 LoginUser（含角色 + 权限集合）
                    └─ 注入 SecurityContextHolder
                    
Controller 方法上  →  @PreAuthorize("hasAuthority('system:user:add')")
                         └─ Spring Security 自动比对 LoginUser.authorities
```

### 5.3 密码安全工具类（PwdSecurityUtil）

等保2.0 三重校验，全为静态方法，任一不通过即抛出 `BusinessException`：

| 规则             | 实现逻辑                                            |
| ---------------- | --------------------------------------------------- |
| 最小长度 8 位    | `password.length() < 8`                             |
| 复杂度 ≥ 2 类    | 统计大写字母/小写字母/数字/特殊符号的种类数，< 2 拒绝 |
| 弱口令黑名单     | 内置 20 个常见弱口令（忽略大小写比对）               |

### 5.4 RBAC 权限模型

```
用户 (sys_user)
    └── N:M ──▶  角色 (sys_role)        通过 sys_user_role
                    └── N:M ──▶  菜单 (sys_menu)    通过 sys_role_menu
                                    └── perms 字段 = 权限标识
                                         如 system:user:add
```

菜单表的 `menu_type` 字段区分三类节点：
- `0`：目录（前端路由 Layout 层）
- `1`：菜单（前端页面路由）
- `2`：按钮（仅用于接口鉴权，前端按钮显隐）

### 5.5 统一响应体

```java
// 所有接口均返回此结构
Result<T> {
    int    code;    // 200=成功, 401=未认证, 403=无权限, 423=账号锁定,
                    // 428=密码过期, 500=系统错误, ...（见 ResultCode 枚举）
    String msg;
    T      data;
}
```

---

## 六、前端核心设计

### 6.1 Axios 拦截器（request.js）

```
请求拦截：自动在 Header 注入 Authorization: Bearer <token>（来自 localStorage）

响应拦截：
    ├─ code === 200 → 正常返回 data
    ├─ code === 401 → 清除 Vuex + localStorage，跳转 /login
    ├─ code === 423 → Message.error('账号已被锁定，请联系管理员')
    ├─ code === 428 → router.push('/change-password')（密码过期强制修改）
    └─ 其他错误   → 弹出 Message.error(msg)
```

### 6.2 路由守卫（router/index.js）

```
router.beforeEach：
    ├─ 无 token → 非白名单页面一律跳转 /login
    ├─ 有 token → 检查目标路由的 meta.perms
    │               ├─ 无 perms（首页/个人中心等）→ 放行
    │               └─ 有 perms → 检查 store.getters.permissions 是否包含
    │                               └─ 不包含 → 跳转 403 页或首页
    └─ 路由 /login + 已登录 → 跳转首页
```

### 6.3 Vuex 状态管理（store/index.js）

| state 字段    | 类型     | 说明                              |
| ------------- | -------- | --------------------------------- |
| `token`       | String   | JWT Token，同步写 localStorage    |
| `userInfo`    | Object   | 当前用户基本信息                  |
| `roles`       | Array    | 角色编码列表，如 `['ROLE_ADMIN']` |
| `permissions` | Array    | 权限标识列表，如 `['system:user:add']` |

---

## 七、接口一览

| 方法   | 路径                       | 所需权限                | 说明           |
| ------ | -------------------------- | ----------------------- | -------------- |
| POST   | `/api/auth/login`          | 无需认证                | 用户登录       |
| PUT    | `/api/auth/password`       | 已登录                  | 修改密码       |
| GET    | `/api/user/page`           | `system:user:list`      | 用户分页查询   |
| POST   | `/api/user`                | `system:user:add`       | 新增用户       |
| PUT    | `/api/user`                | `system:user:edit`      | 修改用户       |
| DELETE | `/api/user/{id}`           | `system:user:delete`    | 删除用户       |
| PUT    | `/api/user/unlock/{id}`    | `system:user:unlock`    | 解锁账号       |
| GET    | `/api/role/list`           | `system:role:list`      | 角色列表       |
| POST   | `/api/role`                | `system:role:add`       | 新增角色       |
| PUT    | `/api/role`                | `system:role:edit`      | 修改角色（含菜单分配）|
| DELETE | `/api/role/{id}`           | `system:role:delete`    | 删除角色       |
| GET    | `/api/menu/tree`           | `system:menu:list`      | 菜单树         |
| POST   | `/api/menu`                | `system:menu:add`       | 新增菜单       |
| PUT    | `/api/menu`                | `system:menu:edit`      | 修改菜单       |
| DELETE | `/api/menu/{id}`           | `system:menu:delete`    | 删除菜单       |

> 完整文档访问：`http://localhost:8080/doc.html`（Knife4j）

---

## 八、单元测试覆盖

### PwdSecurityUtilTest（13 个用例）

| 测试方法                     | 验证内容                              |
| ---------------------------- | ------------------------------------- |
| `testValidPassword`          | 合法密码通过校验                      |
| `testTooShort`               | 长度 < 8 抛出异常                     |
| `testOnlyDigits`             | 单一类型（纯数字）不满足复杂度        |
| `testWeakQwe123456`（参数化）| qwe123456 / Qwe123456 / QWE123456 均被黑名单拦截 |
| `testWeakAdmin123`           | admin123 被拦截                       |
| `testComplexityTwoTypes`     | 两类字符通过                          |
| `testComplexityThreeTypes`   | 三类字符通过                          |
| ... 等                       |                                       |

### AuthServiceTest（6 个用例）

| 测试方法                          | 验证内容                                   |
| --------------------------------- | ------------------------------------------ |
| `testLoginSuccess`                | 正常登录，返回 token，错误次数清零         |
| `testUserNotFound`                | 用户不存在，返回业务异常                   |
| `testLoginLockedAccount`          | 账号已锁定，返回 423                       |
| `testWrongPasswordIncrement`      | 密码错误，`pwd_wrong_count` +1             |
| `testLoginLockedAfter5Wrong`      | 第 5 次密码错误，`status` 置 0（锁定）    |
| `testPasswordExpired`             | 登录成功但密码已过期，响应包含过期标志     |

---

## 九、安全规范说明（等保2.0 对照）

| 等保要求                      | 本项目实现                                  | 代码位置                             |
| ----------------------------- | ------------------------------------------- | ------------------------------------ |
| 口令加密存储                  | BCryptPasswordEncoder，Salt 随机，不可逆    | `SecurityConfig` + `UserServiceImpl` |
| 口令复杂度                    | ≥8位，大/小/数/特殊符号至少两类             | `PwdSecurityUtil.validatePassword()` |
| 弱口令拦截                    | 内置 20 个黑名单，忽略大小写比对            | `PwdSecurityUtil.WEAK_PASSWORDS`     |
| 口令有效期                    | 90 天，过期跳转强制修改                     | `AuthServiceImpl` + 前端 428 拦截    |
| 登录失败锁定                  | 连续 5 次失败，账号 status 置 0             | `AuthServiceImpl.handleWrongPassword()` |
| 登录审计                      | 记录 IP、状态、原因到 sys_login_log         | `AuthServiceImpl.recordLoginLog()`   |
| 密码禁止明文返回              | 所有 VO 类不含 password 字段                | `UserVO` 等全部 VO                   |

---

## 十、快速启动

```bash
# 1. 建库并执行初始化脚本（MySQL 8.0）
#    backend/sql/init.sql

# 2. 后端配置
cd backend
cp application-dev-example.yml src/main/resources/application.yml
# 编辑 application.yml，填入 MySQL 密码

# 3. 启动后端
mvn spring-boot:run
# 接口文档：http://localhost:8080/doc.html

# 4. 启动前端
cd web-vue
npm install
npm run serve
# 访问：http://localhost:3000

# 5. 默认账号
# admin / Admin@2024（超级管理员）
```

---

## 十一、注意事项

1. **JDK 版本**：必须使用 **JDK 8**，不支持 JDK 17+（SpringBoot 3 才支持 JDK 17）
2. **SpringBoot 版本**：锁定 **2.7.18**，禁止随意升级
3. **配置文件安全**：`application.yml` 已加入 `.gitignore`，提交仓库时只保留 `application-dev-example.yml`
4. **密码安全**：密码字段仅落库，全链路禁止返回前端
5. **Node 版本**：推荐 Node.js 14.x ~ 18.x，过高版本可能与 Vue CLI 5 存在兼容问题
6. **数据库字符集**：必须使用 `utf8mb4 + utf8mb4_unicode_ci`，避免 Emoji 等特殊字符问题

---

*文档由项目自动生成，最后更新：项目 MySQL 迁移版本*
