<template>
  <div class="skill-panel-shell">
    <div v-if="loading" class="skill-panel-placeholder">
      <LoadingSpinner message="스킬 정보를 불러오는 중입니다..." />
    </div>
    <div v-else-if="errorMessage" class="skill-panel-placeholder">
      <ErrorMessage
        title="스킬 정보를 불러올 수 없어요"
        :message="errorMessage"
        :retry="true"
        :dismissible="false"
        @retry="$emit('retry')"
      />
    </div>
    <div v-else-if="!hasRenderableContent" class="skill-panel-placeholder">
      <EmptyState
        icon="🎯"
        title="스킬 데이터가 감지되지 않았어요"
        :description="emptyStateDescription"
      >
        <button v-if="characterName" type="button" class="skill-panel-retry" @click="$emit('retry')">
          다시 불러오기
        </button>
      </EmptyState>
    </div>
    <div v-else class="skill-panel-layout">
      <section
        v-for="section in skillSections"
        :key="section.key"
        class="skill-section"
        :class="section.modifier"
      >
        <div class="section-heading">
          <div>
            <h4>{{ section.title }}</h4>
            <p>{{ section.subtitle }}</p>
          </div>
        </div>
        <div class="skill-card-grid">
          <article
            v-for="skill in section.cards"
            :key="skill.key"
            class="skill-card"
            :class="{ 'skill-card--compact': skill.isCompact }"
          >
            <div class="skill-card-main">
              <div class="skill-card-hero">
                <div class="skill-card-icon-block">
                  <LazyImage
                    v-if="skill.icon"
                    :src="skill.icon"
                    :alt="skill.name"
                    width="64"
                    height="64"
                    imageClass="skill-card-icon"
                    errorIcon="✨"
                    :useProxy="true"
                  />
                  <p class="skill-card-name">{{ skill.name }}</p>
                  <p class="skill-card-meta">
                    <span v-if="skill.levelLabel">{{ skill.levelLabel }}</span>
                  </p>
                </div>
              </div>
              <div
                v-if="skill.tripods.length || skill.rune"
                class="skill-tripod-rail"
                :class="{ 'skill-tripod-rail--compact': skill.isCompact }"
              >
                <div v-for="tripod in skill.tripods" :key="tripod.key" class="skill-tripod">
                  <div class="tripod-icon" :class="{ 'tripod-icon--tooltip': tripod.description }">
                    <LazyImage
                      v-if="tripod.icon"
                      :src="tripod.icon"
                      :alt="tripod.name"
                      width="40"
                      height="40"
                      imageClass="tripod-image"
                      errorIcon="🌀"
                      :useProxy="true"
                    />
                    <div v-if="tripod.description" class="tripod-tooltip">
                      <p>{{ tripod.description }}</p>
                    </div>
                  </div>
                  <div class="tripod-body">
                    <div class="tripod-head">
                      <strong>{{ tripod.name }}</strong>
                      <div class="tripod-head-meta">
                        <span v-if="tripod.tier" class="tripod-tier-label">T{{ tripod.tier }}</span>
                        <span v-if="tripod.levelLabel">{{ tripod.levelLabel }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="skill.rune" class="skill-rune skill-rune--inline">
                  <div class="skill-rune-icon">
                    <LazyImage
                      v-if="skill.rune.icon"
                      :src="skill.rune.icon"
                      :alt="skill.rune.name"
                      width="40"
                      height="40"
                      imageClass="rune-image"
                      errorIcon="💠"
                      :useProxy="true"
                    />
                  </div>
                  <div>
                    <p class="skill-rune-grade">{{ skill.rune.grade || '룬' }}</p>
                    <strong class="skill-rune-name">{{ skill.rune.name }}</strong>
                  </div>
                </div>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section v-if="gemCards.length" class="skill-section skill-section--gems">
        <div class="section-heading">
          <div>
            <h4>보석 설정</h4>
            <p>슬롯별 보석 레벨과 연결된 스킬을 확인하세요.</p>
          </div>
        </div>
        <div class="gem-card-grid">
          <article v-for="gem in gemCards" :key="gem.key" class="gem-card">
            <header class="gem-card-head">
              <LazyImage
                v-if="gem.icon"
                :src="gem.icon"
                :alt="gem.name"
                width="48"
                height="48"
                imageClass="gem-card-icon"
                errorIcon="💎"
                :useProxy="true"
              />
              <div>
                <p class="gem-card-grade">{{ gem.grade || '보석' }}</p>
                <strong class="gem-card-name">{{ gem.name }}</strong>
                <span v-if="gem.levelLabel" class="gem-card-level">{{ gem.levelLabel }}</span>
              </div>
            </header>
            <p v-if="gem.skillName" class="gem-card-skill">
              적용 스킬: <strong>{{ gem.skillName }}</strong>
            </p>
            <p v-if="gem.skillDescription" class="gem-card-description">
              {{ gem.skillDescription }}
            </p>
          </article>
        </div>
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
import type { CombatSkill, SkillMenuResponse } from '@/api/types'

const props = defineProps<{
  response: SkillMenuResponse | null
  loading: boolean
  errorMessage: string | null
  characterName: string
}>()

defineEmits<{
  (e: 'retry'): void
}>()

interface SkillTripodView {
  key: string
  name: string
  icon?: string
  tier?: number
  levelLabel?: string
  description?: string
}

interface SkillRuneView {
  name: string
  grade?: string
  icon?: string
  description?: string
}

interface SkillGemBadge {
  key: string
  name: string
  levelLabel?: string
}

interface SkillCardView {
  key: string
  name: string
  icon?: string
  levelLabel?: string
  typeLabel?: string
  pointLabel?: string
  description?: string
  tripods: SkillTripodView[]
  rune: SkillRuneView | null
  gemBadges: SkillGemBadge[]
  isCompact: boolean
  isAwakening: boolean
}

interface GemCardView {
  key: string
  name: string
  icon?: string
  grade?: string
  levelLabel?: string
  skillName?: string
  skillDescription?: string
}

const combatSkills = computed(() => props.response?.combatSkills ?? [])
const skillGems = computed(() => props.response?.skillGems ?? [])

const sanitizeInline = (value?: string | null) => {
  if (!value) return ''
  return stripHtml(value).replace(/\s+/g, ' ').trim()
}

const isAwakeningSkill = (skill: CombatSkill) => {
  const candidates = [skill.skillType, skill.type, skill.name]
  return candidates.some(value => sanitizeInline(value).includes('각성'))
}

const flattenTooltipLines = (tooltip?: string | null): string[] => {
  if (!tooltip) return []
  const bucket: string[] = []
  const visit = (node: unknown) => {
    if (!node) return
    if (typeof node === 'string') {
      bucket.push(node)
      return
    }
    if (Array.isArray(node)) {
      node.forEach(visit)
      return
    }
    if (typeof node === 'object') {
      Object.values(node as Record<string, unknown>).forEach(visit)
    }
  }
  try {
    visit(JSON.parse(tooltip))
  } catch {
    visit(tooltip)
  }
  return bucket
    .map(line => sanitizeInline(line))
    .filter(Boolean)
}

const summarizeTooltip = (tooltip?: string | null, fallback = '') => {
  const lines = flattenTooltipLines(tooltip)
  if (!lines.length) return fallback
  if (lines.length === 1) return lines[0]
  const preferred = lines.find((line, index) => index > 0 && !/레벨|Lv/i.test(line))
  return preferred ?? lines[0]
}

const formatLevelLabel = (level?: number | null, prefix = 'Lv.') => {
  if (typeof level !== 'number' || Number.isNaN(level)) return ''
  return `${prefix} ${level}`
}

const gemBadgesBySkill = computed(() => {
  const map = new Map<string, SkillGemBadge[]>()
  skillGems.value.forEach((gem, index) => {
    const skillName = sanitizeInline(gem.skill?.name) || ''
    if (!skillName) return
    const badge: SkillGemBadge = {
      key: `${skillName}-${index}`,
      name: sanitizeInline(gem.name) || '보석',
      levelLabel: formatLevelLabel(gem.level)
    }
    if (!map.has(skillName)) {
      map.set(skillName, [])
    }
    map.get(skillName)!.push(badge)
  })
  return map
})

const skillCards = computed<SkillCardView[]>(() => {
  if (!combatSkills.value.length) return []
  const sorted = [...combatSkills.value].sort((a, b) => {
    const levelA = typeof a.level === 'number' ? a.level : -1
    const levelB = typeof b.level === 'number' ? b.level : -1
    if (levelA === levelB) {
      return sanitizeInline(b.name).localeCompare(sanitizeInline(a.name))
    }
    return levelB - levelA
  })
  return sorted
    .filter(skill => skill.name)
    .map((skill, index) => {
      const name = sanitizeInline(skill.name) || `스킬 ${index + 1}`
      const typeParts = [skill.skillType, skill.type].map(part => sanitizeInline(part)).filter(Boolean)
      const isLowLevel = typeof skill.level === 'number' && skill.level < 4
      const isAwakening = isAwakeningSkill(skill)

      const rune = skill.rune?.name
        ? {
            name: sanitizeInline(skill.rune.name),
            grade: sanitizeInline(skill.rune.grade),
            icon: skill.rune.icon || undefined,
            description: summarizeTooltip(skill.rune.tooltip, sanitizeInline(skill.rune.tooltip))
          }
        : null

      const tripods =
        skill.tripods
          ?.filter(tripod => tripod.name && tripod.selected !== false)
          .map((tripod, tripodIndex) => ({
            key: `${name}-tripod-${tripodIndex}`,
            name: sanitizeInline(tripod.name) || `트라이포드 ${tripodIndex + 1}`,
            icon: tripod.icon || undefined,
            tier: tripod.tier ?? undefined,
            levelLabel: formatLevelLabel(tripod.level),
            description: summarizeTooltip(tripod.tooltip, sanitizeInline(tripod.tooltip))
          })) ?? []

      const gemBadges = gemBadgesBySkill.value.get(name) ?? []
      const isCompact = isLowLevel && tripods.length === 0 && !rune && !isAwakening

      return {
        key: `${name}-${skill.level ?? index}`,
        name,
        icon: skill.icon || undefined,
        levelLabel: formatLevelLabel(skill.level),
        typeLabel: typeParts.join(' · ') || undefined,
        pointLabel: typeof skill.skillPoints === 'number' ? `${skill.skillPoints.toLocaleString()} 포인트` : undefined,
        description: summarizeTooltip(skill.tooltip, sanitizeInline(skill.tooltip)),
        tripods,
        rune,
        gemBadges,
        isCompact,
        isAwakening
      }
    })
})

const awakeningCards = computed(() => skillCards.value.filter(card => card.isAwakening))
const regularSkillCards = computed(() => skillCards.value.filter(card => !card.isAwakening))

const skillSections = computed(() => {
  const sections: Array<{ key: string; title: string; subtitle: string; cards: SkillCardView[]; modifier?: string }> = []
  if (awakeningCards.value.length) {
    sections.push({
      key: 'awakening',
      title: '각성기',
      subtitle: '각성기 쿨타임과 트라이포드를 빠르게 확인하세요.',
      cards: awakeningCards.value,
      modifier: 'skill-section--awakening'
    })
  }
  if (regularSkillCards.value.length) {
    sections.push({
      key: 'combat',
      title: '전투 스킬 트리',
      subtitle: '선택된 트라이포드, 룬, 보석 정보를 한눈에 살펴보세요.',
      cards: regularSkillCards.value
    })
  }
  return sections
})

const gemCards = computed<GemCardView[]>(() => {
  if (!skillGems.value.length) return []
  return skillGems.value
    .filter(gem => gem.name || gem.skill?.name)
    .map((gem, index) => ({
      key: `${gem.name ?? 'gem'}-${index}`,
      name: sanitizeInline(gem.name) || '보석',
      icon: gem.icon || undefined,
      grade: sanitizeInline(gem.grade),
      levelLabel: formatLevelLabel(gem.level),
      skillName: sanitizeInline(gem.skill?.name),
      skillDescription: sanitizeInline(gem.skill?.description)
    }))
})

const hasRenderableContent = computed(() => skillCards.value.length > 0 || gemCards.value.length > 0)

const emptyStateDescription = computed(() => {
  if (!props.characterName) {
    return '캐릭터를 검색하면 전투 스킬 프리셋을 불러옵니다.'
  }
  return `'${props.characterName}' 캐릭터의 스킬 프리셋이 감지되지 않았어요. 인게임에서 스킬을 저장했는지 확인해 주세요.`
})
</script>

<style scoped>
.skill-panel-shell {
  width: 100%;
}

.skill-panel-placeholder {
  padding: 32px;
  border-radius: 16px;
  border: 1px dashed var(--border-color, #e5e7eb);
  background: var(--surface-muted, #f9fafb);
}

.skill-panel-retry {
  margin-top: 12px;
  padding: 8px 16px;
  border-radius: 9999px;
  border: 1px solid var(--border-color, #d1d5db);
  background: transparent;
  cursor: pointer;
  font-weight: 500;
}

.skill-panel-layout {
  display: flex;
  flex-direction: column;
  gap: 32px;
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
  color: var(--text-primary, #1f2937);
}

.section-heading p {
  margin: 0;
  color: var(--text-muted, #6b7280);
}

.skill-section {
  padding: 24px;
  border-radius: 16px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #fbfbfb);
}

.skill-section--awakening {
  border-color: rgba(251, 146, 60, 0.5);
  background: rgba(251, 191, 36, 0.08);
}

.skill-card-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 16px;
}

.skill-card {
  width: fit-content;
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 16px;
  padding: 16px;
  background: var(--surface-color, #fff);
}

.skill-card-main {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.skill-card-hero {
  display: flex;
  gap: 16px;
  min-width: fit-content;
}

.skill-card-icon-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
  /* min-width: 90px; */
}

.skill-card-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.skill-card-name {
  margin: 0;
  font-size: 1.05rem;
  color: var(--text-primary, #1f2937);
  font-weight: 600;
}

.skill-card-meta {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  color: var(--text-muted, #6b7280);
  font-size: 0.9rem;
}

.skill-card-description,
.skill-rune-description,
.gem-card-description {
  margin: 0;
  color: var(--text-secondary, #374151);
  white-space: pre-line;
  line-height: 1.5;
}

.skill-gem-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-gem-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 9999px;
  background: var(--surface-muted, #f3f4f6);
  font-size: 0.85rem;
  color: var(--text-secondary, #374151);
}

.gem-badge-level {
  font-weight: 600;
  color: var(--accent-color, #2563eb);
}

.skill-tripod-rail {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  flex: 1 1 260px;
  align-items: stretch;
}

.skill-tripod-rail--compact {
  flex: 0 0 auto;
  flex-wrap: nowrap;
  align-items: center;
  gap: 8px;
}

.skill-tripod {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 12px;
  background: var(--surface-muted, #f9fafb);
}

.tripod-icon {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.tripod-icon--tooltip {
  cursor: help;
}

.tripod-tooltip {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  min-width: 200px;
  max-width: 280px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(17, 24, 39, 0.95);
  color: #fff;
  font-size: 0.85rem;
  line-height: 1.4;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.2);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s ease;
  z-index: 5;
}

.tripod-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-width: 6px;
  border-style: solid;
  border-color: rgba(17, 24, 39, 0.95) transparent transparent transparent;
}

.tripod-icon--tooltip:hover .tripod-tooltip,
.tripod-icon--tooltip:focus-within .tripod-tooltip {
  opacity: 1;
}

.tripod-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tripod-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 0.9rem;
  color: var(--text-primary, #111827);
}

.tripod-head-meta {
  display: flex;
  gap: 6px;
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
}

.tripod-tier-label {
  font-weight: 600;
}

.skill-rune {
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(37, 99, 235, 0.07);
}

.skill-rune--inline {
  margin-top: 8px;
}

.skill-tripod-rail .skill-rune--inline {
  margin-top: 0;
  align-items: center;
}

.skill-card--compact .skill-card-main {
  flex-wrap: nowrap;
  align-items: center;
  gap: 12px;
}

.skill-card--compact .skill-card-hero {
  flex: 0 0 auto;
  min-width: 0;
  gap: 12px;
}

.skill-card--compact .skill-card-info {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}

.skill-card--compact .skill-card-meta {
  flex-wrap: nowrap;
  gap: 6px;
  white-space: nowrap;
}

.skill-card--compact .skill-card-icon-block {
  min-width: 64px;
}

.skill-card--compact .skill-tripod-rail {
  margin-left: auto;
}

.skill-rune-grade {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
}

.skill-rune-name {
  display: block;
  font-size: 1rem;
  color: var(--text-primary, #1f2937);
}

.gem-card-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.gem-card {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 16px;
  padding: 16px;
  background: var(--surface-color, #fff);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.gem-card-head {
  display: flex;
  gap: 12px;
  align-items: center;
}

.gem-card-grade {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
}

.gem-card-name {
  font-size: 1rem;
  color: var(--text-primary, #1f2937);
}

.gem-card-level {
  font-size: 0.85rem;
  color: var(--accent-color, #2563eb);
  font-weight: 600;
}

.gem-card-skill {
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-secondary, #374151);
}

@media (max-width: 768px) {
  .skill-card-main {
    flex-direction: column;
  }

  .skill-card-hero {
    flex-direction: column;
    min-width: 0;
  }

  .skill-tripod-rail {
    width: 100%;
  }

  .skill-card-grid,
  .gem-card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
