# Product Overview

## One-Sentence Definition

Rental Viewing Assistant is a local-first mobile app that helps renters record apartment visits in a structured way, share AI-friendly review material from the phone, and compare multiple properties with clearer evidence.

## Goals

The product is designed to improve three moments in the apartment-hunting workflow:

- reduce missed checks during on-site visits
- preserve evidence, notes, and context after a visit
- make multi-property comparison more stable and less memory-driven

## Product Shape

The current product loop is:

1. Create a local property record.
2. Fill in key property facts.
3. Complete a structured checklist during the visit.
4. Attach notes and photos to suspicious or uncertain items.
5. Generate AI-friendly outbound share content.
6. Send text plus images to an external AI app.
7. Paste structured AI results back into the app.
8. Review a single-property diagnosis and compare multiple properties.

## Local-First Principles

- Raw records, media files, and imported analysis results stay on device by default.
- Data leaves the device only when the user explicitly shares, copies, or exports it.
- The app remains useful even if the user never sends anything to an external AI tool.

## MVP Scope

### Target Users

- first-time or less experienced renters
- users who visit multiple properties in a short period
- users who want to turn on-site impressions into comparable decision material

### Platform Rollout

- mobile-first
- Android in phase 1
- iOS in phase 2
- HarmonyOS in phase 3

### On-Site Inputs

- property basic information
- photos
- text notes
- built-in checklist
- user-defined checklist additions

### Analysis Outputs

- AI Quick Share
- single-property diagnosis
- multi-property comparison board
- missing-information reminders
- suggested recheck items

## Core Flow

### Create Property Record

Each property is saved as an independent record. Suggested minimum fields:

- `title`
- `address`
- `rent`
- `deposit_terms`
- `layout`
- `area`
- `floor`
- `orientation`
- `viewing_time`
- `agent_or_landlord_note`

### Complete On-Site Inspection

Users go through checklist items during the visit. Each item supports one of four states:

- `未检查`
- `正常`
- `有风险`
- `不确定`

Each item can also include:

- a short note
- linked photos
- user-defined subitems

### Generate AI Share Content

The app prepares outbound content optimized for mainstream mobile AI tools: text in `share_note.md` or
`share_note.txt` plus a small set of key JPEG images.

### Import AI Results

Users paste AI output back into the app. Valid structured results are then used to render diagnosis
and comparison views.

## Default Checklist Categories

The current built-in checklist framework has 10 categories:

- noise and sound insulation
- lighting and ventilation
- humidity, mold, and odor
- appliance condition and energy-use concerns
- hygiene, pests, and drainage
- water, electricity, gas, and connectivity
- doors, windows, and safety
- public area quality and neighborhood environment
- commuting cost and nearby amenities
- contract, fees, and hidden terms

## Data Objects

### `PropertyRecord`

Stores the property facts and user-entered context.

### `ChecklistItem`

Stores the structured inspection state of one item.

### `PhotoItem`

Stores image metadata and the relationships between photos and checklist items.

### `AIAnalysisResult`

Stores structured AI output returned to the app for visualization and comparison.

## AI Sharing Protocol

### Outbound Layer

The current outbound sharing layer is designed for free mainstream mobile AI clients:

- `share_note.md` or `share_note.txt`
- standalone `JPEG` images

This format is chosen because text plus images is the most stable common denominator across mobile AI
apps.

### Internal Structured Layer

The internal structured layer is kept in `manifest.json`, which stores:

- property facts
- checklist entries
- image references and captions
- import linkage such as `property_id`

### Return Layer

The AI return layer is a strict structured JSON object so that:

- imported results remain parseable
- diagnosis views are repeatable
- multi-property comparison consumes stable fields

## Result Views

### Single-Property Diagnosis

The diagnosis view should surface:

- overall recommendation
- top concerns
- missing information
- suggested recheck items
- original notes and key photos

### Multi-Property Comparison

The comparison board should display at least:

- comfort
- hidden costs
- hygiene and health
- safety
- contract and fee risk
- information completeness

The current product direction prefers discrete levels and grouped labels over a complex numerical
score.

## Current Product Judgments

- The core scenario is rental viewing and post-visit comparison.
- The most important inputs are checklist structure, notes, and key photos.
- The most important share format is `Markdown/TXT + JPEG`.
- The most important internal structure is `manifest.json + AIAnalysisResult JSON`.
- The most important decision surface is the multi-property comparison board.

