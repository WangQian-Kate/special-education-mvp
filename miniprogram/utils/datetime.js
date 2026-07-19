// utils/datetime.js
// 日期时间工具：后端统一按上海时区（+08:00）处理，前端拼串不做时区换算

/** 返回今天的 YYYY-MM-DD */
function today() {
  const d = new Date();
  const p = (n) => (n < 10 ? '0' + n : '' + n);
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

/** 当前时刻 HH:mm（time picker 默认值用） */
function nowHhmm() {
  const d = new Date();
  const p = (n) => (n < 10 ? '0' + n : '' + n);
  return `${p(d.getHours())}:${p(d.getMinutes())}`;
}

/**
 * 拼补记用的发生时间
 * @param {string} dateStr YYYY-MM-DD
 * @param {string} hhmm HH:mm
 * @returns {string} YYYY-MM-DDTHH:mm:00+08:00
 */
function formatIsoCst(dateStr, hhmm) {
  return `${dateStr}T${hhmm}:00+08:00`;
}

/**
 * 判断 dateStr + hhmm 是否晚于当前时刻（补记禁未来）
 * @param {string} dateStr YYYY-MM-DD
 * @param {string} hhmm HH:mm
 */
function isFutureTime(dateStr, hhmm) {
  const t = today();
  if (dateStr > t) return true;
  if (dateStr < t) return false;
  return hhmm > nowHhmm();
}

/**
 * ISO 8601 时间 → HH:mm 展示
 * @param {string} isoStr 例 2026-07-18T09:12:45+08:00
 */
function formatTime(isoStr) {
  if (!isoStr) return '';
  const m = String(isoStr).match(/T(\d{2}:\d{2})/);
  return m ? m[1] : isoStr;
}

module.exports = { today, nowHhmm, formatIsoCst, isFutureTime, formatTime };
