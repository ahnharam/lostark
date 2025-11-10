<template>
  <div class="character-detail-panel">
    <div v-if="loading" class="detail-placeholder">
      <LoadingSpinner message="상세 정보를 불러오는 중입니다..." />
    </div>
    <div v-else-if="errorMessage" class="detail-placeholder">
      {{ errorMessage }}
    </div>
    <div v-else-if="!character" class="detail-placeholder">
      캐릭터를 선택하면 상세 정보가 표시됩니다.
    </div>
    <div v-else class="detail-content" @click.stop>
      <section class="gear-columns">
        <div class="gear-column" v-for="(column, idx) in gearColumnList" :key="idx">
          <article v-for="item in column" :key="item.name" class="gear-card">
            <div class="card-left">
              <LazyImage
                v-if="item.icon"
                :src="item.icon"
                :alt="item.name"
                width="52"
                height="52"
                imageClass="gear-icon"
                errorIcon="⚔️"
              />
              <div class="tier-stack">
                <span class="tier-chip">{{ formatGrade(item.grade) }}</span>
                <span v-if="getParsedEquipment(item)?.quality !== undefined" class="quality-chip">
                  {{ getParsedEquipment(item)?.quality }}
                </span>
              </div>
            </div>
            <div class="card-body">
              <h3>{{ item.name }}</h3>
              <small>{{ item.type }}</small>

              <div class="value-lines" v-if="getCoreValues(item).length">
                <span v-for="(line, lineIdx) in getCoreValues(item)" :key="`core-${lineIdx}`">
                  {{ line }}
                </span>
              </div>
              <div class="value-lines subtle" v-if="getExtraValues(item).length">
                <span v-for="(line, lineIdx) in getExtraValues(item)" :key="`extra-${lineIdx}`">
                  {{ line }}
                </span>
              </div>

              <div class="pill-row" v-if="getEffectPills(item).length">
                <span
                  v-for="(pill, pillIdx) in getEffectPills(item)"
                  :key="`pill-${pillIdx}`"
                  :class="['effect-pill', pill.variant]"
                >
                  {{ pill.text }}
                </span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section class="engraving-card" v-if="engravings.length">
        <h4>각인 정보</h4>
        <ul>
          <li v-for="engraving in engravings" :key="engraving.name">
            <LazyImage
              v-if="engraving.icon"
              :src="engraving.icon"
              :alt="engraving.name"
              width="36"
              height="36"
              imageClass="engraving-icon"
              errorIcon="📜"
            />
            <div>
              <strong>{{ engraving.name }}</strong>
              <p>{{ engraving.description }}</p>
            </div>
          </li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LazyImage from './LazyImage.vue'
import LoadingSpinner from './LoadingSpinner.vue'
import { parseTooltip, type ParsedTooltip } from '@/utils/tooltipParser'

interface Character {
  characterName: string
  characterClassName: string
  characterImage?: string
  itemMaxLevel: string
}

interface Equipment {
  name: string
  type: string
  icon?: string
  grade?: string
  quality?: number
  tooltip?: string
}

interface Engraving {
  name: string
  icon?: string
  description: string
}

interface Props {
  character: Character | null
  equipment?: Equipment[]
  engravings?: Engraving[]
  loading?: boolean
  errorMessage?: string | null
}

const props = withDefaults(defineProps<Props>(), {
  character: null,
  equipment: () => [],
  engravings: () => [],
  loading: false,
  errorMessage: null
})

const parsedEquipment = computed<Record<string, ParsedTooltip>>(() => {
  const map: Record<string, ParsedTooltip> = {}
  props.equipment.forEach(item => {
    if (item.tooltip) {
      map[item.name] = parseTooltip(item.tooltip)
    }
  })
  return map
})

const tooltipValueMap = computed<Record<string, string[]>>(() => {
  const map: Record<string, string[]> = {}
  props.equipment.forEach(item => {
    map[item.name] = extractTooltipValues(item)
  })
  return map
})

const getParsedEquipment = (item: Equipment) => parsedEquipment.value[item.name]
const getTooltipValues = (item: Equipment) => tooltipValueMap.value[item.name] || []
const getCoreValues = (item: Equipment) => getTooltipValues(item).slice(0, 3)
const getExtraValues = (item: Equipment) => getTooltipValues(item).slice(3, 7)

const getEffectPills = (item: Equipment) => {
  const pills: { text: string; variant: 'engraving' | 'elixir' }[] = []
  getParsedEquipment(item)?.engravingEffects?.forEach(effect =>
    pills.push({ text: effect, variant: 'engraving' })
  )
  getParsedEquipment(item)?.elixirEffects?.forEach(effect =>
    pills.push({ text: effect, variant: 'elixir' })
  )
  return pills
}

const leftKeywords = ['무기', '투구', '상의', '하의', '장갑', '어깨', '엘릭서', '초월', '보석', '스톤']
const gearColumnList = computed(() => {
  const left: Equipment[] = []
  const right: Equipment[] = []
  props.equipment.forEach(item => {
    const type = item.type || ''
    if (leftKeywords.some(keyword => type.includes(keyword))) {
      left.push(item)
    } else {
      right.push(item)
    }
  })
  return [left, right]
})

const formatGrade = (grade?: string) => grade || '장비'

const extractTooltipValues = (item: Equipment): string[] => {
  if (!item.tooltip) return []
  try {
    const raw = JSON.parse(item.tooltip)
    const normalize = (value: any): string[] => {
      if (!value) return []
      if (typeof value === 'string') return [cleanText(value)]
      if (Array.isArray(value)) return value.flatMap(normalize)
      if (typeof value === 'object') {
        if ('value' in value) return normalize(value.value)
        return Object.values(value).flatMap(normalize)
      }
      return []
    }
    return Object.values(raw).flatMap(normalize).filter(Boolean)
  } catch {
    return [cleanText(item.tooltip)]
  }
}

const cleanText = (text: string) =>
  text
    .replace(/<[^>]+>/g, ' ')
    .replace(/\\n/g, ' ')
    .replace(/&[^;]+;/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
</script>

<style scoped>
.character-detail-panel {
  width: 100%;
}

.detail-placeholder {
  padding: 40px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: var(--card-bg);
  text-align: center;
  color: var(--text-secondary);
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.gear-columns {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 18px;
}

.gear-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.gear-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  background: var(--card-bg);
  box-shadow: var(--shadow-md);
}

.card-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.gear-icon {
  border-radius: 12px;
  border: 1px solid var(--border-color);
  object-fit: cover;
}

.tier-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.tier-chip {
  font-size: 0.7rem;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 999px;
  background: #1f2937;
  color: #fbbf24;
  text-transform: uppercase;
}

.quality-chip {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--primary-color);
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card-body h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--text-primary);
}

.card-body small {
  color: var(--text-secondary);
}

.value-lines {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.9rem;
  color: var(--text-primary);
}

.value-lines.subtle {
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.pill-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.effect-pill {
  padding: 4px 8px;
  border-radius: 10px;
  font-size: 0.75rem;
  border: 1px solid rgba(99, 102, 241, 0.2);
  color: var(--primary-color);
  background: rgba(99, 102, 241, 0.12);
}

.effect-pill.elixir {
  color: #0f9d58;
  border-color: rgba(15, 157, 88, 0.2);
  background: rgba(15, 157, 88, 0.12);
}

.engraving-card {
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 18px;
  background: var(--card-bg);
}

.engraving-card h4 {
  margin: 0 0 12px 0;
  font-size: 1rem;
}

.engraving-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.engraving-card li {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.engraving-card p {
  margin: 2px 0 0;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

@media (max-width: 900px) {
  .detail-content {
    gap: 16px;
  }
}
</style>
