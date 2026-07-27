# P0 阻塞问题修复设计

## 背景

对抗式审查发现 6 个 P0 阻塞问题，阻止 push develop 分支。

## 修复清单

### F1: 替换计划文档中的明文 API Key
- **文件**: `docs/superpowers/plans/2026-07-26-prompt-optimization.md`
- **改动**: 将 `f177b831269c4feab7e9c0e9e32f1aeb.h16BbKFPydqe397s` 替换为 `${AI_API_KEY}`

### F2: 配置 .gitattributes 统一行尾符
- **文件**: 新建 `.gitattributes`
- **内容**: `* text=auto`

### F3: SQL 数据口径一致化
- **文件**: `backend/src/main/resources/mapper/AiReportMapper.xml`
- **改动**: 在 `countBehaviors` 的 WHERE 条件中增加 `AND br.detail_saved = TRUE`，与 `findBehaviorRecords` 保持一致

### F4: RestClient 超时配置
- **文件**: `backend/src/main/java/com/specialed/assistant/api/aireport/AiReportService.java`
- **改动**: 为 RestClient 添加 `SimpleClientHttpRequestFactory`，设置 connectTimeout=5000ms, readTimeout=30000ms

### F5: 删除操作回滚机制
- **文件**: `miniprogram/components/behavior-counter/behavior-counter.js`
- **改动**: 在 DELETE 请求的 fail 回调和 code!==0 回调中，触发反向 countchange 事件回滚计数

### F6: 移除硬编码 localhost
- **文件**: `miniprogram/components/behavior-counter/behavior-counter.js`
- **改动**: 将 `http://localhost:3000/api/behavior-records/` 改为使用 `../../utils/request` 模块

## 涉及文件

| 文件 | 改动类型 |
|---|---|
| `docs/superpowers/plans/2026-07-26-prompt-optimization.md` | 修改（替换 API Key） |
| `.gitattributes` | 新建 |
| `backend/src/main/resources/mapper/AiReportMapper.xml` | 修改（SQL 口径） |
| `backend/src/main/java/.../AiReportService.java` | 修改（超时配置） |
| `miniprogram/components/behavior-counter/behavior-counter.js` | 修改（回滚 + localhost） |
