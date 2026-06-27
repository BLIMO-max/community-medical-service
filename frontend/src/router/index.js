import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  // 根路径：根据是否登录决定去哪里
  {
    path: '/',
    redirect: () => {
      const user = localStorage.getItem('user')
      return user ? '/home/appointment' : '/login'
    }
  },

  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },

  // Home 父路由（布局页）
  {
    path: '/home',
    // 建议不要给父路由起名 Home（不是必须），避免你之前那类 warning/误用 name
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true },
    redirect: '/home/appointment',
    children: [
      {
        path: 'appointment',
        name: 'Appointment',
        component: () => import('../views/Appointment.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'record',
        name: 'MedicalRecord',
        component: () => import('../views/MedicalRecord.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'health',
        name: 'Health',
        component: () => import('../views/Health.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'prescription',
        name: 'Prescription',
        component: () => import('../views/Prescription.vue'),
        meta: { requiresAuth: true }
      },

      // 医生
      {
        path: 'patient-list',
        name: 'PatientList',
        component: () => import('../views/PatientList.vue'),
        meta: { requiresAuth: true, role: [1] }
      },
      {
        path: 'my-schedule',
        name: 'MySchedule',
        component: () => import('../views/MySchedule.vue'),
        meta: { requiresAuth: true, role: [1] }
      },
      {
        path: 'diagnosis',
        name: 'Diagnosis',
        component: () => import('../views/Diagnosis.vue'),
        meta: { requiresAuth: true, role: [1] }
      },

      // 管理员
      {
        path: 'medicine',
        name: 'MedicineManage',
        component: () => import('../views/admin/MedicineManage.vue'),
        meta: { requiresAuth: true, role: [0] }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('../views/admin/UserManage.vue'),
        meta: { requiresAuth: true, role: [0] }
      },
      {
        path: 'schedule-manage',
        name: 'ScheduleManage',
        component: () => import('../views/admin/ScheduleManage.vue'),
        meta: { requiresAuth: true, role: [0] }
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('../views/admin/Stats.vue'),
        meta: { requiresAuth: true, role: [0] }
      }
    ]
  },

  // 可选：兜底 404
  // { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  // 解析 user
  let user = null
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      user = JSON.parse(userStr)
    } catch (e) {
      localStorage.removeItem('user')
      user = null
    }
  }

  // 已登录就别进 login/register
  if (to.path === '/login' || to.path === '/register') {
    if (user) return '/home/appointment'
    return true
  }

  // 需要登录但未登录
  const requiresAuth = to.matched.some(r => r.meta && r.meta.requiresAuth)
  if (requiresAuth && !user) {
    return '/login'
  }

  // 角色权限校验（取第一个声明了 role 的路由记录来判断）
  const roleRecord = to.matched.find(r => r.meta && r.meta.role)
  if (roleRecord && user) {
    const allowRoles = roleRecord.meta.role
    const userRole = Number(user.role)
    if (!allowRoles.includes(userRole)) {
      ElMessage.error('您没有权限访问该页面')
      return '/home/appointment'
    }
  }

  return true
})

export default router