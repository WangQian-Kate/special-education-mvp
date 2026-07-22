// api/plan.js
// 训练计划域接口封装（阶段二 mock，阶段三接后端）
const request = require('../utils/request');
const { get } = request;

/**
 * 查询训练目标关联的行为记录列表
 * GET /training-plan/goals/{standardNumber}/records?limit=10
 * @param {number} standardNumber 训练目标编号
 * @param {number} limit 返回条数，默认 10
 * @returns {Promise<{standardNumber, goalText, totalCount, records}>}
 */
const getGoalRecords = (standardNumber, limit = 10) =>
  get(`/training-plan/goals/${standardNumber}/records`, { limit });

module.exports = { getGoalRecords };
