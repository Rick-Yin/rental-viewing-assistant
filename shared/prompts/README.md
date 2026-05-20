# Shared Prompts

This directory is for reusable outbound prompt templates.

Prompt families should stay split by stage:

- viewing stage prompts
- signing stage prompts
- import contract instructions

Prompts should be versioned alongside schema changes.

Current implementation note:

- viewing and signing use separate AI result schemas, so prompt templates should target one stage at a time rather than branching inside a single prompt.
- Full prompt templates and AI workflow are defined in [docs/product/ai-workflow.md](../../docs/product/ai-workflow.md).
