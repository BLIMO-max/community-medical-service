<!-- 文件：src/views/admin/UserManage.vue -->
<template>
  <div class="user-manage-container">
    <!-- 搜索和操作栏 -->
    <el-card class="search-card" shadow="hover">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名/真实姓名/手机号"
            :prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.role" placeholder="用户角色" clearable @change="handleSearch">
            <el-option label="全部" :value="null" />
            <el-option label="居民" :value="2" />
            <el-option label="医生" :value="1" />
            <el-option label="管理员" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="账号状态" clearable @change="handleSearch">
            <el-option label="全部" :value="null" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="10" class="operation-btns">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增用户
          </el-button>
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><User /></el-icon> 用户列表</span>
          <span class="total-count">共 {{ total }} 条记录</span>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleName(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              active-text="正常"
              inactive-text="禁用"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" sortable>
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="240" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(scope.row)"
              :disabled="scope.row.role === 0"
            >
              <el-icon><Delete /></el-icon>删除
            </el-button>
            <el-button type="info" link @click="handleResetPwd(scope.row)">
              <el-icon><Key /></el-icon>重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadUserList"
        @current-change="loadUserList"
        class="pagination"
      />
    </el-card>

    <!-- 用户表单对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="userForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userForm.username" 
            :disabled="dialogType === 'edit'"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="userForm.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="userForm.age" :min="1" :max="120" style="width: 100%" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="userForm.role">
            <el-radio :label="2">居民</el-radio>
            <el-radio :label="1">医生</el-radio>
            <el-radio :label="0">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input 
            v-model="userForm.password" 
            type="password" 
            show-password 
            placeholder="请输入密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download, Refresh, Edit, Delete, Key, User } from '@element-plus/icons-vue'

const API_BASE = 'http://localhost:8080'

// 数据列表
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  role: null,
  status: null
})

// 对话框
const dialogVisible = ref(false)
const dialogType = ref('add')
const submitting = ref(false)
const formRef = ref(null)

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '未知'
  try {
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return '未知'
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return '日期错误'
  }
}

// 用户表单
const userForm = reactive({
  id: '',
  username: '',
  realName: '',
  phone: '',
  gender: '',
  age: null,
  role: 2,
  status: 1,
  password: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

onMounted(() => {
  loadUserList()
})

// 加载用户列表
const loadUserList = () => {
  loading.value = true
  
  const params = {
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }
  
  if (searchForm.keyword) params.keyword = searchForm.keyword
  if (searchForm.role !== null && searchForm.role !== '') params.role = searchForm.role
  if (searchForm.status !== null && searchForm.status !== '') params.status = searchForm.status
  
  axios.get(`${API_BASE}/user/list`, { params })
    .then(res => {
      loading.value = false
      if (res.data.code === 200) {
        const data = res.data.data
        userList.value = data.records || []
        total.value = data.total || 0
      } else {
        ElMessage.error(res.data.msg || '加载失败')
      }
    })
    .catch(error => {
      loading.value = false
      console.error('加载用户列表失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    })
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadUserList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.role = null
  searchForm.status = null
  handleSearch()
}

// 重置表单
const resetForm = () => {
  Object.assign(userForm, {
    id: '',
    username: '',
    realName: '',
    phone: '',
    gender: '',
    age: null,
    role: 2,
    status: 1,
    password: ''
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 新增用户
const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    phone: row.phone || '',
    gender: row.gender || '',
    age: row.age,
    role: row.role,
    status: row.status
  })
  dialogVisible.value = true
}

// 删除用户
const handleDelete = (row) => {
  if (row.role === 0) {
    ElMessage.warning('管理员账号不能删除')
    return
  }
  
  ElMessageBox.confirm(`确定删除用户 "${row.username}" 吗？删除后无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    axios.delete(`${API_BASE}/user/${row.id}`)
      .then(res => {
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          loadUserList()
        } else {
          ElMessage.error(res.data.msg || '删除失败')
        }
      })
      .catch(error => {
        console.error('删除失败:', error)
        ElMessage.error('网络错误，请稍后重试')
      })
  }).catch(() => {})
}

// 重置密码
const handleResetPwd = (row) => {
  ElMessageBox.confirm(`确定重置用户 "${row.username}" 的密码吗？密码将重置为 123456`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    axios.put(`${API_BASE}/user/reset-pwd/${row.id}`)
      .then(res => {
        if (res.data.code === 200) {
          ElMessage.success('密码已重置为 123456')
        } else {
          ElMessage.error(res.data.msg || '重置失败')
        }
      })
      .catch(error => {
        console.error('重置密码失败:', error)
        ElMessage.error('网络错误，请稍后重试')
      })
  }).catch(() => {})
}

// 状态切换
const handleStatusChange = (row) => {
  const newStatus = row.status
  axios.put(`${API_BASE}/user/status/${row.id}?status=${newStatus}`)
    .then(res => {
      if (res.data.code === 200) {
        ElMessage.success(`已${newStatus === 1 ? '启用' : '禁用'}用户`)
      } else {
        row.status = row.status === 1 ? 0 : 1
        ElMessage.error(res.data.msg || '状态更新失败')
      }
    })
    .catch(error => {
      row.status = row.status === 1 ? 0 : 1
      console.error('状态更新失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    })
}

// 提交表单
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      
      const submitData = { ...userForm }
      
      if (dialogType.value === 'edit') {
        delete submitData.password
      }
      
      const url = dialogType.value === 'add' 
        ? `${API_BASE}/user/add`
        : `${API_BASE}/user/update`
      
      const method = dialogType.value === 'add' ? 'post' : 'put'
      
      axios({
        method,
        url,
        data: submitData
      })
        .then(res => {
          submitting.value = false
          if (res.data.code === 200) {
            dialogVisible.value = false
            ElMessage.success(dialogType.value === 'add' ? '新增成功' : '编辑成功')
            loadUserList()
          } else {
            ElMessage.error(res.data.msg || '操作失败')
          }
        })
        .catch(error => {
          submitting.value = false
          console.error('提交失败:', error)
          ElMessage.error('网络错误，请稍后重试')
        })
    }
  })
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 获取角色名称
const getRoleName = (role) => {
  const map = { 0: '管理员', 1: '医生', 2: '居民' }
  return map[role] || '未知'
}

// 获取角色标签类型
const getRoleType = (role) => {
  const map = { 0: 'danger', 1: 'warning', 2: 'success' }
  return map[role] || 'info'
}
</script>

<style scoped>
.user-manage-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.operation-btns { display: flex; justify-content: flex-end; gap: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.total-count { font-size: 14px; color: #909399; }
.table-card { margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>