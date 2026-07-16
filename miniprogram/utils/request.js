// utils/request.js
// Promise 化 wx.request：统一 baseURL / 身份注入 / 错误提示
// 阶段二伪登录：请求头携带 X-Teacher-Id，后端直接认此 ID（不做 token 校验）

const store = require('./store');

/**
 * 环境切换：开发时改 ENV 即可
 * - local  : 开发者工具（需在工具详情里勾选"不校验合法域名"）
 * - tunnel : 内网穿透 HTTPS 地址（真机预览用，等后端提供后填入）
 */
const ENV = 'local';
const BASE_URLS = {
  local: 'http://localhost:3000/api',
  tunnel: 'https://TODO-你的穿透域名.example.com/api'
};
const BASE_URL = BASE_URLS[ENV];

const TIMEOUT = 10000;

/**
 * 发起请求
 * @param {Object} options { url, method, data, showLoading, hideError }
 * @returns {Promise<any>} resolve 后端 data 字段；非 0 业务码 / 网络错误 reject
 *
 * 约定的响应包体（需与后端对齐）：{ code: 0, message: 'ok', data: {...} }
 */
function request({ url, method = 'GET', data = {}, showLoading = false, hideError = false }) {
  const teacher = store.getTeacher();
  if (showLoading) wx.showLoading({ title: '加载中', mask: true });

  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method,
      data,
      timeout: TIMEOUT,
      header: {
        'Content-Type': 'application/json',
        // 白名单伪登录：后端按此 ID 识别教师，正式登录接入后此处换 token
        'X-Teacher-Id': teacher ? teacher.id : ''
      },
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          const body = res.data || {};
          if (body.code === 0) {
            resolve(body.data);
          } else {
            if (!hideError) wx.showToast({ title: body.message || '请求失败', icon: 'none' });
            reject(body);
          }
        } else {
          if (!hideError) wx.showToast({ title: `服务异常(${res.statusCode})`, icon: 'none' });
          reject(res);
        }
      },
      fail(err) {
        if (!hideError) wx.showToast({ title: '网络异常，请检查连接', icon: 'none' });
        reject(err);
      },
      complete() {
        if (showLoading) wx.hideLoading();
      }
    });
  });
}

const get = (url, data, opts = {}) => request({ url, method: 'GET', data, ...opts });
const post = (url, data, opts = {}) => request({ url, method: 'POST', data, ...opts });

/** 阶段二联调用：后端健康检查 */
const ping = () => get('/health', {}, { hideError: true });

module.exports = { request, get, post, ping, BASE_URL };
