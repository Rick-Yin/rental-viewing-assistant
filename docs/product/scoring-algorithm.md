# 评分算法

## 概述

项目存在两套评分体系，职责不同：

| 体系 | 计算方 | 维度 | 用途 |
|------|--------|------|------|
| ScoreCard | App 自动计算 | 6 个维度，0-100 分 | 多房源横向对比排序 |
| AI 分析维度 | 外部 AI 输出 | 5 个维度，定性评级 | 单房风险诊断 |

两套体系独立运作，互不影响。

## 体系 A：ScoreCard（App 内部计算）

### 维度定义

| 维度 | 字段名 | 含义 |
|------|--------|------|
| 交通 | transportScore | 通勤便利性、夜间安全、交通可达性 |
| 环境 | environmentScore | 噪音、邻里、周边配套、小区公共区域 |
| 户型 | layoutScore | 采光、朝向、通风、门窗隔音、家具状态 |
| 设施 | facilityScore | 水电燃气、网络、家电、厨房卫生间、墙面地板 |
| 风险 | riskScore | 违规隔断、消防隐患、串串房/甲醛、燃气安全 |
| 性价比 | pricePerformanceScore | 中介可靠性、议价空间、隐性费用 |

### Category → Dimension 映射

每个 checklist item 通过所属 category 映射到一个 scoreCard 维度：

| category | item code | → 维度 |
|----------|-----------|--------|
| neighborhood | late_night_safety | transportScore |
| neighborhood | external_noise_sources | environmentScore |
| neighborhood | daily_amenities | environmentScore |
| neighborhood | neighbor_public_area | environmentScore |
| property_safety | illegal_space_or_partition | riskScore |
| property_safety | old_building_fire_safety | riskScore |
| property_safety | renovation_formaldehyde_risk | riskScore |
| living_areas | natural_light_orientation | layoutScore |
| living_areas | window_door_sound_seal | layoutScore |
| living_areas | furniture_mattress_condition | layoutScore |
| kitchen | kitchen_drain_smoke_flue | facilityScore |
| kitchen | gas_safety | riskScore |
| bathroom | bathroom_water_drainage | facilityScore |
| bathroom | bathroom_mold_odor | facilityScore |
| whole_unit | meter_network_baseline | facilityScore |
| whole_unit | appliance_condition | facilityScore |
| whole_unit | visible_damage | facilityScore |
| co_living | co_living_rules | 不进入 ScoreCard |
| agency | agency_reliability | pricePerformanceScore |
| agency | negotiation_leverage | pricePerformanceScore |

> 注：`gas_safety` 虽然在 kitchen category 下，但燃气安全属于安全红线，映射到 riskScore。

### 单 Item 得分公式

```
itemScore = baseValue × severityMultiplier × priorityWeight
```

#### baseValue（由 resultValue 决定）

| resultValue | baseValue | 说明 |
|-------------|-----------|------|
| ok | 100 | 检查通过 |
| uncertain | 50 | 不确定，需要复查 |
| risk | 0 | 发现风险 |
| unchecked | null | 未检查，不参与计算 |

#### severityMultiplier（由 severity 决定）

| severity | multiplier | 含义 |
|----------|------------|------|
| none | 1.0 | 无严重性标注 |
| low | 0.85 | 低风险 |
| medium | 0.65 | 中等风险 |
| high | 0.40 | 高风险 |

#### priorityWeight（由 item 的 priority 决定）

| priority | weight | 说明 |
|----------|--------|------|
| high | 1.2 | 高优先级 item，权重放大 |
| medium | 1.0 | 标准权重 |
| low | 0.8 | 低优先级 item，权重缩小 |

#### 计算示例

| 场景 | resultValue | severity | priority | itemScore |
|------|-------------|----------|----------|-----------|
| 正常通过 | ok | none | high | 100 × 1.0 × 1.2 = 120 → 封顶 100 |
| 低风险 | risk | low | medium | 0 × 0.85 × 1.0 = 0 |
| 不确定+高严重 | uncertain | high | high | 50 × 0.40 × 1.2 = 24 |
| 正常通过 | ok | none | low | 100 × 1.0 × 0.8 = 80 |

> 注意：itemScore 上限为 100，下限为 0。

### 维度得分计算

```
dimensionScore = sum(itemScore_i) / count(已检查 items)
```

- 只有 `resultValue` 不是 `unchecked` 的 item 才参与计算
- 未检查的 item 不拉低分数（鼓励用户先检查再评分）
- 如果某维度下所有 item 都未检查，该维度显示为"待检查"状态，不计入总分

### 总分计算

```
totalScore = transport × 0.15
           + environment × 0.20
           + layout × 0.15
           + facility × 0.20
           + risk × 0.20
           + pricePerf × 0.10
```

权重分配逻辑：
- environment 和 facility 各 0.20：日常居住体验最直接影响
- risk 0.20：安全红线不能忽视
- transport 和 layout 各 0.15：重要但可妥协
- pricePerf 0.10：辅助决策

### 特殊规则

#### 安全红线兜底

当 `riskScore` 维度中存在以下条件时，`totalScore` 上限锁定为 **60 分**：

- 任何 `resultValue = risk` 且 `severity = high` 的 item

这意味着即使其他维度满分，有重大安全隐患的房源也不会排到对比前列。

#### 维度缺失处理

- 如果某维度所有 item 都未检查：该维度标记为 `pending`，不参与 totalScore 计算
- totalScore 只用已计算的维度按比例归一化：
  ```
  totalScore = (已检查维度得分 × 权重) / (已检查维度权重之和)
  ```

#### 版本管理

- 当前版本：`scoringVersion: "1.0.0"`
- 公式调整时递增版本号（如调整权重 → `"1.1.0"`，增加新维度 → `"2.0.0"`）
- 历史 ScoreCard 记录保留生成时的 scoringVersion，确保旧数据可解释

## 体系 B：AI 分析维度（外部 AI 输出）

App 不计算这套维度，只解析和展示 AI 返回的结果。

### 维度定义

| 维度 | 字段名 | 含义 |
|------|--------|------|
| 舒适度 | comfort | 采光、通风、噪音、空间感受 |
| 隐性成本 | hiddenCosts | 商水商电、中介费、家电维修、隐藏杂费 |
| 卫生与健康 | hygieneHealth | 潮湿、霉菌、甲醛、清洁度 |
| 安全 | safety | 消防、门锁、隐私、夜间安全 |
| 信息完整度 | informationCompleteness | 已收集信息是否充分、缺失项 |

### 评级

每个维度有：
- `level`：positive / mixed / risk / unknown
- `summary`：一句话说明

### 与 ScoreCard 的关系

| ScoreCard 维度 | 相关 AI 维度 | 关系 |
|----------------|-------------|------|
| transportScore | comfort | 部分重叠 |
| environmentScore | comfort, safety | 部分重叠 |
| layoutScore | comfort | 部分重叠 |
| facilityScore | hiddenCosts, hygieneHealth | 部分重叠 |
| riskScore | safety | 强相关 |
| pricePerformanceScore | hiddenCosts | 部分重叠 |

两套体系可以互相参考，但不应强制对齐。AI 维度更灵活，可以覆盖 ScoreCard 无法量化的主观判断。

## 非功能要求

- 计算必须在本地完成，不依赖网络
- 计算耗时应在 100ms 以内（单房源）
- 公式变更必须通过 scoringVersion 管理，不能静默修改
- 导出时必须包含 scoringVersion 和各维度原始得分
