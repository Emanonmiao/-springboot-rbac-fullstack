import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    roles: JSON.parse(localStorage.getItem('roles') || '[]'),
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]')
  },
  getters: {
    token: state => state.token,
    userInfo: state => state.userInfo,
    roles: state => state.roles,
    permissions: state => state.permissions,
    isAdmin: state => state.roles.includes('ROLE_ADMIN'),
    // 判断是否有某个权限
    hasPermission: state => perm => state.permissions.includes(perm)
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    SET_ROLES(state, roles) {
      state.roles = roles
      localStorage.setItem('roles', JSON.stringify(roles))
    },
    SET_PERMISSIONS(state, permissions) {
      state.permissions = permissions
      localStorage.setItem('permissions', JSON.stringify(permissions))
    },
    LOGOUT(state) {
      state.token = ''
      state.userInfo = {}
      state.roles = []
      state.permissions = []
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('roles')
      localStorage.removeItem('permissions')
    }
  },
  actions: {
    // 登录后保存数据
    setLoginInfo({ commit }, loginVO) {
      commit('SET_TOKEN', loginVO.token)
      commit('SET_USER_INFO', {
        userId: loginVO.userId,
        username: loginVO.username,
        realName: loginVO.realName
      })
      commit('SET_ROLES', loginVO.roles || [])
      commit('SET_PERMISSIONS', loginVO.permissions || [])
    },
    // 退出登录
    logout({ commit }) {
      commit('LOGOUT')
    }
  }
})
