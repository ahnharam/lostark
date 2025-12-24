---
name: lostark-refactor-utils-extraction
description: Extract pure utility functions from Vue components into frontend/src/utils with templates and type-safety rules. Use when refactoring large components, creating data-transform utils, or preparing extraction prompts.
---

# Lostark Utils Extraction Refactor

## Workflow
1. Read `docs/references/refactoring-guide-utils-extraction.md` for the step-by-step process.
2. Use `docs/references/refactoring-code-snippets.md` for composable/UI templates.
3. Use `docs/references/refactoring-automation-prompt.md` when generating prompts.
4. Enforce type safety: no `any`, use explicit types and imports.
5. Keep Vue Composition API out of utils; export only pure functions.
6. Run `npm run type-check` after changes.

## Output expectations
- Provide a list of candidate functions for extraction.
- Suggest target file path(s) under `frontend/src/utils/`.
- Include required imports and notes on type guards/util helpers.

## Resources
- `docs/references/refactoring-guide-utils-extraction.md`
- `docs/references/refactoring-code-snippets.md`
- `docs/references/refactoring-automation-prompt.md`
