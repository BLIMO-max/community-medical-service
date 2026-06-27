<template>
  <div class="health-container">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><User /></el-icon> 基本信息</span>
              <el-button type="primary" link @click="editProfile">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
            </div>
          </template>
          
          <div class="profile-info">
            <el-avatar :size="80" class="profile-avatar">
              {{ userInfo.realName?.charAt(0) || '用' }}
            </el-avatar>
            <div class="info-list">
              <div class="info-item">
                <span class="label">姓名：</span>
                <span class="value">{{ userInfo.realName || '未填写' }}</span>
              </div>
              <div class="info-item">
                <span class="label">用户名：</span>
                <span class="value">{{ userInfo.username || '未填写' }}</span>
              </div>
              <div class="info-item">
                <span class="label">性别：</span>
                <span class="value">{{ userInfo.gender || '未填写' }}</span>
              </div>
              <div class="info-item">
                <span class="label">年龄：</span>
                <span class="value">{{ userInfo.age ? userInfo.age + '岁' : '未填写' }}</span>
              </div>
              <div class="info-item">
                <span class="label">血型：</span>
                <span class="value">{{ userInfo.bloodType || '未填写' }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机：</span>
                <span class="value">{{ userInfo.phone || '未填写' }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 过敏史卡片 -->
        <el-card class="allergy-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><Warning /></el-icon> 过敏史</span>
              <el-button type="primary" link @click="editAllergy">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
            </div>
          </template>
          
          <div class="allergy-list">
            <el-tag 
              v-for="item in allergies" 
              :key="item"
              type="danger"
              class="allergy-tag"
            >
              {{ item }}
            </el-tag>
            <el-empty v-if="allergies.length === 0" description="暂无过敏史" :image-size="60" />
          </div>
        </el-card>
      </el-col>

      <!-- 健康指标卡片 -->
      <el-col :span="16">
        <el-card class="metrics-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><DataLine /></el-icon> 健康指标</span>
              <el-button type="primary" @click="addMetric" size="small">
                <el-icon><Plus /></el-icon>新增记录
              </el-button>
            </div>
          </template>

          <el-row :gutter="20">
            <el-col :span="8" v-for="metric in metrics" :key="metric.name">
              <el-card class="metric-item" :body-style="{ padding: '15px' }" shadow="hover">
                <div class="metric-header">
                  <span class="metric-name">{{ metric.name }}</span>
                  <span class="metric-unit">{{ metric.unit }}</span>
                </div>
                <div class="metric-value" :class="metric.status">
                  {{ metric.value }}
                </div>
                <div class="metric-range">正常范围: {{ metric.normalRange }}</div>
                <div class="metric-time">测量时间: {{ metric.time }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>

        <!-- 健康趋势图表 -->
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><TrendCharts /></el-icon> 血压趋势</span>
              <el-radio-group v-model="chartRange" size="small">
                <el-radio-button label="week">最近一周</el-radio-button>
                <el-radio-button label="month">最近一月</el-radio-button>
                <el-radio-button label="year">最近一年</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <div class="chart-placeholder">
            <el-empty description="图表展示区域（开发中）" />
          </div>
        </el-card>

        <!-- 健康建议卡片 -->
        <el-card class="advice-card" shadow="hover">
          <template #header>
            <span><el-icon><Message /></el-icon> 健康建议</span>
          </template>
          
          <el-timeline>
            <el-timeline-item
              v-for="advice in healthAdvice"
              :key="advice.date"
              :timestamp="advice.date"
              :type="advice.type"
            >
              {{ advice.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑个人信息对话框 -->
    <el-dialog v-model="profileDialog" title="编辑个人信息" width="500px">
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="真实姓名">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="userForm.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="userForm.age" :min="1" :max="120" style="width: 100%" />
        </el-form-item>
        <el-form-item label="血型">
          <el-select v-model="userForm.bloodType" placeholder="请选择血型" style="width: 100%">
            <el-option label="A型" value="A" />
            <el-option label="B型" value="B" />
            <el-option label="O型" value="O" />
            <el-option label="AB型" value="AB" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="profileDialog = false">取消</el-button>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑过敏史对话框 -->
    <el-dialog v-model="allergyDialog" title="编辑过敏史" width="500px">
      <el-form label-width="100px">
        <el-form-item label="过敏药物">
          <el-select 
            v-model="newAllergy" 
            filterable 
            allow-create 
            default-first-option
            placeholder="请输入或选择过敏药物"
            style="width: 100%"
          >
            <el-option 
              v-for="item in commonAllergies" 
              :key="item" 
              :label="item" 
              :value="item" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="已添加">
          <el-tag 
            v-for="item in tempAllergies" 
            :key="item"
            type="danger"
            closable
            @close="removeTempAllergy(item)"
            class="allergy-tag"
          >
            {{ item }}
          </el-tag>
          <el-button type="primary" link @click="addTempAllergy" :disabled="!newAllergy">
            <el-icon><Plus /></el-icon>添加
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="allergyDialog = false">取消</el-button>
          <el-button type="primary" @click="saveAllergy">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Edit, Warning, DataLine, TrendCharts, Message, Plus } from '@element-plus/icons-vue'
import axios from 'axios'

const API_BASE = 'http://localhost:8080'

// 获取当前用户
const getCurrentUser = () => {
  try {
    return JSON.parse(localStorage.getItem('user') || '{}')
  } catch (e) {
    return {}
  }
}

// 用户信息
const userInfo = reactive({
  id: '',
  username: '',
  realName: '',
  gender: '',
  age: null,
  bloodType: '',
  phone: ''
})

// 加载用户信息
const loadUserInfo = () => {
  const stored = getCurrentUser()
  Object.assign(userInfo, stored)
}

// 用户表单
const userForm = reactive({
  id: '',
  realName: '',
  gender: '',
  age: null,
  bloodType: '',
  phone: ''
})

// 过敏史
const allergies = ref(['青霉素', '磺胺类'])
const tempAllergies = ref([])
const newAllergy = ref('')
const commonAllergies = ['青霉素', '头孢类', '磺胺类', '阿司匹林', '布洛芬', '花粉', '海鲜', '鸡蛋', '牛奶']

// 健康指标（模拟数据）
const metrics = ref([
  {
    name: '收缩压',
    value: 118,
    unit: 'mmHg',
    normalRange: '90-140',
    time: new Date().toLocaleString(),
    status: 'normal'
  },
  {
    name: '舒张压',
    value: 82,
    unit: 'mmHg',
    normalRange: '60-90',
    time: new Date().toLocaleString(),
    status: 'normal'
  },
  {
    name: '心率',
    value: 72,
    unit: '次/分',
    normalRange: '60-100',
    time: new Date().toLocaleString(),
    status: 'normal'
  }
])

// 健康建议
const healthAdvice = ref([
  {
    date: new Date().toISOString().split('T')[0],
    type: 'primary',
    content: '根据您的健康档案，建议保持规律作息，适量运动。'
  }
])

// 图表范围
const chartRange = ref('week')

// 对话框
const profileDialog = ref(false)
const allergyDialog = ref(false)
const saving = ref(false)

onMounted(() => {
  loadUserInfo()
})

// 编辑个人信息
const editProfile = () => {
  Object.assign(userForm, {
    id: userInfo.id,
    realName: userInfo.realName || '',
    gender: userInfo.gender || '',
    age: userInfo.age,
    bloodType: userInfo.bloodType || '',
    phone: userInfo.phone || ''
  })
  profileDialog.value = true
}

// 保存个人信息
const saveProfile = () => {
  saving.value = true
  
  axios.put(`${API_BASE}/user/update`, userForm)
    .then(res => {
      saving.value = false
      if (res.data.code === 200) {
        // 更新本地数据
        Object.assign(userInfo, userForm)
        // 更新 localStorage
        const stored = getCurrentUser()
        Object.assign(stored, userForm)
        localStorage.setItem('user', JSON.stringify(stored))
        
        profileDialog.value = false
        ElMessage.success('保存成功')
      } else {
        ElMessage.error(res.data.msg || '保存失败')
      }
    })
    .catch(error => {
      saving.value = false
      console.error('保存失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    })
}

// 编辑过敏史
const editAllergy = () => {
  tempAllergies.value = [...allergies.value]
  newAllergy.value = ''
  allergyDialog.value = true
}

// 添加临时过敏史
const addTempAllergy = () => {
  if (newAllergy.value && !tempAllergies.value.includes(newAllergy.value)) {
    tempAllergies.value.push(newAllergy.value)
    newAllergy.value = ''
  }
}

// 移除临时过敏史
const removeTempAllergy = (item) => {
  tempAllergies.value = tempAllergies.value.filter(a => a !== item)
}

// 保存过敏史
const saveAllergy = () => {
  allergies.value = [...tempAllergies.value]
  allergyDialog.value = false
  ElMessage.success('过敏史已更新')
  // 注意：过敏史目前没有后端接口，只保存在前端
  // 如需保存到数据库，需要在 user 表添加 allergy 字段
}

// 新增健康指标
const addMetric = () => {
  ElMessage.info('新增健康指标功能开发中')
}
</script>

<style scoped>
.health-container {
  padding: 20px;
}

.profile-card,
.allergy-card,
.metrics-card,
.chart-card,
.advice-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-info {
  text-align: center;
}

.profile-avatar {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #409EFF, #36d1dc);
  color: white;
  font-size: 32px;
}

.info-list {
  text-align: left;
}

.info-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}

.info-item .label {
  width: 80px;
  color: #909399;
}

.info-item .value {
  flex: 1;
  color: #303133;
  font-weight: 500;
}

.allergy-list {
  min-height: 100px;
}

.allergy-tag {
  margin: 0 8px 8px 0;
}

.metric-item {
  margin-bottom: 15px;
  transition: all 0.3s;
}

.metric-item:hover {
  transform: translateY(-3px);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.metric-name {
  font-size: 14px;
  color: #909399;
}

.metric-unit {
  font-size: 12px;
  color: #909399;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.metric-value.warning {
  color: #E6A23C;
}

.metric-value.danger {
  color: #F56C6C;
}

.metric-range {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.metric-time {
  font-size: 12px;
  color: #909399;
}

.chart-placeholder {
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9f9f9;
  border-radius: 4px;
}
</style>