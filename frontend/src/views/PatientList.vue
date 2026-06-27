<!-- 文件：src/views/PatientList.vue -->
<template>
  <div class="patient-list-container">
    <!-- 搜索和筛选栏 -->
    <el-card class="search-card" shadow="hover">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索患者姓名/病历号"
            :prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="就诊状态" clearable @change="handleSearch">
            <el-option label="待就诊" value="waiting" />
            <el-option label="就诊中" value="visiting" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-date-picker
            v-model="searchForm.date"
            type="date"
            placeholder="就诊日期"
            format="YYYY-MM-DD"
            @change="handleSearch"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="10" class="operation-btns">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button type="success" @click="exportList">
            <el-icon><Download /></el-icon>导出列表
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in patientStats" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '15px' }" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" :style="{ background: stat.bgColor }">
              <el-icon :size="20" :color="stat.color"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">{{ stat.title }}</span>
              <span class="stat-value">{{ stat.value }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 患者列表标签页 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="今日待就诊" name="today" />
            <el-tab-pane label="全部患者" name="all" />
            <el-tab-pane label="历史患者" name="history" />
          </el-tabs>
          <div class="header-right">
            <el-button type="primary" size="small" @click="startConsultation">
              开始接诊
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="patientList" v-loading="loading" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="visitTime" label="就诊时间" width="120" sortable>
          <template #default="scope">
            {{ scope.row.visitTime || scope.row.appointmentTime }}
          </template>
        </el-table-column>
        <el-table-column prop="patientName" label="患者姓名" width="100" />
        <el-table-column prop="patientAge" label="年龄" width="60" align="center" />
        <el-table-column prop="patientGender" label="性别" width="60" align="center" />
        <el-table-column prop="medicalNo" label="病历号" width="120" />
        <el-table-column prop="complaint" label="主诉" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="viewPatientDetail(scope.row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button type="success" link @click="startDiagnosis(scope.row)" :disabled="scope.row.status !== 'waiting'">
              <el-icon><Edit /></el-icon>接诊
            </el-button>
            <el-button type="info" link @click="viewMedicalRecord(scope.row)">
              <el-icon><Document /></el-icon>病历
            </el-button>
            <el-button type="warning" link @click="prescribe(scope.row)" :disabled="scope.row.status === 'waiting'">
              <el-icon><Reading /></el-icon>开方
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
      />
    </el-card>

    <!-- 患者详情对话框 -->
    <el-dialog v-model="detailVisible" title="患者详情" width="700px">
      <div v-if="selectedPatient" class="patient-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="患者姓名">{{ selectedPatient.patientName }}</el-descriptions-item>
          <el-descriptions-item label="病历号">{{ selectedPatient.medicalNo }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ selectedPatient.patientGender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ selectedPatient.patientAge }}岁</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedPatient.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="就诊时间">{{ selectedPatient.visitTime }}</el-descriptions-item>
          <el-descriptions-item label="主诉" :span="2">{{ selectedPatient.complaint || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="既往史" :span="2">{{ selectedPatient.history || '无' }}</el-descriptions-item>
          <el-descriptions-item label="过敏史" :span="2">{{ selectedPatient.allergy || '无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 历史就诊记录 -->
        <div class="history-section" v-if="selectedPatient.historyRecords && selectedPatient.historyRecords.length > 0">
          <h4>历史就诊记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(record, index) in selectedPatient.historyRecords"
              :key="index"
              :timestamp="record.date"
              type="primary"
            >
              {{ record.diagnosis }} - {{ record.doctor }}
            </el-timeline-item>
          </el-timeline>
        </div>
        <el-empty v-else description="暂无历史就诊记录" :image-size="80" />
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button type="primary" @click="startDiagnosisFromDetail">开始接诊</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 接诊对话框 -->
    <el-dialog v-model="diagnosisVisible" title="接诊" width="600px">
      <el-form :model="diagnosisForm" label-width="100px">
        <el-form-item label="主诉">
          <el-input v-model="diagnosisForm.complaint" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="现病史">
          <el-input v-model="diagnosisForm.presentIllness" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="既往史">
          <el-input v-model="diagnosisForm.pastHistory" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="体格检查">
          <el-input v-model="diagnosisForm.physicalExam" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="初步诊断">
          <el-input v-model="diagnosisForm.diagnosis" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="处理意见">
          <el-input v-model="diagnosisForm.treatment" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="处方">
          <el-input v-model="diagnosisForm.prescription" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="复诊建议">
          <el-input v-model="diagnosisForm.followUp" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="diagnosisVisible = false">取消</el-button>
          <el-button type="primary" @click="saveDiagnosis" :loading="saving">
            保存并开始诊疗
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'                                 // 🔧 新增导入
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download, View, Edit, Document, Reading } from '@element-plus/icons-vue'

const API_BASE = 'http://localhost:8080'                 // 🔧 新增 API 前缀

// 当前医生信息
const doctorInfo = JSON.parse(localStorage.getItem('user') || '{}')

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  date: ''
})

// 标签页
const activeTab = ref('today')

// 列表数据
const loading = ref(false)
const patientList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框
const detailVisible = ref(false)
const diagnosisVisible = ref(false)
const selectedPatient = ref(null)
const saving = ref(false)

// 接诊表单
const diagnosisForm = reactive({
  complaint: '',
  presentIllness: '',
  pastHistory: '',
  physicalExam: '',
  diagnosis: '',
  treatment: '',
  prescription: '',
  followUp: ''
})

// 患者统计
const patientStats = ref([
  { title: '今日待就诊', value: 0, icon: 'Timer', bgColor: '#fdf6ec', color: '#E6A23C' },
  { title: '今日已接诊', value: 0, icon: 'CircleCheck', bgColor: '#f0f9eb', color: '#67C23A' },
  { title: '总患者数', value: 0, icon: 'User', bgColor: '#ecf5ff', color: '#409EFF' },
  { title: '复诊患者', value: 0, icon: 'Refresh', bgColor: '#fef0f0', color: '#F56C6C' }
])

onMounted(() => {
  loadPatientList()
})

const user = JSON.parse(localStorage.getItem('user') || '{}')

// 加载患者列表
const loadPatientList = () => {
  if (!user.id) {
    ElMessage.warning('请先登录')
    return
  }
  loading.value = true
  const today = activeTab.value === 'today' ? new Date().toISOString().split('T')[0] : null
  
  axios.get(`${API_BASE}/appointment/doctor/${user.id}/patients`, {
    params: { date: today }
  })
    .then(res => {
      loading.value = false
      if (res.data.code === 200) {
        patientList.value = (res.data.data || []).map(item => ({
          id: item.id,
          patientName: item.patientName,
          patientAge: item.patientAge,
          patientGender: item.patientGender,
          medicalNo: item.medicalNo,
          phone: item.patientPhone,
          visitTime: `${item.workDate} ${item.timeSlot === 1 ? '上午' : '下午'}`,
          complaint: item.complaint || '暂无',
          status: item.status === 0 ? 'waiting' : 'completed'
        }))
        total.value = patientList.value.length
        updateStats()
      } else {
        ElMessage.error(res.data.msg || '加载失败')
      }
    })
    .catch(error => {
      loading.value = false
      console.error('加载患者列表失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    })
}

// 更新统计
const updateStats = () => {
  const stats = patientStats.value
  stats[0].value = patientList.value.filter(p => p.status === 'waiting').length
  stats[1].value = patientList.value.filter(p => p.status === 'visiting').length
  stats[2].value = patientList.value.length
  stats[3].value = patientList.value.filter(p => p.historyRecords?.length > 0).length
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadPatientList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.date = ''
  handleSearch()
}

// 标签页切换
const handleTabChange = () => {
  currentPage.value = 1
  loadPatientList()
}

// 导出列表
const exportList = () => {
  ElMessage.success('导出功能开发中')
}

// 开始接诊
const startConsultation = () => {
  ElMessage.info('请选择患者开始接诊')
}

// 查看患者详情
const viewPatientDetail = (row) => {
  // 加载患者的历史病历
  if (row.patientId) {
    axios.get(`${API_BASE}/medical-record/patient/${row.patientId}`)
      .then(res => {
        if (res.data.code === 200) {
          row.historyRecords = (res.data.data || []).map(r => ({
            date: r.visitDate,
            diagnosis: r.diagnosis,
            doctor: r.doctorName
          }))
        }
        selectedPatient.value = row
        detailVisible.value = true
      })
      .catch(() => {
        selectedPatient.value = row
        detailVisible.value = true
      })
  } else {
    selectedPatient.value = row
    detailVisible.value = true
  }
}

// 从详情页开始接诊
const startDiagnosisFromDetail = () => {
  if (selectedPatient.value) {
    detailVisible.value = false
    diagnosisForm.complaint = selectedPatient.value.complaint || ''
    diagnosisForm.pastHistory = selectedPatient.value.history || ''
    diagnosisForm.presentIllness = ''
    diagnosisForm.physicalExam = ''
    diagnosisForm.diagnosis = ''
    diagnosisForm.treatment = ''
    diagnosisForm.prescription = ''
    diagnosisForm.followUp = ''
    diagnosisVisible.value = true
  }
}

// 开始诊断
const startDiagnosis = (row) => {
  selectedPatient.value = row
  diagnosisForm.complaint = row.complaint
  diagnosisForm.pastHistory = row.history
  diagnosisVisible.value = true
}

// 保存诊断
const saveDiagnosis = () => {
  if (!selectedPatient.value) return
  
  saving.value = true
  
  const medicalRecord = {
    patientId: selectedPatient.value.patientId || selectedPatient.value.id,
    doctorId: user.id,
    medicalNo: 'M' + new Date().toISOString().slice(0,10).replace(/-/g,'') + 
               String(Math.floor(Math.random() * 1000)).padStart(3, '0'),
    visitDate: new Date().toISOString().split('T')[0],
    complaint: diagnosisForm.complaint || selectedPatient.value.complaint,
    presentIllness: diagnosisForm.presentIllness,
    pastHistory: diagnosisForm.pastHistory,
    physicalExam: diagnosisForm.physicalExam,
    diagnosis: diagnosisForm.diagnosis,
    treatment: diagnosisForm.treatment,
    prescription: diagnosisForm.prescription,
    followUp: diagnosisForm.followUp,
    status: 1
  }
  
  axios.post(`${API_BASE}/medical-record/create`, medicalRecord)
    .then(res => {
      saving.value = false
      if (res.data.code === 200) {
        diagnosisVisible.value = false
        ElMessage.success('病历保存成功')
        
        if (selectedPatient.value.appointmentId) {
          axios.put(`${API_BASE}/appointment/complete/${selectedPatient.value.appointmentId}`)
        }
        
        loadPatientList()
        resetDiagnosisForm()
      } else {
        ElMessage.error(res.data.msg || '保存失败')
      }
    })
    .catch(error => {
      saving.value = false
      console.error('保存失败:', error)
      ElMessage.error('网络错误')
    })
}

const resetDiagnosisForm = () => {
  diagnosisForm.complaint = ''
  diagnosisForm.presentIllness = ''
  diagnosisForm.pastHistory = ''
  diagnosisForm.physicalExam = ''
  diagnosisForm.diagnosis = ''
  diagnosisForm.treatment = ''
  diagnosisForm.prescription = ''
  diagnosisForm.followUp = ''
}

// 查看病历
const viewMedicalRecord = (row) => {
  ElMessage.info('查看病历功能开发中')
}

// 开处方
const prescribe = (row) => {
  ElMessage.info('开方功能开发中')
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    waiting: 'warning',
    visiting: 'success',
    completed: 'info'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    waiting: '待就诊',
    visiting: '就诊中',
    completed: '已完成'
  }
  return map[status] || '未知'
}
</script>

<style scoped>
/* 样式保持不变，与原文件一致 */
.patient-list-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.stat-cards { margin-bottom: 20px; }
.stat-card { transition: all 0.3s; }
.stat-card:hover { transform: translateY(-3px); }
.stat-item { display: flex; align-items: center; gap: 12px; }
.stat-icon { width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.stat-info { display: flex; flex-direction: column; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value { font-size: 20px; font-weight: bold; color: #303133; }
.operation-btns { display: flex; gap: 10px; justify-content: flex-end; }
.table-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-right { display: flex; gap: 10px; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
.patient-detail { padding: 20px; }
.history-section { margin-top: 20px; }
.history-section h4 { margin: 0 0 15px; color: #303133; }
</style>