# 特殊教育智能辅助干预系统（MVP）

面向特殊学生融合教育场景的智能辅助干预系统，基于微信小程序实现。

## 核心功能

- **角色选择** — 教师/家长角色切换
- **ABC 行为记录** — 前因（Antecedent）→ 行为（Behavior）→ 后果（Consequence）快速采集
- **学生管理** — 学生信息录入与管理
- **统计分析** — 行为数据统计与可视化展示
- **AI 分析报告** — 基于行为数据自动生成干预建议（规划中）

## 技术栈

- **前端**：微信小程序
- **后端**：本地开发环境（局域网联调）
- **数据库**：待定

## 目录结构

```
├── miniprogram/           # 小程序源码
│   ├── app.js             # 应用入口
│   ├── app.json           # 全局配置
│   ├── app.wxss           # 全局样式
│   ├── pages/             # 页面
│   │   ├── index/         # 首页/角色选择
│   │   ├── record/        # ABC 行为记录
│   │   ├── students/      # 学生管理
│   │   ├── statistics/    # 统计分析
│   │   └── report/        # 分析报告
│   ├── components/        # 公共组件
│   ├── utils/             # 工具函数
│   ├── api/               # 接口封装
│   └── images/            # 图片资源
├── project.config.json    # 微信开发者工具配置
├── .eslintrc.json         # ESLint 配置
└── package.json
```

## 快速开始

```bash
# 安装依赖
npm install

# 代码检查
npm run lint
```

用微信开发者工具打开项目根目录即可预览。

## 协作文档与模拟数据

- 项目协作文档见 `docs/`
- 项目总文档见 `docs/project-documentation.md`
- 测试与 AI 模拟数据见 `mock/`
