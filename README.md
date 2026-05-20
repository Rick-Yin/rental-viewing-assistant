# Rental Viewing Assistant

[English](./README.md) | [简体中文](./README.zh-CN.md)

> A local-first mobile app for structured apartment viewing, shortlist comparison, and signing review.

[![Stage](https://img.shields.io/badge/stage-pre--MVP-0f766e)](#current-status)
[![Platforms](https://img.shields.io/badge/platform%20roadmap-Android%20%E2%86%92%20iOS%20%E2%86%92%20HarmonyOS-2563eb)](./docs/decisions/0001-platform-roadmap.md)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-b91c1c)](./LICENSE)

Rental Viewing Assistant helps renters turn fragmented viewing notes and signing concerns into structured decision material.
Users inspect multiple properties during the viewing stage, compare shortlisted options, then move exactly one selected property into a signing stage with its own checklist, materials, and AI-assisted review.

## Why This Project Exists

Apartment hunting usually breaks down in the same places:

- Too many details must be checked on-site in a short amount of time.
- Weak signals such as noise, dampness, poor ventilation, aging appliances, hidden fees, or vague promises are easy to miss.
- Photos, chat logs, and memory do not naturally turn into comparable decisions.
- After choosing one property, signing risks are often reviewed in a rushed and unstructured way.

This project focuses on the missing workflow between *visiting multiple properties*, *shortlisting one option*, and *reviewing signing risks before committing*.

## Core Product Loop

1. Create local property records.
2. Capture viewing facts, checklist items, notes, and photos for multiple properties.
3. Compare shortlisted options side by side.
4. Move one selected property into the signing stage.
5. Review a signing checklist, attach signing materials, and generate an AI-friendly signing review package.
6. Paste structured AI output back into the app and finalize the property as `signed` or `signing_abandoned`.

## Product Principles

- `Local-first`: raw records, media, and analysis history stay on device by default.
- `AI-friendly`: outbound sharing is optimized for mainstream mobile AI clients.
- `Structured`: internal schemas stay stable even when external AI apps vary.
- `Decision support`: the app organizes evidence and recommendations during both viewing and signing; it does not replace the renter's judgment.

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

- [`docs/product`](./docs/product): product definition, page tree, flows, and interaction-level specs.
- [`docs/research`](./docs/research): competitor and market research.
- [`docs/decisions`](./docs/decisions): key project decisions and rationale.
- [`design`](./design): HTML interactive prototype and design-related references.
- [`shared`](./shared): cross-platform schemas, prompt contracts, checklist definitions, and fixtures.
- [`apps`](./apps): client implementations by platform.
- [`samples`](./samples): sanitized examples only. No real property data.

## Current Status

This repository is currently in the `pre-MVP` phase.

What is already defined:

- product direction
- target users
- local-first storage stance
- dual-stage workflow: `viewing -> signing`
- shared manifest contract
- dual AI result contracts for viewing and signing
- built-in checklist libraries for viewing and signing
- competitor landscape
- platform rollout order

What still needs to be locked down before implementation:

- Android module skeleton and persistence wiring

## Recommended Reading Order

1. [docs/product/overview.md](./docs/product/overview.md)
2. [docs/product/information-architecture.md](./docs/product/information-architecture.md)
3. [docs/decisions/0001-platform-roadmap.md](./docs/decisions/0001-platform-roadmap.md)
4. [design/prototype/index.html](./design/prototype/index.html)

## License

This repository uses **PolyForm Noncommercial 1.0.0**.

- Source is visible and reusable for noncommercial purposes.
- Commercial use is not permitted under the default license.
- If commercial use is needed, a separate commercial license or written permission is required from the licensor.

See [LICENSE](./LICENSE) and [NOTICE](./NOTICE).
