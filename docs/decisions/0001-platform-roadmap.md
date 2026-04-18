# 0001 Platform Roadmap

- Status: accepted
- Date: 2026-04-19

## Decision

The project will roll out in this order:

1. Android
2. iOS
3. HarmonyOS

## Rationale

- Android provides the broadest early device coverage.
- The product's first critical loop is on-site mobile capture, AI sharing, and structured import.
- Android is the fastest path to validating that loop with the largest initial audience.
- iOS is the next platform needed to cover the mainstream mobile market.
- HarmonyOS follows after the first two mobile platforms are validated.

## Consequences

- The first implementation work should assume Android as the primary client.
- Shared schemas, prompts, and checklist definitions should be kept platform-neutral from day one.
- iOS and HarmonyOS directories can remain as placeholders until their phases start.
- Design work should avoid Android-specific assumptions when defining product structure, while still
  allowing Android to lead implementation.

