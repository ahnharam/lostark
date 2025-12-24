---
name: wcag-readable-text-style
description: Analyze WCAG 3.0 for web design needs, especially text readability, focus, input, error handling, layout, motion, and user control. Use when asked to summarize WCAG 3.0, build a web accessibility style guide, create 2.3–2.7 checklists, or define/readjust text block standards (line length, line height, paragraph spacing, alignment).
---

# WCAG Readable Text Style

## Overview
Provide a repeatable way to summarize WCAG 3.0 (draft) and apply it to web design decisions, with a deep focus on text readability.

## Workflow
1. Identify the request scope:
   - **Full WCAG overview**: Use `references/wcag-3-overview.md`.
   - **Text readability deep dive**: Use `references/wcag-readable-blocks-of-text.md`.
   - **2.3–2.7 focus/input/layout/motion checklists**: Use `references/wcag-3-checklists-2-3-2-7.md`.
2. Identify content language(s) and the closest orthography fallback.
3. Draft the guide or review notes with:
   - The relevant section map (2.x overview or text-specific table)
   - Web implementation guidance (layout + CSS tokens)
   - A verification checklist
   - A clear callout that WCAG 3.0 values are draft/example values
4. If working in this repository, align with `frontend/docs/guide-readable-text-style.md` and update it when necessary.

## Web Implementation Guidance
- Use `ch` units for line-length control (min/max widths).
- Keep paragraph spacing at least the block margin.
- Default to left alignment; justify only when the content purpose supports it.

## Output Checklist
- Cite the WCAG 3.0 source URL.
- State the draft status and placeholder nature of metrics.
- List values for each characteristic; mark missing values as TBD.
- Call out language-specific gaps and the chosen fallback language.

## Resources
- `references/wcag-3-overview.md`
- `references/wcag-3-checklists-2-3-2-7.md`
- `references/wcag-readable-blocks-of-text.md`
