// api/plan.js
// 训练计划域接口封装
const request = require('../utils/request');
const { get } = request;

/**
 * 查询训练目标关联的行为记录列表
 * GET /training-plan/goals/{standardNumber}/records?limit=10
 */
const getGoalRecords = (standardNumber, limit = 10) =>
  get(`/training-plan/goals/${standardNumber}/records`, { limit });

const getGoalHistory = (standardNumber, limit = 20) =>
  get(`/training-plan/goals/${standardNumber}/history`, { limit });

const getRecentGoalChanges = (days = 7, limit = 30) =>
  get('/training-plan/goals/changes/recent', { days, limit });

/**
 * 获取学生训练计划条目列表
 * GET /training-plan/items
 */
const getPlanItems = () => get('/training-plan/items');

/**
 * 更新单条训练目标
 * PATCH /training-plan/items/{itemId}
 * @param {number} itemId
 * @param {Object} patch { currentLevel?, phase?, status? }
 */
const updatePlanItem = (itemId, patch) =>
  request.request({ url: `/training-plan/items/${itemId}`, method: 'PATCH', data: patch });

module.exports = { getGoalRecords, getGoalHistory, getRecentGoalChanges, getPlanItems, updatePlanItem };
