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
  tunnel: 'https://4e389b4e.r40.cpolar.top/api'
};
const BASE_URL = BASE_URLS[ENV];

const TIMEOUT = 10000;

// 40101/401 并发拦截互斥：多个请求同时身份失效时只跳一次登录页
let redirecting = false;

/** 白名单身份失效：清身份并回登录页 */
function handleUnauthorized() {
  if (redirecting) return;
  redirecting = true;
  store.clearIdentity();
  store.clearAccessToken();
  wx.reLaunch({ url: '/pages/login/login' });
  setTimeout(() => { redirecting = false; }, 1000);
}

/**
 * 发起请求
 * @param {Object} options { url, method, data, showLoading, hideError }
 * @returns {Promise<any>} resolve 后端 data 字段；非 0 业务码 / 网络错误 reject
 *
 * 约定的响应包体（需与后端对齐）：{ code: 0, message: 'ok', data: {...} }
 */
function request({ url, method = 'GET', data = {}, showLoading = false, hideError = false, timeout }) {
  const accessToken = store.getAccessToken();
  const teacherId = store.getTeacherId();
  if (showLoading) wx.showLoading({ title: '加载中', mask: true });

  return new Promise((resolve, reject) => {
    var headers = { 'Content-Type': 'application/json' };
    if (accessToken) {
      headers['Authorization'] = 'Bearer ' + accessToken;
    } else if (teacherId) {
      headers['X-Teacher-Id'] = teacherId;
    }
    wx.request({
      url: BASE_URL + url,
      method,
      data,
      timeout: timeout || TIMEOUT,
      header: headers,
      success(res) {
        const body = res.data || {};
        // 白名单身份失效：统一拦截，清身份回登录页
        if (res.statusCode === 401 || body.code === 40101) {
          handleUnauthorized();
          reject(body.code ? body : res);
          return;
        }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          if (body.code === 0) {
            resolve(body.data);
          } else {
            if (!hideError) wx.showToast({ title: body.message || '请求失败', icon: 'none' });
            reject(body);
          }
        } else {
          // 4xx/5xx 也是统一包体（400/404 带业务 code 和 message），优先展示业务提示
          if (!hideError) wx.showToast({ title: body.message || `服务异常(${res.statusCode})`, icon: 'none' });
          reject(body.code ? body : res);
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
const put = (url, data, opts = {}) => request({ url, method: 'PUT', data, ...opts });
const patch = (url, data, opts = {}) => request({ url, method: 'PATCH', data, ...opts });
const del = (url, data, opts = {}) => request({ url, method: 'DELETE', data, ...opts });

/** 阶段二联调用：后端健康检查 */
const ping = () => get('/health', {}, { hideError: true });

module.exports = { request, get, post, put, patch, del, ping, BASE_URL };
