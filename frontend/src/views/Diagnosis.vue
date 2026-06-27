<!-- 文件：src/views/Diagnosis.vue -->
<template>
  <div class="diagnosis-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索患者姓名/病历号"
            :prefix-icon="Search"
            clearable
          />
        </el-col>
        <el-col :span="4">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.diagnosis" placeholder="诊断类型" clearable>
            <el-option label="上呼吸道感染" value="uri" />
            <el-option label="高血压" value="hypertension" />
            <el-option label="糖尿病" value="diabetes" />
            <el-option label="胃炎" value="gastritis" />
          </el-select>
        </el-col>
        <el-col :span="10" class="operation-btns">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button type="success" @click="exportDiagnosis">
            <el-icon><Download /></el-icon>导出记录
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 诊断记录列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon><Document /></el-icon> 诊断记录</span>
          <el-button type="primary" size="small" @click="addDiagnosis">
            <el-icon><Plus /></el-icon>新建诊断
          </el-button>
        </div>
      </template>

      <el-table :data="diagnosisList" v-loading="loading" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="date" label="诊断日期" width="120" sortable />
        <el-table-column prop="patientName" label="患者姓名" width="100" />
        <el-table-column prop="patientAge" label="年龄" width="60" align="center" />
        <el-table-column prop="patientGender" label="性别" width="60" align="center" />
        <el-table-column prop="medicalNo" label="病历号" width="120" />
        <el-table-column prop="complaint" label="主诉" width="150" show-overflow-tooltip />
        <el-table-column prop="diagnosis" label="诊断" width="150" show-overflow-tooltip />
        <el-table-column prop="prescription" label="处方" width="150" show-overflow-tooltip />
        <el-table-column prop="followUp" label="复诊建议" width="120" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button type="success" link @click="editDiagnosis(scope.row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" link @click="deleteDiagnosis(scope.row)">
              <el-icon><Delete /></el-icon>删除
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

    <!-- 诊断详情对话框 -->
    <el-dialog v-model="detailVisible" title="诊断详情" width="700px">
      <div v-if="selectedDiagnosis" class="diagnosis-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="患者姓名">{{ selectedDiagnosis.patientName }}</el-descriptions-item>
          <el-descriptions-item label="病历号">{{ selectedDiagnosis.medicalNo }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ selectedDiagnosis.patientGender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ selectedDiagnosis.patientAge }}岁</el-descriptions-item>
          <el-descriptions-item label="诊断日期">{{ selectedDiagnosis.date }}</el-descriptions-item>
          <el-descriptions-item label="接诊医生">{{ selectedDiagnosis.doctorName }}</el-descriptions-item>
          <el-descriptions-item label="主诉" :span="2">{{ selectedDiagnosis.complaint }}</el-descriptions-item>
          <el-descriptions-item label="现病史" :span="2">{{ selectedDiagnosis.presentIllness }}</el-descriptions-item>
          <el-descriptions-item label="既往史" :span="2">{{ selectedDiagnosis.pastHistory }}</el-descriptions-item>
          <el-descriptions-item label="体格检查" :span="2">{{ selectedDiagnosis.physicalExam }}</el-descriptions-item>
          <el-descriptions-item label="诊断结果" :span="2">{{ selectedDiagnosis.diagnosis }}</el-descriptions-item>
          <el-descriptions-item label="处理意见" :span="2">{{ selectedDiagnosis.treatment }}</el-descriptions-item>
          <el-descriptions-item label="处方" :span="2">{{ selectedDiagnosis.prescription }}</el-descriptions-item>
          <el-descriptions-item label="复诊建议" :span="2">{{ selectedDiagnosis.followUp }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ selectedDiagnosis.notes || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 诊断表单对话框 -->
    <el-dialog v-model="formVisible" :title="formType === 'add' ? '新建诊断' : '编辑诊断'" width="600px">
      <el-form :model="diagnosisForm" label-width="100px">
        <el-form-item label="患者姓名">
          <el-select v-model="diagnosisForm.patientId" placeholder="选择患者" filterable>
            <el-option
              v-for="patient in patientOptions"
              :key="patient.id"
              :label="patient.name"
              :value="patient.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="主诉">
          <el-input v-model="diagnosisForm.complaint" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="现病史">
          <el-input v-model="diagnosisForm.presentIllness" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="既往史">
          <el-input v-model="diagnosisForm.pastHistory" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="体格检查">
          <el-input v-model="diagnosisForm.physicalExam" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="诊断结果">
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
        <el-form-item label="备注">
          <el-input v-model="diagnosisForm.notes" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="formVisible = false">取消</el-button>
          <el-button type="primary" @click="saveDiagnosis" :loading="saving">
            保存
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
import { Search, Refresh, Download, Document, Plus, View, Edit, Delete } from '@element-plus/icons-vue'

const API_BASE = 'http://localhost:8080'                 // 🔧 新增 API 前缀

// 搜索表单
const searchForm = reactive({
  keyword: '',
  dateRange: [],
  diagnosis: ''
})

// 列表数据
const loading = ref(false)
const diagnosisList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框
const detailVisible = ref(false)
const formVisible = ref(false)
const formType = ref('add')
const selectedDiagnosis = ref(null)
const saving = ref(false)

// 诊断表单
const diagnosisForm = reactive({
  patientId: '',
  complaint: '',
  presentIllness: '',
  pastHistory: '',
  physicalExam: '',
  diagnosis: '',
  treatment: '',
  prescription: '',
  followUp: '',
  notes: ''
})

// 患者选项
const patientOptions = ref([
  { id: 1, name: '张三' },
  { id: 2, name: '李四' },
  { id: 3, name: '王五' },
  { id: 4, name: '赵六' }
])

onMounted(() => {
  loadDiagnosisList()
})

// 加载诊断记录
const loadDiagnosisList = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  if (!user.id) {
    ElMessage.warning('请先登录')
    return
  }
  
  loading.value = true
  axios.get(`${API_BASE}/medical-record/doctor/${user.id}`)
    .then(res => {
      loading.value = false
      if (res.data.code === 200) {
        diagnosisList.value = (res.data.data || []).map(item => ({
          id: item.id,
          date: item.visitDate,
          patientName: item.patientName,
          patientAge: item.patientAge,
          patientGender: item.patientGender,
          medicalNo: item.medicalNo,
          complaint: item.complaint,
          diagnosis: item.diagnosis,
          prescription: item.prescription,
          followUp: item.followUp,
          presentIllness: item.presentIllness,
          pastHistory: item.pastHistory,
          physicalExam: item.physicalExam,
          treatment: item.treatment,
          notes: item.notes,
          doctorName: item.doctorName
        }))
        total.value = diagnosisList.value.length
      } else {
        ElMessage.error(res.data.msg || '加载失败')
      }
    })
    .catch(error => {
      loading.value = false
      console.error('加载诊断记录失败:', error)
      ElMessage.error('网络错误')
    })
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadDiagnosisList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.dateRange = []
  searchForm.diagnosis = ''
  handleSearch()
}

// 导出记录
const exportDiagnosis = () => {
  ElMessage.success('导出成功')
}

// 新建诊断
const addDiagnosis = () => {
  formType.value = 'add'
  Object.keys(diagnosisForm).forEach(key => {
    diagnosisForm[key] = ''
  })
  formVisible.value = true
}

// 查看详情
const viewDetail = (row) => {
  selectedDiagnosis.value = row
  detailVisible.value = true
}

// 编辑诊断
const editDiagnosis = (row) => {
  formType.value = 'edit'
  Object.assign(diagnosisForm, row)
  formVisible.value = true
}

// 删除诊断
const deleteDiagnosis = (row) => {
  ElMessageBox.confirm('确定删除该诊断记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    loadDiagnosisList()
  })
}

// 保存诊断
const saveDiagnosis = () => {
  saving.value = true
  setTimeout(() => {
    saving.value = false
    formVisible.value = false
    ElMessage.success(`${formType.value === 'add' ? '新建' : '编辑'}成功`)
    loadDiagnosisList()
  }, 1000)
}
</script>

<style scoped>
.diagnosis-container { padding: 20px; }
.search-card { margin-bottom: 20px; }
.operation-btns { display: flex; gap: 10px; justify-content: flex-end; }
.table-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
.diagnosis-detail { padding: 20px; }
</style>