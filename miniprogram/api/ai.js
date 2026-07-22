// api/ai.js
// AI 辅助分析接口（AI-001：生成周报/月报）
const request = require('../utils/request');
const { post } = request;

/**
 * 请求 AI 生成辅助分析报告
 * POST /ai/report
 * @param {Object} payload { period: 'WEEKLY'|'MONTHLY', referenceDate: 'YYYY-MM-DD' }
 * @returns {Promise<{behaviorChanges:[], attentionConcerns:[], alternativeSuggestions:[]}>}
 */
const getAiReport = (period, referenceDate) =>
  post('/ai/report', { period, referenceDate }, { hideError: true });

module.exports = { getAiReport };
