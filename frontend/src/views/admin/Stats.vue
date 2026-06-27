<template>
  <div class="stats-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="item in statistics" :key="item.title">
        <el-card class="stat-card" shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-item">
            <div class="stat-icon" :style="{ background: item.bgColor }">
              <el-icon :size="24" :color="item.color">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">{{ item.title }}</span>
              <span class="stat-value">{{ item.value }}</span>
              <span class="stat-trend" v-if="item.trend">
                <el-icon :color="item.trend > 0 ? '#67C23A' : '#F56C6C'">
                  <ArrowUp v-if="item.trend > 0" />
                  <ArrowDown v-else />
                </el-icon>
                {{ Math.abs(item.trend) }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图（使用表格形式展示） -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><DataLine /></el-icon> 预约趋势</span>
              <el-radio-group v-model="trendRange" size="small" @change="loadTrendData">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">全年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          
          <!-- 趋势数据表格 -->
          <el-table :data="trendData" stripe border size="small">
            <el-table-column prop="date" label="日期" />
            <el-table-column prop="count" label="预约量" align="center">
              <template #default="scope">
                <el-progress 
                  :percentage="scope.row.percentage" 
                  :status="scope.row.percentage > 80 ? 'warning' : 'success'"
                  :stroke-width="15"
                  striped
                />
                <span style="margin-left: 10px;">{{ scope.row.count }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="compare" label="环比" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.compare > 0 ? 'success' : 'danger'" size="small">
                  {{ scope.row.compare > 0 ? '+' : '' }}{{ scope.row.compare }}%
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span><el-icon><PieChart /></el-icon> 科室预约分布</span>
          </template>
          
          <!-- 科室分布列表 -->
          <div v-for="item in deptDistribution" :key="item.name" class="dept-item">
            <div class="dept-info">
              <span>{{ item.name }}</span>
              <span>{{ item.value }} ({{ item.percentage }}%)</span>
            </div>
            <el-progress 
              :percentage="item.percentage" 
              :color="item.color"
              :stroke-width="12"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 表格数据 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <span><el-icon><User /></el-icon> 医生工作量排行</span>
          </template>
          <el-table :data="doctorWorkload" v-loading="loading.doctor" stripe>
            <el-table-column prop="rank" label="排名" width="60" align="center">
              <template #default="scope">
                <el-tag :type="scope.$index < 3 ? 'danger' : 'info'" circle>
                  {{ scope.$index + 1 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="doctorName" label="医生姓名" />
            <el-table-column prop="department" label="科室" />
            <el-table-column prop="appointmentCount" label="接诊量" align="center" />
            <el-table-column prop="completionRate" label="完成率" align="center" width="120">
              <template #default="scope">
                <el-progress 
                  :percentage="scope.row.completionRate" 
                  :width="50"
                  type="circle"
                  :stroke-width="6"
                  size="small"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <span><el-icon><Document /></el-icon> 疾病排行榜</span>
          </template>
          <el-table :data="diseaseRank" v-loading="loading.disease" stripe>
            <el-table-column prop="rank" label="排名" width="60" align="center">
              <template #default="scope">
                <el-tag :type="scope.$index < 3 ? 'warning' : 'info'" circle>
                  {{ scope.$index + 1 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="diseaseName" label="疾病名称" />
            <el-table-column prop="count" label="发病人数" align="center" />
            <el-table-column prop="percentage" label="占比" align="center" width="120">
              <template #default="scope">
                <el-progress 
                  :percentage="scope.row.percentage" 
                  :stroke-width="12"
                  striped
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 收入统计 -->
    <el-card class="revenue-card" shadow="hover">
      <template #header>
        <span><el-icon><Money /></el-icon> 收入统计</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in revenueStats" :key="item.title">
          <div class="revenue-item">
            <span class="revenue-label">{{ item.title }}</span>
            <span class="revenue-value">¥{{ item.value.toFixed(2) }}</span>
            <span class="revenue-compare" :class="{ up: item.compare > 0, down: item.compare < 0 }">
              <el-icon :size="12">
                <ArrowUp v-if="item.compare > 0" />
                <ArrowDown v-else />
              </el-icon>
              {{ Math.abs(item.compare) }}%
            </span>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  DataLine, 
  PieChart, 
  User, 
  Document, 
  Money,
  ArrowUp,
  ArrowDown
} from '@element-plus/icons-vue'

// 加载状态
const loading = reactive({
  statistics: false,
  trend: false,
  dept: false,
  doctor: false,
  disease: false,
  revenue: false
})

// 统计数据卡片
const statistics = ref([
  { 
    title: '总预约量', 
    value: 1248, 
    icon: 'DataLine', 
    bgColor: '#ecf5ff', 
    color: '#409EFF',
    trend: 12.5 
  },
  { 
    title: '今日预约', 
    value: 86, 
    icon: 'Calendar', 
    bgColor: '#f0f9eb', 
    color: '#67C23A',
    trend: -3.2 
  },
  { 
    title: '就诊人数', 
    value: 1123, 
    icon: 'User', 
    bgColor: '#fdf6ec', 
    color: '#E6A23C',
    trend: 8.1 
  },
  { 
    title: '总收入', 
    value: '¥156,800', 
    icon: 'Money', 
    bgColor: '#fef0f0', 
    color: '#F56C6C',
    trend: 15.3 
  }
])

// 趋势图范围
const trendRange = ref('week')

// 趋势数据
const trendData = ref([
  { date: '2026-01-15', count: 45, percentage: 45, compare: 5.2 },
  { date: '2026-01-16', count: 52, percentage: 52, compare: 15.6 },
  { date: '2026-01-17', count: 48, percentage: 48, compare: -7.7 },
  { date: '2026-01-18', count: 63, percentage: 63, compare: 31.3 },
  { date: '2026-01-19', count: 58, percentage: 58, compare: -7.9 },
  { date: '2026-01-20', count: 71, percentage: 71, compare: 22.4 },
  { date: '2026-01-21', count: 55, percentage: 55, compare: -22.5 }
])

// 科室分布
const deptDistribution = ref([
  { name: '内科', value: 328, percentage: 32, color: '#409EFF' },
  { name: '外科', value: 256, percentage: 25, color: '#67C23A' },
  { name: '儿科', value: 198, percentage: 19, color: '#E6A23C' },
  { name: '妇科', value: 156, percentage: 15, color: '#F56C6C' },
  { name: '骨科', value: 92, percentage: 9, color: '#909399' }
])

// 医生工作量
const doctorWorkload = ref([
  { doctorName: '张明', department: '内科', appointmentCount: 156, completionRate: 98 },
  { doctorName: '李芳', department: '外科', appointmentCount: 142, completionRate: 95 },
  { doctorName: '王强', department: '儿科', appointmentCount: 138, completionRate: 92 },
  { doctorName: '赵霞', department: '妇科', appointmentCount: 125, completionRate: 96 },
  { doctorName: '刘伟', department: '骨科', appointmentCount: 98, completionRate: 94 }
])

// 疾病排行
const diseaseRank = ref([
  { diseaseName: '上呼吸道感染', count: 245, percentage: 24 },
  { diseaseName: '高血压', count: 186, percentage: 18 },
  { diseaseName: '糖尿病', count: 152, percentage: 15 },
  { diseaseName: '急性胃肠炎', count: 98, percentage: 10 },
  { diseaseName: '关节炎', count: 76, percentage: 8 }
])

// 收入统计
const revenueStats = ref([
  { title: '门诊收入', value: 98600, compare: 12.5 },
  { title: '药品收入', value: 45800, compare: 8.3 },
  { title: '检查收入', value: 12400, compare: -2.1 },
  { title: '其他收入', value: 5600, compare: 5.7 }
])

onMounted(() => {
  loadAllData()
})

// 加载所有数据
const loadAllData = () => {
  // 这里将来替换为真实的API调用
  console.log('加载统计数据')
}

// 加载趋势数据
const loadTrendData = () => {
  console.log('加载趋势数据，范围：', trendRange.value)
  // 这里将来调用真实的API
}
</script>

<style scoped>
.stats-container {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
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
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dept-item {
  margin-bottom: 15px;
}

.dept-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 14px;
}

.table-card {
  margin-bottom: 20px;
}

.revenue-card {
  margin-bottom: 20px;
}

.revenue-item {
  text-align: center;
  padding: 15px;
  border-right: 1px solid #ebeef5;
}

.revenue-item:last-child {
  border-right: none;
}

.revenue-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.revenue-value {
  display: block;
  font-size: 22px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.revenue-compare {
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.revenue-compare.up {
  color: #67C23A;
}

.revenue-compare.down {
  color: #F56C6C;
}
</style>