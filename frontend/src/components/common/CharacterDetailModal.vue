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
                :useProxy="true"
              />
              <div class="tier-stack">
                <span class="tier-chip">{{ formatGrade(item.grade) }}</span>
                <span v-if="getParsedEquipment(item)?.quality !== undefined && (getParsedEquipment(item)?.quality ?? 0) >= 0" class="quality-chip">
                  {{ getParsedEquipment(item)?.quality }}
                </span>
              </div>
            </div>
            <div class="card-body">
              <h3>{{ item.name }}</h3>
              <small>{{ item.type }}</small>

              <!-- <div class="value-lines" v-if="getCoreValues(item).length">
                <span v-for="(line, lineIdx) in getCoreValues(item)" :key="`core-${lineIdx}`">
                  {{ line }}
                </span>
              </div> -->
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
              :useProxy="true"
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
import type { Equipment, Engraving } from '@/api/types'

interface Character {
  characterName: string
  characterClassName: string
  characterImage?: string
  itemMaxLevel: string
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

const specialEquipmentKeywords = ['나침반', '부적', '문장', '보주']

const isSpecialEquipment = (item: Equipment) => {
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()
  return specialEquipmentKeywords.some(keyword => target.includes(keyword.toLowerCase()))
}

const parsedEquipment = computed<Record<string, ParsedTooltip>>(() => {
  const map: Record<string, ParsedTooltip> = {}
  props.equipment.forEach(item => {
    if (item.tooltip) {
      map[item.name] = parseTooltip(item.tooltip)
    }
  })
  return map
})

interface TooltipValueBuckets {
  // stats: 수치 기반 정보 (아이템 레벨, 능력치 등)
  stats: string[]
  // descriptions: 장비 종류/품질 등 서술형 설명
  descriptions: string[]
  // restrictions: 착용 조건이나 전용 문구 등 제한 조건
  restrictions: string[]
  // urls: 이미지 혹은 외부 리소스 링크
  urls: string[]
  // flavor: 아이템 배경, 연출 등 분위기 문장
  flavor: string[]
  // system: 제작, 재활용, 분해 같은 시스템 안내
  system: string[]
  // enhancements: 연마/초월/슬롯 등 강화 관련 설명
  enhancements: string[]
  // ownership: 귀속/거래불가 등 소유권 제약
  ownership: string[]
  // durability: 내구도 정보
  durability: string[]
  // summary: 누적 수치/총합 요약 문구
  summary: string[]
  // misc: 위 분류로 묶기 애매한 기타 문장
  misc: string[]
}

type TooltipCategory = keyof TooltipValueBuckets

interface TooltipDisplayRule {
  primary: TooltipCategory[]
  secondary: TooltipCategory[]
}

// 장비 부위별로 tooltip 문자열을 의미별로 나눠 저장
const tooltipValueMap = computed<Record<string, TooltipValueBuckets>>(() => {
  const map: Record<string, TooltipValueBuckets> = {}
  props.equipment.forEach(item => {
    const values = extractTooltipValues(item)
    map[item.name] = categorizeTooltipValues(values)
  })
  return map
})

const getParsedEquipment = (item: Equipment) => parsedEquipment.value[item.name]
const getTooltipValues = (item: Equipment, categories: TooltipCategory[]) => {
  const buckets = tooltipValueMap.value[item.name]
  if (!buckets) return []
  return categories.flatMap(category => buckets[category] ?? [])
}

const getDisplayRule = (item: Equipment): TooltipDisplayRule => {
  const type = item.type?.toLowerCase() ?? ''
  if (matchKeywords(type, ['무기', 'weapon', '창', '검', '활', '건'])) {
    return {
      primary: ['stats', 'descriptions'],
      secondary: ['restrictions', 'urls', 'enhancements', 'ownership', 'durability', 'summary', 'flavor', 'system', 'misc']
    }
  }
  if (matchKeywords(type, ['투구', '상의', '하의', '장갑', '어깨', '벨트', 'armor'])) {
    return {
      primary: ['stats'],
      secondary: ['descriptions', 'restrictions', 'enhancements', 'ownership', 'durability', 'summary', 'flavor', 'system', 'misc']
    }
  }
  if (matchKeywords(type, ['목걸이', '귀걸이', '반지', '팔찌', 'accessory'])) {
    return {
      primary: ['stats'],
      secondary: ['restrictions', 'descriptions', 'enhancements', 'ownership', 'durability', 'summary', 'flavor', 'system', 'misc']
    }
  }
  if (matchKeywords(type, ['어빌리티 스톤', '스톤'])) {
    return {
      primary: ['stats', 'restrictions'],
      secondary: ['descriptions', 'enhancements', 'ownership', 'durability', 'summary', 'flavor', 'system', 'misc']
    }
  }
  if (matchKeywords(type, ['보석', 'gem'])) {
    return {
      primary: ['descriptions'],
      secondary: ['stats', 'enhancements', 'ownership', 'summary', 'system', 'misc']
    }
  }
  return {
    primary: ['stats'],
    secondary: ['descriptions', 'restrictions', 'urls', 'enhancements', 'ownership', 'durability', 'summary', 'flavor', 'system', 'misc']
  }
}

const getCoreValues = (item: Equipment) => getTooltipValues(item, getDisplayRule(item).primary)
const getExtraValues = (item: Equipment) => getTooltipValues(item, getDisplayRule(item).secondary)

// 디버깅: 아이템 아이콘 URL 확인
const logItemIcon = (item: Equipment) => item.icon

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
    if (isSpecialEquipment(item)) {
      return
    }
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

const categorizeTooltipValues = (values: string[]): TooltipValueBuckets => {
  const buckets: TooltipValueBuckets = {
    stats: [],
    descriptions: [],
    restrictions: [],
    urls: [],
    flavor: [],
    system: [],
    enhancements: [],
    ownership: [],
    durability: [],
    summary: [],
    misc: []
  }

  values.forEach(value => {
    const text = value.trim()
    if (!text) return

    if (/https?:\/\//i.test(text)) {
      buckets.urls.push(text)
    } else if (/(장착|전용|요구|불가|사용 가능)/.test(text)) {
      buckets.restrictions.push(text)
    } else if (/(레벨|티어|힘|민첩|지능|체력|치명|특화|신속|제압|인내|숙련|공격력|방어력|품질|피해|생명력)/.test(text)) {
      buckets.stats.push(text)
    } else if (/(무기|방패|창|검|활|장갑|투구|어깨|상의|하의|악세서리|보석|목걸이|귀걸이|반지)/.test(text)) {
      buckets.descriptions.push(text)
    } else if (/(연마|초월|추가 효과|슬롯 효과|강화|아크 패시브)/.test(text)) {
      buckets.enhancements.push(text)
    } else if (/(귀속|거래 불가|교환 불가|캐릭터 귀속)/.test(text)) {
      buckets.ownership.push(text)
    } else if (/(내구도)/.test(text)) {
      buckets.durability.push(text)
    } else if (/(총|전체|모든 장비|적용된)/.test(text)) {
      buckets.summary.push(text)
    } else if (/(제작|재련|해체|분해|거래|획득|교환|연성)/.test(text)) {
      buckets.system.push(text)
    } else if (/(느껴|전설|고대|기억|전해지|신화)/.test(text)) {
      buckets.flavor.push(text)
    } else {
      buckets.misc.push(text)
    }
  })

  return buckets
}

const matchKeywords = (text: string, keywords: string[]) =>
  keywords.some(keyword => text.includes(keyword))

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
