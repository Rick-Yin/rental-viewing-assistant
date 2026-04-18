# Rental Viewing Assistant

[English](./README.md) | [简体中文](./README.zh-CN.md)

> A local-first mobile app for structured apartment viewing, AI-assisted risk review, and multi-property comparison.

[![Stage](https://img.shields.io/badge/stage-pre--MVP-0f766e)](#current-status)
[![Platforms](https://img.shields.io/badge/platform-roadmap-Android%20%E2%86%92%20iOS%20%E2%86%92%20HarmonyOS-2563eb)](./docs/decisions/0001-platform-roadmap.md)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-b91c1c)](./LICENSE)

Rental Viewing Assistant helps renters turn messy apartment viewings into structured decision material.
Users record notes, checklist results, and photos during a visit, share an AI-friendly bundle from their phone, then paste structured results back into the app to review risks and compare multiple properties.

## Why This Project Exists

Apartment hunting usually breaks down in the same places:

- Too many details must be checked on-site in a short amount of time.
- Weak signals such as noise, dampness, poor ventilation, aging appliances, or hidden fees are easy to miss.
- Photos, chat logs, and memory do not naturally turn into comparable decisions.
- Generic AI chats are useful, but they rarely receive stable structured input.

This project focuses on the missing workflow between *visiting a property* and *making a clear decision afterward*.

## Core Product Loop

1. Create a local property record.
2. Fill in key facts, checklist items, notes, and photos during the visit.
3. Generate an AI-friendly share package from the phone.
4. Send `share_note + images` to a mainstream AI app.
5. Paste structured AI output back into the app.
6. Review a single-property diagnosis and compare multiple options side by side.

## Product Principles

- `Local-first`: raw records, media, and analysis history stay on device by default.
- `AI-friendly`: outbound sharing is optimized for mainstream mobile AI clients.
- `Structured`: internal schemas stay stable even when external AI apps vary.
- `Decision support`: the app organizes evidence and recommendations; it does not replace the renter's judgment.

## Platform Roadmap

- `Phase 1`: Android
- `Phase 2`: iOS
- `Phase 3`: HarmonyOS

The current release strategy is documented in [0001-platform-roadmap.md](./docs/decisions/0001-platform-roadmap.md).

## Repository Map

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

### What Goes Where

- [`docs/product`](./docs/product): product definition, information architecture, flows, and interaction-level specs.
- [`docs/research`](./docs/research): competitor and market research.
- [`docs/decisions`](./docs/decisions): key project decisions and rationale.
- [`design`](./design): Figma links, exported previews, and design-related references.
- [`shared`](./shared): cross-platform schemas, prompts, checklist definitions, and fixtures.
- [`apps`](./apps): client implementations by platform.
- [`samples`](./samples): sanitized examples only. No real property data.

## Current Status

This repository is currently in the `pre-MVP` phase.

What is already defined:

- product direction
- target users
- local-first storage stance
- AI quick share format
- result views
- competitor landscape
- platform rollout order

What still needs to be locked down before implementation:

- information architecture and page tree
- strict schema definitions
- prompt and AI return contract
- checklist item library
- Android technical stack and storage choices

## Recommended Reading Order

1. [docs/product/overview.md](./docs/product/overview.md)
2. [docs/research/competitors.md](./docs/research/competitors.md)
3. [docs/decisions/0001-platform-roadmap.md](./docs/decisions/0001-platform-roadmap.md)
4. [design/figma-links.md](./design/figma-links.md)

## License

This repository uses **PolyForm Noncommercial 1.0.0**.

- Source is visible and reusable for noncommercial purposes.
- Commercial use is not permitted under the default license.
- If commercial use is needed, a separate commercial license or written permission is required from the licensor.

See [LICENSE](./LICENSE) and [NOTICE](./NOTICE).

