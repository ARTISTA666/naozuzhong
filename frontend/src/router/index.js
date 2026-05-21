import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录', noAuth: true }
  },
  { path: '/', redirect: '/greenway' },
  {
    path: '/greenway',
    name: 'Greenway',
    component: () => import('../views/GreenwayView.vue'),
    meta: { title: '绿道管理', icon: 'Ambulance' }
  },
  {
    path: '/nihss',
    name: 'Nihss',
    component: () => import('../views/NihssView.vue'),
    meta: { title: 'NIHSS评估', icon: 'Document' }
  },
  {
    path: '/thrombolysis',
    name: 'Thrombolysis',
    component: () => import('../views/ThrombolysisView.vue'),
    meta: { title: '溶栓禁忌检查', icon: 'Warning' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/DashboardView.vue'),
    meta: { title: '数据驾驶舱', icon: 'DataAnalysis' }
  },
  {
    path: '/followup',
    name: 'Followup',
    component: () => import('../views/FollowupView.vue'),
    meta: { title: '随访管理', icon: 'ChatLineSquare' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!token && !to.meta.noAuth) {
    return '/login'
  }
  if (token && to.path === '/login') {
    return '/'
  }
})

export default router
