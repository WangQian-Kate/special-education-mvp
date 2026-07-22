const LOCAL_BASE_URL = 'http://localhost:3000/api'

function normalizeBaseUrl(value) {
  const normalized = String(value || '').trim().replace(/\/+$/, '')
  if (!normalized) {
    return LOCAL_BASE_URL
  }
  return normalized.endsWith('/api') ? normalized : `${normalized}/api`
}

Page({
  data: {
    baseUrlInput: LOCAL_BASE_URL,
    healthUrl: `${LOCAL_BASE_URL}/health`,
    state: 'idle',
    stateText: '等待测试',
    message: '尚未发起请求',
    responseText: ''
  },

  onLoad() {
    const storedBaseUrl = normalizeBaseUrl(wx.getStorageSync('backendBaseUrl'))
    this.setData({
      baseUrlInput: storedBaseUrl,
      healthUrl: `${storedBaseUrl}/health`
    }, () => this.testConnection())
  },

  onBaseUrlInput(event) {
    this.setData({
      baseUrlInput: event.detail.value
    })
  },

  saveAndTest() {
    const baseUrl = normalizeBaseUrl(this.data.baseUrlInput)
    wx.setStorageSync('backendBaseUrl', baseUrl)
    this.setData({
      baseUrlInput: baseUrl,
      healthUrl: `${baseUrl}/health`
    }, () => this.testConnection())
  },

  useLocalAddress() {
    wx.removeStorageSync('backendBaseUrl')
    this.setData({
      baseUrlInput: LOCAL_BASE_URL,
      healthUrl: `${LOCAL_BASE_URL}/health`
    }, () => this.testConnection())
  },

  testConnection() {
    if (this.data.state === 'loading') {
      return
    }

    this.setData({
      state: 'loading',
      stateText: '正在连接',
      message: '正在请求后端健康检查接口...',
      responseText: ''
    })

    const baseUrl = normalizeBaseUrl(this.data.baseUrlInput)
    const healthUrl = `${baseUrl}/health`

    this.setData({ healthUrl })

    wx.request({
      url: healthUrl,
      method: 'GET',
      timeout: 10000,
      success: (res) => {
        const body = res.data || {}
        const connected = res.statusCode === 200
          && body.code === 0
          && body.data
          && body.data.status === 'UP'

        console.log('后端连接状态', connected, `HTTP ${res.statusCode}`, body.message, body)

        this.setData({
          state: connected ? 'success' : 'error',
          stateText: connected ? '连接成功' : '响应异常',
          message: connected
            ? `HTTP ${res.statusCode}，Spring Boot 服务运行正常`
            : `HTTP ${res.statusCode}，响应包体不符合约定`,
          responseText: JSON.stringify(body, null, 2)
        })
      },
      fail: (error) => {
        console.error('后端连接失败', error)

        this.setData({
          state: 'error',
          stateText: '连接失败',
          message: error.errMsg || '无法连接后端服务',
          responseText: ''
        })
      }
    })
  }
})
