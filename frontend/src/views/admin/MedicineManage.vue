<template>
  <div class="medicine-manage-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索药品名称/编码"
            :prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-col>
        <el-col :span="14" class="operation-btns">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增药品
          </el-button>
          <el-button type="warning" @click="showLowStock">
            <el-icon><Warning /></el-icon>库存预警
            <el-badge v-if="lowStockCount > 0" :value="lowStockCount" class="badge" />
          </el-button>
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 药品列表 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <span><el-icon><Medicine /></el-icon> 药品库存列表</span>
      </template>

      <el-table :data="medicineList" v-loading="loading" stripe border>
        <el-table-column prop="code" label="药品编码" width="120" />
        <el-table-column prop="name" label="药品名称" width="150" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="unit" label="单位" width="60" align="center" />
        <el-table-column prop="price" label="单价" width="100" align="right">
          <template #default="scope">
            ¥{{ scope.row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" align="center">
          <template #default="scope">
            <span :class="{ 'low-stock': scope.row.stock <= scope.row.minStock }">
              {{ scope.row.stock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="预警线" width="80" align="center" />
        <el-table-column prop="manufacturer" label="生产厂家" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="scope">
            <el-button type="success" link @click="handleInStock(scope.row)">
              <el-icon><Upload /></el-icon>入库
            </el-button>
            <el-button type="warning" link @click="handleOutStock(scope.row)">
              <el-icon><Download /></el-icon>出库
            </el-button>
            <el-button type="primary" link @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">
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

    <!-- 药品表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="medicineForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="药品编码" prop="code">
          <el-input v-model="medicineForm.code" />
        </el-form-item>
        <el-form-item label="药品名称" prop="name">
          <el-input v-model="medicineForm.name" />
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="medicineForm.specification" placeholder="如：0.25g*20粒" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-select v-model="medicineForm.unit" style="width: 100%">
            <el-option label="盒" value="盒" />
            <el-option label="瓶" value="瓶" />
            <el-option label="支" value="支" />
            <el-option label="袋" value="袋" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="medicineForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="初始库存" prop="stock">
          <el-input-number v-model="medicineForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存预警线" prop="minStock">
          <el-input-number v-model="medicineForm.minStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="medicineForm.manufacturer" />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input v-model="medicineForm.remarks" type="textarea" rows="2" />
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

    <!-- 入库/出库对话框 -->
    <el-dialog v-model="stockDialogVisible" :title="stockType === 'in' ? '药品入库' : '药品出库'" width="400px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="药品名称">
          <span>{{ currentMedicine?.name }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ currentMedicine?.stock }}</span>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="stockForm.quantity" :min="1" :max="stockType === 'out' ? currentMedicine?.stock : 999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="stockForm.operator" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.reason" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stockDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStock" :loading="stockSubmitting">
            确认{{ stockType === 'in' ? '入库' : '出库' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 库存预警对话框 -->
    <el-dialog v-model="lowStockVisible" title="库存预警" width="600px">
      <el-table :data="lowStockList" stripe border>
        <el-table-column prop="code" label="药品编码" />
        <el-table-column prop="name" label="药品名称" />
        <el-table-column prop="stock" label="当前库存" align="center">
          <template #default="scope">
            <el-tag type="danger">{{ scope.row.stock }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="预警线" align="center" />
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleInStock(scope.row)">
              立即补货
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="lowStockList.length === 0" description="暂无库存预警" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Warning, Download, Medicine, Upload, Edit, Delete } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('user') || '{}')

// 列表数据
const loading = ref(false)
const medicineList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref(null)

// 库存对话框
const stockDialogVisible = ref(false)
const stockType = ref('in')
const currentMedicine = ref(null)
const stockForm = reactive({ quantity: 1, operator: '', reason: '' })
const stockSubmitting = ref(false)

// 预警对话框
const lowStockVisible = ref(false)
const lowStockList = ref([])

// 药品表单
const medicineForm = reactive({
  id: '',
  code: '',
  name: '',
  specification: '',
  unit: '盒',
  price: 0,
  stock: 0,
  minStock: 10,
  manufacturer: '',
  remarks: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }]
}

// 预警数量
const lowStockCount = computed(() => lowStockList.value.length)

onMounted(() => {
  loadMedicineList()
  loadLowStock()
})

// 加载药品列表
const loadMedicineList = () => {
  loading.value = true
  axios.get('http://localhost:8080/medicine/list', {
    params: { 
      pageNum: currentPage.value, 
      pageSize: pageSize.value, 
      keyword: searchKeyword.value 
    }
  }).then(res => {
    loading.value = false
    if (res.data.code === 200) {
      const data = res.data.data
      medicineList.value = data.records || data
      total.value = data.total || medicineList.value.length
    } else {
      ElMessage.error(res.data.msg || '加载失败')
    }
  }).catch(error => {
    loading.value = false
    console.error('加载药品失败:', error)
    ElMessage.error('网络错误')
  })
}

// 模拟数据
const loadMockData = () => {
  const mockList = [
    { id: 1, code: 'M001', name: '阿莫西林胶囊', specification: '0.25g*20粒', unit: '盒', price: 25.5, stock: 156, minStock: 20, manufacturer: '华北制药' },
    { id: 2, code: 'M002', name: '布洛芬缓释片', specification: '0.3g*10片', unit: '盒', price: 18.9, stock: 89, minStock: 15, manufacturer: '中美史克' },
    { id: 3, code: 'M003', name: '头孢克肟胶囊', specification: '0.1g*6粒', unit: '盒', price: 32.0, stock: 45, minStock: 10, manufacturer: '白云山' },
    { id: 4, code: 'M004', name: '维生素C片', specification: '100片/瓶', unit: '瓶', price: 12.8, stock: 234, minStock: 30, manufacturer: '汤臣倍健' },
    { id: 5, code: 'M005', name: '感冒灵颗粒', specification: '10袋/盒', unit: '盒', price: 15.5, stock: 67, minStock: 15, manufacturer: '三九医药' }
  ]
  medicineList.value = mockList
  total.value = mockList.length
}

// 加载库存预警
const loadLowStock = () => {
  axios.get('http://localhost:8080/medicine/low-stock').then(res => {
    if (res.data.code === 200) {
      lowStockList.value = res.data.data
    }
  }).catch(() => {
    lowStockList.value = medicineList.value.filter(m => m.stock <= m.minStock)
  })
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadMedicineList()
}

const resetSearch = () => {
  searchKeyword.value = ''
  handleSearch()
}

// 新增药品
const handleAdd = () => {
  dialogTitle.value = '新增药品'
  Object.assign(medicineForm, {
    id: '', code: '', name: '', specification: '', unit: '盒', price: 0, stock: 0, minStock: 10, manufacturer: '', remarks: ''
  })
  dialogVisible.value = true
}

// 编辑药品
const handleEdit = (row) => {
  dialogTitle.value = '编辑药品'
  Object.assign(medicineForm, row)
  dialogVisible.value = true
}

// 删除药品
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除药品 ${row.name} 吗？`, '提示', { type: 'warning' })
    .then(() => {
      ElMessage.success('删除成功')
      loadMedicineList()
    })
}

// 入库
const handleInStock = (row) => {
  currentMedicine.value = row
  stockType.value = 'in'
  stockForm.quantity = 1
  stockForm.operator = user.realName || user.username
  stockForm.reason = ''
  stockDialogVisible.value = true
}

// 出库
const handleOutStock = (row) => {
  currentMedicine.value = row
  stockType.value = 'out'
  stockForm.quantity = 1
  stockForm.operator = user.realName || user.username
  stockForm.reason = ''
  stockDialogVisible.value = true
}

// 提交库存操作
const submitStock = () => {
  if (!stockForm.quantity || stockForm.quantity <= 0) {
    ElMessage.warning('请输入数量')
    return
  }
  if (!stockForm.operator) {
    ElMessage.warning('请输入操作人')
    return
  }
  if (stockType.value === 'out' && stockForm.quantity > currentMedicine.value.stock) {
    ElMessage.warning('库存不足')
    return
  }

  stockSubmitting.value = true
  
  const url = stockType.value === 'in' 
    ? 'http://localhost:8080/medicine/in-stock'
    : 'http://localhost:8080/medicine/out-stock'
    
  axios.post(url, null, {
    params: {
      id: currentMedicine.value.id,
      quantity: stockForm.quantity,
      operator: stockForm.operator,
      reason: stockForm.reason
    }
  })
    .then(res => {
      stockSubmitting.value = false
      if (res.data.code === 200) {
        stockDialogVisible.value = false
        ElMessage.success(`${stockType.value === 'in' ? '入库' : '出库'}成功`)
        loadMedicineList()
        loadLowStock()
      } else {
        ElMessage.error(res.data.msg || '操作失败')
      }
    })
    .catch(error => {
      stockSubmitting.value = false
      console.error('操作失败:', error)
      ElMessage.error('网络错误')
    })
}

// 提交表单
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      setTimeout(() => {
        submitting.value = false
        dialogVisible.value = false
        ElMessage.success(`${dialogTitle.value}成功`)
        loadMedicineList()
      }, 500)
    }
  })
}

// 显示预警
const showLowStock = () => {
  lowStockVisible.value = true
}

// 导出
const handleExport = () => {
  ElMessage.success('导出成功')
}
</script>

<style scoped>
.medicine-manage-container {
  padding: 20px;
}
.search-card {
  margin-bottom: 20px;
}
.operation-btns {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.table-card {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.low-stock {
  color: #f56c6c;
  font-weight: bold;
}
.badge {
  margin-left: 5px;
}
</style>