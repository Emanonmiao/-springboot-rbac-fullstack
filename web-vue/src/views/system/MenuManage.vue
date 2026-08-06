<template>
  <div>
    <el-card>
      <div class="toolbar">
        <el-button
          v-if="hasPermission('system:menu:add')"
          type="success"
          icon="el-icon-plus"
          @click="openDialog(null)"
        >新增菜单</el-button>
      </div>

      <el-table
        :data="tableData"
        border
        row-key="id"
        :tree-props="{ children: 'children' }"
        v-loading="loading"
      >
        <el-table-column prop="menuName" label="菜单名称" />
        <el-table-column prop="menuType" label="类型" width="80">
          <template slot-scope="{ row }">
            <el-tag :type="['', 'primary', 'warning'][row.menuType]" size="small">
              {{ ['目录', '菜单', '按钮'][row.menuType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="perms" label="权限标识" />
        <el-table-column prop="path" label="路由路径" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="{ row }">
            <el-button
              v-if="hasPermission('system:menu:add') && row.menuType < 2"
              size="mini" type="text"
              @click="openDialog(row.id)"
            >添加子菜单</el-button>
            <el-button
              v-if="hasPermission('system:menu:edit')"
              size="mini" type="text"
              @click="openEditDialog(row)"
            >编辑</el-button>
            <el-button
              v-if="hasPermission('system:menu:delete')"
              size="mini" type="text" class="text-danger"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑菜单弹窗 -->
    <el-dialog :title="form.id ? '编辑菜单' : '新增菜单'" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="menuForm" label-width="90px">
        <el-form-item label="上级菜单">
          <el-cascader
            v-model="parentIds"
            :options="menuOptions"
            :props="{ checkStrictly: true, label: 'menuName', value: 'id', children: 'children', emitPath: false }"
            clearable
            style="width:100%"
            @change="val => form.parentId = val || 0"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="form.menuType">
            <el-radio :label="0">目录</el-radio>
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.menuType === 2">
          <el-input v-model="form.perms" placeholder="如 system:user:add" />
        </el-form-item>
        <el-form-item label="路由路径" v-if="form.menuType < 2">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.menuType === 1">
          <el-input v-model="form.component" />
        </el-form-item>
        <el-form-item label="图标" v-if="form.menuType < 2">
          <el-input v-model="form.icon" placeholder="Element-UI图标，如 el-icon-user" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listMenuTree, createMenu, updateMenu, deleteMenu } from '@/api'
import { mapGetters } from 'vuex'

export default {
  name: 'MenuManage',
  data() {
    return {
      loading: false,
      tableData: [],
      dialogVisible: false,
      parentIds: null,
      menuOptions: [],
      form: {
        id: null, parentId: 0, menuName: '',
        menuType: 0, path: '', component: '',
        icon: '', perms: '', sortOrder: 0, status: 1
      },
      rules: {
        menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
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
        const res = await listMenuTree()
        this.tableData = res.data || []
        this.menuOptions = res.data || []
      } finally {
        this.loading = false
      }
    },
    openDialog(parentId) {
      this.form = {
        id: null, parentId: parentId || 0, menuName: '',
        menuType: 0, path: '', component: '',
        icon: '', perms: '', sortOrder: 0, status: 1
      }
      this.parentIds = parentId || null
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.form = { ...row }
      this.parentIds = row.parentId || null
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.menuForm.validate(async valid => {
        if (!valid) return
        if (this.form.id) {
          await updateMenu(this.form)
          this.$message.success('修改成功')
        } else {
          await createMenu(this.form)
          this.$message.success('新增成功')
        }
        this.dialogVisible = false
        this.loadData()
      })
    },
    handleDelete(row) {
      this.$confirm(`确定删除菜单 "${row.menuName}"？`, '确认', { type: 'warning' })
        .then(async () => {
          await deleteMenu(row.id)
          this.$message.success('删除成功')
          this.loadData()
        }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.text-danger { color: #f56c6c; }
</style>
