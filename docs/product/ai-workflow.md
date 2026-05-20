# AI 工作流

## 概述

AI 功能在本产品中是可选增强，不是必需品。即使用户完全不使用外部 AI，App 的核心流程（记录、对比、签约核查）仍然完整可用。

AI 工作流分为三个阶段：

1. **Outbound**：App 生成结构化的 AI Quick Share 内容
2. **External**：用户将内容粘贴到外部 AI 客户端
3. **Return**：用户将 AI 输出粘贴回 App，App 解析并展示

## 1. Outbound：AI Quick Share

### 看房阶段

App 将房源的看房数据打包为 Markdown 文本，用户复制后粘贴到任意 AI 客户端。

#### 生成内容结构

```markdown
# 看房分析请求

## 房源基础信息
- 标题：{property.title}
- 小区：{property.communityName}
- 地址：{property.address}
- 月租：{property.rentAmount} 元
- 押付：{property.depositRule}
- 户型：{property.layoutText}
- 面积：{property.areaSquareMeter} m²
- 楼层：{property.floorInfo}
- 朝向：{property.orientation}

## 看房记录
（每条 viewing 的时间、主观印象、噪音/异味/采光/通风评级）

## 检查项结果
（每个 checklist item 的 label、resultValue、severity、comment）

## 照片说明
（每张照片的 note）

## 请求
请根据以上信息，输出一份结构化的看房分析结果，格式严格遵循以下 JSON schema：
{viewing-analysis-result.v1.json 的完整内容}

请只输出 JSON，不要输出其他文字。
```

#### 触发方式

- 房源详情页 → "AI 分析" 按钮
- 对比页 → "批量分析" 按钮（逐个生成）

### 签约阶段

App 将签约 checklist 结果和签约材料打包为 Markdown 文本。

#### 生成内容结构

```markdown
# 签约审查请求

## 房源基础信息
（同上）

## 签约 checklist 结果
（每个 signing checklist item 的 label、resultValue、severity、comment）

## 签约材料
（text_note 类型材料的 textContent）
（image 类型材料的 note）

## 请求
请根据以上信息，输出一份结构化的签约风险审查结果，格式严格遵循以下 JSON schema：
{signing-analysis-result.v1.json 的完整内容}

请只输出 JSON，不要输出其他文字。
```

#### 触发方式

- 签约阶段页 → "AI 审查" 按钮

## 2. Return：AI 结果导入

### 交互流程

1. 用户在 AI 客户端获得输出后，回到 App
2. 点击"导入 AI 结果"按钮
3. App 弹出文本输入框（支持粘贴）
4. 用户粘贴 AI 输出的 JSON
5. App 执行解析和校验
6. 成功 → 展示结果页；失败 → 提示错误

### 解析规则

#### 步骤 1：基本格式校验

- 内容是否为合法 JSON
- 是否包含必需字段（schemaVersion、stage、propertyId 等）

#### 步骤 2：Schema 校验

- 看房结果：校验是否符合 `viewing-analysis-result.v1.json`
- 签约结果：校验是否符合 `signing-analysis-result.v1.json`

#### 步骤 3：业务校验

- `propertyId` 是否与当前房源匹配
- `stage` 是否与当前阶段匹配
- `schemaVersion` 是否为支持的版本

### 错误处理

| 错误类型 | 提示信息 | 处理方式 |
|----------|----------|----------|
| 非法 JSON | "无法解析，请检查是否为完整的 JSON 格式" | 阻止导入 |
| Schema 不匹配 | "JSON 结构不完整，缺少 {fieldName} 字段" | 阻止导入 |
| schemaVersion 不匹配 | "AI 输出版本不兼容，期望 {expected}，实际 {actual}" | 阻止导入 |
| propertyId 不匹配 | "AI 结果对应的房源不是当前房源，请确认" | 允许强制导入（用户确认后） |
| stage 不匹配 | "AI 结果是{签约/看房}阶段的，当前是{看房/签约}阶段" | 阻止导入 |

### 成功后行为

- 看房结果：跳转到"看房诊断页"，展示 overallRecommendation、summary、topConcerns、missingInformation、suggestedRechecks、dimensionAssessments
- 签约结果：跳转到"签约风险审查页"，展示 overallRecommendation、summary、criticalRisks、missingClauses、negotiationPoints、requiredFollowUps、dimensionAssessments

## 3. Prompt 模板

### 看房分析 Prompt

```
你是一个专业的租房顾问。请根据以下看房信息，分析这套房源的优缺点和风险。

{AI Quick Share 内容}

请严格按照以下 JSON schema 输出结果，不要输出任何其他文字：
{viewing-analysis-result.v1.json}

要求：
1. overallRecommendation 根据风险程度选择 continue_considering / recheck_before_decision / reject
2. summary 用 2-3 句话概括整体判断
3. topConcerns 列出最值得关注的问题，每个问题标注 severity
4. missingInformation 列出看房时未检查但很重要的项目
5. suggestedRechecks 列出建议复查的项目
6. dimensionAssessments 对 5 个维度分别给出 level 和 summary
```

### 签约审查 Prompt

```
你是一个专业的租房法律顾问。请根据以下签约信息，审查租赁合同的风险。

{AI Quick Share 内容}

请严格按照以下 JSON schema 输出结果，不要输出任何其他文字：
{signing-analysis-result.v1.json}

要求：
1. overallRecommendation 根据风险程度选择 can_sign / clarify_before_sign / do_not_sign
2. summary 用 2-3 句话概括合同风险判断
3. criticalRisks 列出关键风险条款，每个标注 severity
4. missingClauses 列出合同中缺失但应写入的条款
5. negotiationPoints 列出可以与房东协商的要点
6. requiredFollowUps 列出签约前必须补齐的材料或确认事项
7. dimensionAssessments 对 6 个维度分别给出 status 和 summary
```

## 4. 数据存储

### 导入的 AI 结果

- 保存到本地数据库，与 propertyId 关联
- 支持覆盖更新（同一房源多次导入以最新为准）
- 支持删除（用户可以清除 AI 结果）

### 导出时包含

- AI 结果包含在 manifest 导出中
- 通过 `exportProfile` 字段区分导出用途
