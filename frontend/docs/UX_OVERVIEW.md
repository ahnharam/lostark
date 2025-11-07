# Lostark Character Explorer – UX & UI Documentation

## 1. Application structure

The current Vue 3 single-page application renders a single high-level page through `App.vue`, which mounts the `CharacterSearch` experience. Routing is defined but unused (`src/router/index.ts` exports an empty route array), so the entire UX is concentrated in one vertically split view:

- **Sidebar (left column) – Persistent utilities**
  - Favorite characters list with avatar, name, and item level.
  - Recent searches list with clear-all control.
  - Theme toggle persists across sessions via `useTheme` composable.
- **Main content (right column) – Character discovery workflow**
  - Search header, query input with autocomplete, and global cache reset button.
  - Dynamic status surfaces (loading indicator, cache badge, contextual error banner).
  - Character profile card with image, meta, and tabbed panels for specific data domains.

The top-level layout is contained inside `src/components/CharacterSearch.vue`. Supporting composables, API helpers, and stores live under `src/composables`, `src/api`, and `src/stores` respectively.

## 2. Primary UX flow

1. **Landing state**
   - Sidebar surfaces empty-state copy for favorites and history.
   - Search input invites character name entry, with autocomplete seeded by stored favorites/history once available.
2. **Query & autocomplete**
   - Typing triggers `AutocompleteInput` suggestions (favorites prioritized, history secondary) with keyboard navigation.
   - Selecting a suggestion or pressing Enter issues `lostarkApi` requests for profile, equipment, engravings, and sibling roster; results are cached.
3. **Result state**
   - Header shows character avatar, name, server, and favorite toggle.
   - Tab set exposes four domains: 기본 정보 (basic stats), 장비 (equipment grid with modal details), 각인 (engraving summary + cards), 보유 캐릭터 (siblings grouped by server).
   - Loading placeholders and empty states guard each tab until data resolves.
4. **Follow-up actions**
   - User can favorite the character (adds to sidebar and autocomplete suggestions).
   - History updates chronologically with clear-all control.
   - Selecting equipment opens `EquipmentDetailModal`; selecting sibling re-triggers search flow.

## 3. Key UI components

| Component | Location | Responsibility |
|-----------|----------|----------------|
| `CharacterSearch` | `src/components/CharacterSearch.vue` | Owns global layout, API orchestration, and tabbed presentation. |
| `AutocompleteInput` | `src/components/common/AutocompleteInput.vue` | Provides accessible autocomplete with keyboard support and categorised suggestions. |
| `LoadingSpinner` | `src/components/common/LoadingSpinner.vue` | Displays activity indicator with optional label. |
| `ErrorMessage` | `src/components/common/ErrorMessage.vue` | Shows error banner with retry/dismiss affordances. |
| `EmptyState` | `src/components/common/EmptyState.vue` | Reusable empty placeholder supporting icon, title, description. |
| `EngravingCard` | `src/components/common/EngravingCard.vue` | Renders engraving badge with tooltip-ready copy. |
| `EquipmentDetailModal` | `src/components/common/EquipmentDetailModal.vue` | Modal overlay for detailed equipment stats. |
| `ThemeToggle` | `src/components/common/ThemeToggle.vue` | Switches between dark/light themes using CSS variables. |
| `LazyImage` | `src/components/common/LazyImage.vue` | Handles image loading fallback and placeholders. |

### Data/composable helpers
- `useTheme` (`src/composables/useTheme.ts`): Manages persisted theme preference and `data-theme` attribute on `<html>`.
- `lostarkApi` (`src/api/lostark.ts`): Wraps REST endpoints for profile, equipment, engraving, and sibling requests with caching.
- `engravingParser` (`src/utils/engravingParser.ts`): Parses engraving descriptions into structured `ParsedEngraving` objects and computes aggregated grades.

## 4. Interaction states & responsiveness

- **Favorites/history lists**: Clickable rows with hover styling; empty states emphasise guidance when arrays are empty. Favorites include thumbnail and metadata; history shows text rows with clear-all button aligned right.
- **Search panel**: Autocomplete handles keyboard navigation (`↑/↓/Enter/Esc`), loses suggestions on blur, and highlights substring matches.
- **Tabbed content**: Buttons show active state class; content area swaps sections without navigation reload. Each tab features its own loading boolean to prevent layout shifts.
- **Modals and overlays**: `EquipmentDetailModal` overlays content with dimmed background and close button; invoked via equipment grid item click.
- **Responsive behaviour**: Layout leverages flexbox—sidebar is fixed width; main content grows. On smaller breakpoints (< 1024px) the `CharacterSearch` stylesheet stacks sidebar above main content (see `.app-container` responsive media queries inside component `<style scoped>` block).

## 5. Deliverables roadmap (design system parity)

While direct Figma authoring is outside this environment, the following artefacts outline the expected design tasks:

1. **Figma page setup**
   - Create pages for *Foundation*, *Components*, and user flows (*Landing*, *Character Detail*, *Modal interactions*).
2. **Wireframes**
   - Translate sidebar + main layout into low-fidelity frames capturing empty, loading, and populated states. Highlight tab transitions and modal invocation.
3. **Component set**
   - Document reusable UI patterns (sidebar list item, tab, stat row, card grid tile, modal) and capture variants (default/hover/empty/error).
4. **Visual language**
   - Define color palette (light/dark), typography scale (headings, body, metadata), elevation system, and micro-interaction notes (hover, focus, skeletons).
5. **Implementation specs**
   - Annotate responsive breakpoints, spacing scale, and state transitions in Figma notes.
   - Provide per-component props/states matrix mirroring Vue props (e.g., `AutocompleteInput` states for focus, empty, overflow) to accelerate frontend alignment.

## 6. Next steps for collaboration

- Export the above structure into Figma and attach design tokens that map to existing CSS variables (`--background`, `--text` etc. inside `ThemeToggle` styles).
- Share the Figma project link and include redlines/measurements aligned with the flex layouts described.
- Maintain changelog in this repository (`docs/` folder) to synchronise future iterations between design and engineering teams.
