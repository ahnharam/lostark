<template>
  <div class="ark-grid-shell">
    <div v-if="loading" class="ark-grid-placeholder">
      <LoadingSpinner message="아크 그리드 정보를 불러오는 중입니다..." />
    </div>
    <div v-else-if="errorMessage" class="ark-grid-placeholder">
      <ErrorMessage
        title="아크 그리드를 불러올 수 없어요"
        :message="errorMessage"
        :retry="true"
        :dismissible="false"
        @retry="$emit('retry')"
      />
    </div>
    <div v-else-if="!hasRenderableContent" class="ark-grid-placeholder">
      <EmptyState
        icon="🌀"
        title="아크 그리드 데이터가 감지되지 않았어요"
        :description="emptyStateDescription"
      >
        <button v-if="characterName" type="button" class="ark-grid-retry" @click="$emit('retry')">
          다시 불러오기
        </button>
      </EmptyState>
    </div>
    <div v-else class="ark-grid-layout">
      <section class="ark-grid-overview">
        <div>
          <p class="ark-grid-label">현재 루트</p>
          <h3>{{ arkPassiveTitle }}</h3>
          <p class="ark-grid-subtext">
            패치된 아크 포인트 분배와 코어 조합을 한 눈에 확인할 수 있도록 3단 레이아웃으로 구성했습니다.
          </p>
        </div>
        <ul v-if="pointSummary.length" class="ark-grid-point-list">
          <li v-for="point in pointSummary" :key="point.name" class="ark-grid-point">
            <span class="point-label">{{ point.name }}</span>
            <strong class="point-value">{{ formatPointValue(point.value) }}</strong>
            <span v-if="point.description" class="point-description">{{ sanitizeInline(point.description) }}</span>
          </li>
        </ul>
      </section>

      <section v-if="passiveMatrix.length" class="ark-grid-passives">
        <div class="section-heading">
          <div>
            <h4>아크 패시브 계층</h4>
            <p>진화 · 깨달음 · 도약 단계를 각각 분리하여 핵심 효과만 보여줍니다.</p>
          </div>
        </div>
        <div class="passive-matrix">
          <div class="matrix-header">
            <div class="matrix-cell matrix-cell--tier">티어</div>
            <div
              v-for="section in PASSIVE_SECTIONS"
              :key="section.key"
              class="matrix-cell matrix-cell--section"
            >
              {{ section.label }}
            </div>
          </div>
          <div v-for="row in passiveMatrix" :key="row.id" class="matrix-row">
            <div class="matrix-cell matrix-cell--tier">{{ row.label }}</div>
            <div
              v-for="section in PASSIVE_SECTIONS"
              :key="`${row.id}-${section.key}`"
              class="matrix-cell matrix-cell--section"
            >
              <article
                v-for="effect in row.columns[section.key]"
                :key="effect.key"
                class="passive-card passive-table-card"
              >
                <div class="passive-card-head">
                  <LazyImage
                    v-if="effect.icon"
                    :src="effect.icon"
                    :alt="effect.name || '아크 패시브 아이콘'"
                    width="44"
                    height="44"
                    imageClass="passive-card-icon"
                    errorIcon="✨"
                    :useProxy="true"
                  />
                  <strong class="passive-card-name">{{ effect.name }}</strong>
                </div>
                <p v-if="effect.levelLine" class="passive-card-description">{{ effect.levelLine }}</p>
                <p v-if="effect.summaryLine" class="passive-card-summary">{{ effect.summaryLine }}</p>
              </article>
            </div>
          </div>
        </div>
      </section>

      <section v-if="slotCards.length" class="ark-grid-slots">
        <div class="section-heading">
          <div>
            <h4>코어 & 젬 네트워크</h4>
            <p>각 코어가 제공하는 옵션과 연결된 젬 효과를 나란히 보여줍니다.</p>
          </div>
        </div>
        <div class="slot-card-grid">
          <article v-for="slot in slotCards" :key="slot.index" class="slot-card">
            <header class="slot-card-head">
              <LazyImage
                v-if="slot.icon"
                :src="slot.icon"
                :alt="slot.name || '아크 코어'"
                width="56"
                height="56"
                imageClass="slot-card-icon"
                errorIcon="🧱"
                :useProxy="true"
              />
              <div>
                <p class="slot-card-grade">{{ slot.grade || '등급 미상' }}</p>
                <strong class="slot-card-name">{{ slot.name }}</strong>
                <span v-if="slot.point !== undefined" class="slot-card-point">{{ slot.point }}P</span>
              </div>
            </header>
            <p v-if="slot.tooltipTitle" class="slot-card-title">{{ slot.tooltipTitle }}</p>
            <ul v-if="slot.tooltipLines.length" class="slot-tooltip-list">
              <li v-for="(line, idx) in slot.tooltipLines" :key="`slot-line-${slot.index}-${idx}`">
                {{ line }}
              </li>
            </ul>
            <div v-if="slot.gemCards.length" class="slot-gem-stack">
              <div v-for="gem in slot.gemCards" :key="`gem-${slot.index}-${gem.index}`" class="gem-card">
                <div class="gem-card-head">
                  <LazyImage
                    v-if="gem.icon"
                    :src="gem.icon"
                    :alt="gem.title || '젬'"
                    width="38"
                    height="38"
                    imageClass="gem-card-icon"
                    errorIcon="💠"
                    :useProxy="true"
                  />
                  <div>
                    <p class="gem-card-grade">{{ gem.grade || '젬' }}</p>
                    <strong class="gem-card-name">{{ gem.title || `젬 ${gem.index ?? ''}` }}</strong>
                  </div>
                </div>
                <ul v-if="gem.tooltipLines.length" class="gem-tooltip-list">
                  <li v-for="(line, idx) in gem.tooltipLines" :key="`gem-line-${slot.index}-${gem.index}-${idx}`">
                    {{ line }}
                  </li>
                </ul>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section v-if="gridEffects.length" class="ark-grid-effects">
        <div class="section-heading">
          <div>
            <h4>추가 효과</h4>
            <p>아크 그리드 전체에 적용되는 보너스를 요약했습니다.</p>
          </div>
        </div>
        <ul class="effect-list">
          <li v-for="effect in gridEffects" :key="`${effect.name}-${effect.level}`" class="effect-item">
            <span v-if="effect.level" class="effect-level">Lv. {{ effect.level }}</span>
            <div>
              <strong>{{ effect.name }}</strong>
              <p v-if="effect.tooltip" class="effect-description">{{ sanitizeInline(effect.tooltip) }}</p>
            </div>
          </li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LoadingSpinner from './LoadingSpinner.vue'
import EmptyState from './EmptyState.vue'
import ErrorMessage from './ErrorMessage.vue'
import LazyImage from './LazyImage.vue'
import { stripHtml } from '@/utils/tooltipParser'
import type { ArkGridResponse, ArkGridSlot, ArkPassiveEffect, ArkPassivePoint } from '@/api/types'

const props = defineProps<{
  response: ArkGridResponse | null
  loading: boolean
  errorMessage: string | null
  characterName: string
}>()

defineEmits<{
  (e: 'retry'): void
}>()

const arkPassive = computed(() => props.response?.arkPassive ?? null)
const arkGrid = computed(() => props.response?.arkGrid ?? null)

interface PassiveCard {
  key: string
  name: string
  icon?: string
  tierLabel: string
  levelLine: string
  summaryLine: string
}

interface PassiveSection {
  key: 'evolution' | 'realization' | 'leap'
  label: string
  keyword: string
}

type PassiveSectionKey = PassiveSection['key']

interface PassiveMatrixRow {
  id: string
  label: string
  columns: Record<PassiveSectionKey, PassiveCard[]>
}

const PASSIVE_SECTIONS: readonly PassiveSection[] = [
  { key: 'evolution', label: '진화', keyword: '진화' },
  { key: 'realization', label: '깨달음', keyword: '깨달음' },
  { key: 'leap', label: '도약', keyword: '도약' }
]

interface ParsedTooltip {
  title: string
  lines: string[]
}

const sanitizeInline = (value?: string | null) => {
  if (!value) return ''
  return stripHtml(value)
    .replace(/\\r\\n|\\n|\\r/g, ' ')
    .replace(/\\s+/g, ' ')
    .trim()
}

const normalizeTooltipText = (value: string) => {
  return value
    .replace(/\\r\\n/g, '\n')
    .replace(/\\n/g, '\n')
    .replace(/\\r/g, '\n')
    .replace(/\r/g, '\n')
    .replace(/\n/g, '\n')
    .replace(/\|/g, '\n')
}

const flattenTooltip = (value: unknown, bucket: string[]) => {
  if (!value) return
  if (typeof value === 'string') {
    normalizeTooltipText(value)
      .split('\\n')
      .map(line => stripHtml(line).trim())
      .filter(Boolean)
      .forEach(line => bucket.push(line))
    return
  }
  if (Array.isArray(value)) {
    value.forEach(item => flattenTooltip(item, bucket))
    return
  }
  if (typeof value === 'object') {
    Object.values(value).forEach(item => flattenTooltip(item, bucket))
  }
}

const parseTooltip = (tooltip?: string | null): ParsedTooltip => {
  if (!tooltip) {
    return { title: '', lines: [] }
  }
  const bucket: string[] = []
  try {
    const parsed = JSON.parse(tooltip)
    flattenTooltip(parsed, bucket)
  } catch {
    flattenTooltip(tooltip, bucket)
  }
  const sanitized = bucket.map(line =>
    line
      .replace(/\\s+/g, ' ')
      .trim()
  )
  const [title, ...rest] = sanitized
  return {
    title: title || '',
    lines: rest
  }
}

const parsePointSummary = (points?: ArkPassivePoint[]) => {
  return (points ?? []).filter(point => point.name)
}

const pointSummary = computed(() => parsePointSummary(arkPassive.value?.points))

const arkPassiveTitle = computed(() => sanitizeInline(arkPassive.value?.title) || '루트 정보 미상')

const buildPassiveCard = (effect: ArkPassiveEffect, index: number): PassiveCard => {
  const tooltip = parseTooltip(effect.toolTip)
  const tierLabel = sanitizeInline(effect.description)
  const levelLine =
    tooltip.lines.find(line => line.includes('아크 패시브 레벨')) || tooltip.title || '아크 패시브 레벨 정보 없음'
  const lastLineCandidate = tooltip.lines.length ? tooltip.lines[tooltip.lines.length - 1] : tooltip.title
  const summaryLine =
    lastLineCandidate && lastLineCandidate !== levelLine ? lastLineCandidate : tooltip.lines.find(line => line !== levelLine) || ''

  return {
    key: `${effect.name}-${index}`,
    name: sanitizeInline(effect.name),
    icon: effect.icon,
    tierLabel,
    levelLine,
    summaryLine
  }
}

const passiveMatrix = computed<PassiveMatrixRow[]>(() => {
  const effects = arkPassive.value?.effects ?? []
  const rows: PassiveMatrixRow[] = []
  const rowMap = new Map<string, PassiveMatrixRow>()

  const createEmptyColumns = () => {
    return PASSIVE_SECTIONS.reduce<Record<string, PassiveCard[]>>((acc, section) => {
      acc[section.key] = []
      return acc
    }, {})
  }

  const resolveSection = (card: PassiveCard) =>
    PASSIVE_SECTIONS.find(section => card.tierLabel.includes(section.keyword)) ??
    PASSIVE_SECTIONS.find(section => card.levelLine.includes(section.keyword)) ??
    PASSIVE_SECTIONS[0]

  effects.forEach((effect, index) => {
    const card = buildPassiveCard(effect, index)
    const section = resolveSection(card)
    const rowLabel = card.tierLabel || '티어 정보 없음'
    let row = rowMap.get(rowLabel)
    if (!row) {
      row = {
        id: `${rowLabel}-${rows.length}`,
        label: rowLabel,
        columns: createEmptyColumns()
      }
      rowMap.set(rowLabel, row)
      rows.push(row)
    }
    row.columns[section.key].push(card)
  })

  return rows
})

const slotCards = computed(() => {
  return (arkGrid.value?.slots ?? []).map((slot: ArkGridSlot) => {
    const tooltip = parseTooltip(slot.tooltip)
    const gemCards =
      slot.gems?.map(gem => {
        const gemTooltip = parseTooltip(gem.tooltip)
        return {
          ...gem,
          title: gemTooltip.title,
          tooltipLines: gemTooltip.lines
        }
      }) ?? []
    return {
      ...slot,
      tooltipTitle: tooltip.title,
      tooltipLines: tooltip.lines,
      gemCards
    }
  })
})

const gridEffects = computed(() => arkGrid.value?.effects ?? [])

const hasRenderableContent = computed(() => {
  return (
    pointSummary.value.length > 0 ||
    passiveMatrix.value.length > 0 ||
    slotCards.value.length > 0 ||
    gridEffects.value.length > 0
  )
})

const formatPointValue = (value?: number | null) => {
  if (typeof value !== 'number') return '0P'
  return `${value}P`
}

const emptyStateDescription = computed(() => {
  if (!props.characterName) {
    return '캐릭터를 검색하면 아크 패시브와 코어 구성이 여기에 표시됩니다.'
  }
  return `${props.characterName}의 아크 그리드 기록이 아직 없거나 비공개 상태입니다.`
})

</script>

<style scoped>
.ark-grid-shell {
  width: 100%;
}

.ark-grid-placeholder {
  padding: 32px;
  border: 1px dashed var(--border-color, #d5d5d5);
  border-radius: 16px;
  background: var(--card-bg, #f9fafb);
}

.ark-grid-retry {
  margin-top: 16px;
  padding: 8px 16px;
  border-radius: 999px;
  background: var(--primary-color, #3b82f6);
  color: #fff;
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.ark-grid-layout {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.ark-grid-overview {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  border-radius: 16px;
  background: var(--card-bg, #f9fafb);
  border: 1px solid var(--border-color, #e5e7eb);
}

.ark-grid-label {
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 4px;
}

.ark-grid-overview h3 {
  margin: 0;
  font-size: 1.5rem;
}

.ark-grid-subtext {
  margin: 4px 0 0;
  color: var(--text-muted, #6b7280);
}

.ark-grid-point-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.ark-grid-point {
  padding: 12px 16px;
  border-radius: 12px;
  background: var(--surface-color, #fff);
  border: 1px solid var(--border-color, #e5e7eb);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.point-label {
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
}

.point-value {
  font-size: 1.4rem;
  font-weight: 600;
}

.point-description {
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.section-heading h4 {
  margin: 0 0 4px;
  font-size: 1.1rem;
}

.section-heading p {
  margin: 0;
  color: var(--text-muted, #6b7280);
}

.ark-grid-passives,
.ark-grid-slots,
.ark-grid-effects {
  padding: 24px;
  border-radius: 16px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #fbfbfb);
}

.passive-matrix {
  margin-top: 16px;
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 16px;
  overflow: hidden;
}

.matrix-header,
.matrix-row {
  display: grid;
  grid-template-columns: 120px repeat(3, minmax(0, 1fr));
}

.matrix-header {
  background: var(--surface-muted, #f3f4f6);
  border-bottom: 1px solid var(--border-color, #e5e7eb);
  font-weight: 600;
  color: var(--text-muted, #6b7280);
}

.matrix-row:not(:last-child) {
  border-bottom: 1px solid var(--border-color, #e5e7eb);
}

.matrix-cell {
  padding: 16px;
  border-right: 1px solid var(--border-color, #e5e7eb);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.matrix-cell:last-child {
  border-right: none;
}

.matrix-cell--tier {
  justify-content: center;
  align-items: center;
  background: var(--surface-color, #fff);
  font-weight: 600;
  color: var(--text-muted, #6b7280);
  text-align: center;
}

.passive-card {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 16px;
  padding: 16px;
  background: var(--surface-color, #fff);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.passive-table-card {
  margin: 0;
  border-radius: 12px;
}

.passive-card-head {
  display: flex;
  gap: 12px;
  align-items: center;
}

.passive-card-icon {
  border-radius: 12px;
}

.passive-card-tier {
  font-size: 0.8rem;
  color: var(--text-muted, #6b7280);
  margin: 0;
}

.passive-card-name {
  font-size: 1rem;
}

.passive-card-description {
  margin: 0;
  color: var(--text-primary, #1f2937);
  font-size: calc(1rem - 2px);
}

.passive-card-summary {
  margin: 0;
  color: var(--text-muted, #6b7280);
  font-size: calc(0.9rem - 2px);
}

.slot-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 18px;
  margin-top: 16px;
}

.slot-card {
  padding: 16px;
  border-radius: 16px;
  background: var(--surface-color, #fff);
  border: 1px solid var(--border-color, #e5e7eb);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.slot-card-head {
  display: flex;
  gap: 12px;
}

.slot-card-icon {
  border-radius: 12px;
}

.slot-card-grade {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
}

.slot-card-name {
  display: block;
  font-size: 1.05rem;
}

.slot-card-point {
  display: inline-flex;
  background: rgba(59, 130, 246, 0.12);
  color: var(--primary-color, #2563eb);
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 0.8rem;
  margin-top: 4px;
}

.slot-card-title {
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-muted, #6b7280);
}

.slot-tooltip-list,
.gem-tooltip-list {
  margin: 0;
  padding-left: 18px;
  font-size: 0.9rem;
  color: var(--text-primary, #1f2937);
}

.slot-gem-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.gem-card {
  border: 1px dashed var(--border-color, #d1d5db);
  border-radius: 12px;
  padding: 12px;
  background: var(--surface-muted, #f9fafb);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.gem-card-head {
  display: flex;
  gap: 10px;
  align-items: center;
}

.gem-card-name {
  font-size: 0.95rem;
}

.ark-grid-effects .effect-list {
  margin: 16px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.effect-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--surface-color, #fff);
}

.effect-level {
  font-weight: 600;
  color: var(--primary-color, #2563eb);
}

.effect-description {
  margin: 4px 0 0;
  color: var(--text-muted, #6b7280);
}

@media (max-width: 768px) {
  .ark-grid-overview {
    padding: 16px;
  }

  .section-heading {
    flex-direction: column;
  }
}
</style>
