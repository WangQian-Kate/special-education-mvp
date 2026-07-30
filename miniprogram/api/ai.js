// api/ai.js
// AI 辅助分析接口（AI-001：生成周报/月报/学期报告）
const request = require('../utils/request');
const { post } = request;

/**
 * 请求 AI 生成辅助分析报告
 * POST /ai/report
 * @param {Object} payload { period: 'WEEKLY'|'MONTHLY'|'SEMESTER', referenceDate: 'YYYY-MM-DD' }
 * @returns {Promise<Object>} 后端返回 SEAT 四字段格式（hypothesizedFunction/causalChainAnalysis/antecedentInterventions/replacementBehaviors），
 *   前端 statistics.js 中的 transformAiReport() 会将其转换为三维度格式（behaviorChanges/attentionConcerns/alternativeSuggestions）
 */
const getAiReport = (period, referenceDate) =>
  post('/ai/report', { period, referenceDate }, { hideError: true, timeout: period === 'SEMESTER' ? 180000 : 90000 });

module.exports = { getAiReport };
