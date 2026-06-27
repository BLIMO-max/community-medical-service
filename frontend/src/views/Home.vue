<!-- 文件位置：src/views/Home.vue -->
<template>
  <el-container class="layout-container">
    <!-- 动态背景 -->
    <div class="layout-background">
      <div class="bg-gradient"></div>
      <div class="bg-pattern"></div>
    </div>

    <!-- 侧边栏 -->
    <el-aside width="240px" class="aside">
      <div class="logo-area">
        <div class="logo-icon">
          <el-icon :size="32" color="#409EFF"><FirstAidKit /></el-icon>
        </div>
        <div class="logo-text">
          <h3>社区医疗系统</h3>
          <span>Community Health</span>
        </div>
      </div>
      
      <el-menu
        :default-active="$route.path"
        class="custom-menu"
        :collapse-transition="false"
        :router="true"
      >
        <el-menu-item index="/home/appointment">
          <el-icon><Calendar /></el-icon>
          <span>预约挂号</span>
          <el-badge v-if="hasNewAppointment" is-dot class="menu-badge" />
        </el-menu-item>
        
        <el-menu-item index="/home/record">
          <el-icon><Document /></el-icon>
          <span>我的病历</span>
        </el-menu-item>

        <el-menu-item index="/home/health" v-if="user.role === 2">
          <el-icon><Monitor /></el-icon>
          <span>健康档案</span>
        </el-menu-item>

        <el-menu-item index="/home/prescription" v-if="user.role === 2">
          <el-icon><Reading /></el-icon>
          <span>我的处方</span>
        </el-menu-item>

        <!-- 医生专属菜单 -->
        <el-sub-menu index="doctor" v-if="user.role === 1">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>医生工作台</span>
          </template>
          <el-menu-item index="/home/patient-list">患者列表</el-menu-item>
          <el-menu-item index="/home/my-schedule">我的排班</el-menu-item>
          <el-menu-item index="/home/diagnosis">诊断记录</el-menu-item>
        </el-sub-menu>

        <!-- 管理员专属菜单 -->
        <el-sub-menu index="admin" v-if="user.role === 0">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/home/user-manage">用户管理</el-menu-item>
          <el-menu-item index="/home/schedule-manage">排班管理</el-menu-item>
          <el-menu-item index="/home/stats">数据统计</el-menu-item>
          <el-menu-item index="/home/medicine">药品管理</el-menu-item>
        </el-sub-menu>
      </el-menu>

      <div class="aside-footer">
        <div class="system-info">
          <span>v2.0.0</span>
        </div>
      </div>
    </el-aside>

    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home/appointment' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ routeName }}</el-breadcrumb-item>
          </el-breadcrumb>
          
          <!-- 搜索框 -->
          <div class="search-box" v-if="showSearch">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索医生、科室..."
              :prefix-icon="Search"
              size="small"
              class="header-search"
            />
          </div>
        </div>

        <div class="header-right">
          <!-- 消息通知 -->
          <el-badge :value="3" class="notification-badge">
            <el-button :icon="Bell" circle size="small" @click="handleNotification" />
          </el-badge>

          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :src="userAvatar" class="user-avatar">
                {{ user.realName?.charAt(0) || '用' }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ user.realName || '用户' }}</span>
                <span class="user-role">{{ userRoleText }}</span>
              </div>
              <el-icon class="arrow-down"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 核心内容区 - 修复：移除 keep-alive，确保每次路由切换都重新渲染 -->
      <el-main class="main-content">
        <router-view :key="$route.fullPath" />
      </el-main>

      <!-- 底部 -->
      <el-footer class="footer">
        <span>© 社区就诊服务系统 - 用心守护您的健康</span>
        <span class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">帮助中心</a>
          <a href="#">联系我们</a>
        </span>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  Calendar, Document, FirstAidKit, Monitor, Reading,
  UserFilled, Setting, Search, Bell, ArrowDown,
  User, SwitchButton
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()

const user = JSON.parse(localStorage.getItem('user') || '{}')
const searchKeyword = ref('')
const hasNewAppointment = ref(true)

// 用户角色文本
const userRoleText = computed(() => {
  const roles = { 0: '管理员', 1: '医生', 2: '居民' }
  return roles[user.role] || '访客'
})

// 用户头像
const userAvatar = computed(() => '')

// 当前页面名称
const routeName = computed(() => {
  const path = route.path
  if (path.includes('appointment')) return '预约挂号'
  if (path.includes('record')) return '我的病历'
  if (path.includes('health')) return '健康档案'
  if (path.includes('prescription')) return '我的处方'
  if (path.includes('patient-list')) return '患者列表'
  if (path.includes('my-schedule')) return '我的排班'
  if (path.includes('diagnosis')) return '诊断记录'
  if (path.includes('user-manage')) return '用户管理'
  if (path.includes('schedule-manage')) return '排班管理'
  if (path.includes('stats')) return '数据统计'
  if (path.includes('medicine')) return '药品管理'
  return '首页'
})

// 是否显示搜索框
const showSearch = computed(() => {
  return route.path.includes('appointment') || route.path.includes('patient-list')
})

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/home/health')
      break
    case 'settings':
      ElMessage.info('设置开发中')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理通知
const handleNotification = () => {
  ElMessage.info('消息通知开发中')
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('user')
    router.push('/login')
    ElMessage.success('已退出登录')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  position: relative;
}

.layout-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
}

.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
}

.bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 30%, rgba(64, 158, 255, 0.03) 0%, transparent 20%),
    radial-gradient(circle at 80% 70%, rgba(52, 199, 89, 0.03) 0%, transparent 20%),
    repeating-linear-gradient(45deg, rgba(0,0,0,0.02) 0px, rgba(0,0,0,0.02) 2px, transparent 2px, transparent 8px);
}

.aside {
  background: rgba(48, 65, 86, 0.95);
  backdrop-filter: blur(10px);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.1);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-area {
  padding: 25px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.logo-text h3 {
  margin: 0;
  font-size: 18px;
  color: white;
  font-weight: 600;
}

.logo-text span {
  font-size: 12px;
  color: #a0b3cc;
  letter-spacing: 1px;
}

.custom-menu {
  flex: 1;
  background: transparent;
  border-right: none;
  padding: 15px 0;
}

.custom-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s;
  color: #bfcbd9;
}

.custom-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.custom-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #409EFF, #36d1dc);
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
  color: white;
}

.custom-menu :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s;
  color: #bfcbd9;
}

.custom-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.menu-badge {
  margin-left: auto;
  margin-right: 10px;
}

.aside-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;
  color: #a0b3cc;
  font-size: 12px;
}

.header {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02);
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 30px;
}

.header-search {
  width: 250px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 30px;
  transition: all 0.3s;
}

.user-info:hover {
  background: rgba(0, 0, 0, 0.02);
}

.user-avatar {
  background: linear-gradient(135deg, #409EFF, #36d1dc);
  color: white;
  font-weight: bold;
}

.user-detail {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.user-role {
  font-size: 12px;
  color: #7f8c8d;
}

.arrow-down {
  font-size: 14px;
  color: #7f8c8d;
}

.main-content {
  padding: 25px;
  overflow-y: auto;
  height: calc(100vh - 110px);
}

.footer {
  height: 50px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  color: #7f8c8d;
  font-size: 13px;
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-links a {
  color: #7f8c8d;
  text-decoration: none;
  transition: all 0.3s;
}

.footer-links a:hover {
  color: #409EFF;
}
</style>