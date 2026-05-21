import axios from 'axios'
import { ElMessage } from 'element-plus'

const BASE_URL = '/api'

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动附加 Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 错误消息防抖：1 秒内相同的错误不重复弹
const messageCache = new Map()
function dedupMessage(msg) {
  const key = msg
  const now = Date.now()
  const last = messageCache.get(key)
  if (last && now - last < 1000) return
  messageCache.set(key, now)
  ElMessage.error(msg)
}

// 响应拦截器：统一错误处理
api.interceptors.response.use(
  res => res.data,
  err => {
    const status = err.response?.status
    const data = err.response?.data
    const msg = data?.message || ''

    // 401：Token 过期 → 清除登录态跳转
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
      return Promise.reject(err)
    }

    // 403：无权限
    if (status === 403) {
      dedupMessage('无权限执行此操作')
      return Promise.reject(err)
    }

    // 502/504：网关/服务不可达
    if (status === 502 || status === 504) {
      dedupMessage('服务暂时不可用，请稍后重试')
      return Promise.reject(err)
    }

    // 网络错误（无响应）
    if (!status && err.message === 'Network Error') {
      dedupMessage('网络连接异常，请检查网络')
      return Promise.reject(err)
    }

    // 一般错误：优先使用后端消息
    if (msg) {
      dedupMessage(msg)
    } else if (status) {
      dedupMessage(`请求失败 (${status})`)
    }

    return Promise.reject(err)
  }
)

// ==================== 认证 API ====================
export const authApi = {
  login(data) { return api.post('/v3/auth/login', data) },
  register(data) { return api.post('/v3/auth/register', data) }
}

// ==================== 绿道 API ====================
export const greenwayApi = {
  create(data) {
    return api.post('/v3/greenway', data)
  },
  changeState(encounterId, data) {
    return api.post(`/v3/greenway/${encounterId}/state-change`, data)
  },
  getDetail(encounterId) {
    return api.get(`/v3/greenway/${encounterId}`)
  },
  getHistory(encounterId) {
    return api.get(`/v3/greenway/${encounterId}/history`)
  }
}

// ==================== CDS 决策 API ====================
export const decisionApi = {
  checkThrombolysis(data) {
    return api.post('/v3/decisions/thrombolysis-check', data)
  }
}

// ==================== NIHSS 评估 API ====================
export const assessmentApi = {
  submitNihss(data) {
    return api.post('/v3/assessments/nihss', data)
  },
  getHistory(encounterId) {
    return api.get(`/v3/assessments/${encounterId}/nihss`)
  },
  getLatest(encounterId) {
    return api.get(`/v3/assessments/${encounterId}/nihss/latest`)
  },
  getScaleItems() {
    return api.get('/v3/assessments/nihss/items')
  }
}

// ==================== 驾驶舱 API ====================
export const dashboardApi = {
  getOverview(startDate, endDate) {
    return api.get('/v3/dashboard/overview', { params: { startDate, endDate } })
  },
  getDntTrend(startDate, endDate) {
    return api.get('/v3/dashboard/dnt-trend', { params: { startDate, endDate } })
  },
  getMonthlyRate(year) {
    return api.get('/v3/dashboard/thrombolysis-rate', { params: { year } })
  },
  getIndicators() {
    return api.get('/v3/dashboard/indicators')
  }
}

// ==================== 随访 API ====================
export const followupApi = {
  getPlans(patientId) {
    return api.get('/v3/followup/plans', { params: { patientId } })
  },
  getTasks(planId) {
    return api.get('/v3/followup/tasks', { params: { planId } })
  },
  submitReport(data) {
    return api.post('/v3/followup/reports', data)
  }
}

// ==================== 集成 API ====================
export const integrationApi = {
  matchPatient(data) {
    return api.post('/v3/integration/patient-match', data)
  },
  checkGender(gender) {
    return api.post('/v3/integration/standardize/gender', { gender })
  }
}

export default api
