import { ElNotification } from 'element-plus'

let ws = null
let reconnectTimer = null
const RECONNECT_DELAY = 5000

/**
 * WebSocket 连接管理器
 * 自动重连、接收告警推送、弹窗通知
 */
export function connectWebSocket() {
  if (ws && ws.readyState === WebSocket.OPEN) return

  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  const url = `${protocol}//${location.host}/api/v3/ws/alerts`

  ws = new WebSocket(url)

  ws.onopen = () => {
    console.log('[WS] 连接已建立')
    // 启动心跳
    heartbeat()
  }

  ws.onmessage = (evt) => {
    if (evt.data === 'pong') return
    try {
      const alert = JSON.parse(evt.data)
      showAlert(alert)
    } catch { /* ignore non-JSON */ }
  }

  ws.onclose = () => {
    console.log('[WS] 连接断开，准备重连...')
    ws = null
    reconnectTimer = setTimeout(connectWebSocket, RECONNECT_DELAY)
  }

  ws.onerror = () => {
    console.warn('[WS] 连接异常')
    ws?.close()
  }
}

/** 断开 WebSocket */
export function disconnectWebSocket() {
  clearTimeout(reconnectTimer)
  if (ws) {
    ws.onclose = null  // 阻止自动重连
    ws.close()
    ws = null
  }
}

/** 心跳：每 30 秒发一次 ping */
function heartbeat() {
  if (!ws || ws.readyState !== WebSocket.OPEN) return
  ws.send('ping')
  setTimeout(heartbeat, 30000)
}

/** 展示告警通知 */
function showAlert(alert) {
  const typeMap = {
    critical: 'error',
    warning: 'warning',
    info: 'info'
  }

  ElNotification({
    title: alert.title || '系统通知',
    message: alert.message || '',
    type: typeMap[alert.severity] || 'info',
    duration: alert.severity === 'critical' ? 0 : 6000,  // 严重告警不自动关闭
    position: 'top-right'
  })
}
