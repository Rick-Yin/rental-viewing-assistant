# App 导航与状态机设计

## 一级导航

Android MVP 使用 4 个一级区：

| 一级区 | 职责 | 默认空状态 |
| --- | --- | --- |
| 房源 | 房源列表、详情、看房记录、照片备注 | 引导新增第一套真实房源 |
| 对比 | 候选房源对比、推荐推进、进入签约 | 提示至少需要 2 套候选房源 |
| 签约 | 当前签约房源、阻断项、材料、AI 审查、终态确认 | 提示从候选房源进入签约 |
| 我的 | 画像、模板、数据、隐私、使用指引 | 不需要特殊空状态 |

签约是独立阶段入口，不只是房源列表中的状态筛选。没有签约中房源时，签约 tab 仍保留，但展示说明和返回候选房源的入口。

## 页面返回关系

| 当前页 | 返回目标 |
| --- | --- |
| 房源详情 | 进入来源：房源列表或对比页 |
| 看房记录编辑 | 房源详情 |
| 看房 checklist | 房源详情 |
| 看房 item 详情 | 看房 checklist |
| 照片与备注 | 房源详情或看房 checklist |
| 对比决策 | 对比 tab |
| 签约总览 | 签约 tab |
| 签约 checklist | 签约总览 |
| 签约 item 详情 | 签约 checklist |
| 签约材料 | 签约总览 |
| AI 审查结果 | 签约总览或房源详情 |
| 画像与模板 | 我的 |

页面需要记录进入来源，避免用户从 checklist、材料、AI 页返回时迷路。

## 房源状态

| 状态 | 含义 | 可进入状态 |
| --- | --- | --- |
| `draft` | 已创建但信息不足 | `to_view`, `viewed`, `rejected`, `archived` |
| `to_view` | 准备看房 | `viewed`, `rejected`, `archived` |
| `viewed` | 至少有一条看房记录 | `shortlisted`, `rejected`, `archived` |
| `shortlisted` | 候选房源 | `signing`, `rejected`, `archived` |
| `signing` | 活跃签约核查中 | `signed`, `signing_abandoned` |
| `signed` | 已签约 | `archived` |
| `signing_abandoned` | 签约放弃 | `shortlisted`, `archived` |
| `rejected` | 已排除 | `shortlisted`, `archived` |
| `archived` | 已归档 | 不提供主流程恢复，恢复入口放更多操作 |

## 状态约束

- 没有看房记录的房源不能进入 `signing`。
- 同一时间只允许一个活跃 `signing` 房源。
- 当已有 `signing` 房源时，其他房源的进入签约按钮禁用，并说明当前签约房源。
- `signed` 和 `signing_abandoned` 是签约终态，必须通过确认弹窗进入。
- `signing_abandoned` 允许填写放弃原因。
- `rejected` 必须填写或选择排除原因，恢复后回到 `shortlisted`。

## 主行动映射

| 状态 | 主行动 | 次级行动 |
| --- | --- | --- |
| `draft` | 记录看房 | 编辑房源、排除 |
| `to_view` | 记录看房 | 编辑房源、排除 |
| `viewed` | 补 checklist | 加入候选、AI 分析、排除 |
| `shortlisted` | 进入对比 | 进入签约、编辑、排除 |
| `signing` | 处理签前阻断项 | 材料、AI 审查、放弃签约 |
| `signed` | 查看归档信息 | 导出摘要 |
| `signing_abandoned` | 查看放弃原因 | 恢复候选、归档 |
| `rejected` | 查看排除原因 | 恢复候选、归档 |
| `archived` | 查看归档信息 | 恢复入口放更多操作 |

## 失败提示

| 场景 | 提示 |
| --- | --- |
| 没有看房记录却进入签约 | 先记录至少一次看房，再进入签约核查 |
| 已有其他签约中房源 | 当前已有「{title}」签约中，请先完成或放弃该签约 |
| 签约 checklist 有风险仍点已签约 | 仍有必须确认项，确认签约前请人工核验 |
| AI 结果阶段不匹配 | 这像是{看房/签约}分析结果，不适用于当前阶段 |
| 导入数据破坏单一签约约束 | 导入文件中存在多个签约中房源，请先选择保留一个 |
