<!-- 文件：src/views/admin/ScheduleManage.vue -->
<template>
  <div class="schedule-manage-container">
    <!-- 操作栏 -->
    <el-card class="operation-card" shadow="hover">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-date-picker
            v-model="searchDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="filterSchedule"
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchDoctorId" placeholder="选择医生" clearable @change="loadScheduleList">
            <el-option
              v-for="doctor in doctorList"
              :key="doctor.id"
              :label="doctor.realName"
              :value="doctor.id"
            />
          </el-select>
        </el-col>
        <el-col :span="12" class="operation-btns">
          <el-switch
            v-model="hideExpired"
            active-text="隐藏过期"
            inactive-text="显示全部"
            @change="loadScheduleList"
            style="margin-right: 15px;"
          />
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增排班
          </el-button>
          <el-button type="success" @click="handleBatchAdd">
            <el-icon><DocumentAdd /></el-icon>批量生成
          </el-button>
          <el-button type="danger" @click="handleClearExpired" :disabled="expiredCount === 0">
            <el-icon><Delete /></el-icon>清理过期
            <el-badge v-if="expiredCount > 0" :value="expiredCount" class="badge" />
          </el-button>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 排班列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Calendar /></el-icon> 排班列表</span>
          <span class="total-count">共 {{ total }} 条记录</span>
        </div>
      </template>

      <el-table :data="paginatedList" v-loading="loading" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="doctorName" label="医生姓名" width="120" />
        <el-table-column prop="workDate" label="日期" width="120" sortable>
          <template #default="scope">
            <span :class="{ 'expired-text': isExpired(scope.row.workDate) }">
              {{ scope.row.workDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="时段" width="130" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.timeSlot === 1 ? 'success' : 'warning'">
              {{ scope.row.timeSlot === 1 ? '上午 (8:00-12:00)' : '下午 (14:00-18:00)' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxCount" label="限额" width="80" align="center" />
        <el-table-column prop="bookedCount" label="已预约" width="80" align="center" />
        <el-table-column label="剩余号源" width="100" align="center">
          <template #default="scope">
            <span :class="{ 'full': scope.row.bookedCount >= scope.row.maxCount }">
              {{ scope.row.maxCount - scope.row.bookedCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="预约进度" width="150">
          <template #default="scope">
            <el-progress 
              :percentage="getPercentage(scope.row.bookedCount, scope.row.maxCount)" 
              :status="scope.row.bookedCount >= scope.row.maxCount ? 'exception' : 'success'"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getScheduleStatusType(scope.row)">
              {{ getScheduleStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="handleEdit(scope.row)"
              :disabled="isExpired(scope.row.workDate)"
            >
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(scope.row)"
              :disabled="scope.row.bookedCount > 0"
            >
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="filteredList.length === 0 && !loading" description="暂无排班信息" />

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 排班表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px" @close="resetForm">
      <el-form :model="scheduleForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="医生" prop="doctorId">
          <el-select v-model="scheduleForm.doctorId" placeholder="请选择医生" style="width: 100%">
            <el-option
              v-for="doctor in doctorList"
              :key="doctor.id"
              :label="doctor.realName"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="workDate">
          <el-date-picker
            v-model="scheduleForm.workDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="时段" prop="timeSlot">
          <el-radio-group v-model="scheduleForm.timeSlot">
            <el-radio :label="1">上午 (8:00-12:00)</el-radio>
            <el-radio :label="2">下午 (14:00-18:00)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="限额" prop="maxCount">
          <el-input-number v-model="scheduleForm.maxCount" :min="1" :max="100" style="width: 100%" />
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

    <!-- 批量生成对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量生成排班" width="550px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="选择医生">
          <el-select v-model="batchForm.doctors" multiple placeholder="请选择医生（可多选）" style="width: 100%">
            <el-option
              v-for="doctor in doctorList"
              :key="doctor.id"
              :label="doctor.realName"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时段">
          <el-checkbox-group v-model="batchForm.timeSlots">
            <el-checkbox :label="1">上午 (8:00-12:00)</el-checkbox>
            <el-checkbox :label="2">下午 (14:00-18:00)</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="每日限额">
          <el-input-number v-model="batchForm.maxCount" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="跳过周末">
          <el-switch v-model="batchForm.skipWeekend" />
          <span style="margin-left: 10px; color: #909399;">周六、周日不生成排班</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="generateBatch" :loading="generating">
            开始生成
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, DocumentAdd, Refresh, Calendar, Edit, Delete } from '@element-plus/icons-vue'

const API_BASE = 'http://localhost:8080'

// 数据
const loading = ref(false)
const allScheduleList = ref([])
const filteredList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const doctorList = ref([])

// 搜索
const searchDate = ref('')
const searchDoctorId = ref(null)
const hideExpired = ref(true)
const expiredCount = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref(null)
const batchDialogVisible = ref(false)
const generating = ref(false)

// 表单
const scheduleForm = reactive({
  id: '',
  doctorId: '',
  workDate: '',
  timeSlot: 1,
  maxCount: 30
})

// 批量表单
const batchForm = reactive({
  dateRange: [],
  doctors: [],
  timeSlots: [1, 2],
  maxCount: 30,
  skipWeekend: true
})

// 验证规则
const rules = {
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  workDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时段', trigger: 'change' }],
  maxCount: [{ required: true, message: '请输入限额', trigger: 'blur' }]
}

// 分页数据
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

onMounted(() => {
  loadDoctors()
  loadScheduleList()
  loadExpiredCount()
})

// 加载医生列表
const loadDoctors = () => {
  axios.get(`${API_BASE}/user/doctors`)
    .then(res => {
      if (res.data.code === 200) {
        doctorList.value = res.data.data || []
      }
    })
    .catch(err => {
      console.error('加载医生失败:', err)
      ElMessage.error('加载医生列表失败')
    })
}

// 加载过期排班数量
const loadExpiredCount = () => {
  axios.get(`${API_BASE}/schedule/expired/count`)
    .then(res => {
      if (res.data.code === 200) {
        expiredCount.value = res.data.data || 0
      }
    })
    .catch(err => console.error('加载过期数量失败:', err))
}

// 加载排班列表
const loadScheduleList = () => {
  loading.value = true
  
  axios.get(`${API_BASE}/schedule/all`, {
    params: { 
      doctorId: searchDoctorId.value || null,
      includeExpired: !hideExpired.value
    }
  })
    .then(res => {
      loading.value = false
      if (res.data.code === 200) {
        allScheduleList.value = res.data.data || []
        filterSchedule()
        loadExpiredCount()
      } else {
        ElMessage.error(res.data.msg || '加载排班失败')
      }
    })
    .catch(error => {
      loading.value = false
      console.error('加载排班失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    })
}

// 过滤排班
const filterSchedule = () => {
  let list = [...allScheduleList.value]
  
  if (searchDate.value) {
    list = list.filter(item => item.workDate === searchDate.value)
  }
  
  list.sort((a, b) => {
    if (a.workDate !== b.workDate) {
      return a.workDate.localeCompare(b.workDate)
    }
    return a.timeSlot - b.timeSlot
  })
  
  filteredList.value = list
  total.value = list.length
  currentPage.value = 1
}

// 判断是否过期
const isExpired = (workDate) => {
  return workDate < new Date().toISOString().split('T')[0]
}

// 重置筛选
const resetFilter = () => {
  searchDate.value = ''
  searchDoctorId.value = null
  hideExpired.value = true
  loadScheduleList()
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 获取百分比
const getPercentage = (booked, max) => {
  if (!max) return 0
  return Math.round((booked / max) * 100)
}

// 获取排班状态类型
const getScheduleStatusType = (row) => {
  if (isExpired(row.workDate)) return 'info'
  if (row.bookedCount >= row.maxCount) return 'danger'
  if (row.bookedCount > 0) return 'warning'
  return 'success'
}

// 获取排班状态文本
const getScheduleStatusText = (row) => {
  if (isExpired(row.workDate)) return '已过期'
  if (row.bookedCount >= row.maxCount) return '已满'
  if (row.bookedCount > 0) return '有预约'
  return '可预约'
}

// 新增排班
const handleAdd = () => {
  dialogTitle.value = '新增排班'
  resetForm()
  dialogVisible.value = true
}

// 编辑排班
const handleEdit = (row) => {
  if (isExpired(row.workDate)) {
    ElMessage.warning('过期的排班无法编辑')
    return
  }
  dialogTitle.value = '编辑排班'
  Object.assign(scheduleForm, {
    id: row.id,
    doctorId: row.doctorId,
    workDate: row.workDate,
    timeSlot: row.timeSlot,
    maxCount: row.maxCount
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(scheduleForm, {
    id: '',
    doctorId: '',
    workDate: '',
    timeSlot: 1,
    maxCount: 30
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 提交表单
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      
      const url = scheduleForm.id 
        ? `${API_BASE}/schedule/update`
        : `${API_BASE}/schedule/add`
      
      const method = scheduleForm.id ? 'put' : 'post'
      
      axios({
        method,
        url,
        data: scheduleForm
      })
        .then(res => {
          submitting.value = false
          if (res.data.code === 200) {
            dialogVisible.value = false
            ElMessage.success(scheduleForm.id ? '更新成功' : '添加成功')
            loadScheduleList()
          } else {
            ElMessage.error(res.data.msg || '操作失败')
          }
        })
        .catch(error => {
          submitting.value = false
          console.error('操作失败:', error)
          ElMessage.error('网络错误')
        })
    }
  })
}

// 删除排班
const handleDelete = (row) => {
  if (row.bookedCount > 0) {
    ElMessage.warning('该排班已有预约记录，无法删除')
    return
  }
  
  ElMessageBox.confirm('确定删除该排班吗？', '提示', { type: 'warning' })
    .then(() => {
      axios.delete(`${API_BASE}/schedule/${row.id}`)
        .then(res => {
          if (res.data.code === 200) {
            ElMessage.success('删除成功')
            loadScheduleList()
          } else {
            ElMessage.error(res.data.msg || '删除失败')
          }
        })
        .catch(error => {
          console.error('删除失败:', error)
          ElMessage.error('网络错误')
        })
    }).catch(() => {})
}

// 清除过期排班
const handleClearExpired = () => {
  ElMessageBox.confirm(
    '确定要清理过期的排班吗？只会删除没有任何预约记录的过期排班。',
    '清理过期排班',
    { type: 'warning' }
  ).then(() => {
    loading.value = true
    axios.delete(`${API_BASE}/schedule/expired`)
      .then(res => {
        loading.value = false
        if (res.data.code === 200) {
          ElMessage.success(res.data.msg || '清理成功')
          loadScheduleList()
          loadExpiredCount()
        } else {
          ElMessage.error(res.data.msg || '清理失败')
        }
      })
      .catch(error => {
        loading.value = false
        console.error('清理失败:', error)
        ElMessage.error('网络错误')
      })
  }).catch(() => {})
}

// 批量生成
const handleBatchAdd = () => {
  batchForm.dateRange = []
  batchForm.doctors = []
  batchForm.timeSlots = [1, 2]
  batchForm.maxCount = 30
  batchForm.skipWeekend = true
  batchDialogVisible.value = true
}

// 生成批量排班
const generateBatch = () => {
  if (!batchForm.dateRange || batchForm.dateRange.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  if (!batchForm.doctors || batchForm.doctors.length === 0) {
    ElMessage.warning('请选择医生')
    return
  }
  if (!batchForm.timeSlots || batchForm.timeSlots.length === 0) {
    ElMessage.warning('请选择时段')
    return
  }

  generating.value = true
  
  const startDate = new Date(batchForm.dateRange[0])
  const endDate = new Date(batchForm.dateRange[1])
  const dates = []
  
  for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
    const dayOfWeek = d.getDay()
    if (batchForm.skipWeekend && (dayOfWeek === 0 || dayOfWeek === 6)) {
      continue
    }
    dates.push(new Date(d).toISOString().split('T')[0])
  }
  
  const promises = []
  batchForm.doctors.forEach(doctorId => {
    dates.forEach(date => {
      batchForm.timeSlots.forEach(timeSlot => {
        promises.push(
          axios.post(`${API_BASE}/schedule/add`, {
            doctorId,
            workDate: date,
            timeSlot,
            maxCount: batchForm.maxCount
          }).catch(err => err) // 捕获单个错误继续执行
        )
      })
    })
  })
  
  Promise.all(promises)
    .then(results => {
      generating.value = false
      const successCount = results.filter(r => r.data?.code === 200).length
      const failCount = results.length - successCount
      if (failCount > 0) {
        ElMessage.warning(`成功生成 ${successCount} 条，失败 ${failCount} 条（可能已存在）`)
      } else {
        ElMessage.success(`成功生成 ${successCount} 条排班`)
      }
      batchDialogVisible.value = false
      loadScheduleList()
    })
    .catch(error => {
      generating.value = false
      console.error('批量生成失败:', error)
      ElMessage.error('批量生成失败')
    })
}

// 分页
const handleSizeChange = () => { currentPage.value = 1 }
const handleCurrentChange = () => {}
</script>

<style scoped>
.schedule-manage-container { padding: 20px; }
.operation-card { margin-bottom: 20px; }
.operation-btns { display: flex; gap: 10px; justify-content: flex-end; }
.table-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.total-count { font-size: 14px; color: #909399; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
.full { color: #f56c6c; font-weight: bold; }
.expired-text { color: #c0c4cc; text-decoration: line-through; }
.badge { margin-left: 5px; }
</style>