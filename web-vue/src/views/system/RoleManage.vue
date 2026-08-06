<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button
          v-if="hasPermission('system:role:add')"
          type="success"
          icon="el-icon-plus"
          @click="openDialog()"
        >新增角色</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{ row }">
            <el-button
              v-if="hasPermission('system:role:edit')"
              size="mini" type="text"
              @click="openDialog(row)"
            >编辑</el-button>
            <el-button
              v-if="hasPermission('system:role:edit')"
              size="mini" type="text"
              @click="openAssignDialog(row)"
            >分配权限</el-button>
            <el-button
              v-if="hasPermission('system:role:delete')"
              size="mini" type="text" class="text-danger"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog :title="form.id ? '编辑角色' : '新增角色'" :visible.sync="dialogVisible" width="420px">
      <el-form :model="form" :rules="rules" ref="roleForm" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="如 ROLE_ADMIN" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog title="分配菜单权限" :visible.sync="assignDialogVisible" width="480px">
      <el-tree
        ref="menuTree"
        :data="menuTreeData"
        :props="{ label: 'menuName', children: 'children' }"
        :default-checked-keys="checkedMenuIds"
        node-key="id"
        show-checkbox
      />
      <span slot="footer">
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listRoles, createRole, updateRole, deleteRole, getRoleMenuIds, listMenuTree } from '@/api'
import { mapGetters } from 'vuex'

export default {
  name: 'RoleManage',
  data() {
    return {
      loading: false,
      tableData: [],
      dialogVisible: false,
      assignDialogVisible: false,
      currentRoleId: null,
      checkedMenuIds: [],
      menuTreeData: [],
      form: { id: null, roleName: '', roleCode: '', remark: '' },
      rules: {
        roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
        roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['hasPermission'])
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await listRoles()
        this.tableData = res.data || []
      } finally {
        this.loading = false
      }
    },
    openDialog(row) {
      this.form = row ? { ...row } : { id: null, roleName: '', roleCode: '', remark: '' }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.roleForm.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await updateRole(this.form)
          this.$message.success('修改成功')
        } else {
          await createRole(this.form)
          this.$message.success('新增成功')
        }
        this.dialogVisible = false
        this.loadData()
      })
    },
    handleDelete(row) {
      this.$confirm(`确定删除角色 "${row.roleName}"？`, '确认', { type: 'warning' })
        .then(async () => {
          await deleteRole(row.id)
          this.$message.success('删除成功')
          this.loadData()
        }).catch(() => {})
    },
    async openAssignDialog(row) {
      this.currentRoleId = row.id
      // 加载菜单树
      const menuRes = await listMenuTree()
      this.menuTreeData = menuRes.data || []
      // 加载已分配菜单
      const idRes = await getRoleMenuIds(row.id)
      this.checkedMenuIds = idRes.data || []
      this.assignDialogVisible = true
    },
    async handleAssign() {
      // 获取所有选中节点（含半选父节点，确保父目录也保存）
      const checkedNodes = this.$refs.menuTree.getCheckedKeys()
      const halfCheckedNodes = this.$refs.menuTree.getHalfCheckedKeys()
      const menuIds = [...checkedNodes, ...halfCheckedNodes]
      // 调用角色权限分配接口
      await updateRole({ id: this.currentRoleId, roleName: '', roleCode: '', menuIds })
      this.$message.success('权限分配成功')
      this.assignDialogVisible = false
    }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.text-danger { color: #f56c6c; }
</style>
