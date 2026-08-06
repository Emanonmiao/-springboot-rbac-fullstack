import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'system/user',
        name: 'User',
        component: () => import('@/views/system/UserManage.vue'),
        meta: { title: '用户管理', perms: 'system:user:list' }
      },
      {
        path: 'system/role',
        name: 'Role',
        component: () => import('@/views/system/RoleManage.vue'),
        meta: { title: '角色管理', perms: 'system:role:list' }
      },
      {
        path: 'system/menu',
        name: 'Menu',
        component: () => import('@/views/system/MenuManage.vue'),
        meta: { title: '菜单管理', perms: 'system:menu:list' }
      },
      {
        path: 'change-password',
        name: 'ChangePassword',
        component: () => import('@/views/ChangePassword.vue'),
        meta: { title: '修改密码' }
      }
    ]
  },
  {
    path: '*',
    redirect: '/login'
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - RBAC管理系统` : 'RBAC管理系统'

  // 不需要认证的页面直接放行
  if (to.meta.noAuth) {
    return next()
  }

  const token = localStorage.getItem('token')
  if (!token) {
    return next('/login')
  }

  // 检查权限
  if (to.meta.perms) {
    const permissions = store.getters.permissions
    if (!permissions.includes(to.meta.perms)) {
      Vue.prototype.$message.error('没有访问权限')
      return next(from.path)
    }
  }

  next()
})

export default router
