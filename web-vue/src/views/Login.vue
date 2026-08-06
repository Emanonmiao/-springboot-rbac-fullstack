<template>
  <div class="login-wrapper">
    <div class="login-box">
      <h2>RBAC 权限管理系统</h2>
      <p class="subtitle">基于 SpringBoot + Vue2 全栈项目</p>
      <el-form :model="form" :rules="rules" ref="loginForm" @keyup.enter.native="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            prefix-icon="el-icon-user"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            prefix-icon="el-icon-lock"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >登录</el-button>
        </el-form-item>
      </el-form>
      <div class="tip">初始管理员账号：admin / Admin@2024</div>
    </div>
  </div>
</template>

<script>
import { login } from '@/api'

export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      form: { username: '', password: '' },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const res = await login(this.form)
          const loginVO = res.data

          // 保存token和用户信息到Vuex + localStorage
          await this.$store.dispatch('setLoginInfo', loginVO)

          // 密码过期：强制跳转修改密码
          if (loginVO.pwdExpired) {
            this.$message.warning('密码已过期，请先修改密码')
            this.$router.push('/change-password')
          } else {
            this.$message.success('登录成功')
            this.$router.push('/dashboard')
          }
        } catch (e) {
          // 错误已由拦截器处理
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-wrapper {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.login-box {
  width: 380px;
  background: #fff;
  padding: 40px 36px;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0,0,0,.1);
}
.login-box h2 { text-align: center; margin-bottom: 8px; font-size: 22px; color: #303133; }
.subtitle { text-align: center; color: #909399; font-size: 13px; margin-bottom: 28px; }
.tip { text-align: center; color: #909399; font-size: 12px; margin-top: 12px; }
</style>
