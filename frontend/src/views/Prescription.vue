<template>
  <div class="prescription-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-date-picker
            v-model="searchDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="handleSearch"
          />
        </el-col>
        <el-col :span="6">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索诊断/药品" 
            clearable 
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="10" class="operation-btns">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '15px' }" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #ecf5ff">
              <el-icon :size="20" color="#409EFF"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">处方总数</span>
              <span class="stat-value">{{ stats.prescriptionCount }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '15px' }" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f0f9eb">
              <el-icon :size="20" color="#67C23A"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">近30天</span>
              <span class="stat-value">{{ stats.recentCount }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '15px' }" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #fdf6ec">
              <el-icon :size="20" color="#E6A23C"><User /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">就诊医生</span>
              <span class="stat-value">{{ stats.doctorCount }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" :body-style="{ padding: '15px' }" shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #fef0f0">
              <el-icon :size="20" color="#F56C6C"><FirstAidKit /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">病历总数</span>
              <span class="stat-value">{{ stats.totalRecords }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 处方列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Document /></el-icon> 我的处方记录</span>
          <span class="total-count">共 {{ total }} 条记录</span>
        </div>
      </template>

      <el-table :data="paginatedList" v-loading="loading" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="visitDate" label="就诊日期" width="120" sortable />
        <el-table-column prop="doctorName" label="开方医生" width="120" />
        <el-table-column prop="diagnosis" label="诊断结果" min-width="150" show-overflow-tooltip />
        <el-table-column prop="prescription" label="处方内容" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.prescription">{{ scope.row.prescription }}</span>
            <span v-else class="empty-text">暂无处方</span>
          </template>
        </el-table-column>
        <el-table-column prop="medicalNo" label="病历号" width="140" />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="viewDetail(scope.row)"
            >
              <el-icon><View /></el-icon>详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="filteredList.length === 0 && !loading" description="暂无处方记录" />

      <el-pagination
        v-if="total > 0"
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

    <!-- 处方详情对话框 -->
    <el-dialog v-model="detailVisible" title="处方详情" width="600px">
      <div v-if="selectedRecord" class="prescription-detail">
        <div class="detail-header">
          <h3>处方单</h3>
          <div class="prescription-no">病历号：{{ selectedRecord.medicalNo }}</div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="患者姓名">{{ userInfo.realName || userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ selectedRecord.visitDate }}</el-descriptions-item>
          <el-descriptions-item label="就诊医生">{{ selectedRecord.doctorName }}</el-descriptions-item>
          <el-descriptions-item label="诊断结果">{{ selectedRecord.diagnosis }}</el-descriptions-item>
          <el-descriptions-item label="主诉" :span="2">{{ selectedRecord.complaint || '无' }}</el-descriptions-item>
          <el-descriptions-item label="处方" :span="2">
            <div class="prescription-content">{{ selectedRecord.prescription || '无' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="处理意见" :span="2">{{ selectedRecord.treatment || '无' }}</el-descriptions-item>
          <el-descriptions-item label="复诊建议" :span="2">{{ selectedRecord.followUp || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div class="footer-note">
          <p>医生签名：{{ selectedRecord.doctorName }}</p>
          <p>社区就诊服务系统</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button type="primary" @click="printPrescription">打印</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Document, View, Calendar, User, FirstAidKit } from '@element-plus/icons-vue'

const API_BASE = 'http://localhost:8080'

// 获取用户信息
const getUserInfo = () => {
  try {
    return JSON.parse(localStorage.getItem('user') || '{}')
  } catch (e) {
    return {}
  }
}

const userInfo = getUserInfo()

// 数据
const loading = ref(false)
const allRecords = ref([])
const filteredList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索
const searchDateRange = ref([])
const searchKeyword = ref('')

// 详情对话框
const detailVisible = ref(false)
const selectedRecord = ref(null)

// 统计数据
const stats = reactive({
  prescriptionCount: 0,
  recentCount: 0,
  doctorCount: 0,
  totalRecords: 0
})

onMounted(() => {
  loadMedicalRecords()
})

// 加载病历记录
const loadMedicalRecords = () => {
  if (!userInfo.id) {
    console.log('用户未登录')
    return
  }
  
  loading.value = true
  
  axios.get(`${API_BASE}/medical-record/patient/${userInfo.id}`)
    .then(res => {
      loading.value = false
      console.log('病历数据:', res.data)
      if (res.data.code === 200) {
        allRecords.value = res.data.data || []
        filterRecords()
      } else {
        console.error('加载失败:', res.data.msg)
        allRecords.value = []
        filteredList.value = []
        total.value = 0
      }
    })
    .catch(error => {
      loading.value = false
      console.error('加载病历失败:', error)
      allRecords.value = []
      filteredList.value = []
      total.value = 0
    })
}

// 过滤记录
const filterRecords = () => {
  let list = [...allRecords.value]
  
  // 只显示有处方的记录
  list = list.filter(r => r.prescription && r.prescription.trim() !== '')
  
  // 日期范围筛选
  if (searchDateRange.value && searchDateRange.value.length === 2) {
    const startDate = searchDateRange.value[0]
    const endDate = searchDateRange.value[1]
    list = list.filter(r => r.visitDate >= startDate && r.visitDate <= endDate)
  }
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(r => 
      (r.diagnosis && r.diagnosis.toLowerCase().includes(keyword)) ||
      (r.prescription && r.prescription.toLowerCase().includes(keyword)) ||
      (r.doctorName && r.doctorName.toLowerCase().includes(keyword))
    )
  }
  
  // 按日期倒序
  list.sort((a, b) => new Date(b.visitDate) - new Date(a.visitDate))
  
  filteredList.value = list
  total.value = list.length
  currentPage.value = 1
  
  // 更新统计
  updateStats()
}

// 更新统计数据
const updateStats = () => {
  const prescriptionRecords = allRecords.value.filter(r => r.prescription && r.prescription.trim() !== '')
  stats.prescriptionCount = prescriptionRecords.length
  stats.totalRecords = allRecords.value.length
  
  // 近30天
  const thirtyDaysAgo = new Date()
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
  stats.recentCount = prescriptionRecords.filter(r => {
    const visitDate = new Date(r.visitDate)
    return visitDate >= thirtyDaysAgo
  }).length
  
  // 就诊医生数
  const doctors = new Set(prescriptionRecords.map(r => r.doctorName).filter(Boolean))
  stats.doctorCount = doctors.size
}

// 搜索
const handleSearch = () => {
  filterRecords()
}

// 重置搜索
const resetSearch = () => {
  searchDateRange.value = []
  searchKeyword.value = ''
  filterRecords()
}

// 查看详情
const viewDetail = (row) => {
  selectedRecord.value = row
  detailVisible.value = true
}

// 打印处方
const printPrescription = () => {
  ElMessage.info('打印功能开发中')
}

// 分页处理
const handleSizeChange = () => {
  currentPage.value = 1
}

const handleCurrentChange = () => {}

// 分页数据
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})
</script>

<style scoped>
.prescription-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.operation-btns {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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
  gap: 12px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-count {
  font-size: 14px;
  color: #909399;
}

.empty-text {
  color: #c0c4cc;
  font-style: italic;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.prescription-detail {
  padding: 20px;
}

.detail-header {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #409EFF;
}

.detail-header h3 {
  margin: 0 0 10px;
  font-size: 20px;
  color: #303133;
}

.prescription-no {
  font-size: 14px;
  color: #909399;
}

.prescription-content {
  font-weight: 500;
  color: #303133;
  line-height: 1.6;
}

.footer-note {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed #ebeef5;
  display: flex;
  justify-content: space-between;
  color: #909399;
}
</style>