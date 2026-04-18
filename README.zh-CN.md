# Rental Viewing Assistant

[English](./README.md) | [简体中文](./README.zh-CN.md)

> 一款本地优先的移动端租房看房助手，用结构化记录、AI 辅助分析和多房对比来支持租房决策。

[![Stage](https://img.shields.io/badge/stage-pre--MVP-0f766e)](./README.zh-CN.md#当前状态)
[![Platforms](https://img.shields.io/badge/platform-roadmap-Android%20%E2%86%92%20iOS%20%E2%86%92%20HarmonyOS-2563eb)](./docs/decisions/0001-platform-roadmap.md)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-b91c1c)](./LICENSE)

Rental Viewing Assistant 帮助租客把混乱的看房过程整理成结构化决策材料。  
用户可以在看房现场记录备注、checklist 和照片，从手机生成适合 AI 读取的分享内容，再把结构化结果粘贴回 App，用于查看单房诊断和多房对比。

## 项目要解决什么问题

租房看房经常在这些环节失真：

- 现场时间短，需要检查的细节很多，容易漏看。
- 隔音、潮湿、通风、老旧电器、隐藏费用等弱信号不容易被及时记录。
- 照片、聊天记录和主观记忆很难自然变成可比较的判断材料。
- 通用 AI 虽然能辅助分析，但经常拿不到稳定、结构化的输入。

这个项目聚焦的是从“看完一套房”到“做出清晰判断”之间的缺口。

## 核心产品闭环

1. 创建一套房的本地记录。
2. 填写房屋基础信息、checklist、备注和照片。
3. 生成适合手机端 AI 的分享内容。
4. 把 `share_note + images` 发送给主流 AI App。
5. 将结构化 AI 结果粘贴回 App。
6. 查看单房诊断，并把多套房放到同一个对比板中比较。

## 产品原则

- `Local-first`：原始记录、媒体文件和分析历史默认留在设备本地。
- `AI-friendly`：对外分享格式优先适配主流手机端 AI。
- `Structured`：内部 schema 稳定，方便回导、对比和跨平台复用。
- `Decision support`：产品组织证据和建议，不替代用户做最终决定。

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

- [`docs/product`](./docs/product)：产品定义、信息架构、页面流和交互规格
- [`docs/research`](./docs/research)：竞品与市场调研
- [`docs/decisions`](./docs/decisions)：关键决策记录
- [`design`](./design)：Figma 链接、导出图和设计参考
- [`shared`](./shared)：跨平台 schema、prompt、checklist 和 fixtures
- [`apps`](./apps)：各平台客户端实现
- [`samples`](./samples)：脱敏示例数据，不存真实房屋信息

## 当前状态

当前仓库处于 `pre-MVP` 阶段。

已经确定的内容：

- 产品方向
- 目标用户
- 本地优先原则
- AI Quick Share 形式
- 结果页方向
- 竞品大盘
- 平台发布顺序

在正式开发前仍需锁定的内容：

- 信息架构和页面树
- 严格 schema 定义
- prompt 与 AI 回贴协议
- checklist 题库
- Android 端技术选型和本地存储方案

## 建议阅读顺序

1. [docs/product/overview.md](./docs/product/overview.md)
2. [docs/research/competitors.md](./docs/research/competitors.md)
3. [docs/decisions/0001-platform-roadmap.md](./docs/decisions/0001-platform-roadmap.md)
4. [design/figma-links.md](./design/figma-links.md)

## 许可协议

本仓库使用 **PolyForm Noncommercial 1.0.0**。

- 源码可见，可在非商业场景下使用和复用
- 默认不允许商业使用
- 如果需要商业使用，需要额外商业授权或作者书面许可

详见 [LICENSE](./LICENSE) 和 [NOTICE](./NOTICE)。
