<template>
  <div class="appointment-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" :style="{ background: stat.bgColor }">
              <el-icon :size="24" :color="stat.color"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">{{ stat.title }}</span>
              <span class="stat-value">{{ stat.value }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的预约列表 -->
    <el-card class="my-appointments-card" shadow="hover" v-if="myAppointmentList.length > 0">
      <template #header>
        <div class="card-header">
          <span><el-icon><Calendar /></el-icon> 我的预约</span>
          <div class="header-actions">
            <el-button type="primary" link @click="showMyAppointments = !showMyAppointments">
              {{ showMyAppointments ? '收起' : '展开' }}
            </el-button>
            <el-button 
              v-if="hasCancelledOrMissed" 
              type="danger" 
              link 
              size="small" 
              @click="clearInvalidAppointments"
              style="margin-left: 10px;"
            >
              <el-icon><Delete /></el-icon>清除无效记录
            </el-button>
          </div>
        </div>
      </template>
      
      <el-collapse-transition>
        <div v-show="showMyAppointments">
          <el-table :data="myAppointmentList" stripe size="small">
            <el-table-column prop="doctorName" label="医生" width="100" />
            <el-table-column prop="workDate" label="就诊日期" width="110" />
            <el-table-column label="时段" width="70">
              <template #default="scope">
                <el-tag :type="scope.row.timeSlot === 1 ? 'warning' : 'success'" size="small">
                  {{ scope.row.timeSlot === 1 ? '上午' : '下午' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="scope">
                <el-tag :type="getAppointmentStatusType(scope.row.status)" size="small">
                  {{ getAppointmentStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="预约时间" width="150">
              <template #default="scope">
                {{ formatDateTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button 
                  v-if="scope.row.status === 0"
                  type="danger" 
                  link 
                  size="small"
                  @click="cancelAppointment(scope.row)"
                >
                  取消预约
                </el-button>
                <el-button 
                  v-if="scope.row.status === 2 || scope.row.status === 3 || scope.row.status === 4"
                  type="info" 
                  link 
                  size="small"
                  @click="deleteAppointment(scope.row)"
                >
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 快捷预约 -->
    <el-card class="quick-book-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📅 快捷预约</span>
          <el-button type="primary" link @click="showQuickBook = !showQuickBook">
            {{ showQuickBook ? '收起' : '展开' }}
            <el-icon><ArrowDown v-if="!showQuickBook" /><ArrowUp v-else /></el-icon>
          </el-button>
        </div>
      </template>
      
      <el-collapse-transition>
        <div v-show="showQuickBook">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-select v-model="quickBook.doctorId" placeholder="选择医生" clearable class="quick-input">
                <el-option 
                  v-for="doctor in doctorList" 
                  :key="doctor.id" 
                  :label="doctor.realName" 
                  :value="doctor.id" 
                />
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-date-picker
  v-model="selectDate"
  type="date"
  placeholder="筛选日期"
  format="YYYY-MM-DD"
  value-format="YYYY-MM-DD"
  @change="filterSchedule"
  size="small"
  style="width: 150px;"
  :disabled-date="filterDateDisabled"
/>
            </el-col>
            <el-col :span="8">
              <el-button type="primary" @click="quickBookAppointment" :loading="quickBooking">
                快速预约
              </el-button>
              <el-button @click="resetQuickBook">重置</el-button>
            </el-col>
          </el-row>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 排班列表卡片 -->
    <el-card class="schedule-card" shadow="hover">
      <template #header>
        <div class="header-box">
          <span>📋 可预约排班</span>
          <div class="filter-box">
            <el-select v-model="filterDoctorId" placeholder="筛选医生" clearable size="small" style="width: 150px; margin-right: 10px;" @change="filterSchedule">
              <el-option 
                v-for="doctor in doctorList" 
                :key="doctor.id" 
                :label="doctor.realName" 
                :value="doctor.id" 
              />
            </el-select>
            <el-date-picker
              v-model="selectDate"
              type="date"
              placeholder="筛选日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="filterSchedule"
              size="small"
              style="width: 150px;"
            />
            <el-button type="primary" size="small" @click="resetFilter" style="margin-left: 10px;">
              重置
            </el-button>
          </div>
        </div>
      </template>

      <div class="schedule-grid" v-loading="loading">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in paginatedList" :key="item.id">
            <el-card class="schedule-item" :body-style="{ padding: '0' }" shadow="hover">
              <div class="schedule-header" :class="getTimeSlotClass(item.timeSlot)">
                <span class="date">{{ item.workDate }}</span>
                <el-tag size="small" :type="item.timeSlot === 1 ? 'warning' : 'success'" effect="dark">
                  {{ item.timeSlot === 1 ? '上午' : '下午' }}
                </el-tag>
              </div>
              
              <div class="schedule-body">
                <div class="doctor-info">
                  <el-avatar :size="50" class="doctor-avatar">
                    {{ item.doctorName?.charAt(0) || '医' }}
                  </el-avatar>
                  <div class="doctor-detail">
                    <span class="doctor-name">{{ item.doctorName || '未知医生' }}</span>
                    <span class="doctor-title">{{ getDoctorTitle(item.doctorId) }}</span>
                  </div>
                </div>

                <div class="schedule-stats">
                  <div class="stat-row">
                    <span>已预约</span>
                    <span class="stat-number">{{ item.bookedCount }}</span>
                  </div>
                  <div class="stat-row">
                    <span>剩余</span>
                    <span class="stat-number">{{ item.maxCount - item.bookedCount }}</span>
                  </div>
                  <div class="stat-row">
                    <span>总数</span>
                    <span class="stat-number">{{ item.maxCount }}</span>
                  </div>
                </div>

                <el-progress 
                  :percentage="getPercentage(item.bookedCount, item.maxCount)" 
                  :status="getProgressStatus(item.bookedCount, item.maxCount)"
                  :stroke-width="8"
                  striped
                  striped-flow
                  :duration="20"
                />
                
                <div v-if="isBooked(item.id)" class="booked-badge">
                  <el-tag type="success" size="small">✅ 您已预约</el-tag>
                </div>
              </div>

              <div class="schedule-footer">
                <el-button 
                  v-if="!isBooked(item.id)"
                  type="primary" 
                  :disabled="item.bookedCount >= item.maxCount"
                  @click="handleBook(item)"
                  class="book-btn"
                >
                  <el-icon><Calendar /></el-icon>
                  {{ item.bookedCount >= item.maxCount ? '已满' : '立即预约' }}
                </el-button>
                <el-button 
                  v-else
                  type="info" 
                  disabled
                  class="book-btn"
                >
                  <el-icon><CircleCheck /></el-icon>
                  已预约
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty v-if="paginatedList.length === 0 && !loading" description="暂无排班信息" />
      </div>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalItems"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 预约确认对话框 -->
    <el-dialog v-model="showBookDialog" title="确认预约" width="400px">
      <div v-if="selectedSchedule" class="book-confirm">
        <div class="confirm-item">
          <span class="label">医生：</span>
          <span class="value">{{ selectedSchedule.doctorName }}</span>
        </div>
        <div class="confirm-item">
          <span class="label">就诊日期：</span>
          <span class="value">{{ selectedSchedule.workDate }}</span>
        </div>
        <div class="confirm-item">
          <span class="label">就诊时段：</span>
          <span class="value">{{ selectedSchedule.timeSlot === 1 ? '上午 (8:00-12:00)' : '下午 (14:00-18:00)' }}</span>
        </div>
        <div class="confirm-item">
          <span class="label">就诊人：</span>
          <span class="value">{{ user.realName || user.username }}</span>
        </div>
        <div class="confirm-item" v-if="missedCount > 0">
          <span class="label">爽约记录：</span>
          <span class="value" style="color: #f56c6c;">您有 {{ missedCount }} 次爽约记录，请按时就诊！</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBookDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmBook" :loading="booking">
            确认预约
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Calendar, Search, ArrowDown, ArrowUp,
  User, Timer, Document, Star, CircleCheck, Delete
} from '@element-plus/icons-vue'

const API_BASE = 'http://localhost:8080'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const allScheduleList = ref([])
const filteredList = ref([])
const loading = ref(false)
const quickBooking = ref(false)
const showQuickBook = ref(false)
const showMyAppointments = ref(true)
const selectDate = ref('')
const filterDoctorId = ref(null)
const currentPage = ref(1)
const pageSize = ref(8)
const totalItems = ref(0)
const showBookDialog = ref(false)
const selectedSchedule = ref(null)
const booking = ref(false)

// 我的预约
const myAppointments = ref(0)
const myAppointmentList = ref([])
const myBookedScheduleIds = ref(new Set())
const missedCount = ref(0)

// 医生列表
const doctorList = ref([])

// 快捷预约表单
const quickBook = ref({
  doctorId: '',
  date: '',
  timeSlot: null
})

// ==================== 计算属性 ====================

// 统计数据
const stats = computed(() => {
  const total = allScheduleList.value.length
  const available = allScheduleList.value.filter(s => s.bookedCount < s.maxCount).length
  const today = new Date().toISOString().split('T')[0]
  const todayCount = allScheduleList.value.filter(s => s.workDate === today).length
  
  return [
    { title: '今日可约', value: todayCount, icon: 'Calendar', bgColor: 'rgba(64, 158, 255, 0.1)', color: '#409EFF' },
    { title: '可预约号源', value: available, icon: 'Timer', bgColor: 'rgba(103, 194, 58, 0.1)', color: '#67C23A' },
    { title: '总排班数', value: total, icon: 'Document', bgColor: 'rgba(230, 162, 60, 0.1)', color: '#E6A23C' },
    { title: '我的预约', value: myAppointments.value, icon: 'Star', bgColor: 'rgba(245, 108, 108, 0.1)', color: '#F56C6C' }
  ]
})

// 是否有已取消/爽约/停诊的记录
const hasCancelledOrMissed = computed(() => {
  return myAppointmentList.value.some(a => a.status === 2 || a.status === 3 || a.status === 4)
})

// 分页数据
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

// ==================== 生命周期 ====================

onMounted(() => {
  loadDoctors()
  loadSchedule()
  loadMyAppointments()
  loadMissedCount()
})

// ==================== 数据加载 ====================

// 加载医生列表
const loadDoctors = () => {
  axios.get(`${API_BASE}/user/doctors`)
    .then(res => {
      if (res.data.code === 200) {
        doctorList.value = res.data.data || []
      }
    })
    .catch(err => console.error('加载医生列表失败:', err))
}

// 加载我的预约
const loadMyAppointments = () => {
  if (!user.id) return
  
  axios.get(`${API_BASE}/appointment/user/${user.id}`)
    .then(res => {
      if (res.data.code === 200) {
        // 过滤掉已删除的，保留待就诊(0)、已完成(1)、已取消(2)、爽约(3)、停诊(4)
        myAppointmentList.value = (res.data.data || []).filter(a => a.status !== null)
        myAppointments.value = myAppointmentList.value.filter(a => a.status === 0).length
        
        myBookedScheduleIds.value.clear()
        myAppointmentList.value.forEach(a => {
          if (a.status === 0) {
            myBookedScheduleIds.value.add(a.scheduleId)
          }
        })
      }
    })
    .catch(err => console.error('加载我的预约失败:', err))
}

// 加载爽约次数
const loadMissedCount = () => {
  if (!user.id) return
  
  axios.get(`${API_BASE}/appointment/missed-count/${user.id}`)
    .then(res => {
      if (res.data.code === 200) {
        missedCount.value = res.data.data || 0
      }
    })
    .catch(err => console.error('加载爽约次数失败:', err))
}

// 加载排班数据
const loadSchedule = () => {
  loading.value = true
  
  // 添加 onlyFuture 参数，只获取未来的排班
  axios.get(`${API_BASE}/schedule/list`, {
    params: { onlyFuture: true }
  })
    .then(res => {
      loading.value = false
      if (res.data.code === 200 && res.data.data) {
        const today = new Date().toISOString().split('T')[0]
        
        const schedules = res.data.data
          .map(item => ({
            ...item,
            id: item.id,
            doctorId: item.doctorId,
            doctorName: item.doctorName || '未知医生',
            workDate: item.workDate,
            timeSlot: item.timeSlot,
            maxCount: item.maxCount || 30,
            bookedCount: item.bookedCount || 0
          }))
          // 前端再次过滤，确保只显示今天及以后
          .filter(item => item.workDate >= today)
        
        allScheduleList.value = schedules
        filterSchedule()
      }
    })
    .catch(error => {
      loading.value = false
      console.error('加载排班失败:', error)
    })
}

// ==================== 预约状态相关 ====================

// 获取预约状态文本
const getAppointmentStatusText = (status) => {
  const map = { 
    0: '待就诊', 
    1: '已完成', 
    2: '已取消', 
    3: '爽约', 
    4: '医生停诊' 
  }
  return map[status] || '未知'
}

// 获取预约状态类型（颜色）
const getAppointmentStatusType = (status) => {
  const map = { 
    0: 'warning',    // 待就诊 - 橙色
    1: 'success',    // 已完成 - 绿色
    2: 'info',       // 已取消 - 灰色
    3: 'danger',     // 爽约 - 红色
    4: 'danger'      // 医生停诊 - 红色
  }
  return map[status] || 'info'
}

// 检查是否已预约
const isBooked = (scheduleId) => {
  return myBookedScheduleIds.value.has(scheduleId)
}

// ==================== 预约操作 ====================

// 处理预约
const handleBook = (schedule) => {
  if (!user.id) {
    ElMessage.error('请先登录')
    return
  }
  selectedSchedule.value = schedule
  showBookDialog.value = true
}

// 确认预约
const confirmBook = () => {
  if (!selectedSchedule.value) return
  
  booking.value = true
  
  axios.post(`${API_BASE}/appointment/book`, null, {
    params: {
      userId: user.id,
      scheduleId: selectedSchedule.value.id
    }
  })
    .then(res => {
      if (res.data.code === 200) {
        ElMessage.success(res.data.msg || '预约成功！')
        showBookDialog.value = false
        loadSchedule()
        loadMyAppointments()
      } else {
        ElMessage.error(res.data.msg || '预约失败')
      }
    })
    .catch(error => {
      console.error('预约失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    })
    .finally(() => {
      booking.value = false
    })
}

// 取消预约
const cancelAppointment = (appointment) => {
  ElMessageBox.confirm('确定取消该预约吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    axios.put(`${API_BASE}/appointment/cancel/${appointment.id}`)
      .then(res => {
        if (res.data.code === 200) {
          ElMessage.success('取消成功')
          loadSchedule()
          loadMyAppointments()
        } else {
          ElMessage.error(res.data.msg || '取消失败')
        }
      })
      .catch(error => {
        console.error('取消失败:', error)
        ElMessage.error('网络错误')
      })
  }).catch(() => {})
}

// 删除单条预约记录
const deleteAppointment = (appointment) => {
  ElMessageBox.confirm('确定删除这条预约记录吗？删除后无法恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    axios.delete(`${API_BASE}/appointment/${appointment.id}`)
      .then(res => {
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          loadMyAppointments()
          loadSchedule() // 同时刷新排班数据
        } else {
          ElMessage.error(res.data.msg || '删除失败')
        }
      })
      .catch(error => {
        console.error('删除失败:', error)
        if (error.response) {
          console.error('错误响应:', error.response.data)
          ElMessage.error(error.response.data.msg || '服务器错误')
        } else {
          ElMessage.error('网络错误，请检查后端服务是否启动')
        }
      })
  }).catch(() => {})
}

// 清除所有无效记录（已取消、爽约、停诊）
const clearInvalidAppointments = () => {
  ElMessageBox.confirm('确定删除所有已取消、爽约和停诊的预约记录吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 修改：使用 /invalid 接口
    axios.delete(`${API_BASE}/appointment/invalid?userId=${user.id}`)
      .then(res => {
        if (res.data.code === 200) {
          ElMessage.success('清除成功')
          loadMyAppointments()
        } else {
          ElMessage.error(res.data.msg || '清除失败')
        }
      })
      .catch(error => {
        console.error('清除失败:', error)
        // 打印详细错误信息
        if (error.response) {
          console.error('错误响应:', error.response.data)
          ElMessage.error(error.response.data.msg || '服务器错误')
        } else {
          ElMessage.error('网络错误，请检查后端服务是否启动')
        }
      })
  }).catch(() => {})
}


// ==================== 快捷预约 ====================

const quickBookAppointment = () => {
  if (!quickBook.value.doctorId || !quickBook.value.date) {
    ElMessage.warning('请选择医生和日期')
    return
  }
  
  const schedule = allScheduleList.value.find(s => 
    s.doctorId === quickBook.value.doctorId && 
    s.workDate === quickBook.value.date
  )
  
  if (!schedule) {
    ElMessage.warning('该医生当天没有排班')
    return
  }
  
  handleBook(schedule)
}

const resetQuickBook = () => {
  quickBook.value = { doctorId: '', date: '', timeSlot: null }
}

// ==================== 筛选相关 ====================

const filterSchedule = () => {
  const today = new Date().toISOString().split('T')[0]
  
  let list = [...allScheduleList.value].filter(item => item.workDate >= today)
  
  if (selectDate.value) {
    list = list.filter(item => item.workDate === selectDate.value)
  }
  
  if (filterDoctorId.value) {
    list = list.filter(item => item.doctorId === filterDoctorId.value)
  }
  
  filteredList.value = list
  totalItems.value = list.length
  currentPage.value = 1
}

// 禁用过去的日期（快捷预约日期选择器）
const disabledDate = (time) => {
  // 禁用今天之前的日期
  return time.getTime() < Date.now() - 8.64e7
}

// 筛选日期选择器也禁用过去日期
const filterDateDisabled = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}
// ==================== 辅助函数 ====================

const getPercentage = (booked, max) => {
  if (!max) return 0
  return Number(((booked / max) * 100).toFixed(0))
}

const getProgressStatus = (booked, max) => {
  if (booked >= max) return 'exception'
  if (booked / max > 0.8) return 'warning'
  return 'success'
}

const getTimeSlotClass = (slot) => {
  return slot === 1 ? 'morning-slot' : 'afternoon-slot'
}

const getDoctorTitle = (doctorId) => {
  const titles = ['主任医师', '副主任医师', '主治医师', '住院医师']
  return titles[doctorId % titles.length] || '主治医师'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '未知'
  try {
    const date = new Date(dateTime)
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return '无效日期'
    }
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  } catch (e) {
    return '日期错误'
  }
}

// ==================== 分页 ====================

const handleSizeChange = () => { currentPage.value = 1 }
const handleCurrentChange = () => {}
</script>

<style scoped>
.appointment-container { padding: 20px; }
.stat-cards { margin-bottom: 20px; }
.stat-card { transition: all 0.3s; }
.stat-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1); }
.stat-item { display: flex; align-items: center; gap: 15px; }
.stat-icon { width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.stat-info { display: flex; flex-direction: column; }
.stat-label { font-size: 13px; color: #909399; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; }

.my-appointments-card { margin-bottom: 20px; }
.header-actions { display: flex; align-items: center; }

.quick-book-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.quick-input { width: 100%; }

.schedule-card { min-height: 500px; }
.header-box { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.filter-box { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; }

.schedule-grid { margin-top: 20px; }
.schedule-item { margin-bottom: 20px; transition: all 0.3s; border-radius: 12px; overflow: hidden; }
.schedule-item:hover { transform: translateY(-5px); box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15); }

.schedule-header { padding: 15px; display: flex; justify-content: space-between; align-items: center; color: white; }
.morning-slot { background: linear-gradient(135deg, #f5b042, #f39c12); }
.afternoon-slot { background: linear-gradient(135deg, #3498db, #2980b9); }
.schedule-header .date { font-size: 16px; font-weight: 500; }

.schedule-body { padding: 20px; position: relative; }
.doctor-info { display: flex; align-items: center; gap: 15px; margin-bottom: 15px; }
.doctor-avatar { background: linear-gradient(135deg, #409EFF, #36d1dc); color: white; font-weight: bold; }
.doctor-detail { display: flex; flex-direction: column; }
.doctor-name { font-size: 16px; font-weight: 600; color: #303133; }
.doctor-title { font-size: 12px; color: #909399; }

.schedule-stats { display: flex; justify-content: space-between; margin-bottom: 15px; padding: 10px 0; border-top: 1px solid #ebeef5; border-bottom: 1px solid #ebeef5; }
.stat-row { display: flex; flex-direction: column; align-items: center; }
.stat-row span:first-child { font-size: 12px; color: #909399; }
.stat-number { font-size: 18px; font-weight: bold; color: #303133; }

.booked-badge { margin-top: 10px; text-align: center; }
.schedule-footer { padding: 15px; background: #f9f9f9; }
.book-btn { width: 100%; border-radius: 20px; }

.book-confirm { padding: 20px; }
.confirm-item { display: flex; margin-bottom: 15px; padding: 8px 0; border-bottom: 1px dashed #ebeef5; }
.confirm-item .label { width: 80px; color: #909399; }
.confirm-item .value { flex: 1; color: #303133; font-weight: 500; }

.pagination { margin-top: 20px; display: flex; justify-content: center; }

@media (max-width: 768px) {
  .header-box { flex-direction: column; align-items: flex-start; }
  .filter-box { width: 100%; }
  .stat-cards .el-col { margin-bottom: 10px; }
}
</style>