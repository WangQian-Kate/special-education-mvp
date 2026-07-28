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

/**
 * 根据当前日期判断所属学期，返回学期起始日期（用于传给后端 referenceDate 参数）。
 * 规则：春季 2-6月，秋季 9-次年1月，7-8月归入春季。
 * @returns {{ start: string, end: string, label: string }}
 */
function currentSemester() {
  var now = new Date();
  var year = now.getFullYear();
  var month = now.getMonth() + 1;

  if (month >= 2 && month <= 6) {
    return { start: year + '-02-01', end: year + '-06-30', label: '春季学期' };
  } else if (month >= 9) {
    return { start: year + '-09-01', end: (year + 1) + '-01-31', label: '秋季学期' };
  } else if (month === 1) {
    return { start: (year - 1) + '-09-01', end: year + '-01-31', label: '秋季学期' };
  } else {
    return { start: year + '-02-01', end: year + '-06-30', label: '春季学期' };
  }
}

module.exports = { today, nowHhmm, formatIsoCst, isFutureTime, formatTime, currentSemester };
