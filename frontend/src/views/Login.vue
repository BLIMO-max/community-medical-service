<!-- views/Login.vue - 完整文件 -->
<template>
  <div class="login-wrapper">
    <!-- 动态粒子背景 -->
    <div class="background">
      <div class="gradient-bg"></div>
      <div class="particles">
        <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
      </div>
      <!-- 医疗图标浮动元素 -->
      <div class="floating-elements">
        <div class="floating-icon cross">✚</div>
        <div class="floating-icon heart">❤️</div>
        <div class="floating-icon pulse">📊</div>
        <div class="floating-icon capsule">💊</div>
      </div>
    </div>

    <div class="login-box">
      <div class="login-header">
        <div class="logo-animation">
          <el-icon :size="50" color="#409EFF" class="pulse-icon">
            <FirstAidKit />
          </el-icon>
        </div>
        <h2 class="title">社区就诊服务系统</h2>
        <p class="subtitle">守护您的健康，便捷每一天</p>
      </div>

      <el-form :model="form" size="large" class="login-form">
        <el-form-item>
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            class="custom-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            class="custom-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-radio-group v-model="form.role" class="role-group">
            <el-radio-button :label="2">👤 我是居民</el-radio-button>
            <el-radio-button :label="1">👨‍⚕️ 我是医生</el-radio-button>
            <el-radio-button :label="0">⚙️ 管理员</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            native-type="button"
            @click="handleLogin"
          >
            <span v-if="!loading">立即登录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>

        <div class="tips">
          <span>没有账号？</span>
          <el-link type="primary" @click="$router.push('/register')" class="register-link">
            立即注册 <el-icon><ArrowRight /></el-icon>
          </el-link>
        </div>
      </el-form>

      <div class="footer-decoration">
        <div class="wave wave1"></div>
        <div class="wave wave2"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, FirstAidKit, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '', role: 2 })

// 生成随机粒子样式
const getParticleStyle = (i) => {
  const size = Math.random() * 6 + 2
  const left = Math.random() * 100
  const delay = Math.random() * 20
  const duration = Math.random() * 20 + 10
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号密码')
    return
  }

  loading.value = true
  
  // 🔧 临时使用模拟登录（后端未启动时）
//  setTimeout(() => {
   // loading.value = false
    
    //const mockUser = {
      //id: 1,
      //username: form.username,
      //realName: '测试用户',
      //role: Number(form.role)
    //}
    
    //localStorage.setItem('user', JSON.stringify(mockUser))
    //ElMessage.success('欢迎回来！(模拟登录)')
    //router.push('/home/appointment')
  // }, 1000)
  
  // 真实登录代码（后端启动后取消注释上面代码，注释掉模拟登录）
  try {
    const res = await axios.post('http://127.0.0.1:8080/user/login', {
      username: form.username,
      password: form.password
    })

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg || '登录失败')
      return
    }

    const user = res.data.data
    if (!user) {
      ElMessage.error('登录返回数据为空')
      return
    }

    const serverRole = Number(user.role)
    const selectedRole = Number(form.role)

    if (serverRole !== selectedRole) {
      ElMessage.error('身份选择错误，请检查您的角色')
      return
    }

    localStorage.setItem('user', JSON.stringify(user))
    ElMessage.success('欢迎回来！')
    router.push('/home/appointment')
  } catch (e) {
    console.error('登录异常：', e)
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.gradient-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(125deg, #667eea 0%, #764ba2 50%, #6b8cff 100%);
  background-size: 400% 400%;
  animation: gradient 15s ease infinite;
}

@keyframes gradient {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.particle {
  position: absolute;
  bottom: -100px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  animation: float 15s infinite linear;
}

@keyframes float {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(-1000px) rotate(720deg);
    opacity: 0;
  }
}

.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.floating-icon {
  position: absolute;
  font-size: 24px;
  color: rgba(255, 255, 255, 0.2);
  animation: floatIcon 20s infinite linear;
}

.floating-icon.cross { top: 10%; left: 10%; animation-duration: 25s; }
.floating-icon.heart { top: 70%; right: 15%; animation-duration: 22s; }
.floating-icon.pulse { bottom: 20%; left: 20%; animation-duration: 28s; }
.floating-icon.capsule { top: 40%; right: 25%; animation-duration: 30s; }

@keyframes floatIcon {
  0% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(30px, -50px) rotate(120deg); }
  66% { transform: translate(-20px, 20px) rotate(240deg); }
  100% { transform: translate(0, 0) rotate(360deg); }
}

.login-box {
  width: 450px;
  padding: 50px 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 30px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  text-align: center;
  z-index: 2;
  position: relative;
  animation: slideUp 0.8s ease-out;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  margin-bottom: 40px;
  position: relative;
}

.logo-animation {
  margin-bottom: 20px;
}

.pulse-icon {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.title {
  margin: 15px 0 8px;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
  background: linear-gradient(45deg, #2c3e50, #3498db);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  color: #7f8c8d;
  font-size: 16px;
  margin: 0;
  letter-spacing: 1px;
}

.custom-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 18px rgba(64, 158, 255, 0.3);
  transform: translateY(-2px);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 18px rgba(64, 158, 255, 0.4);
}

.role-group {
  display: flex;
  width: 100%;
  gap: 5px;
}

.role-group :deep(.el-radio-button__inner) {
  padding: 12px 20px;
  font-size: 14px;
  border-radius: 12px !important;
  transition: all 0.3s;
}

.role-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(45deg, #409EFF, #36d1dc);
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 4px;
  background: linear-gradient(45deg, #409EFF, #36d1dc);
  border: none;
  border-radius: 25px;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(64, 158, 255, 0.5);
}

.login-btn:active {
  transform: translateY(-1px);
}

.tips {
  margin-top: 25px;
  font-size: 14px;
  color: #7f8c8d;
}

.register-link {
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.register-link:hover {
  transform: translateX(5px);
}

.footer-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 60px;
  overflow: hidden;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 30px;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320"><path fill="%23409eff" fill-opacity="0.2" d="M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,154.7C960,171,1056,181,1152,170.7C1248,160,1344,128,1392,112L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path></svg>');
  background-size: cover;
  animation: wave 10s linear infinite;
}

.wave1 { bottom: 0; opacity: 0.5; animation: wave 15s linear infinite; }
.wave2 { bottom: 0; opacity: 0.3; animation: wave 20s linear reverse infinite; }

@keyframes wave {
  0% { transform: translateX(0); }
  50% { transform: translateX(-25%); }
  100% { transform: translateX(-50%); }
}
</style>