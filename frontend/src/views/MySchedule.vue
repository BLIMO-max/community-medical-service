<!-- 文件：src/views/MySchedule.vue -->
<template>
  <div class="my-schedule-container">
    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :span="6" v-for="item in overviewData" :key="item.title">
        <el-card class="overview-card" :body-style="{ padding: '15px' }" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon" :style="{ background: item.bgColor }">
              <el-icon :size="20" :color="item.color"><component :is="item.icon" /></el-icon>
            </div>
            <div class="overview-info">
              <span class="overview-label">{{ item.title }}</span>
              <span class="overview-value">{{ item.value }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 排班日历 -->
    <el-card class="calendar-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Calendar /></el-icon> 我的排班</span>
          <div class="header-right">
            <el-radio-group v-model="viewType" size="small">
              <el-radio-button label="week">周视图</el-radio-button>
              <el-radio-button label="list">列表视图</el-radio-button>
            </el-radio-group>
            <el-button-group class="date-nav">
              <el-button size="small" @click="prevPeriod">
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <el-button size="small" @click="today">今天</el-button>
              <el-button size="small" @click="nextPeriod">
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <!-- 周视图 -->
      <div v-if="viewType === 'week'" class="week-view">
        <el-table :data="weekTableData" border stripe>
          <el-table-column label="时段" width="120" align="center">
            <template #default="{ row }">
              {{ row.timeLabel }}
            </template>
          </el-table-column>
          <el-table-column
            v-for="day in weekDays"
            :key="day.date"
            :label="day.label"
            align="center"
          >
            <template #default="{ row }">
              <div v-if="getScheduleForDay(day.date, row.timeSlot)" class="schedule-cell">
                <el-tag :type="row.timeSlot === 1 ? 'success' : 'warning'" size="small">
                  出诊
                </el-tag>
                <div class="schedule-detail">
                  <span>限额: {{ getScheduleForDay(day.date, row.timeSlot).maxCount }}</span>
                  <span>已约: {{ getScheduleForDay(day.date, row.timeSlot).bookedCount }}</span>
                </div>
              </div>
              <div v-else class="empty-cell">
                <span>休息</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 列表视图 -->
      <div v-else class="list-view">
        <el-table :data="paginatedSchedules" v-loading="loading" stripe border>
          <el-table-column prop="workDate" label="日期" width="120" sortable />
          <el-table-column label="时段" width="150" align="center">
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
          <el-table-column label="预约进度" min-width="150">
            <template #default="scope">
              <el-progress 
                :percentage="getPercentage(scope.row.bookedCount, scope.row.maxCount)" 
                :status="scope.row.bookedCount >= scope.row.maxCount ? 'exception' : 'success'"
                :stroke-width="8"
              />
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="filteredSchedules.length === 0 && !loading" description="暂无排班" />

        <el-pagination
          v-if="filteredSchedules.length > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20]"
          layout="total, sizes, prev, pager, next"
          class="pagination"
        />
      </div>
    </el-card>

    <!-- 今日预约患者 -->
    <el-card class="appointments-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><User /></el-icon> 今日预约患者</span>
          <el-button type="primary" size="small" @click="loadTodayAppointments">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>

      <el-table :data="todayAppointments" stripe border>
        <el-table-column prop="timeSlotText" label="时段" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.timeSlot === 1 ? 'success' : 'warning'" size="small">
              {{ scope.row.timeSlot === 1 ? '上午' : '下午' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="patientName" label="患者姓名" width="100" />
        <el-table-column prop="patientAge" label="年龄" width="60" align="center" />
        <el-table-column prop="patientGender" label="性别" width="60" align="center" />
        <el-table-column prop="patientPhone" label="联系电话" width="130" />
        <el-table-column prop="createTime" label="预约时间" width="150">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="viewPatient(scope.row)">
              <el-icon><View /></el-icon>查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="todayAppointments.length === 0" description="今日暂无预约患者" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'                                 // 🔧 新增导入
import { ElMessage } from 'element-plus'
import { Calendar, ArrowLeft, ArrowRight, User, Refresh, View } from '@element-plus/icons-vue'

const API_BASE = 'http://localhost:8080'                 // 🔧 新增 API 前缀

const user = JSON.parse(localStorage.getItem('user') || '{}')

// 视图
const viewType = ref('week')
const currentDate = ref(new Date())
const loading = ref(false)

// 数据
const schedules = ref([])
const todayAppointments = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 概览数据
const overviewData = ref([
  { title: '本周出诊', value: 0, icon: 'Calendar', bgColor: '#ecf5ff', color: '#409EFF' },
  { title: '今日预约', value: 0, icon: 'User', bgColor: '#f0f9eb', color: '#67C23A' },
  { title: '本周预约', value: 0, icon: 'Document', bgColor: '#fdf6ec', color: '#E6A23C' },
  { title: '总排班数', value: 0, icon: 'List', bgColor: '#fef0f0', color: '#F56C6C' }
])

// 周视图表格数据
const weekTableData = [
  { timeSlot: 1, timeLabel: '上午 (8:00-12:00)' },
  { timeSlot: 2, timeLabel: '下午 (14:00-18:00)' }
]

// 计算属性
const weekDays = computed(() => {
  const days = []
  const startOfWeek = new Date(currentDate.value)
  startOfWeek.setDate(currentDate.value.getDate() - currentDate.value.getDay() + 1)
  
  const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(startOfWeek)
    date.setDate(startOfWeek.getDate() + i)
    days.push({
      date: date.toISOString().split('T')[0],
      label: `${date.getMonth() + 1}/${date.getDate()} ${weekdays[i]}`
    })
  }
  return days
})

const filteredSchedules = computed(() => {
  return schedules.value.filter(s => {
    if (viewType.value === 'week') {
      const weekStart = weekDays.value[0].date
      const weekEnd = weekDays.value[6].date
      return s.workDate >= weekStart && s.workDate <= weekEnd
    }
    return true
  }).sort((a, b) => {
    if (a.workDate !== b.workDate) return a.workDate.localeCompare(b.workDate)
    return a.timeSlot - b.timeSlot
  })
})

const total = computed(() => filteredSchedules.value.length)

const paginatedSchedules = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredSchedules.value.slice(start, start + pageSize.value)
})

onMounted(() => {
  loadSchedules()
  loadTodayAppointments()
})

// 加载排班
const loadSchedules = () => {
  if (!user.id) return
  
  loading.value = true
  axios.get(`${API_BASE}/schedule/doctor/${user.id}`)
    .then(res => {
      loading.value = false
      if (res.data.code === 200) {
        schedules.value = res.data.data || []
        updateOverview()
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

// 加载今日预约
const loadTodayAppointments = () => {
  if (!user.id) return
  
  const today = new Date().toISOString().split('T')[0]
  
  axios.get(`${API_BASE}/appointment/doctor/${user.id}/patients`, {
    params: { date: today }
  })
    .then(res => {
      if (res.data.code === 200) {
        todayAppointments.value = res.data.data || []
      }
    })
    .catch(error => {
      console.error('加载今日预约失败:', error)
    })
}

// 更新概览
const updateOverview = () => {
  const today = new Date().toISOString().split('T')[0]
  
  // 本周排班
  const weekStart = weekDays.value[0]?.date
  const weekEnd = weekDays.value[6]?.date
  const weekSchedules = schedules.value.filter(s => 
    s.workDate >= weekStart && s.workDate <= weekEnd
  )
  
  overviewData.value[0].value = weekSchedules.length
  overviewData.value[1].value = schedules.value
    .filter(s => s.workDate === today)
    .reduce((sum, s) => sum + (s.bookedCount || 0), 0)
  overviewData.value[2].value = weekSchedules.reduce((sum, s) => sum + (s.bookedCount || 0), 0)
  overviewData.value[3].value = schedules.value.length
}

// 获取指定日期的排班
const getScheduleForDay = (date, timeSlot) => {
  return schedules.value.find(s => s.workDate === date && s.timeSlot === timeSlot)
}

// 获取百分比
const getPercentage = (booked, max) => {
  if (!max) return 0
  return Math.round((booked / max) * 100)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 导航
const prevPeriod = () => {
  const date = new Date(currentDate.value)
  date.setDate(date.getDate() - 7)
  currentDate.value = date
  loadSchedules()
}

const nextPeriod = () => {
  const date = new Date(currentDate.value)
  date.setDate(date.getDate() + 7)
  currentDate.value = date
  loadSchedules()
}

const today = () => {
  currentDate.value = new Date()
  loadSchedules()
}

// 查看患者
const viewPatient = (row) => {
  ElMessage.info(`患者: ${row.patientName}, 联系电话: ${row.patientPhone}`)
}
</script>

<style scoped>
/* 样式与原文件一致 */
.my-schedule-container { padding: 20px; }
.overview-cards { margin-bottom: 20px; }
.overview-card { transition: all 0.3s; }
.overview-card:hover { transform: translateY(-3px); }
.overview-item { display: flex; align-items: center; gap: 12px; }
.overview-icon { width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.overview-info { display: flex; flex-direction: column; }
.overview-label { font-size: 12px; color: #909399; }
.overview-value { font-size: 20px; font-weight: bold; color: #303133; }
.calendar-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-right { display: flex; gap: 15px; align-items: center; }
.week-view { margin-top: 10px; }
.schedule-cell { padding: 8px; }
.schedule-detail { display: flex; flex-direction: column; font-size: 12px; margin-top: 5px; color: #606266; }
.empty-cell { color: #c0c4cc; font-size: 13px; }
.list-view { margin-top: 10px; }
.appointments-card { margin-bottom: 20px; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
.full { color: #f56c6c; font-weight: bold; }
</style>