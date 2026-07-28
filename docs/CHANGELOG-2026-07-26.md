# 变更日志 2026-07-26

> 本文档记录 7 月 26 日以来的所有改动，供团队成员了解变更内容和运行方式。

---

## 一、P0 关键修复（commit: 4498036）

### 1. SQL 查询口径一致化
- **问题**：`countBehaviors` 统计了所有记录，但 `findBehaviorRecords` 只返回 `detail_saved = TRUE` 的记录，导致 AI 分析数据口径不一致
- **修复**：在 `AiReportMapper.xml` 的 `countBehaviors` 查询中增加 `AND br.detail_saved = TRUE` 过滤条件
- **影响文件**：`backend/src/main/resources/mapper/AiReportMapper.xml`

### 2. RestClient 超时配置
- **问题**：AI API 调用没有超时设置，如果 GLM 接口无响应会导致线程永久阻塞
- **修复**：在 `AiReportService.java` 构造函数中增加 `SimpleClientHttpRequestFactory`，设置连接超时 5 秒、读取超时 30 秒
- **影响文件**：`backend/src/main/java/com/specialed/assistant/api/aireport/AiReportService.java`

### 3. 前端删除操作回滚机制
- **问题**：行为计数器删除时先减后请求 API，如果 API 失败计数不会恢复
- **修复**：在 `behavior-counter.js` 的 `.catch()` 中触发 `delta: 1` 恢复计数
- **影响文件**：`miniprogram/components/behavior-counter/behavior-counter.js`

### 4. 移除硬编码 localhost
- **问题**：`behavior-counter.js` 中直接写死 `http://localhost:3000`，真机调试无法连接
- **修复**：改用 `request` 工具模块，统一使用 `BASE_URL` 配置
- **影响文件**：`miniprogram/components/behavior-counter/behavior-counter.js`

### 5. API Key 脱敏
- **问题**：实施计划文档中硬编码了真实 API Key
- **修复**：替换为 `<your-api-key>` 占位符
- **影响文件**：`docs/superpowers/plans/2026-07-26-prompt-optimization.md`

### 6. INNER JOIN 改为 LEFT JOIN
- **问题**：`findBehaviorRecords` 使用 INNER JOIN 关联 `course_type` 和 `environment_type`，导致缺少类型定义的记录被静默丢弃
- **修复**：改为 LEFT JOIN，保留所有记录
- **影响文件**：`backend/src/main/resources/mapper/AiReportMapper.xml`

### 7. .gitattributes 行尾规范化
- **问题**：Windows 和 Mac 开发者之间协作时行尾符号不一致导致 diff 噪音
- **修复**：新增 `.gitattributes` 文件，设置 `* text=auto`
- **影响文件**：`.gitattributes`

---

## 二、AI Prompt 优化（commit: b707f94）

### 1. ConfidenceResult 增加 contextCount 字段
- **改动**：`ConfidenceResult` record 增加第 5 个字段 `long contextCount`，用于记录跨情境数量
- **影响文件**：`AiReportModels.java`、`AiReportService.java`

### 2. Prompt 增加 5 步推理强制要求
- **改动**：在 `buildPrompt()` 中插入推理步骤段落，强制 AI 按以下顺序执行：
  1. 统计 A 标签分布（TOP 3 高频前因）
  2. 统计 C 标签分布（TOP 3 高频后果）
  3. A→C 映射分析（与 SEAT 先验知识库比对）
  4. 排除法验证（解释为何排除其他三个功能）
  5. 生成因果链（必须引用具体 recordId）
- **影响文件**：`AiReportService.java`

### 3. 因果链输出格式增强约束
- **改动**：输出格式说明中明确要求 antecedent/behavior/consequence 必须来自原始记录文本，recordIds 必须引用真实 ID
- **影响文件**：`AiReportService.java`

### 4. reasoning 末尾追加置信度解释
- **改动**：在 `parseHypothesizedFunction` 的主路径和 fallback 分支中，均在 reasoning 末尾追加一句话置信度解释，格式如：
  > 置信度 0.57（MEDIUM）：基于30条记录，57%模式一致，跨3种情境。
- **影响文件**：`AiReportService.java`

---

## 三、如何运行

### 后端启动

```powershell
# 1. 确保 MySQL 已启动（需要管理员权限）
net start MySQL80

# 2. 设置环境变量并启动后端
$env:JAVA_HOME="D:\Java"
$env:DB_PASSWORD="special_ed_2024"
$env:AI_API_KEY="<你的智谱清言API Key>"
$env:SERVER_ADDRESS="0.0.0.0"
cd "D:\main\NKU\大二暑期\实训-智能辅助干预系统研发\special-education-mvp\backend"
.\mvnw.cmd spring-boot:run
```

后端启动后监听 `http://0.0.0.0:3000`。

### 微信小程序

1. 打开微信开发者工具，导入项目根目录 `special-education-mvp`（不是 miniprogram 子文件夹）
2. AppID 使用 `wx85f5b6f85abcf1578` 或测试号
3. 在「详情 → 本地设置」中勾选 **不校验合法域名**
4. 基础库版本建议使用 3.3.4

### 拉取最新代码后

```powershell
cd "D:\main\NKU\大二暑期\实训-智能辅助干预系统研发\special-education-mvp"
git pull origin develop
# 后端无需额外安装依赖，mvnw 会自动下载
# 重新按上述步骤启动后端即可
```

### 验证 AI 分析功能

在小程序中进入「统计」页面，切换到「周报」标签，点击 AI 分析按钮。如果后端日志出现 `AI报告查询到 X 条统计行，X 条记录行` 并最终返回 SEAT 框架分析结果，说明功能正常。

---

## 四、注意事项

1. **环境变量必须设置**：`DB_PASSWORD` 和 `AI_API_KEY` 缺一不可，否则后端启动失败或 AI 功能不可用
2. **JAVA_HOME 必须指向 D:\Java**：避免与其他 JDK 版本冲突
3. **端口 3000**：确保没有其他进程占用，启动前可用 `netstat -ano | findstr :3000` 检查
4. **真机调试**：需要 Windows 防火墙放行 TCP 3000 端口入站连接
5. **数据库**：如果 AI 分析返回空结果，检查数据库中是否有 `detail_saved = TRUE` 的行为记录（只有完成了 ABC 详细填写的记录才会被 AI 分析使用）
