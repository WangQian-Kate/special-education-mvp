// pages/record/record.js
// ABC 行为记录页面：Antecedent（前因）- Behavior（行为）- Consequence（后果）
Page({
  data: {
    // 当前选中的学生
    selectedStudent: null,
    // ABC 表单数据
    formData: {
      antecedent: '',   // 前因 — 行为发生前的事件/环境
      behavior: '',     // 行为 — 目标行为描述
      consequence: '',  // 后果 — 行为发生后的结果
      location: '',     // 发生地点
      intensity: 1      // 行为强度 1-5
    },
    // 强度等级选项
    intensityLevels: [
      { value: 1, label: '轻微' },
      { value: 2, label: '较轻' },
      { value: 3, label: '中等' },
      { value: 4, label: '较重' },
      { value: 5, label: '严重' }
    ]
  },

  onLoad() {
    // 页面加载
  },

  /**
   * 表单输入绑定
   */
  handleInputChange(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    this.setData({
      [`formData.${field}`]: value
    });
  },

  /**
   * 选择行为强度
   */
  handleIntensitySelect(e) {
    const { value } = e.currentTarget.dataset;
    this.setData({
      'formData.intensity': value
    });
  },

  /**
   * 提交行为记录
   */
  handleSubmit() {
    const { formData } = this.data;

    // 基本校验
    if (!formData.behavior) {
      wx.showToast({ title: '请填写行为描述', icon: 'none' });
      return;
    }

    // TODO: 调用后端接口提交数据
    console.log('提交行为记录:', formData);

    wx.showToast({ title: '记录成功', icon: 'success' });
  },

  /**
   * 重置表单
   */
  handleReset() {
    this.setData({
      formData: {
        antecedent: '',
        behavior: '',
        consequence: '',
        location: '',
        intensity: 1
      }
    });
  }
});
