import request from './request'

/** 首页统计 */
export const getDashboardStat = () => request.get('/dashboard/stat')

/** 登录 */
export const login = data => request.post('/auth/login', data)

/** 修改密码 */
export const changePassword = data => request.put('/auth/password', data)

/** 获取当前用户信息 */
export const getUserInfo = () => request.get('/user/info')

/** 获取当前用户菜单树 */
export const getUserMenus = () => request.get('/user/menus')

/** 分页查询用户 */
export const pageUsers = params => request.get('/user/page', { params })

/** 新增用户 */
export const createUser = data => request.post('/user', data)

/** 修改用户 */
export const updateUser = data => request.put('/user', data)

/** 删除用户 */
export const deleteUser = id => request.delete(`/user/${id}`)

/** 解锁用户 */
export const unlockUser = id => request.put(`/user/unlock/${id}`)

/** 查询全部角色 */
export const listRoles = () => request.get('/role/list')

/** 新增角色 */
export const createRole = data => request.post('/role', data)

/** 修改角色 */
export const updateRole = data => request.put('/role', data)

/** 删除角色 */
export const deleteRole = id => request.delete(`/role/${id}`)

/** 查询角色菜单ID */
export const getRoleMenuIds = id => request.get(`/role/${id}/menuIds`)

/** 分配角色菜单权限 */
export const assignRoleMenus = (id, menuIds) => request.put(`/role/${id}/menus`, menuIds)

/** 查询全部菜单树 */
export const listMenuTree = () => request.get('/menu/tree')

/** 新增菜单 */
export const createMenu = data => request.post('/menu', data)

/** 修改菜单 */
export const updateMenu = data => request.put('/menu', data)

/** 删除菜单 */
export const deleteMenu = id => request.delete(`/menu/${id}`)
