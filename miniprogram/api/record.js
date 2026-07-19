// api/record.js
// 随班记录域接口封装（CLASS-001 ~ CLASS-012 + 行为卡片字典）
// 路径与契约见 docs/前端相关端口.md；统一走 utils/request 携带 X-Teacher-Id

const { get, post, put, patch, del } = require('../utils/request');

/** CLASS-001 查询指定日期课堂记录列表 → ClassRecordSummary[]（按 id 升序） */
const getDayRecords = (recordDate) => get('/class-records', { recordDate });

/**
 * CLASS-002 新建一次独立课堂记录 → ClassRecordDetail
 * @param {Object} payload { recordDate, courseCode, environmentCode, observationDurationMinutes, overallRemark? }
 */
const createClassRecord = (payload) => post('/class-records', payload);

/** CLASS-003 查询课程对应行为卡片字典 → CodeLabel[]（未配置课程返回 []） */
const getBehaviorOptions = (courseCode) => get('/class-records/behavior-options', { courseCode });

/** CLASS-005 查询课堂详情和行为卡片次数 → ClassRecordDetail */
const getClassRecordDetail = (id) => get(`/class-records/${id}`);

/**
 * CLASS-006 局部更新课堂信息/备注（自动保存用，可高频调用）→ ClassRecordDetail
 * partial 仅限白名单 7 字段；备注静默保存，页面自行做状态提示
 */
const patchClassRecord = (id, partial) => patch(`/class-records/${id}`, partial, { hideError: true });

/** CLASS-007 快速新增一次行为（点 +，时间后端生成）→ { record, card } */
const quickAddBehavior = (id, behaviorCode) =>
  post(`/class-records/${id}/behavior-records/quick`, { behaviorCode });

/** CLASS-008 补记过去发生的一次行为 → { record, card } */
const supplementBehavior = (id, behaviorCode, occurredAt) =>
  post(`/class-records/${id}/behavior-records/supplement`, { behaviorCode, occurredAt });

/** CLASS-009 查询某行为发生时间列表 → [{ id, occurredAt, detailSaved }] */
const listBehaviorRecords = (id, behaviorCode) =>
  get(`/class-records/${id}/behavior-records`, { behaviorCode });

/** CLASS-010 查询一条详细记录 → BehaviorRecordDetail */
const getBehaviorDetail = (recordId) => get(`/behavior-records/${recordId}`);

/**
 * CLASS-011 保存完整详细记录 → BehaviorRecordDetail
 * payload: { durationMinutes, stageCode, antecedentText, behaviorDescription,
 *            consequenceText, functionCode, assistances[{code,content}], assistanceResultText }
 * assistances 必传（可空数组）；stage/function 字典为空时传 null
 */
const saveBehaviorDetail = (recordId, payload) => put(`/behavior-records/${recordId}/details`, payload);

/** CLASS-012 删除一条行为记录 → null（是否先确认由前端按 detailSaved 判断） */
const deleteBehaviorRecord = (recordId) => del(`/behavior-records/${recordId}`);

/** CLASS-004 查询周/月只读汇总 → BehaviorCountStatistics */
const getSummary = (period, referenceDate) => get('/class-records/summary', { period, referenceDate });

/** EVAL-001 查询日/周/月行为频次统计 → BehaviorCountStatistics */
const getEvaluationStats = (period, referenceDate) => get('/student-evaluation/statistics', { period, referenceDate });

module.exports = {
  getDayRecords,
  createClassRecord,
  getBehaviorOptions,
  getClassRecordDetail,
  patchClassRecord,
  quickAddBehavior,
  supplementBehavior,
  listBehaviorRecords,
  getBehaviorDetail,
  saveBehaviorDetail,
  deleteBehaviorRecord,
  getSummary,
  getEvaluationStats
};
