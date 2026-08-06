<template>
  <div>
    <el-card>
      <!-- 搜索工具栏 -->
      <div class="toolbar">
        <el-input
          v-model="queryForm.username"
          placeholder="搜索用户名"
          clearable
          style="width:200px"
          @keyup.enter.native="loadData"
        />
        <el-button type="primary" icon="el-icon-search" @click="loadData">搜索</el-button>
        <el-button
          v-if="hasPermission('system:user:add')"
          type="success"
          icon="el-icon-plus"
          @click="openDialog()"
        >新增</el-button>
      </div>

      <!-- 用户列表 -->
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '锁定' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roles" label="角色" min-width="120">
          <template slot-scope="{ row }">
            <el-tag
              v-for="r in (row.roles || [])"
              :key="r.id"
              size="small"
              style="margin-right:4px"
            >{{ r.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pwdExpireTime" label="密码过期时间" width="170" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{ row }">
            <el-button
              v-if="hasPermission('system:user:edit')"
              size="mini" type="text"
              @click="openDialog(row)"
            >编辑</el-button>
            <el-button
              v-if="row.status === 0 && hasPermission('system:user:unlock')"
              size="mini" type="text" class="text-success"
              @click="handleUnlock(row)"
            >解锁</el-button>
            <el-button
              v-if="hasPermission('system:user:delete')"
              size="mini" type="text" class="text-danger"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        :current-page="queryForm.pageNum"
        :page-size="queryForm.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="userForm" label-width="80px">
        <el-form-item label="用户名" prop="username" v-if="!form.id">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="密码至少8位，含两类字符" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width:100%">
            <el-option
              v-for="r in allRoles"
              :key="r.id"
              :label="r.roleName"
              :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" v-if="form.id">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">锁定</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { pageUsers, createUser, updateUser, deleteUser, unlockUser, listRoles } from '@/api'
import { mapGetters } from 'vuex'

export default {
  name: 'UserManage',
  data() {
    return {
      loading: false,
      submitting: false,
      tableData: [],
      total: 0,
      queryForm: { pageNum: 1, pageSize: 10, username: '' },
      dialogVisible: false,
      form: { id: null, username: '', password: '', realName: '', phone: '', roleIds: [], status: 1 },
      allRoles: [],
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['hasPermission']),
    dialogTitle() { return this.form.id ? '编辑用户' : '新增用户' }
  },
  created() {
    this.loadData()
    this.loadRoles()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await pageUsers(this.queryForm)
        this.tableData = res.data.records || []
        this.total = res.data.total || 0
      } finally {
        this.loading = false
      }
    },
    async loadRoles() {
      const res = await listRoles()
      this.allRoles = res.data || []
    },
    openDialog(row) {
      if (row) {
        this.form = {
          id: row.id,
          username: row.username,
          realName: row.realName,
          phone: row.phone,
          roleIds: (row.roles || []).map(r => r.id),
          status: row.status
        }
      } else {
        this.form = { id: null, username: '', password: '', realName: '', phone: '', roleIds: [], status: 1 }
      }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.userForm.validate(async valid => {
        if (!valid) return
        this.submitting = true
        try {
          if (this.form.id) {
            await updateUser(this.form)
            this.$message.success('修改成功')
          } else {
            await createUser(this.form)
            this.$message.success('新增成功')
          }
          this.dialogVisible = false
          this.loadData()
        } finally {
          this.submitting = false
        }
      })
    },
    handleDelete(row) {
      this.$confirm(`确定删除用户 "${row.username}"？`, '确认', { type: 'warning' })
        .then(async () => {
          await deleteUser(row.id)
          this.$message.success('删除成功')
          this.loadData()
        }).catch(() => {})
    },
    async handleUnlock(row) {
      await unlockUser(row.id)
      this.$message.success('解锁成功')
      this.loadData()
    },
    handlePageChange(page) {
      this.queryForm.pageNum = page
      this.loadData()
    },
    resetForm() {
      this.$refs.userForm && this.$refs.userForm.resetFields()
    }
  }
}
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.pagination { margin-top: 16px; text-align: right; }
.text-danger { color: #f56c6c; }
.text-success { color: #67c23a; }
</style>
