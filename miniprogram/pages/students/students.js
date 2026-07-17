// pages/students/students.js
Page({
  data: {
    // 学生列表
    students: [],
    // 是否显示添加弹窗
    showAddModal: false,
    // 新学生表单
    newStudent: {
      name: '',
      grade: '',
      remark: ''
    }
  },

  onLoad() {
    // TODO: 从后端获取学生列表
  },

  /**
   * 显示/隐藏添加弹窗
   */
  handleToggleAddModal() {
    this.setData({ showAddModal: !this.data.showAddModal });
  },

  /**
   * 表单输入绑定
   */
  handleInputChange(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    this.setData({
      [`newStudent.${field}`]: value
    });
  },

  /**
   * 添加学生
   */
  handleAddStudent() {
    const { newStudent } = this.data;
    if (!newStudent.name) {
      wx.showToast({ title: '请输入学生姓名', icon: 'none' });
      return;
    }

    // TODO: 调用后端接口添加学生
    console.log('添加学生:', newStudent);

    this.setData({
      showAddModal: false,
      newStudent: { name: '', grade: '', remark: '' }
    });
    wx.showToast({ title: '添加成功', icon: 'success' });
  }
});
