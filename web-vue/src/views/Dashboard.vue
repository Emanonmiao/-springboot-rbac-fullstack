<template>
  <div>
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="item in stats" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <i :class="item.icon" :style="{ color: item.color }"></i>
            <div>
              <div class="stat-num">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="info-card">
      <div slot="header">当前登录信息</div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userInfo.realName }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="r in roles" :key="r" size="small" style="margin-right:4px">{{ r }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="welcome-card">
      <div class="welcome-text">
        <h3>欢迎使用 RBAC 权限管理系统</h3>
        <p>基于 SpringBoot 2.7.18 + SpringSecurity5.7 + JWT + MyBatis-Plus 3.5.3.2 + Vue2 + Element-UI 实现</p>
        <p>参考等保2.0标准实现：弱口令拦截、90天口令有效期、登录失败锁定、登录审计日志</p>
      </div>
    </el-card>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getDashboardStat } from '@/api'

export default {
  name: 'Dashboard',
  data() {
    return {
      stats: [
        { label: '用户总数', value: '-', icon: 'el-icon-user',       color: '#409eff' },
        { label: '角色总数', value: '-', icon: 'el-icon-s-custom',   color: '#67c23a' },
        { label: '菜单总数', value: '-', icon: 'el-icon-menu',       color: '#e6a23c' },
        { label: '登录日志', value: '-', icon: 'el-icon-document',   color: '#f56c6c' }
      ]
    }
  },
  computed: {
    ...mapGetters(['userInfo', 'roles'])
  },
  created() {
    this.loadStat()
  },
  methods: {
    async loadStat() {
      try {
        const res = await getDashboardStat()
        const d = res.data
        this.stats[0].value = d.userCount
        this.stats[1].value = d.roleCount
        this.stats[2].value = d.menuCount
        this.stats[3].value = d.logCount
      } catch (e) {
        // 加载失败不影响其他功能
      }
    }
  }
}
</script>

<style scoped>
.stat-row { margin-bottom: 16px; }
.stat-card .stat-content { display: flex; align-items: center; gap: 16px; }
.stat-card i { font-size: 40px; }
.stat-num { font-size: 24px; font-weight: bold; color: #303133; }
.stat-label { color: #909399; font-size: 13px; }
.info-card { margin-bottom: 16px; }
.welcome-card {}
.welcome-text h3 { color: #303133; margin-bottom: 8px; }
.welcome-text p { color: #606266; line-height: 1.8; }
</style>
