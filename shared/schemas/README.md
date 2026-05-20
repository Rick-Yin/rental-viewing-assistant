# Shared Schemas

This directory is for strict cross-platform structures.

Current files:

- `manifest.v1.json`: strict single-property manifest covering both viewing and signing stages
- `viewing-analysis-result.v1.json`: AI return contract for the viewing stage
- `signing-analysis-result.v1.json`: AI return contract for the signing stage

Notes:

- `manifest.v1.json` uses `relativePath` for media references so the bundle stays portable across devices and platforms.
- App-internal absolute file paths should be resolved by the platform client, not stored in exported data.
- Viewing and signing use separate AI result schemas on purpose. Their responsibilities are different and should stay parseable without stage-specific branching.
