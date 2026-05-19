import axios from 'axios'

const BASE_URL = '/api'

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 响应拦截器：统一错误处理
api.interceptors.response.use(
  res => res.data,
  err => {
    console.error('API Error:', err)
    return Promise.reject(err)
  }
)

// ==================== 绿道 API ====================
export const greenwayApi = {
  create(data) {
    return api.post('/greenway', data)
  },
  changeState(encounterId, data) {
    return api.post(`/greenway/${encounterId}/state-change`, data)
  },
  getDetail(encounterId) {
    return api.get(`/greenway/${encounterId}`)
  },
  getHistory(encounterId) {
    return api.get(`/greenway/${encounterId}/history`)
  }
}

// ==================== CDS 决策 API ====================
export const decisionApi = {
  checkThrombolysis(data) {
    return api.post('/decisions/thrombolysis-check', data)
  }
}

// ==================== NIHSS 评估 API ====================
export const assessmentApi = {
  submitNihss(data) {
    return api.post('/assessments/nihss', data)
  },
  getHistory(encounterId) {
    return api.get(`/assessments/${encounterId}/nihss`)
  },
  getLatest(encounterId) {
    return api.get(`/assessments/${encounterId}/nihss/latest`)
  },
  getScaleItems() {
    return api.get('/assessments/nihss/items')
  }
}

// ==================== 驾驶舱 API ====================
export const dashboardApi = {
  getOverview(startDate, endDate) {
    return api.get('/dashboard/overview', { params: { startDate, endDate } })
  },
  getDntTrend(startDate, endDate) {
    return api.get('/dashboard/dnt-trend', { params: { startDate, endDate } })
  },
  getMonthlyRate(year) {
    return api.get('/dashboard/thrombolysis-rate', { params: { year } })
  },
  getIndicators() {
    return api.get('/dashboard/indicators')
  }
}

// ==================== 随访 API ====================
export const followupApi = {
  getPlans(patientId) {
    return api.get('/followup/plans', { params: { patientId } })
  },
  getTasks(planId) {
    return api.get('/followup/tasks', { params: { planId } })
  },
  submitReport(data) {
    return api.post('/followup/reports', data)
  }
}

// ==================== 集成 API ====================
export const integrationApi = {
  matchPatient(data) {
    return api.post('/integration/patient-match', data)
  },
  checkGender(gender) {
    return api.post('/integration/standardize/gender', { gender })
  }
}

export default api
