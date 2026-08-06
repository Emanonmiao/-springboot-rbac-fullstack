<template>
  <div class="page-container">
    <el-card style="max-width: 480px; margin: 40px auto;">
      <div slot="header">
        <span>修改密码</span>
      </div>
      <el-form :model="form" :rules="rules" ref="pwdForm" label-width="90px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" placeholder="请输入当前密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="至少8位，含两类字符" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">修改密码</el-button>
          <el-button @click="$router.go(-1)">取消</el-button>
        </el-form-item>
      </el-form>
      <el-alert
        title="密码规则"
        type="info"
        :closable="false"
        style="margin-top:8px"
      >
        <ul style="padding-left:16px; margin:4px 0; line-height:1.8">
          <li>密码长度不少于8位</li>
          <li>必须包含大写字母、小写字母、数字、特殊符号中的至少两类</li>
          <li>不能使用常见弱口令</li>
          <li>密码有效期90天，过期后需重新设置</li>
        </ul>
      </el-alert>
    </el-card>
  </div>
</template>

<script>
import { changePassword } from '@/api'

export default {
  name: 'ChangePassword',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.form.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      loading: false,
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 8, message: '密码长度不少于8位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleSubmit() {
      this.$refs.pwdForm.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          await changePassword({
            oldPassword: this.form.oldPassword,
            newPassword: this.form.newPassword
          })
          this.$message.success('密码修改成功，请重新登录')
          // 修改密码后退出登录
          setTimeout(() => {
            this.$store.dispatch('logout')
            this.$router.push('/login')
          }, 1500)
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
