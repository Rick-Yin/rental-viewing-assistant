# Checklist 视觉设计与内容展示方案

## 概述

本文档定义 checklist 数据在 App 中的视觉呈现方式，覆盖看房 checklist（第 5 页）和签约 checklist（第 10 页）的列表页与详情页。

核心原则：**checklist JSON 只提供静态内容，功能区由 App 代码硬编码实现。**

## 设计决策

| 决策 | 选择 |
|------|------|
| 一级页面顶部 | 房源摘要卡片（租金、中介费、风险计数、联系人、地址） |
| 一级页面布局 | 分组折叠列表，按 category 分组 |
| 一级卡片信息 | label + oneLine + 优先级指示器 + 状态 |
| 二级页面结构 | 操作区在上 + 功能区（可选）+ 参考区在下 |
| 参考区字段顺序 | 风险信号 → 怎么查 → 其他 |
| 拍照建议位置 | 详情页 + 快捷拍照按钮 |
| 功能区实现方式 | App 代码硬编码，checklist JSON 只提供静态内容 |

---

## 一级页面：Checklist 列表页

适用于信息架构第 5 页（看房 checklist）和第 10 页（签约 checklist）。

### 顶部：房源摘要卡片

checklist 列表页顶部固定显示当前房源的摘要信息卡片，便于用户在填写 checklist 时随时参考关键信息，也便于在多套房源间快速纵向对比。

#### 卡片内容

```
┌─────────────────────────────────────────────────┐
│  翠苑一区 2室1厅 5/6F          [view 3/21] ●    │
│  ¥3,200/月  押一付三           [sign  0/25] ●    │
│                                                 │
│  中介费: ¥1,600 (半月)    风险项: 2  不确定: 1   │
│  中介: 张经理 138xxxx1234                        │
│  📍 浙江省杭州市西湖区翠苑一区12幢  [地图]       │
└─────────────────────────────────────────────────┘
```

#### 字段来源与说明

| 字段 | 数据来源 | 说明 |
|------|----------|------|
| 房源标题 | `Property.title` 或 `communityName` + `layoutText` | 首行左对齐，粗体 |
| 楼层 | `Property.floorInfo` | 标题右侧 |
| 看房进度 | `ViewingChecklistResult` 聚合 | `已检查数/总数`，含 ⚠ 和 ? 计数 |
| 签约进度 | `SigningChecklistResult` 聚合 | `已检查数/总数`，含 ⚠ 和 ? 计数 |
| 月租 | `Property.rentAmount` + `currencyCode` | 首行或次行 |
| 押付规则 | `Property.depositRule` | 月租右侧 |
| 中介费 | 用户手动输入或从 Property 扩展字段读取 | 需新增字段或从聊天记录提取 |
| 风险项计数 | `resultValue === "risk"` 的 item 数量 | 红色高亮 |
| 不确定项计数 | `resultValue === "uncertain"` 的 item 数量 | 黄色高亮 |
| 中介联系人 | `Property.contactName` + `contactPhone` | 可点击拨号 |
| 地址 | `Property.address` 或 `district` + `communityName` | 末行 |
| 地图入口 | 基于 `Property.address` 或经纬度 | 点击跳转地图 App |

#### 设计要点

- **固定定位**：卡片固定在页面顶部，checklist 列表滚动时卡片始终可见（或滚动时折叠为迷你摘要条）
- **多房源对比场景**：用户在多套房源间切换时，通过摘要卡片快速识别当前正在查看哪套房、关键风险数量
- **点击交互**：
  - 地址行点击 → 打开地图（系统地图或内嵌地图）
  - 电话号码点击 → 拨号或复制
  - 卡片整体可点击 → 跳转房源详情页（第 3 页）
- **看房 vs 签约差异**：
  - 看房 checklist 页：默认展开看房进度，签约进度灰显或隐藏
  - 签约 checklist 页：两个进度都显示，签约进度高亮

#### 地图集成

- **MVP 方案**：点击地址行调起系统地图 App（高德/百度/Google Maps），通过 Intent 传入地址或经纬度
- **后续增强**：内嵌高德地图 SDK，显示房源位置标记 + 周边配套 POI（超市、地铁站等）
- **注意**：`docs/MVP范围说明.md` 已将"复杂地图路线分析"列为 MVP 不做，但简单的"点击打开地图"功能属于基础交互，不在排除范围内

#### 中介费字段

当前 `Property` schema 中没有 `agencyFee` 字段。两个方案：

1. **用户手动输入**：在房源创建/编辑页增加"中介费"输入框，存入 `Property` 扩展字段
2. **从聊天记录提取**：AI 辅助从用户上传的聊天截图中提取中介费信息（后续增强）

MVP 建议采用方案 1，增加一个可选的 `agencyFee` 字段到 `Property`。

---

### 布局：分组折叠列表

```
┌─────────────────────────────────────────────────┐
│  翠苑一区 2室1厅 5/6F          [view 3/21] ●    │
│  ¥3,200/月  押一付三           [sign  0/25] ●    │
│  中介费: ¥1,600    风险: 2 ⚠   不确定: 1 ?       │
│  张经理 138xxxx1234                              │
│  📍 杭州市西湖区翠苑一区12幢         [打开地图]  │
└─────────────────────────────────────────────────┘

▼ 找房与中介 (1/2)
  ┌─────────────────────────────────────┐
  │ 中介或租赁企业是否可靠              │
  │ 通过中介找房时，要先确认机构、人员、 │
  │ 收费和服务合同。                    │
  │                        [高] ✓ 已检查 │
  └─────────────────────────────────────┘
  ┌─────────────────────────────────────┐
  │ 议价空间是否已经判断                │
  │ 看房时不要过早表现得非常满意，可用   │
  │ 具体缺点和后续成本作为议价依据。     │
  │                        [中] ⚠ 有风险 │
  └─────────────────────────────────────┘

▼ 小区与周边 (0/4)
  ┌─────────────────────────────────────┐
  │ 夜间回家路线是否安全                │
  │ 需要评估夜间回家路线的照明、人流和   │
  │ 死角情况。                          │
  │                        [高] ○ 未检查 │
  └─────────────────────────────────────┘
  ...

▶ 房源类型与安全红线 (0/3)   ← 折叠状态
▶ 客厅与卧室 (0/3)
```

### 卡片信息层级

每个 item 卡片包含：

| 层级 | 字段 | 说明 |
|------|------|------|
| 标题行 | `label` | 主标题，左对齐 |
| 副标题行 | `oneLine` | 一句话说明，灰色小字 |
| 右侧 | `priority` + `resultValue` | 优先级标签 + 状态图标 |

### 状态图标映射

| resultValue | 图标 | 含义 |
|-------------|------|------|
| `unchecked` | ○ | 未检查 |
| `ok` | ✓ | 正常 |
| `risk` | ⚠ | 有风险 |
| `uncertain` | ? | 不确定 |

### 优先级标签

| priority | 显示 |
|----------|------|
| `high` | [高] |
| `medium` | [中] |
| `low` | [低] |

### 分类头信息

```
▼ 找房与中介 (1/2)
  ↑       ↑
 折叠态  已完成数/总数
```

- 显示分类 label 和完成进度
- 点击折叠/展开
- 默认全部展开（首次进入）
- 用户手动折叠后记住状态（本地持久化）

### 排序逻辑

- **分类排序**：按 JSON 中 categories 数组顺序（物理空间动线）
- **Item 排序**：按 category 内 items 数组顺序
- **后续增强**：模板推荐可调整优先级排序（MVP 不做）

---

## 二级页面：Item 详情页

适用于信息架构第 6 页（看房 checklist item 详情）和第 11 页（签约 checklist item 详情）。

### 布局：三区结构

```
┌─────────────────────────────────┐
│ ← 中介或租赁企业是否可靠        │
│                                 │
│ ═══════ 操作区 ═══════          │
│                                 │
│  [✓ OK]  [⚠ 风险]  [? 不确定]  │
│  ┌─────────────────────────┐   │
│  │ 写备注...                │   │
│  └─────────────────────────┘   │
│                                 │
│ ═══════ 功能区（可选）═══════   │
│                                 │
│  (App 代码渲染的动态内容)        │
│  例如：当地水电价格参考          │
│                                 │
│ ═══════ 参考区 ═══════          │
│                                 │
│  常见风险信号                    │
│  • 中介费前后口径不一致          │
│  • 催促先交定金、押金            │
│                                 │
│  怎么查                         │
│  • 确认机构名称、门店...         │
│  • 询问房源委托关系...           │
│                                 │
│  建议拍什么         [📷 拍照]   │
│  • 中介费说明截图                │
│  • 服务合同截图                  │
│                                 │
│  记录建议                       │
│  记录中介公司、门店...           │
│                                 │
│  为什么要查                     │
│  中介费、服务费、房源真实性...   │
│                                 │
│  参考链接                       │
│  • 不要被骗子中介骗了 →         │
└─────────────────────────────────┘
```

### 操作区（顶部）

- **结果选择**：三个按钮，水平排列，选中态高亮
  - `✓ OK` — 正常（对应 `resultValue: "ok"`）
  - `⚠ 风险` — 有问题（对应 `resultValue: "risk"`）
  - `? 不确定` — 需要复查（对应 `resultValue: "uncertain"`）
- **备注输入**：多行文本框，绑定 `ViewingChecklistResult.comment` 或 `SigningChecklistResult.comment`
- **设计意图**：用户进入详情页后可以立即记录结果，不需要滚动

### 功能区（中部，可选）

- **仅部分 item 有此区域**，由 App 代码硬编码实现
- checklist JSON 不提供功能区内容，只提供静态参考内容
- **不显示条件**：如果当前 item 没有对应的功能区代码，此区域完全不渲染
- **典型场景举例**（需 App 实现时按需扩展）：

| item code | 功能区内容 | 数据来源 |
|-----------|-----------|----------|
| `meter_network_baseline` | 当地水电价格参考 | `rentalRegion` |
| `rent_deposit_payment_cycle` | 总费用计算（租金×月数+押金） | `rentAmount` + `depositRule` |
| `daily_amenities` | 周边配套地图 | `property.address` |

### 参考区（底部）

字段按以下顺序渲染，**仅渲染有值的字段**：

| 顺序 | JSON 字段 | 展示形式 | 说明 |
|------|----------|----------|------|
| 1 | `riskSignals` | 无序列表 | 优先展示——用户最需要知道"什么算有问题" |
| 2 | `howToCheck` | 有序列表 | 具体检查步骤 |
| 3 | `captureAdvice` | 无序列表 + 拍照按钮 | 末尾附 `[📷 拍照]` 快捷按钮 |
| 4 | `recordAdvice` | 段落文字 | 记录建议 |
| 5 | `whyCheck` | 段落文字 | 背景说明，折叠或置底 |
| 6 | `referenceLinks` | 链接列表 | 外部参考，每条含 title + url + note |

### 拍照按钮行为

- `[📷 拍照]` 按钮在 `captureAdvice` 列表末尾
- 点击直接调起系统相机（CameraX）
- 拍照完成后自动关联到当前 item 的 `ViewingChecklistResult`（通过 `linkedChecklistResultIds`）
- 照片自动继承当前 `viewingId` 和 `propertyId`

---

## 对 Schema 的影响

当前 schema 不需要修改。以上设计方案完全在 UI 层实现，不改变 JSON 结构。

**隐含要求**：`oneLine` 字段的质量需要保证——它是一级卡片的核心副标题，不能是 label 的简单重复。

### oneLine 质量标准

| 好的 oneLine | 差的 oneLine |
|-------------|-------------|
| "通过中介找房时，要先确认机构、人员、收费和服务合同。" | "确认中介是否可靠"（重复 label） |
| "需要评估夜间回家路线的照明、人流和死角情况。" | "检查夜间安全"（过于笼统） |
| "燃气阀门、软管、灶具和热水器位置都需要重点检查。" | "检查燃气设备"（丢失具体性） |

好的 oneLine 应该：补充"查什么"或"怎么判断"，而不是重复 label 的问题。

---

## 验证结果

### 验证 1：一级卡片模拟（3 个 viewing item）

**item 1 — agency_reliability**
```
┌─────────────────────────────────────────┐
│ 中介或租赁企业是否可靠                  │
│ 通过中介或住房租赁企业找房时，要先确认  │
│ 机构、人员、收费和服务合同。             │
│                            [高] ○ 未检查 │
└─────────────────────────────────────────┘
```
label 问"是否可靠"，oneLine 补充"要确认什么"——不重复 ✓

**item 2 — renovation_formaldehyde_risk**
```
┌─────────────────────────────────────────┐
│ 是否疑似串串房或甲醛风险房              │
│ 全屋精装修、家具家电全新但价格明显低的  │
│ 房源，要警惕低成本翻新和甲醛风险。      │
│                            [高] ✓ 已检查 │
└─────────────────────────────────────────┘
```
label 问"是否疑似"，oneLine 补充"什么特征算疑似"——不重复 ✓

**item 3 — bathroom_mold_odor**
```
┌─────────────────────────────────────────┐
│ 潮湿、霉点和异味是否明显                │
│ 湿区、墙角和柜体霉点通常意味着通风差、  │
│ 渗水或长期潮湿。                        │
│                            [高] ⚠ 有风险 │
└─────────────────────────────────────────┘
```
label 问"是否明显"，oneLine 补充"霉点意味着什么"——不重复 ✓

### 验证 2：二级详情页模拟（1 个 item）

**agency_reliability 详情页**：
- 操作区：三个结果按钮 + 备注框 ✓
- 参考区：riskSignals（3 条）→ howToCheck（3 条）→ captureAdvice（2 条 + 拍照按钮）→ recordAdvice → whyCheck → referenceLinks（2 条）✓
- 无功能区，不渲染 ✓
- 无功能区，不渲染 ✓
- 三区布局合理 ✓

### 验证 3：全部 46 个 item 的 oneLine 质量

**Viewing checklist（21 items）**：全部通过

| category | item code | oneLine 质量 |
|----------|-----------|-------------|
| agency | agency_reliability | ✓ 补充"要确认什么" |
| agency | negotiation_leverage | ✓ 补充"怎么议价" |
| neighborhood | late_night_safety | ✓ 补充"评估什么" |
| neighborhood | external_noise_sources | ✓ 补充"避开哪些噪音源" |
| neighborhood | daily_amenities | ✓ 补充"重点确认哪些配套" |
| neighborhood | neighbor_public_area | ✓ 补充"为什么重要" |
| property_safety | illegal_space_or_partition | ✓ 补充"哪些算非居住空间" |
| property_safety | old_building_fire_safety | ✓ 补充"什么情况要谨慎" |
| property_safety | renovation_formaldehyde_risk | ✓ 补充"什么特征要警惕" |
| living_areas | natural_light_orientation | ✓ 补充"怎么看" |
| living_areas | window_door_sound_seal | ✓ 补充"影响哪些方面" |
| living_areas | furniture_mattress_condition | ✓ 补充"影响什么" |
| kitchen | kitchen_drain_smoke_flue | ✓ 补充"会导致什么问题" |
| kitchen | gas_safety | ✓ 补充"检查什么" |
| bathroom | bathroom_water_drainage | ✓ 补充"影响什么" |
| bathroom | bathroom_mold_odor | ✓ 补充"意味着什么" |
| whole_unit | meter_network_baseline | ✓ 补充"记录什么" |
| whole_unit | appliance_condition | ✓ 补充"影响什么" |
| whole_unit | visible_damage | ✓ 补充"为什么要留痕" |
| whole_unit | privacy_security_risk | ✓ 补充"哪些方面影响隐私" |
| co_living | co_living_rules | ✓ 补充"比整租复杂在哪" |

**Signing checklist（25 items）**：全部通过

| category | item code | oneLine 质量 |
|----------|-----------|-------------|
| identity_authorization | counterparty_identity | ✓ 补充"确认对方是谁" |
| identity_authorization | rental_authority | ✓ 补充"什么时候需要确认授权" |
| identity_authorization | property_rights_risk | ✓ 补充"确认哪些权利风险" |
| identity_authorization | property_match | ✓ 补充"防止什么不一致" |
| filing_local_rules | lease_filing | ✓ 补充"怎么做" |
| filing_local_rules | local_special_use | ✓ 补充"哪些用途要写明" |
| rent_deposit_cycle | rent_deposit_payment_cycle | ✓ 补充"哪些必须写入合同" |
| fund_security | rent_loan_risk | ✓ 补充"哪些说法要警惕" |
| fund_security | fund_supervision | ✓ 补充"什么场景需要确认" |
| lease_term_exit | lease_term_delivery | ✓ 补充"哪些要写清" |
| lease_term_exit | renewal_rent_increase_entry | ✓ 补充"哪些规则要写清" |
| lease_term_exit | termination_breach | ✓ 补充"哪些要具体明确" |
| maintenance_delivery | maintenance_boundary | ✓ 补充"哪些必须明确" |
| maintenance_delivery | handover_inventory | ✓ 补充"清单应写清什么" |
| maintenance_delivery | delivery_condition | ✓ 补充"什么要可核对" |
| inventory_promises | inventory_listed | ✓ 补充"不写入会怎样" |
| inventory_promises | promises_written | ✓ 补充"哪些承诺要转文字" |
| special_terms | sublease_guest_rule | ✓ 补充"影响什么" |
| special_terms | special_living_rules | ✓ 补充"哪些规则要写清" |
| special_terms | supplementary_agreement | ✓ 补充"为什么要核对" |
| post_signing | post_signing_lock_access | ✓ 补充"确认什么" |
| post_signing | post_signing_cleaning | ✓ 补充"清洁什么" |
| post_signing | post_signing_contacts_payment | ✓ 补充"整理什么" |
| dispute_evidence | evidence_chain | ✓ 补充"保留什么" |
| dispute_evidence | dispute_resolution | ✓ 补充"约定什么" |

**结论**：46/46 个 item 的 oneLine 均满足质量标准，无需修改。

---

## 后续可演进方向（MVP 不做）

1. **模板驱动排序**：根据用户画像调整 item 在列表中的顺序
2. **动态 oneLine**：根据用户画像显示不同的副标题重点
3. **功能区 schema 化**：将硬编码的功能区抽象为 schema 声明
4. **item 间条件依赖**：某些 item 的检查结果影响其他 item 的显示
5. **多次看房合并视图**：同一 item 多次记录的聚合展示

---

## 关键文件

- `shared/checklist/viewing-checklist.json` — 看房 item 数据源
- `shared/checklist/signing-checklist.json` — 签约 item 数据源
- `shared/schemas/checklist.schema.json` — item 字段定义
- `shared/schemas/manifest.v1.json` — checklistResult 数据结构
- `docs/product/information-architecture.md` — 页面树定义（第 5、6、10、11 页）
