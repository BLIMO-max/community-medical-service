<!-- views/MedicalRecord.vue -->
<template>
  <div class="medical-record-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" shadow="hover">
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

    <!-- 病历列表 -->
    <el-card class="record-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Document /></el-icon> 我的病历记录</span>
          <div class="filter-box">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              @change="handleSearch"
            />
            <el-input
              v-model="keyword"
              placeholder="搜索诊断"
              :prefix-icon="Search"
              clearable
              size="small"
              style="width: 180px; margin-left: 10px;"
              @input="handleSearch"
            />
          </div>
        </div>
      </template>

      <el-table :data="recordList" v-loading="loading" stripe border>
        <el-table-column prop="visitDate" label="就诊日期" width="120" sortable />
        <el-table-column prop="doctorName" label="就诊医生" width="120" />
        <el-table-column prop="diagnosis" label="诊断结果" min-width="150" show-overflow-tooltip />
        <el-table-column prop="complaint" label="主诉" width="150" show-overflow-tooltip />
        <el-table-column prop="treatment" label="处理意见" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">
              <el-icon><View /></el-icon>详情
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

    <!-- 病历详情对话框 -->
    <el-dialog v-model="detailVisible" title="病历详情" width="700px">
      <div v-if="selectedRecord" class="record-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="就诊日期">{{ selectedRecord.visitDate }}</el-descriptions-item>
          <el-descriptions-item label="就诊医生">{{ selectedRecord.doctorName }}</el-descriptions-item>
          <el-descriptions-item label="主诉" :span="2">{{ selectedRecord.complaint || '无' }}</el-descriptions-item>
          <el-descriptions-item label="现病史" :span="2">{{ selectedRecord.presentIllness || '无' }}</el-descriptions-item>
          <el-descriptions-item label="既往史" :span="2">{{ selectedRecord.pastHistory || '无' }}</el-descriptions-item>
          <el-descriptions-item label="体格检查" :span="2">{{ selectedRecord.physicalExam || '无' }}</el-descriptions-item>
          <el-descriptions-item label="诊断结果" :span="2">
            <el-tag type="danger">{{ selectedRecord.diagnosis }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理意见" :span="2">{{ selectedRecord.treatment || '无' }}</el-descriptions-item>
          <el-descriptions-item label="处方" :span="2">{{ selectedRecord.prescription || '无' }}</el-descriptions-item>
          <el-descriptions-item label="复诊建议" :span="2">{{ selectedRecord.followUp || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Document, Search, View } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('user') || '{}')

// 数据
const loading = ref(false)
const recordList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索
const dateRange = ref([])
const keyword = ref('')

// 详情对话框
const detailVisible = ref(false)
const selectedRecord = ref(null)

// 统计数据
const stats = ref([
  { title: '总病历数', value: 0, icon: 'Document', bgColor: '#ecf5ff', color: '#409EFF' },
  { title: '近30天', value: 0, icon: 'Calendar', bgColor: '#f0f9eb', color: '#67C23A' },
  { title: '就诊医生', value: 0, icon: 'User', bgColor: '#fdf6ec', color: '#E6A23C' },
  { title: '慢性病史', value: 0, icon: 'Warning', bgColor: '#fef0f0', color: '#F56C6C' }
])

onMounted(() => {
  loadRecordList()
})

// 加载病历列表
const loadRecordList = () => {
  if (!user.id) return
  loading.value = true
  
  axios.get(`http://localhost:8080/medical-record/patient/${user.id}`)
    .then(res => {
      loading.value = false
      if (res.data.code === 200) {
        recordList.value = res.data.data || []
        total.value = recordList.value.length
        updateStats()
      } else {
        ElMessage.error(res.data.msg || '加载病历失败')
      }
    })
    .catch(error => {
      loading.value = false
      console.error('加载病历失败:', error)
      ElMessage.error('网络错误')
    })
}

// 模拟数据
const loadMockData = () => {
  const mockList = []
  for (let i = 0; i < 20; i++) {
    const date = new Date()
    date.setDate(date.getDate() - i * 7)
    mockList.push({
      id: i + 1,
      visitDate: date.toISOString().split('T')[0],
      doctorName: ['张明', '李芳', '王强'][Math.floor(Math.random() * 3)],
      diagnosis: ['上呼吸道感染', '高血压', '急性胃炎', '偏头痛'][Math.floor(Math.random() * 4)],
      complaint: ['头痛发热', '咳嗽咳痰', '腹痛腹泻', '胸闷气短'][Math.floor(Math.random() * 4)],
      treatment: '对症治疗，注意休息',
      presentIllness: '患者于3天前出现症状',
      pastHistory: '既往体健',
      physicalExam: '生命体征平稳',
      prescription: '阿莫西林胶囊 0.5g tid',
      followUp: '一周后复诊'
    })
  }
  recordList.value = mockList
  total.value = mockList.length
  updateStats()
}

// 更新统计
const updateStats = () => {
  stats.value[0].value = recordList.value.length
  const thirtyDaysAgo = new Date()
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
  stats.value[1].value = recordList.value.filter(r => new Date(r.visitDate) >= thirtyDaysAgo).length
  const doctors = new Set(recordList.value.map(r => r.doctorName))
  stats.value[2].value = doctors.size
  const chronicDiseases = ['高血压', '糖尿病', '冠心病', '慢性胃炎']
  stats.value[3].value = recordList.value.filter(r => 
    chronicDiseases.some(d => r.diagnosis.includes(d))
  ).length
}

// 搜索
const handleSearch = () => {
  let list = [...recordList.value]
  if (keyword.value) {
    list = list.filter(r => r.diagnosis.includes(keyword.value))
  }
  if (dateRange.value && dateRange.value.length === 2) {
    list = list.filter(r => r.visitDate >= dateRange.value[0] && r.visitDate <= dateRange.value[1])
  }
  recordList.value = list
  total.value = list.length
}

// 查看详情
const viewDetail = (row) => {
  selectedRecord.value = row
  detailVisible.value = true
}
</script>

<style scoped>
.medical-record-container {
  padding: 20px;
}
.stat-cards {
  margin-bottom: 20px;
}
.stat-card {
  transition: all 0.3s;
}
.stat-card:hover {
  transform: translateY(-3px);
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}
.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 13px;
  color: #909399;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.record-detail {
  padding: 20px;
}
</style>