<template>
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside :width="collapsed ? '64px' : '200px'" class="sidebar">
      <div class="logo">
        <span v-if="!collapsed">RBAC管理系统</span>
        <span v-else>R</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="collapsed"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <template v-for="menu in menuTree">
          <el-submenu v-if="menu.children && menu.children.length" :key="menu.id" :index="String(menu.id)">
            <template slot="title">
              <i :class="menu.icon || 'el-icon-folder'"></i>
              <span>{{ menu.menuName }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.id"
              :index="'/' + child.path"
            >
              <i :class="child.icon || 'el-icon-document'"></i>
              <span>{{ child.menuName }}</span>
            </el-menu-item>
          </el-submenu>
          <el-menu-item v-else :key="menu.id" :index="menu.path">
            <i :class="menu.icon || 'el-icon-document'"></i>
            <span slot="title">{{ menu.menuName }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <i
            :class="collapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
            class="collapse-btn"
            @click="collapsed = !collapsed"
          ></i>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-name">
              <i class="el-icon-user"></i>
              {{ userInfo.realName || userInfo.username }}
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="changePwd">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { getUserMenus } from '@/api'
import { mapGetters } from 'vuex'

export default {
  name: 'Layout',
  data() {
    return {
      collapsed: false,
      menuTree: []
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  created() {
    this.loadMenus()
  },
  methods: {
    async loadMenus() {
      try {
        const res = await getUserMenus()
        this.menuTree = res.data || []
      } catch (e) {
        console.error('加载菜单失败', e)
      }
    },
    handleCommand(cmd) {
      if (cmd === 'logout') {
        this.$confirm('确定退出登录？', '提示', {
          type: 'warning'
        }).then(() => {
          this.$store.dispatch('logout')
          this.$router.push('/login')
        }).catch(() => {})
      } else if (cmd === 'changePwd') {
        this.$router.push('/change-password')
      }
    }
  }
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.sidebar { background: #304156; transition: width 0.3s; overflow: hidden; }
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background: #263445;
  overflow: hidden;
}
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 16px;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { font-size: 20px; cursor: pointer; color: #666; }
.header-right { display: flex; align-items: center; }
.user-name { cursor: pointer; color: #333; display: flex; align-items: center; gap: 4px; }
.main-content { background: #f5f7fa; padding: 16px; }
</style>
