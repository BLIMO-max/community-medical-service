// src/utils/request.js
import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000
})

// 全局 Loading 实例
let loadingInstance = null
let loadingCount = 0

// 显示 Loading
const showLoading = () => {
  if (loadingCount === 0) {
    loadingInstance = ElLoading.service({
      fullscreen: true,
      text: '加载中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }
  loadingCount++
}

// 隐藏 Loading
const hideLoading = () => {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
  }
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 不显示 Loading 的请求（可配置）
    if (!config.hideLoading) {
      showLoading()
    }
    
    // 从 localStorage 获取 token
    const user = localStorage.getItem('user')
    if (user) {
      try {
        const userData = JSON.parse(user)
        // 如果有 token 就携带
        if (userData.token) {
          config.headers['Authorization'] = `Bearer ${userData.token}`
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
    
    return config
  },
  error => {
    hideLoading()
    ElMessage.error('请求配置错误')
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    hideLoading()
    
    const res = response.data
    
    // 根据业务状态码处理
    if (res.code === 200) {
      return res
    } else if (res.code === 401) {
      // 未授权，跳转登录
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('user')
      window.location.href = '/login'
      return Promise.reject(res)
    } else {
      // 其他业务错误
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(res)
    }
  },
  error => {
    hideLoading()
    
    // 网络错误处理
    if (error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.message.includes('Network Error')) {
      ElMessage.error('网络连接失败，请检查网络')
    } else if (error.response) {
      const status = error.response.status
      switch (status) {
        case 400:
          ElMessage.error('请求参数错误')
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(`请求失败: ${status}`)
      }
    } else {
      ElMessage.error('未知错误，请稍后重试')
    }
    
    return Promise.reject(error)
  }
)

export default request