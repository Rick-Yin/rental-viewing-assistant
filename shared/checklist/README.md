# Shared Checklist Definitions

This directory contains the structured checklist definitions for the rental viewing assistant.

## Data Source Hierarchy

**JSON is the single source of truth.** Markdown templates are retained as editing references only.

| File | Role | Status |
|------|------|--------|
| `viewing-checklist.json` | Primary data source for viewing checklist | **Active** |
| `signing-checklist.json` | Primary data source for signing checklist | **Active** |
| `viewing-checklist.template.md` | Editing reference / draft | Editing draft |
| `signing-checklist.template.md` | Editing reference / draft | Editing draft |
| `rental-profile.template.md` | Pre-checklist rental profile | Active |

## Structure

Each JSON file contains:

- `categories`: Groups of checklist items organized by physical space (viewing walkthrough order)
- `templateModules`: Module definitions that control which items are shown based on user profile
- Each item has: `code`, `label`, `oneLine`, `whyCheck`, `howToCheck`, `riskSignals`, `captureAdvice`, `recordAdvice`, `referenceLinks`, `priority`, `analysisMapping`

Schema definition: `shared/schemas/checklist.schema.json`

## Editing Workflow

1. Edit the markdown templates to draft new content or refine existing items.
2. Port changes into the JSON files, keeping item `code` values stable.
3. Ensure every item in the markdown template has a corresponding entry in the JSON.
4. After updating JSON, validate against the schema.

## Why JSON over Markdown

- Structured fields (`howToCheck`, `riskSignals`, `captureAdvice`) enable layered card UI.
- `analysisMapping.dimensions` links items to AI analysis result dimensions.
- `templateModules` enables profile-based checklist recommendations.
- All codes match `^[a-z0-9_]+$` for safe use as identifiers.

## Template Modules

Template modules control which checklist items are recommended based on user profile (tenant type, gender, special purposes, etc.). See `docs/product/checklist-template-recommendation.md` for recommendation rules.

- **Always-enabled modules**: Core items every user should check.
- **Optional modules**: Recommended based on user scenario (co-living, pets, children, etc.).

## Viewing and Signing Stay Split

They serve different stages and should not be merged into a single oversized checklist.

Rental profile and search preferences stay separate from checklist items. They are collected before template selection and should guide checklist recommendation and scoring weight.
