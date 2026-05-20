# Rental Viewing Assistant

[English](./README.md) | [简体中文](./README.zh-CN.md)

> 一款本地优先的移动端租房助手，覆盖看房记录、多房对比与签约核查两个阶段。

[![Stage](https://img.shields.io/badge/stage-pre--MVP-0f766e)](./README.zh-CN.md#当前状态)
[![Platforms](https://img.shields.io/badge/platform%20roadmap-Android%20%E2%86%92%20iOS%20%E2%86%92%20HarmonyOS-2563eb)](./docs/decisions/0001-platform-roadmap.md)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-b91c1c)](./LICENSE)

Rental Viewing Assistant 帮助租客把零散的看房记录和签约疑点整理成结构化决策材料。  
用户先在看房阶段记录多套房源的 checklist、备注和照片，再把心仪房源切换到签约阶段，用独立的签约 checklist、签约材料和 AI 辅助审查完成最终决策。

## 项目要解决什么问题

租房过程中经常在这些环节失真：

- 现场时间短，需要检查的细节很多，容易漏看。
- 隔音、潮湿、通风、老旧电器、隐藏费用等弱信号不容易被及时记录。
- 多套房的照片、聊天记录和主观记忆很难自然变成可比较的判断材料。
- 选中一套房后，签约条款、责任边界和口头承诺经常来不及系统核查。

这个项目聚焦的是从“看多套房”到“选出一套签约并核查风险”之间的缺口。

## 核心产品闭环

1. 创建多套房源的本地记录。
2. 在看房阶段填写基础信息、看房 checklist、备注和照片。
3. 对多套房进行横向对比，选出准备推进的一套。
4. 把该房源切换到签约阶段，完成签约 checklist 和签约材料整理。
5. 生成适合手机端 AI 的签约审查内容，并把结构化结果粘贴回 App。
6. 将房源最终标记为 `已签约` 或 `签约放弃`。

## 产品原则

- `Local-first`：原始记录、媒体文件和分析历史默认留在设备本地。
- `AI-friendly`：对外分享格式优先适配主流手机端 AI。
- `Structured`：内部 schema 稳定，方便回导、对比和跨平台复用。
- `Decision support`：产品在看房和签约两个阶段组织证据和建议，不替代用户做最终决定。

## 平台路线

- `第一阶段`：Android
- `第二阶段`：iOS
- `第三阶段`：HarmonyOS

当前平台顺序见 [0001-platform-roadmap.md](./docs/decisions/0001-platform-roadmap.md)。

## 仓库结构

```text
.
├─ apps/
│  ├─ android/
│  ├─ ios/
│  └─ harmony/
├─ design/
├─ docs/
│  ├─ decisions/
│  ├─ product/
│  └─ research/
├─ samples/
├─ scripts/
└─ shared/
   ├─ checklist/
   ├─ fixtures/
   ├─ prompts/
   └─ schemas/
```

### 目录分工

- [`docs/product`](./docs/product)：产品定义、页面树、流程和交互规格
- [`docs/research`](./docs/research)：竞品与市场调研
- [`docs/decisions`](./docs/decisions)：关键决策记录
- [`design`](./design)：HTML 交互原型和设计参考
- [`shared`](./shared)：跨平台 schema、prompt 契约、checklist 和 fixtures
- [`apps`](./apps)：各平台客户端实现
- [`samples`](./samples)：脱敏示例数据，不存真实房屋信息

## 当前状态

当前仓库处于 `pre-MVP` 阶段。

已经确定的内容：

- 产品方向
- 目标用户
- 本地优先原则
- 双阶段流程：`看房 -> 签约`
- 共享 manifest 契约
- 看房/签约两套 AI 回贴契约
- 看房/签约两套 checklist 题库
- 竞品大盘
- 平台发布顺序

在正式开发前仍需锁定的内容：

- Android 模块骨架与本地存储落地

## 建议阅读顺序

1. [docs/product/overview.md](./docs/product/overview.md)
2. [docs/product/information-architecture.md](./docs/product/information-architecture.md)
3. [docs/decisions/0001-platform-roadmap.md](./docs/decisions/0001-platform-roadmap.md)
4. [design/prototype/index.html](./design/prototype/index.html)

## 许可协议

本仓库使用 **PolyForm Noncommercial 1.0.0**。

- 源码可见，可在非商业场景下使用和复用
- 默认不允许商业使用
- 如果需要商业使用，需要额外商业授权或作者书面许可

详见 [LICENSE](./LICENSE) 和 [NOTICE](./NOTICE)。
