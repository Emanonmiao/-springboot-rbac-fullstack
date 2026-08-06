# SpringBoot-RBAC-Fullstack

基于 **SpringBoot 2.7.18 + Vue2** 实现的 RBAC 权限管理全栈系统，参考等保2.0安全规范，实现弱口令拦截、90天口令有效期、登录失败锁定、登录审计日志等账号安全功能。

---

## 项目亮点

- **等保2.0账号安全**：弱口令黑名单拦截、密码复杂度校验、90天口令有效期、连续5次登录失败锁定账号
- **完整RBAC**：用户-角色-菜单三级权限体系，`@PreAuthorize` 注解细粒度鉴权
- **JWT无状态认证**：SpringSecurity5.7 + JJWT 0.11.5，token存入请求头
- **登录审计日志**：记录每次登录的IP、状态、失败原因
- **单元测试**：JUnit5 + Mockito 对密码工具类和登录逻辑编写测试用例
- **前后端联调**：Vue2 + Element-UI，路由权限控制，按钮级权限，响应拦截处理各种异常状态

---

## 技术栈

| 分层       | 技术                               | 版本        |
| ---------- | ---------------------------------- | ----------- |
| 后端框架   | Spring Boot                        | **2.7.18**  |
| 安全认证   | Spring Security + JJWT             | 5.7 / 0.11.5|
| 数据库     | MySQL                              | **8.0+**    |
| ORM        | MyBatis-Plus                       | **3.5.3.2** |
| 接口文档   | Knife4j (OpenAPI2)                 | **4.1.0**   |
| 单元测试   | JUnit5 + Mockito                   | 随Spring Boot|
| 前端框架   | Vue2 + Vue-Router3 + Vuex3         | 2.6.14      |
| UI组件库   | Element-UI                         | 2.15.13     |
| HTTP请求   | Axios                              | 1.4.0       |
| 构建工具   | Maven（后端）/ Vue CLI（前端）      | -           |

---

## 环境要求

| 环境         | 要求         |
| ------------ | ------------ |
| JDK          | **1.8**      |
| MySQL        | 8.0+         |
| Maven        | 3.6+         |
| Node.js      | 14.x ~ 18.x  |
| npm          | 6+           |

---

## 项目结构

```
springboot-rbac-fullstack/
├── backend/                        # SpringBoot 后端
│   ├── src/
│   │   ├── main/java/com/rbac/
│   │   │   ├── config/             # Security、JWT、MyBatisPlus配置
│   │   │   ├── controller/         # 接口层（仅参数接收）
│   │   │   ├── service/            # 业务层接口
│   │   │   │   └── impl/           # 业务层实现
│   │   │   ├── mapper/             # MyBatis-Plus Mapper
│   │   │   ├── entity/             # 数据库实体
│   │   │   ├── dto/                # 请求入参DTO
│   │   │   ├── vo/                 # 返回VO（不含密码）
│   │   │   ├── util/               # JwtUtil、PwdSecurityUtil
│   │   │   ├── exception/          # BusinessException、GlobalExceptionHandler
│   │   │   ├── common/             # Result、ResultCode、Constants
│   │   │   └── security/           # JWT过滤器、LoginUser、UserDetailsService
│   │   └── resources/
│   │       ├── mapper/             # XML映射文件
│   │       └── application.yml     # 配置文件（此文件不提交，见example）
│   ├── sql/
│   │   └── init.sql                # PostgreSQL建表+初始数据脚本
│   ├── application-dev-example.yml # 配置文件示例
│   ├── pom.xml
│   └── .gitignore
├── web-vue/                        # Vue2 前端
│   ├── src/
│   │   ├── api/                    # axios封装 + 接口API
│   │   ├── layout/                 # 布局组件（侧边栏+顶部导航）
│   │   ├── router/                 # 路由（含权限守卫）
│   │   ├── store/                  # Vuex状态管理
│   │   └── views/                  # 页面组件
│   │       ├── Login.vue
│   │       ├── Dashboard.vue
│   │       ├── ChangePassword.vue
│   │       └── system/
│   │           ├── UserManage.vue
│   │           ├── RoleManage.vue
│   │           └── MenuManage.vue
│   ├── package.json
│   ├── vue.config.js
│   └── .gitignore
└── README.md
```

---

## 数据库初始化

### 1. 创建数据库

在 Navicat 中连接到 MySQL，新建查询执行：

```sql
CREATE DATABASE IF NOT EXISTS rbac_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 2. 执行初始化脚本

在 Navicat 中选择 `rbac_db` 数据库，打开并执行 `backend/sql/init.sql`，该脚本会自动建库建表并插入角色、菜单等初始数据。

### 3. 插入管理员用户

由于密码需要 BCrypt 加密，请**先启动后端应用**，然后在 Navicat 的 SQL 编辑器中执行：

```sql
-- 密码 Admin@2024 的 BCrypt 密文，可通过后端日志或工具生成
INSERT INTO sys_user (username, password, real_name, status, pwd_expire_time, pwd_wrong_count, create_time, update_time)
VALUES (
  'admin',
  '$2a$10$N.gV8KsZeqXRkV7xk3kQl.i95iLuL9X.hV5r9k3oHmkKTYd9oT3MK',
  '超级管理员',
  1,
  DATE_ADD(NOW(), INTERVAL 90 DAY),
  0,
  NOW(),
  NOW()
);
-- 绑定管理员角色（role_id=1 为超级管理员）
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
```

> **提示**：上述 BCrypt 密文对应密码 `Admin@2024`。如需自行生成，可在后端 test 中运行：
> ```java
> System.out.println(new BCryptPasswordEncoder().encode("Admin@2024"));
> ```

---

## 后端启动步骤

### 1. 拷贝配置文件

```bash
# 进入后端目录
cd backend

# 复制示例配置文件为实际配置文件
cp application-dev-example.yml src/main/resources/application.yml
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，修改以下数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rbac_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 你的MySQL密码
```

### 3. 编译运行

```bash
# Maven编译并运行
mvn spring-boot:run

# 或者打包后运行
mvn clean package -DskipTests
java -jar target/springboot-rbac-fullstack-1.0.0.jar
```

### 4. 访问接口文档

启动后访问 Knife4j 接口文档：

```
http://localhost:8080/doc.html
```

---

## 前端启动步骤

```bash
# 进入前端目录
cd web-vue

# 安装依赖（国内可使用淘宝镜像）
npm install
# 或
npm install --registry=https://registry.npmmirror.com

# 启动开发服务器
npm run serve
```

启动后访问：

```
http://localhost:3000
```

---

## 系统默认账号

| 账号  | 密码        | 角色       |
| ----- | ----------- | ---------- |
| admin | Admin@2024  | 超级管理员 |

---

## 核心接口说明

| 方法   | 接口路径                  | 权限               | 说明         |
| ------ | ------------------------- | ------------------ | ------------ |
| POST   | /api/auth/login            | 无需认证           | 用户登录     |
| PUT    | /api/auth/password         | 已登录             | 修改密码     |
| GET    | /api/user/page             | system:user:list   | 用户分页查询 |
| POST   | /api/user                  | system:user:add    | 新增用户     |
| PUT    | /api/user                  | system:user:edit   | 修改用户     |
| DELETE | /api/user/{id}             | system:user:delete | 删除用户     |
| PUT    | /api/user/unlock/{id}      | system:user:unlock | 解锁用户     |
| GET    | /api/role/list             | system:role:list   | 查询角色列表 |
| POST   | /api/role                  | system:role:add    | 新增角色     |
| PUT    | /api/role                  | system:role:edit   | 修改角色     |
| DELETE | /api/role/{id}             | system:role:delete | 删除角色     |
| GET    | /api/menu/tree             | system:menu:list   | 菜单树查询   |
| POST   | /api/menu                  | system:menu:add    | 新增菜单     |

---

## 账号安全说明（等保2.0参考实现）

| 安全特性         | 实现位置                               | 规则                          |
| ---------------- | -------------------------------------- | ----------------------------- |
| 密码BCrypt加密   | `UserServiceImpl`、`AuthServiceImpl`   | 禁止明文入库                  |
| 弱口令拦截       | `PwdSecurityUtil.validatePassword()`   | 内置20个弱口令黑名单           |
| 密码复杂度       | `PwdSecurityUtil.validatePassword()`   | 至少两类字符（大/小写/数字/特殊）|
| 密码有效期       | 新建/修改密码时写入 `pwd_expire_time`  | 90天，过期强制跳转修改密码     |
| 登录失败锁定     | `AuthServiceImpl.handleWrongPassword()`| 连续5次失败，账号锁定          |
| 登录审计日志     | `AuthServiceImpl.recordLoginLog()`     | 记录IP、状态、失败原因         |
| JWT无状态认证    | `JwtAuthenticationFilter`             | 每次请求验证token              |

---

## Git提交规范示例

```
【新增功能】实现用户登录接口及JWT认证
【新增功能】实现RBAC角色权限控制
【新增功能】实现弱口令拦截和90天口令有效期
【bug修复】修复密码错误次数未重置问题
【代码优化】UserServiceImpl密码加密逻辑优化
【测试】添加PwdSecurityUtil单元测试用例
```

---

## 注意事项

1. **真实配置文件不要提交**：`application.yml` 已在 `.gitignore` 中屏蔽，只提交 `application-dev-example.yml`
2. **密码从不返回前端**：所有 VO 类均不包含 `password` 字段
3. **数据库驱动**：本项目使用 **MySQL 8.0**，驱动为 `mysql-connector-java`，不支持 PostgreSQL，请勿替换驱动
4. **JDK版本**：必须使用 JDK 8，不兼容 JDK 17+（SpringBoot3需要JDK17）
5. **Node版本**：推荐使用 Node.js 16.x，高版本可能有兼容问题

---

## 简历项目描述参考

> 基于 SpringBoot2.7 + Vue2 实现 RBAC 权限管理全栈系统，参考等保2.0标准实现弱口令拦截（内置黑名单20条）、90天口令有效期、登录连续5次失败锁定、登录审计日志等账号安全机制；使用 SpringSecurity5.7 + JJWT 0.11.5 实现 JWT 无状态登录认证，`@PreAuthorize` 注解实现接口级权限控制；使用 JUnit5 + Mockito 编写密码工具类和登录逻辑单元测试；前端 Vue2 + Element-UI 实现路由权限守卫和按钮级权限控制。
