<template>
  <div class="login-wrapper">
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="40" color="#409EFF"><UserFilled /></el-icon>
        <h2 class="title">新用户注册</h2>
        <p class="subtitle">加入社区医疗，享受便捷服务</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名 (必填)" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码 (必填)" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名 (就诊使用)" :prefix-icon="Postcard" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号码" :prefix-icon="Iphone" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleRegister">立即注册</el-button>
        </el-form-item>
        <div class="tips">
          <el-link type="primary" @click="$router.push('/login')">已有账号？去登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Postcard, Iphone } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  role: 2 // 默认注册为居民
})

const validatePass2 = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const handleRegister = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      
      const submitData = {
        username: form.username,
        password: form.password,
        realName: form.realName,
        phone: form.phone,
        role: 2
      }
      
      axios.post('http://localhost:8080/user/register', submitData)
        .then(res => {
          loading.value = false
          if (res.data.code === 200) {
            ElMessage.success('注册成功，请登录')
            router.push('/login')
          } else {
            ElMessage.error(res.data.msg || '注册失败')
          }
        })
        .catch(error => {
          loading.value = false
          console.error('注册失败:', error)
          ElMessage.error('网络错误，请稍后重试')
        })
    }
  })
}</script>

<style scoped>
/* 复用 Login.vue 的样式 */
.login-wrapper {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}
.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  text-align: center;
}
.login-header { margin-bottom: 20px; }
.title { margin: 10px 0 5px; color: #303133; font-size: 24px; }
.subtitle { color: #909399; font-size: 14px; margin: 0; }
.login-btn { width: 100%; font-weight: bold; letter-spacing: 2px; }
.tips { margin-top: 15px; font-size: 14px; }
</style>