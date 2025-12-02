<template>
  <!-- 스킬 패널 메인 컨테이너 -->
  <div class="skill-panel-shell">
    <!-- 로딩 상태 표시 -->
    <div v-if="loading" class="skill-panel-placeholder">
      <LoadingSpinner message="스킬 정보를 불러오는 중입니다..." />
    </div>

    <!-- 에러 상태 표시 -->
    <div v-else-if="errorMessage" class="skill-panel-placeholder">
      <ErrorMessage title="스킬 정보를 불러올 수 없어요" :message="errorMessage" :retry="true" :dismissible="false"
        @retry="$emit('retry')" />
    </div>

    <!-- 데이터 없음 상태 표시 -->
    <div v-else-if="!hasRenderableContent" class="skill-panel-placeholder">
      <EmptyState icon="🎯" title="스킬 데이터가 감지되지 않았어요" :description="emptyStateDescription">
        <button v-if="characterName" type="button" class="skill-panel-retry" @click="$emit('retry')">
          다시 불러오기
        </button>
      </EmptyState>
    </div>

    <!-- 스킬 정보 메인 레이아웃 -->
    <div v-else class="skill-panel-layout">
      <!-- 초각성 스킬 섹션 (skillTypeCode: 1) -->
      <section v-if="superSkillHighlights.length" class="skill-section skill-section--highlight">
        <div class="section-heading">
          <div>
            <h4>초각성 스킬</h4>
          </div>
        </div>

        <!-- 초각성 스킬 카드 그리드 -->
        <div class="skill-card-grid super-skill-grid">
          <article v-for="skill in superSkillHighlights" :key="`super-${skill.key}`" class="skill-card"
            :class="{ 'skill-card--compact': skill.isCompact }">
            <div class="skill-card-main">
              <div class="skill-card-hero">
                <!-- 스킬 아이콘 및 이름 블록 -->
                <div class="skill-card-icon-block">
                  <div class="skill-icon-wrapper" tabindex="0">
                    <LazyImage v-if="skill.icon" :src="skill.icon" :alt="skill.name" width="40" height="40"
                      imageClass="skill-card-icon" errorIcon="✨" :useProxy="true" />
                  </div>
                  <p class="skill-card-name">{{ skill.name }}</p>
                </div>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- 스킬 섹션 반복 (각성기, 전투 스킬 등) -->
      <section v-for="section in skillSections" :key="section.key" class="skill-section" :class="section.modifier">
        <div class="section-heading">
          <div>
            <h4>{{ section.title }}</h4>
          </div>
        </div>
        <div v-for="row in getSectionRows(section)" :key="row.key"
          :class="['skill-card-group', row.layout === 'pair' ? 'skill-card-group--pairs' : null]">
          <!-- 각성기·초각성기 페어 레이아웃 (좌우 2열 배치) -->
          <template v-if="row.layout === 'pair' && row.pairs?.length">
            <div v-for="(pairRow, rowIndex) in getPairChunks(row.pairs)" :key="`pair-row-${row.key}-${rowIndex}`"
              class="skill-card-pair-row">
              <!-- 각성기 페어 -->
              <div v-for="pair in pairRow" :key="pair.key" class="skill-card-pair">
                <div class="skill-card-pair-columns">
                  <!-- 좌측 컬럼: 각성기 (skillTypeCode: 100) -->
                  <div class="skill-card-pair-column">
                    <template v-if="pair.left">
                      <article class="skill-card" :class="{ 'skill-card--compact': pair.left.isCompact }">
                        <div class="skill-card-main">
                          <div class="skill-card-hero">
                            <div class="skill-card-icon-block">
                              <div class="skill-icon-wrapper" tabindex="0">
                                <LazyImage v-if="pair.left.icon" :src="pair.left.icon" :alt="pair.left.name" width="40"
                                  height="40" imageClass="skill-card-icon" errorIcon="✨" :useProxy="true" />
                              </div>
                              <p class="skill-card-name">{{ pair.left.name }}</p>
                              <div v-if="pair.left.gemBadges.length" class="skill-affix-row">
                                <span v-for="gem in pair.left.gemBadges" :key="`gem-affix-${pair.left.key}-${gem.key}`"
                                  class="skill-affix skill-affix--gem">
                                  <span class="affix-label">{{ gem.effectLabel || gem.name }}</span>
                                  <span class="affix-text">
                                    {{
                                      gem.extraEffect
                                        ? [gem.effectText, gem.extraEffect].filter(Boolean).join(' · ')
                                        : gem.effectText || gem.levelLabel || gem.name
                                    }}
                                  </span>
                                </span>
                              </div>
                            </div>
                          </div>
                          <div v-if="pair.left.tripods.length || pair.left.rune || pair.left.gemBadges.length"
                            class="skill-tripod-rail" :class="{ 'skill-tripod-rail--compact': pair.left.isCompact }">
                            <div v-for="tripod in pair.left.tripods" :key="tripod.key" class="tripod-detail-inline">
                              <div class="tripod-inline-icon">
                                <LazyImage v-if="tripod.icon" :src="tripod.icon" :alt="tripod.name" width="40"
                                  height="40" imageClass="tripod-image" errorIcon="🌀" :useProxy="true" />
                                <!-- <span v-else class="tripod-tier-pill" :class="`tier-${tripod.tier ?? 'x'}`">
                                T{{ tripod.tier ?? '?' }}
                              </span> -->
                              </div>
                              <div class="tripod-inline-name">
                                <span class="tripod-name">{{ tripod.name }}</span>
                                <span v-if="tripod.levelLabel" class="tripod-level">{{ tripod.levelLabel }}</span>
                              </div>
                              <span class="tripod-desc" :class="{ 'tripod-desc--empty': !tripod.description }">
                                {{ tripod.description || '' }}
                              </span>
                              <span class="tripod-slot">슬롯 {{ tripod.slotLabel }}</span>
                            </div>
                            <div v-if="pair.left.rune" class="skill-rune skill-rune--inline">
                              <div class="skill-rune-icon">
                                <LazyImage v-if="pair.left.rune.icon" :src="pair.left.rune.icon"
                                  :alt="pair.left.rune.name" width="32" height="32" imageClass="rune-image"
                                  errorIcon="💠" :useProxy="true" />
                              </div>
                              <div>
                                <p class="skill-rune-grade" :style="{ color: pair.left.rune.gradeColor || undefined }">
                                  {{ pair.left.rune.grade || '룬' }}
                                </p>
                                <strong class="skill-rune-name">{{ pair.left.rune.name }}</strong>
                                <p v-if="getRuneAffixView(pair.left.rune, pair.left.runeEffect)?.text"
                                  class="skill-rune-description">
                                  {{ getRuneAffixView(pair.left.rune, pair.left.runeEffect)!.text }}
                                </p>
                              </div>
                            </div>
                          </div>
                          <div v-if="pair.left.gemBadges.length" class="skill-gem-row">
                            <div v-for="gem in pair.left.gemBadges" :key="`gem-${pair.left.key}-${gem.key}`"
                              class="skill-gem-item">
                              <div class="skill-gem-main">
                                <LazyImage v-if="gem.icon" :src="gem.icon" :alt="gem.name" width="32" height="32"
                                  imageClass="skill-gem-icon-img" errorIcon="💎" :useProxy="true" />
                                <span v-else class="skill-gem-icon-fallback">💎</span>
                                <div class="skill-gem-info">
                                  <span class="skill-gem-name">{{ gem.name }}</span>
                                  <span v-if="gem.levelLabel" class="skill-gem-level">{{ gem.levelLabel }}</span>
                                </div>
                              </div>
                              <div v-if="gem.effectText || gem.extraEffect" class="skill-gem-effect">
                                <span class="skill-gem-effect-main">{{ gem.effectText || '' }}</span>
                                <span v-if="gem.extraEffect" class="skill-gem-extra">
                                  {{ gem.extraEffect }}
                                </span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </article>
                    </template>
                    <article v-else class="skill-card skill-card--empty">
                      <span class="skill-card-column-chip">각성기</span>
                      <p>연결된 스킬이 없습니다.</p>
                    </article>
                  </div>

                  <!-- 우측 컬럼: 초각성기 (skillTypeCode: 101) -->
                  <div class="skill-card-pair-column">
                    <template v-if="pair.right">
                      <article class="skill-card" :class="{ 'skill-card--compact': pair.right.isCompact }">
                        <div class="skill-card-main">
                          <div class="skill-card-hero">
                            <div class="skill-card-icon-block">
                              <div class="skill-icon-wrapper" tabindex="0">
                                <LazyImage v-if="pair.right.icon" :src="pair.right.icon" :alt="pair.right.name"
                                  width="40" height="40" imageClass="skill-card-icon" errorIcon="✨" :useProxy="true" />
                              </div>
                              <p class="skill-card-name">{{ pair.right.name }}</p>
                            </div>
                          </div>
                        </div>
                      </article>
                    </template>
                    <article v-else class="skill-card skill-card--empty">
                      <span class="skill-card-column-chip">초각성기</span>
                      <p>연결된 스킬이 없습니다.</p>
                    </article>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <!-- 전투 스킬 그리드 레이아웃 (skillTypeCode: 0) -->
          <div v-else class="skill-card-grid skill-card-grid--limited">
            <!-- 강화된 스킬 (트라이포드/룬/보석이 있는 스킬) -->
            <template v-for="skill in getEnhancedSkills(row.cards)" :key="`${skill.key}-enhanced`">
              <article class="skill-card skill-card--enhanced-row" :class="{ 'skill-card--compact': skill.isCompact }">
                <div class="skill-card-main">
                  <div class="skill-card-area">

                      <div class="skill-card-hero">
                        <div class="skill-card-icon-block">
                          <div class="skill-icon-wrapper" tabindex="0">
                            <LazyImage v-if="skill.icon" :src="skill.icon" :alt="skill.name" width="50" height="50"
                              imageClass="skill-card-icon" errorIcon="✨" :useProxy="true" />
                          </div>
                          <p class="skill-card-name">{{ skill.name }}</p>
                          <p class="skill-card-meta">
                            <span v-if="skill.levelLabel">{{ skill.levelLabel }}</span>
                            <span v-if="skill.skillPointLabel" class="skill-card-point">{{ skill.skillPointLabel }}</span>
                          </p>
                        </div>

                        <div class="skill-main-destruction">
                          <p v-if="skill.description" class="skill-description" v-html="skill.description"></p>
                        </div>
                      </div>

                        <div v-if="skill.tripods.length || skill.rune || skill.gemBadges.length" class="skill-tripod-rail"
                      :class="{ 'skill-tripod-rail--compact': skill.isCompact }">
                      <div v-for="(tripod, index) in skill.tripods" :key="tripod.key" class="tripod-detail-inline">
                        <div class="tripod-detail-icon">
                          <LazyImage v-if="tripod.icon" :src="tripod.icon" :alt="tripod.name" width="36" height="36"
                            imageClass="tripod-image" errorIcon="🌀" :useProxy="true" />
                        </div>
                        <div class="tripod-detail-body">
                          <div class="tripod-detail-head">

                            <span class="tripod-name">{{ tripod.name }}</span>
                            <span v-if="tripod.description" class="tripod-desc">
                              {{ tripod.description }}
                            </span>
                            <span class="tripod-slot" :class="`tripod-color-${index + 1}`">{{ tripod.slotLabel }}</span>
                            <span v-if="tripod.levelLabel" class="tripod-level">
                              {{ tripod.levelLabel }}
                            </span>
                          </div>
                        </div>
                      </div>
                      <div v-if="skill.rune" class="skill-rune skill-rune--inline">
                        <div class="skill-rune-icon">
                          <LazyImage v-if="skill.rune.icon" :src="skill.rune.icon" :alt="skill.rune.name" width="32"
                            height="32" imageClass="rune-image" errorIcon="💠" :useProxy="true" />
                        </div>
                        <p class="skill-rune-grade" :style="{ color: skill.rune.gradeColor || undefined }">
                          {{ skill.rune.grade || '룬' }}
                        </p>
                        <strong class="skill-rune-name">{{ skill.rune.name }}</strong>
                        <p v-if="getRuneAffixView(skill.rune, skill.runeEffect)?.text" class="skill-rune-description">
                          {{ getRuneAffixView(skill.rune, skill.runeEffect)!.text }}
                        </p>
                      </div>
                      <div v-if="skill.gemBadges.length" class="skill-gem-row">
                        <div v-for="gem in skill.gemBadges" :key="`gem-${skill.key}-${gem.key}`" class="skill-gem-item">
                          <div class="skill-gem-main">
                            <LazyImage v-if="gem.icon" :src="gem.icon" :alt="gem.name" width="32" height="32"
                              imageClass="skill-gem-icon-img" errorIcon="💎" :useProxy="true" />
                            <span v-else class="skill-gem-icon-fallback">💎</span>
                            <div class="skill-gem-info">
                              <span class="skill-gem-name">{{ gem.name }}</span>
                            </div>
                          </div>
                          <div v-if="gem.effectText || gem.extraEffect" class="skill-gem-effect">
                            <span class="skill-gem-effect-main">{{ gem.effectText || '' }}</span>
                            <span v-if="gem.extraEffect" class="skill-gem-extra">
                              {{ gem.extraEffect }}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </article>
            </template>

            <!-- 일반 스킬 (트라이포드/룬/보석이 없는 스킬) - 인라인 행 배치 -->
            <div v-if="getPlainSkills(row.cards).length" class="skill-card-inline-row">
              <article v-for="skill in getPlainSkills(row.cards)" :key="`${skill.key}-plain`"
                class="skill-card skill-card--inline" :class="{ 'skill-card--compact': skill.isCompact }">
                <div class="skill-card-main">
                  <div class="skill-card-hero">
                    <div class="skill-card-icon-block">
                      <div class="skill-icon-wrapper" tabindex="0">
                        <LazyImage v-if="skill.icon" :src="skill.icon" :alt="skill.name" width="40" height="40"
                          imageClass="skill-card-icon" errorIcon="✨" :useProxy="true" />
                        <!-- 스킬 아이콘 호버 시 표시되는 툴팁 -->
                        <div
                          v-if="skill.description || skill.tripods.length"
                          class="skill-icon-tooltip popup-surface popup-surface--tooltip"
                        >
                          <p class="popup-surface__title skill-tooltip-title">{{ skill.name }}</p>
                          <p v-if="skill.description" class="popup-surface__body skill-tooltip-desc" v-html="skill.description"></p>
                        </div>
                      </div>
                      <p class="skill-card-name">{{ skill.name }}</p>
                      <p class="skill-card-meta">
                        <span v-if="skill.levelLabel">{{ skill.levelLabel }}</span>
                      </p>
                    </div>
                  </div>
                </div>
              </article>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * SkillPanel 컴포넌트
 *
 * 로스트아크 캐릭터의 스킬 정보를 표시하는 메인 패널 컴포넌트
 * - 초각성 스킬 (skillTypeCode: 1)
 * - 각성기/초각성기 페어 (skillTypeCode: 100, 101)
 * - 전투 스킬 (skillTypeCode: 0)
 * - 트라이포드, 룬, 보석 정보 포함
 */

import { computed } from 'vue'
import LoadingSpinner from './LoadingSpinner.vue'
import EmptyState from './EmptyState.vue'
import ErrorMessage from './ErrorMessage.vue'
import LazyImage from './LazyImage.vue'
import { extractTooltipColor, flattenTooltipLines, sanitizeInline } from '@/utils/tooltipText'
import type { CombatSkill, SkillMenuResponse } from '@/api/types'

// ===== Props 정의 =====
const props = defineProps<{
  response: SkillMenuResponse | null  // API 응답 데이터
  loading: boolean                     // 로딩 상태
  errorMessage: string | null          // 에러 메시지
  characterName: string                // 캐릭터 이름
}>()

// ===== Emits 정의 =====
defineEmits<{
  (e: 'retry'): void  // 재시도 이벤트
}>()

// ===== Interface 정의 =====

/** 트라이포드 뷰 인터페이스 */
interface SkillTripodView {
  key: string
  name: string
  icon?: string
  tier?: number
  levelLabel?: string
  description?: string
  slotLabel?: string
}

/** 룬 뷰 인터페이스 */
interface SkillRuneView {
  name: string
  grade?: string
  icon?: string
  description?: string
  gradeColor?: string
}

/** 보석 뱃지 인터페이스 */
interface SkillGemBadge {
  key: string
  name: string
  icon?: string
  levelLabel?: string
  effectText?: string      // 메인 효과 텍스트 (예: "쿨타임 -20%")
  effectLabel?: string     // 효과 레이블 (예: "작열", "겁화")
  extraEffect?: string     // 추가 효과 텍스트
}

/** 각성기 종류 타입 */
type AwakeningSkillKind = 'superSkill' | 'awakening'

/** 스킬 카드 뷰 인터페이스 */
interface SkillCardView {
  key: string
  name: string
  icon?: string
  levelLabel?: string
  typeLabel?: string
  pointLabel?: string
  skillPointLabel?: string
  description?: string
  // 메타 정보
  stagger?: string          // 무력화 (예: "중", "상")
  attackType?: string       // 공격 타입 (예: "백 어택", "헤드 어택")
  superArmor?: string       // 슈퍼아머 (예: "경직 면역")
  destruction?: string      // 부위파괴 (예: "1레벨")
  // 기존 필드
  tripods: SkillTripodView[]
  rune: SkillRuneView | null
  gemBadges: SkillGemBadge[]
  isCompact: boolean
  isAwakening: boolean
  awakeningType?: AwakeningSkillKind
  skillTypeCode?: number | null
  originalIndex: number
  runeEffect?: string
}

/** 스킬 섹션 행 인터페이스 */
interface SkillSectionRow {
  key: string
  title?: string
  cards: SkillCardView[]
  layout?: 'grid' | 'pair'  // 레이아웃 타입: 그리드 또는 페어
  pairs?: AwakeningPairGroup[]
}

/** 스킬 섹션 뷰 인터페이스 */
interface SkillSectionView {
  key: string
  title: string
  subtitle: string
  cards: SkillCardView[]
  modifier?: string           // CSS 클래스 수정자
  rows?: SkillSectionRow[]
}

/** 각성기 페어 그룹 인터페이스 (좌우 2열 배치) */
interface AwakeningPairGroup {
  key: string
  title: string
  left?: SkillCardView   // 왼쪽: 각성기 (skillTypeCode: 100)
  right?: SkillCardView  // 오른쪽: 초각성기 (skillTypeCode: 101)
}

/** 보석 카드 뷰 인터페이스 (사용 안함 - 주석 처리됨) */
interface GemCardView {
  key: string
  name: string
  icon?: string
  grade?: string
  levelLabel?: string
  skillName?: string
  skillDescription?: string
}

// ===== Computed Properties =====

/** 전투 스킬 목록 */
const combatSkills = computed(() => props.response?.combatSkills ?? [])

/** 스킬 보석 목록 */
const skillGems = computed(() => props.response?.skillGems ?? [])

// ===== 유틸리티 함수 =====

/**
 * 스킬 이름을 정규화된 키로 변환 (공백 및 특수문자 제거, 소문자 변환)
 * @param value - 스킬 이름
 * @returns 정규화된 키
 */
const normalizeSkillKey = (value?: string | null) =>
  sanitizeInline(value)
    .replace(/[\s\[\]\(\)<>{}]/g, '')
    .toLowerCase()

/**
 * 스킬 이름으로 보석 뱃지 조회 (완전 일치 또는 부분 일치)
 * @param skillName - 스킬 이름
 * @param map - 보석 뱃지 맵
 * @returns 해당 스킬의 보석 뱃지 배열
 */
const resolveGemBadgesForSkill = (skillName: string, map: Map<string, SkillGemBadge[]>) => {
  const key = normalizeSkillKey(skillName)
  if (map.has(key)) return [...map.get(key)!]
  for (const [mapKey, badges] of map.entries()) {
    if (key.includes(mapKey) || mapKey.includes(key)) {
      return [...badges]
    }
  }
  return []
}

/**
 * HTML 문자열에서 폰트 색상 추출
 * @param value - HTML 문자열
 * @returns HEX 색상 코드 (예: "#FF0000")
 */
const extractFontColor = (value?: string | null) => extractTooltipColor(value) || ''

const sanitizeWithColors = (value?: string | null) => {
  if (!value) return ''
  const normalized = String(value).replace(/\r\n|\n|\r/g, ' ')
  // 1) font color -> span color
  let html = normalized.replace(
    /<font[^>]*color=['"]?([^'" >]+)['"]?[^>]*>(.*?)<\/font>/gi,
    (_match, color, inner) => {
      const safeInner = inner.replace(/<(?!br\s*\/?)[^>]+>/gi, '')
      return `<span style="color:${color}">${safeInner}</span>`
    }
  )
  // 2) span style color keep, drop other tags
  html = html.replace(
    /<span[^>]*style=["'][^"']*color\s*:\s*([^;"']+)[^"']*["'][^>]*>(.*?)<\/span>/gi,
    (_match, color, inner) => {
      const safeInner = inner.replace(/<(?!br\s*\/?)[^>]+>/gi, '')
      return `<span style="color:${color}">${safeInner}</span>`
    }
  )
  // 3) allow <br>, strip the rest
  html = html
    .replace(/<br\s*\/?\s*>/gi, '<br />')
    .replace(/<(?!br\s*\/?|span\b|\/span\b)[^>]+>/gi, '')
  return html.trim()
}

/**
 * 각성기 페어 제목 추출 (콜론 앞부분 또는 "(클론" 앞부분)
 * @param value - 스킬 이름
 * @returns 추출된 제목
 */
const extractPairTitle = (value?: string | null) => {
  const sanitized = sanitizeInline(value)
  if (!sanitized) return ''
  const cloneTrigger = sanitized.indexOf(':(클론')
  if (cloneTrigger >= 0) {
    return sanitized.slice(0, cloneTrigger).trim()
  }
  const colonIndex = sanitized.indexOf(':')
  if (colonIndex >= 0) {
    return sanitized.slice(0, colonIndex).trim()
  }
  return sanitized
}

// ===== 상수 정의 =====

/** 초각성기 스킬 타입 코드 */
const SUPER_SKILL_CODES = new Set([101])

/** 각성기 스킬 타입 코드 */
const AWAKENING_SKILL_CODES = new Set([100])

/** 각성기 종류별 라벨 */
const AWAKENING_KIND_LABELS: Record<AwakeningSkillKind, string> = {
  superSkill: '초각성기',
  awakening: '각성기'
}

/**
 * 스킬 타입 코드 파싱 (문자열 또는 숫자를 숫자로 변환)
 * @param value - 스킬 타입 값
 * @returns 파싱된 숫자 또는 null
 */
const parseSkillTypeCode = (value?: string | number | null) => {
  if (value === undefined || value === null) return null
  if (typeof value === 'number') {
    return Number.isNaN(value) ? null : value
  }
  const numeric = Number(sanitizeInline(value))
  if (Number.isNaN(numeric)) return null
  return numeric
}

/**
 * 스킬이 각성기인지 초각성기인지 감지
 * @param skill - 전투 스킬 객체
 * @param parsedSkillType - 파싱된 스킬 타입 코드
 * @returns 각성기 종류 또는 null
 */
const detectAwakeningKind = (
  skill: CombatSkill,
  parsedSkillType: number | null = parseSkillTypeCode(skill.skillType)
): AwakeningSkillKind | null => {
  if (parsedSkillType !== null) {
    if (SUPER_SKILL_CODES.has(parsedSkillType)) return 'superSkill'
    if (AWAKENING_SKILL_CODES.has(parsedSkillType)) return 'awakening'
  }

  const candidates = [skill.skillType, skill.type, skill.name]
    .map(value => sanitizeInline(value)?.toLowerCase())
    .filter(Boolean) as string[]

  if (candidates.some(value => /초\s*각성/.test(value))) {
    return 'superSkill'
  }
  if (candidates.some(value => /각성/.test(value))) {
    return 'awakening'
  }
  return null
}

// ===== 툴팁 파싱 함수 =====

/**
 * 툴팁에서 특정 키워드 다음 줄 추출
 * @param tooltip - 툴팁 문자열
 * @param keyword - 검색할 키워드
 * @returns 키워드 다음 줄의 텍스트
 */
const extractNextLineAfterKeyword = (tooltip?: string | null, keyword?: string) => {
  if (!tooltip || !keyword) return ''
  const lines = flattenTooltipLines(tooltip)
  const idx = lines.findIndex(line => line.includes(keyword))
  if (idx === -1) return ''
  const next = lines.slice(idx + 1).find(Boolean)
  return next || ''
}

/**
 * 툴팁에서 특정 키워드가 포함된 줄 추출
 * @param tooltip - 툴팁 문자열
 * @param keyword - 검색할 키워드
 * @returns 키워드가 포함된 줄의 텍스트
 */
const extractLineWithKeyword = (tooltip?: string | null, keyword?: string) => {
  if (!tooltip || !keyword) return ''
  const lines = flattenTooltipLines(tooltip)
  const line = lines.find(line => line.includes(keyword))
  return line || ''
}

/**
 * 스킬 메타 정보 추출 (무력화, 공격 타입, 슈퍼아머, 부위파괴)
 * @param tooltip - 스킬 툴팁 문자열
 * @returns 메타 정보 객체
 */
const extractSkillMetadata = (tooltip?: string | null) => {
  if (!tooltip) return {}

  const metadata: {
    stagger?: string
    attackType?: string
    superArmor?: string
    destruction?: string
  } = {}

  try {
    const parsed = JSON.parse(tooltip)

    // 모든 Element를 순회하면서 메타 정보 추출
    Object.values(parsed).forEach((element: any) => {
      if (!element?.value) return

      const value = typeof element.value === 'string' ? element.value : ''
      const cleanValue = sanitizeInline(value)

      // 무력화: "무력화 : 중", "무력화: 상" - 한 글자만 추출
      if (!metadata.stagger) {
        const staggerMatch = cleanValue.match(/무력화\s*[:\:]\s*([가-힣]+)/)
        const staggerValue = staggerMatch?.[1]
        if (staggerValue) {
          metadata.stagger = staggerValue.trim()
        }
      }

      // 공격 타입: "백 어택" 또는 "헤드 어택"만 정확히 추출
      if (!metadata.attackType) {
        const attackMatch = cleanValue.match(/공격\s*타입\s*[:\:]\s*(백\s*어택|헤드\s*어택)/)
        const attackValue = attackMatch?.[1]
        if (attackValue) {
          metadata.attackType = attackValue.trim()
        }
      }

      // 슈퍼아머: "경직 면역" 등의 값만 추출 (다음 키워드 전까지)
      if (!metadata.superArmor) {
        const armorMatch = cleanValue.match(/슈퍼아머\s*[:\:]\s*([가-힣\s]+?)(?=\s*무력화|\s*공격|\s*부위|$)/)
        const armorValue = armorMatch?.[1]
        if (armorValue) {
          metadata.superArmor = armorValue.trim()
        }
      }

      // 부위파괴: 실제 데이터 형식 "부위 파괴 : 레벨 1"
      if (!metadata.destruction) {
        // 실제 형식: "부위 파괴 : 레벨 1"
        const destructionMatch = cleanValue.match(/부위\s*파괴\s*[:\:]\s*레벨\s*(\d+)/)
        const destructionValue = destructionMatch?.[1]
        if (destructionValue) {
          metadata.destruction = `${destructionValue}레벨`
        }
      }
    })
  } catch {
    // JSON 파싱 실패 시 기존 방식으로 폴백
    const lines = flattenTooltipLines(tooltip)
    lines.forEach(line => {
      if (!metadata.stagger) {
        const staggerMatch = line.match(/무력화\s*[:\:]\s*([가-힣]+)/)
        const staggerValue = staggerMatch?.[1]
        if (staggerValue) metadata.stagger = staggerValue.trim()
      }
      if (!metadata.attackType) {
        const attackMatch = line.match(/공격\s*타입\s*[:\:]\s*(백\s*어택|헤드\s*어택)/)
        const attackValue = attackMatch?.[1]
        if (attackValue) metadata.attackType = attackValue.trim()
      }
      if (!metadata.superArmor) {
        const armorMatch = line.match(/슈퍼아머\s*[:\:]\s*([가-힣\s]+?)(?=\s*무력화|\s*공격|\s*부위|$)/)
        const armorValue = armorMatch?.[1]
        if (armorValue) metadata.superArmor = armorValue.trim()
      }
      if (!metadata.destruction) {
        // 실제 형식: "부위 파괴 : 레벨 1"
        const destructionMatch = line.match(/부위\s*파괴\s*[:\:]\s*레벨\s*(\d+)/)
        const destructionValue = destructionMatch?.[1]
        if (destructionValue) {
          metadata.destruction = `${destructionValue}레벨`
        }
      }
    })
  }

  return metadata
}

// ===== 보석 효과 파싱 함수 =====

/**
 * 보석 효과 텍스트로부터 레이블 추출 (작열/겁화)
 * @param effectText - 보석 효과 텍스트
 * @returns 보석 효과 레이블 (작열/겁화 등)
 */
const normalizeGemEffectLabel = (effectText?: string | null) => {
  const text = sanitizeInline(effectText)
  if (!text) return ''
  const lowered = text.toLowerCase()
  if (/쿨타임|재사용|대기시간/.test(lowered) && /%/.test(lowered)) return '작열'
  if (/(피해|대미지|데미지)/.test(lowered) && /%/.test(lowered)) return '겁화'
  return text
}

/**
 * 보석 효과 텍스트를 메인 효과와 추가 효과로 분리
 * @param effectText - 메인 효과 텍스트
 * @param extraEffect - 추가 효과 텍스트
 * @returns 분리된 메인/추가 효과 객체
 */
const splitGemEffectText = (effectText?: string | null, extraEffect?: string | null) => {
  const base = sanitizeInline(effectText)
  const extra = sanitizeInline(extraEffect)
  const keyword = '추가 효과'

  if (base && base.includes(keyword)) {
    const [mainPart = '', ...rest] = base.split(keyword)
    const main = mainPart.trim()
    const tail = rest.join(keyword).trim()
    return {
      main: main || base,
      extra: tail || extra
    }
  }

  if (!base && extra) {
    return {
      main: '추가 효과',
      extra
    }
  }

  return {
    main: base,
    extra
  }
}

// ===== 룬 헬퍼 함수 =====

/**
 * 룬 정보를 표시용 뷰 객체로 변환
 * @param rune - 룬 뷰 객체
 * @param effect - 룬 효과 텍스트
 * @returns 룬 표시용 뷰 객체
 */
const getRuneAffixView = (rune: SkillRuneView | null, effect?: string) => {
  if (!rune) return null
  return {
    label: [sanitizeInline(rune.grade), sanitizeInline(rune.name) || '룬'].filter(Boolean).join(' '),
    text: sanitizeInline(effect || rune.description || rune.grade),
    icon: rune.icon || undefined
  }
}

// ===== 포맷팅 헬퍼 함수 =====

/**
 * 스킬 툴팁 텍스트를 요약 (실제 스킬 설명 추출)
 * @param tooltip - 스킬 툴팁 문자열
 * @param fallback - 기본값
 * @returns 요약된 텍스트
 */
const summarizeTooltip = (tooltip?: string | null, fallback = '') => {
  if (!tooltip) return fallback

  try {
    const parsed = JSON.parse(tooltip)

    // 1순위: Element_005에서 추출 (일반적으로 스킬 설명이 위치)
    if (parsed.Element_005?.value) {
      let desc = sanitizeWithColors(parsed.Element_005.value)
      // 메타 정보가 포함된 부분 제거 (무력화, 공격 타입, 슈퍼아머, 부위파괴)
      desc = desc.replace(/(?:무력화|공격\s*타입|슈퍼아머|부위\s*파괴).*$/i, '').trim()
      if (desc && desc.length >= 10) {
        return desc
      }
    }

    // 2순위: SingleTextBox 타입의 Element 찾기
    for (const element of Object.values(parsed) as any[]) {
      if (element?.type === 'SingleTextBox' && element?.value) {
        let desc = sanitizeWithColors(element.value)
        // 메타 정보 제거
        desc = desc.replace(/(?:무력화|공격\s*타입|슈퍼아머|부위\s*파괴).*$/i, '').trim()
        if (desc && desc.length >= 10) {
          return desc
        }
      }
    }
  } catch {
    // JSON 파싱 실패 시 기존 방식으로 폴백
  }

  // 폴백: 평탄화된 라인에서 찾기
  const lines = flattenTooltipLines(tooltip)
  if (!lines.length) return fallback

  const description = lines.find((line, index) => {
    if (index === 0) return false
    if (!line || line.trim().length < 10) return false

    // 메타 정보 제외
    if (/레벨|Lv|재사용|마나.*소모|^\||PvE|PvP|무력화|공격\s*타입|슈퍼아머|부위\s*파괴/i.test(line)) {
      return false
    }

    return true
  })

  if (description) return sanitizeWithColors(description)

  // 최종 폴백: 전체 툴팁에서 색상만 보존해 리턴
  const colored = sanitizeWithColors(tooltip)
  if (colored) return colored

  return fallback
}

/**
 * 트라이포드 툴팁 텍스트를 요약 (트라이포드 설명 추출)
 * 트라이포드 툴팁은 JSON이 아닌 평문 HTML 문자열 형식임
 * @param tooltip - 트라이포드 툴팁 문자열
 * @param fallback - 기본값
 * @returns 요약된 텍스트
 */
const summarizeTripodTooltip = (tooltip?: string | null, fallback = '') => {
  if (!tooltip) return fallback

  // 트라이포드 툴팁은 평문 HTML 문자열이므로 직접 처리
  const cleaned = sanitizeInline(tooltip)
  if (cleaned && cleaned.length >= 10) {
    return cleaned
  }

  // 폴백: 평탄화된 라인에서 찾기
  const lines = flattenTooltipLines(tooltip)
  if (!lines.length) return fallback

  // 트라이포드는 첫 번째 의미있는 줄을 사용
  const description = lines.find((line, index) => {
    if (index === 0) return false // 첫 줄(이름) 제외
    if (!line || line.trim().length < 10) return false
    return true
  })

  return description ?? fallback
}

/**
 * 레벨 숫자를 레이블로 포맷팅
 * @param level - 레벨 숫자
 * @param prefix - 접두사 (기본: "Lv.")
 * @returns 포맷팅된 레벨 문자열
 */
const formatLevelLabel = (level?: number | null, prefix = 'Lv.') => {
  if (typeof level !== 'number' || Number.isNaN(level)) return ''
  return `${prefix} ${level}`
}

/**
 * 보석 툴팁에서 효과 텍스트 추출
 * @param tooltip - 보석 툴팁 문자열
 * @param fallback - 기본값
 * @returns 추출된 효과 텍스트
 */
const pickGemEffectText = (tooltip?: string | null, fallback?: string) => {
  const lines = flattenTooltipLines(tooltip)
  const idx = lines.findIndex(line => /보석\s*효과/.test(line))
  if (idx >= 0) {
    const candidate = lines.slice(idx + 1).find(Boolean)
    if (candidate) return sanitizeInline(candidate)
  }
  const firstMeaningful = lines.find(line => line && !/보석\s*효과/.test(line))
  if (firstMeaningful) return sanitizeInline(firstMeaningful)
  return sanitizeInline(fallback)
}

/**
 * 보석 툴팁 JSON 파싱하여 스킬명과 효과 추출
 * @param tooltip - 보석 툴팁 JSON 문자열
 * @returns 파싱된 스킬명, 효과 텍스트, 추가 효과 객체 또는 null
 */
const parseGemTooltipMapping = (tooltip?: string | null) => {
  if (!tooltip) return null
  try {
    const parsed = JSON.parse(tooltip)
    const part = parsed?.Element_007?.value?.Element_001 ?? parsed?.Element_007?.value?.Element_000
    if (part) {
      const skillMatch = part.match(/<FONT[^>]*>([^<]+)<\/FONT>/i)
      const skillName = sanitizeInline(skillMatch?.[1])
      const lines = flattenTooltipLines(part)
      let effectText = ''
      let extraEffect = ''
      if (lines.length) {
        const first = lines[0] ?? ''
        if (first && skillName && first.includes(skillName)) {
          effectText = sanitizeInline(first.replace(skillName, '').replace(/\[[^\]]+\]\s*/, ''))
        } else if (first) {
          effectText = sanitizeInline(first.replace(/^\[[^\]]+\]\s*/, ''))
        }
        const extraIdx = lines.findIndex(line => /추가\s*효과/.test(line))
        if (extraIdx >= 0) {
          const after = lines.slice(extraIdx + 1).find(Boolean)
          if (after) extraEffect = sanitizeInline(after)
        }
      }
      const split = splitGemEffectText(effectText, extraEffect)
      return {
        skillName,
        effectText: split.main,
        extraEffect: split.extra
      }
    }
  } catch {
    // ignore
  }
  return null
}

// ===== 메인 Computed 속성 =====

/**
 * 스킬별 보석 뱃지 맵 생성
 * - skillGems (API 응답의 보석 데이터)
 * - effects.skills (효과 스킬 데이터)
 * - gems (인벤토리 보석 데이터)
 * 세 가지 소스에서 보석 정보를 수집하여 스킬 이름을 키로 하는 맵 생성
 */
const gemBadgesBySkill = computed(() => {
  const map = new Map<string, SkillGemBadge[]>()
  const effectSkills = ((props.response as any)?.effects?.skills ??
    (props.response as any)?.Effects?.Skills ??
    []) as any[]
  const inventoryGems = ((props.response as any)?.gems ??
    (props.response as any)?.Gems ??
    []) as any[]

  skillGems.value.forEach((gem, index) => {
    const skillName = sanitizeInline(gem.skill?.name) || ''
    const key = normalizeSkillKey(skillName)
    if (!key) return

    // Parse tooltip to extract effect text and extra effect
    const parsed = parseGemTooltipMapping(gem.tooltip)
    let effectTextRaw = ''
    let extraEffectRaw = ''

    if (parsed) {
      effectTextRaw = parsed.effectText
      extraEffectRaw = parsed.extraEffect
    } else {
      effectTextRaw =
        pickGemEffectText(gem.tooltip, gem.skill?.description) ||
        extractNextLineAfterKeyword(gem.tooltip, '보석 효과') ||
        extractNextLineAfterKeyword(gem.tooltip, '보석효과') ||
        sanitizeInline(gem.skill?.description)
    }

    const splitEffect = splitGemEffectText(effectTextRaw, extraEffectRaw)
    const effectLabel = normalizeGemEffectLabel(effectTextRaw || extraEffectRaw)
    const badge: SkillGemBadge = {
      key: `${skillName}-${index}`,
      name: sanitizeInline(gem.name) || '보석',
      icon: gem.icon || undefined,
      levelLabel: formatLevelLabel(gem.level),
      effectText: splitEffect.main,
      extraEffect: splitEffect.extra || undefined,
      effectLabel
    }
    if (!map.has(key)) {
      map.set(key, [])
    }
    map.get(key)!.push(badge)
  })

  effectSkills.forEach((effect, index) => {
    const skillName = sanitizeInline(effect?.Name) || ''
    const key = normalizeSkillKey(skillName)
    if (!key) return
    const effectText = sanitizeInline(
      Array.isArray(effect?.Description) ? effect.Description.join(' ') : effect?.Description
    )
    const splitEffect = splitGemEffectText(effectText)
    const badge: SkillGemBadge = {
      key: `${skillName}-effect-${index}`,
      name: sanitizeInline(effect?.Name) || '보석',
      icon: effect?.Icon || undefined,
      levelLabel: sanitizeInline(effect?.Option),
      effectText: splitEffect.main,
      extraEffect: splitEffect.extra || undefined,
      effectLabel: normalizeGemEffectLabel(effectText)
    }
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(badge)
  })

inventoryGems.forEach((gem, index) => {
  const parsed = parseGemTooltipMapping(gem?.Tooltip)
  if (!parsed) return
  const skillName = parsed.skillName
  const key = normalizeSkillKey(skillName)
  if (!key) return
  const splitEffect = splitGemEffectText(parsed.effectText, parsed.extraEffect)
    const badge: SkillGemBadge = {
      key: `${skillName}-inv-${index}`,
      name: sanitizeInline(gem?.Name) || '보석',
      icon: gem?.Icon || undefined,
      levelLabel: formatLevelLabel(gem?.Level),
      effectText: splitEffect.main,
      extraEffect: splitEffect.extra || undefined,
      effectLabel: normalizeGemEffectLabel(parsed.effectText || parsed.extraEffect)
    }
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(badge)
  })

  return map
})

/**
 * 전투 스킬을 UI 표시용 스킬 카드 뷰로 변환
 * - 레벨 기준 내림차순 정렬
 * - 각 스킬의 트라이포드, 룬, 보석 정보 포함
 * - 각성기/초각성기 여부 판별
 * - 컴팩트 모드 여부 결정 (레벨 4 미만 && 강화 요소 없음)
 */
const skillCards = computed<SkillCardView[]>(() => {
  if (!combatSkills.value.length) return []
  const annotated = combatSkills.value.map((skill, originalIndex) => ({ skill, originalIndex }))
  const sorted = annotated.sort((a, b) => {
    const levelA = typeof a.skill.level === 'number' ? a.skill.level : -1
    const levelB = typeof b.skill.level === 'number' ? b.skill.level : -1
    if (levelA === levelB) {
      return sanitizeInline(b.skill.name).localeCompare(sanitizeInline(a.skill.name))
    }
    return levelB - levelA
  })
  return sorted
    .filter(entry => entry.skill.name)
    .map((entry, index) => {
      const skill = entry.skill
      const name = sanitizeInline(skill.name) || `스킬 ${index + 1}`
      const parsedSkillType = parseSkillTypeCode(skill.skillType)
      const awakeningKind = detectAwakeningKind(skill, parsedSkillType)
      const typeParts: string[] = []
      const payloadType = sanitizeInline(skill.type)
      if (awakeningKind) {
        const explicitLabel =
          parsedSkillType === 100
            ? '각성기'
            : parsedSkillType === 101
              ? '초각성기'
              : null
        typeParts.push(explicitLabel || AWAKENING_KIND_LABELS[awakeningKind])
      }
      if (payloadType) {
        typeParts.push(payloadType)
      }
      const isLowLevel = typeof skill.level === 'number' && skill.level < 4
      const isAwakening = Boolean(awakeningKind)

      const rune = skill.rune?.name
        ? {
          name: sanitizeInline(skill.rune.name),
          grade: sanitizeInline(skill.rune.grade),
          icon: skill.rune.icon || undefined,
          description: summarizeTooltip(skill.rune.tooltip, ''),
          gradeColor: extractFontColor(skill.rune.tooltip)
        }
        : null

      const tripods =
        skill.tripods
          ?.filter(tripod => tripod.name && tripod.selected !== false)
          .map((tripod, tripodIndex) => ({
            key: `${name}-tripod-${tripodIndex}`,
            name: sanitizeInline(tripod.name) || `트라이포드 ${tripodIndex + 1}`,
            icon: tripod.icon || undefined,
            tier: typeof tripod.tier === 'number' ? tripod.tier + 1 : undefined,
            slot: typeof tripod.slot === 'number' ? tripod.slot : undefined,
            slotLabel: typeof tripod.slot === 'number' ? `${tripod.slot}` : `${tripodIndex + 1}`,
            levelLabel: formatLevelLabel(tripod.level),
            description: summarizeTripodTooltip(tripod.tooltip, '')
          })) ?? []

      const gemBadges = resolveGemBadgesForSkill(name, gemBadgesBySkill.value)
      const isCompact = isLowLevel && tripods.length === 0 && !rune && !isAwakening

      const tooltipLines = flattenTooltipLines(skill.tooltip)

      // 메타 정보 추출 (무력화, 공격 타입, 슈퍼아머, 부위파괴)
      const metadata = extractSkillMetadata(skill.tooltip)

      return {
        key: `${name}-${skill.level ?? index}`,
        name,
        icon: skill.icon || undefined,
        levelLabel: formatLevelLabel(skill.level),
        typeLabel: typeParts.join(' · ') || undefined,
        pointLabel: typeof skill.skillPoints === 'number' ? `${skill.skillPoints.toLocaleString()} 포인트` : undefined,
        skillPointLabel:
          typeof skill.skillPoints === 'number' ? `${skill.skillPoints.toLocaleString()}P` : undefined,
        description: summarizeTooltip(skill.tooltip, ''),
        // 메타 정보
        stagger: metadata.stagger,
        attackType: metadata.attackType,
        superArmor: metadata.superArmor,
        destruction: metadata.destruction,
        // 기존 필드
        tooltipLines,
        tripods,
        rune,
        gemBadges,
        isCompact,
        isAwakening,
        awakeningType: awakeningKind ?? undefined,
        skillTypeCode: parsedSkillType ?? undefined,
        originalIndex: entry.originalIndex,
        runeEffect: skill.rune ? extractNextLineAfterKeyword(skill.rune.tooltip, '스킬 룬 효과') : undefined
      }
    })
})

/**
 * 각성기 페어 후보 목록 (skillTypeCode: 100, 101)
 * 원본 인덱스 기준 오름차순 정렬
 */
const awakeningPairCandidates = computed(() =>
  [...skillCards.value]
    .filter(card => card.skillTypeCode === 100 || card.skillTypeCode === 101)
    .sort((a, b) => a.originalIndex - b.originalIndex)
)

/**
 * 각성기·초각성기 페어 그룹 생성
 * - 각성기(100)를 좌측에 배치
 * - 초각성기(101)를 우측에 배치
 * - 페어가 없는 경우 단독으로 표시
 */
const classicAwakeningPairs = computed<AwakeningPairGroup[]>(() => {
  const ordered: AwakeningPairGroup[] = []
  const pendingLeft: AwakeningPairGroup[] = []

  awakeningPairCandidates.value.forEach(card => {
    const title = extractPairTitle(card.name) || '연관 스킬'
    if (card.skillTypeCode === 100) {
      const pair: AwakeningPairGroup = {
        key: `awakening-${card.originalIndex}`,
        title,
        left: card
      }
      ordered.push(pair)
      pendingLeft.push(pair)
    } else if (card.skillTypeCode === 101) {
      let target = pendingLeft.find(p => !p.right)
      if (target) {
        target.right = card
        pendingLeft.splice(pendingLeft.indexOf(target), 1)
      } else {
        const pair: AwakeningPairGroup = {
          key: `awakening-${card.originalIndex}`,
          title,
          right: card
        }
        ordered.push(pair)
      }
    }
  })

  return ordered
})

/**
 * 일반 전투 스킬 목록 (각성기 제외, skillTypeCode: 0) + 스킬포인트/룬이 있는 스킬만 노출
 */
const regularSkillCards = computed(() =>
  skillCards.value.filter(
    card => !card.isAwakening && card.skillTypeCode === 0 && (card.skillPointLabel || card.rune)
  )
)

/**
 * 초각성 스킬 하이라이트 목록 (skillTypeCode: 1)
 */
const superSkillHighlights = computed(() =>
  skillCards.value.filter(card => card.skillTypeCode === 1)
)

/**
 * 스킬 섹션 구성
 * - 각성·초각성기 섹션: 페어 레이아웃
 * - 전투 스킬 섹션: 그리드 레이아웃
 */
const skillSections = computed<SkillSectionView[]>(() => {
  const sections: SkillSectionView[] = []
  const awakeningRows: SkillSectionRow[] = []
  if (classicAwakeningPairs.value.length) {
    awakeningRows.push({
      key: 'paired-awakening',
      title: '각성기·초각성기',
      cards: [],
      layout: 'pair',
      pairs: classicAwakeningPairs.value
    })
  }
  if (awakeningRows.length) {
    sections.push({
      key: 'awakening',
      title: '각성·초각성기',
      subtitle: '각성기와 초각성기를 구분하여 확인하세요.',
      cards: awakeningPairCandidates.value,
      modifier: 'skill-section--awakening',
      rows: awakeningRows
    })
  }
  if (regularSkillCards.value.length) {
    sections.push({
      key: 'combat',
      title: '전투 스킬 트리',
      subtitle: '선택된 트라이포드, 룬, 보석 정보를 한눈에 살펴보세요.',
      cards: regularSkillCards.value,
      rows: [
        {
          key: 'combat-grid',
          cards: regularSkillCards.value,
          layout: 'grid'
        }
      ]
    })
  }
  return sections
})

/**
 * 보석 카드 뷰 목록 (현재 템플릿에서 주석 처리되어 미사용)
 */
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

/**
 * 렌더링 가능한 컨텐츠 존재 여부
 */
const hasRenderableContent = computed(() => skillCards.value.length > 0 || gemCards.value.length > 0)

/**
 * 빈 상태 설명 메시지
 */
const emptyStateDescription = computed(() => {
  if (!props.characterName) {
    return '캐릭터를 검색하면 전투 스킬 프리셋을 불러옵니다.'
  }
  return `'${props.characterName}' 캐릭터의 스킬 프리셋이 감지되지 않았어요. 인게임에서 스킬을 저장했는지 확인해 주세요.`
})

// ===== 렌더링 헬퍼 함수 =====

/**
 * 섹션의 행 목록 반환 (없으면 기본 그리드 행 생성)
 */
const getSectionRows = (section: SkillSectionView): SkillSectionRow[] => {
  if (section.rows?.length) {
    return section.rows
  }
  return [
    {
      key: section.key,
      cards: section.cards,
      layout: 'grid'
    }
  ]
}

/**
 * 강화된 스킬인지 판별 (트라이포드/룬/보석이 있는 스킬)
 */
const isEnhancedSkill = (skill: SkillCardView) =>
  Boolean(skill.pointLabel || skill.rune || skill.tripods.length || (skill.gemBadges && skill.gemBadges.length))

/**
 * 강화된 스킬 목록 필터링
 */
const getEnhancedSkills = (cards: SkillCardView[]) => cards.filter(isEnhancedSkill)

/**
 * 일반 스킬 목록 필터링 (강화 요소가 없는 스킬)
 */
const getPlainSkills = (cards: SkillCardView[]) => cards.filter(card => !isEnhancedSkill(card))

/**
 * 각성기 페어 배열을 청크로 분할 (기본 2개씩 그룹화)
 * @param pairs - 각성기 페어 배열
 * @param chunkSize - 청크 크기 (기본: 2)
 * @returns 청크로 분할된 2차원 배열
 */
const getPairChunks = (pairs?: AwakeningPairGroup[] | null, chunkSize = 2): AwakeningPairGroup[][] => {
  if (!pairs || !pairs.length || chunkSize <= 0) return []
  const chunks: AwakeningPairGroup[][] = []
  for (let i = 0; i < pairs.length; i += chunkSize) {
    chunks.push(pairs.slice(i, i + chunkSize))
  }
  return chunks
}
</script>

<style scoped>
/**
 * ========================================
 * SkillPanel 스타일 정의
 * ========================================
 */

/* ===== 메인 컨테이너 ===== */
.skill-panel-shell {
  width: 100%;
  --icon-scale: 0.8;
  font-size: 0.92rem;
}

/* ===== 플레이스홀더 (로딩, 에러, 빈 상태) ===== */
.skill-panel-placeholder {
  padding: 32px;
  border-radius: 16px;
  background: var(--surface-color, #fff);
  box-shadow: 0 10px 26px rgba(17, 24, 39, 0.06);
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

/* ===== 스킬 패널 레이아웃 ===== */
.skill-panel-layout {
  display: flex;
  flex-wrap: wrap;
  gap: 36px;
}

/* ===== 섹션 헤딩 ===== */
.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.section-heading h4 {
  margin: 0 0 10px;
  font-size: 1rem;
  color: var(--text-primary, #1f2937);
}

.section-heading p {
  margin: 0;
  color: var(--text-muted, #6b7280);
  font-size: 0.85rem;
}

/* ===== 스킬 섹션 ===== */
.skill-section {
  padding: 25px;
  border-radius: 16px;
  background: var(--surface-color, #fff);
  box-shadow: 0 10px 26px rgba(17, 24, 39, 0.06);
  width: fit-content;
}

/* 각성기 섹션 스타일 */
.skill-section--awakening {
  border-color: rgba(251, 146, 60, 0.5);
  background: rgba(251, 191, 36, 0.08);
}

/* 초각성 스킬 하이라이트 섹션 */
.skill-section--highlight {
  border-color: rgba(59, 130, 246, 0.4);
  background:  rgba(59, 130, 246, 0.08);
}

/* ===== 스킬 카드 그룹 ===== */
.skill-card-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skill-card-group--pairs {
  gap: 16px;
}

/* 각성기 페어 행 (2열 그리드) */
.skill-card-pair-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.skill-card-group--pairs .skill-card-pair {
  margin-top: 0;
}

.skill-card-row-title {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-secondary, #374151);
}

/* ===== 스킬 카드 그리드 ===== */
.skill-card-grid {
  display: flex;
  flex-direction: column;
  /* gap: 12px; */
}

/* 초각성 스킬 그리드 */
.super-skill-grid {
  margin-top: 5px;
  flex-direction: row;
  gap: 12px;
}

.super-skill-grid>.skill-card {
  /* flex: 1 1 320px; */
  max-width: 420px;
}

.skill-card-pair {
  margin-top: 8px;
  /* padding: 16px; */
  border-radius: 12px;
  /* border: 1px solid var(--border-color, #e5e7eb); */
  /* background: var(--surface-color, #fff); */
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.skill-card-pair-name {
  margin: 0;
  font-weight: 600;
  color: var(--text-primary, #1f2937);
}

.skill-card-pair-columns {
  display: flex;
  gap: 16px;
  flex-wrap: nowrap;
}

.skill-card-pair-column {
  display: flex;
  flex-direction: column;
  gap: 8px;
  /* min-width: 180px; */
  flex: 1 1 0;
}

.skill-card-column-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(31, 41, 55, 0.08);
  color: var(--text-secondary, #4b5563);
  font-size: 0.75rem;
  font-weight: 600;
  margin-bottom: 6px;
}

.skill-card--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  width: 100%;
  font-size: 0.9rem;
  color: var(--text-muted, #9ca3af);
  background: var(--surface-muted, #f9fafb);
}

.skill-card--empty .skill-card-column-chip {
  margin-right: 8px;
}

.skill-card {
  width: 100%;
  border-radius: 12px;
  padding: 10px;
}

.skill-card-inline-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(50px, 1fr));
  gap: 12px;
}

.skill-card--enhanced-row {
  width: 100%;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.skill-card-main {
  background: transparent;
}

.skill-card--inline {
  width: fit-content;
  min-width: 200px;
  flex: 1 1 220px;
  max-width: 360px;
}

.skill-card-main {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  width: 100%;
}

.skill-card-area{
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.skill-card-hero {
  display: flex;
  flex-direction: row;
  width: 100%;
  gap: 10px;
}

.skill-main-destruction{
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.skill-card-icon-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  text-align: center;
  width: 55px;
}

/* ===== 스킬 메타 정보 스타일 ===== */
.skill-metadata {
  display: flex;
  flex-direction: row;
  gap: 6px;
  height: fit-content;
  min-width: fit-content;
  /* min-width: 110px; */
}

.skill-metadata-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  height: 22px;
  width:fit-content;
}

.skill-metadata-badge--stagger {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border: 1px solid rgba(239, 68, 68, 0.3);
  text-align: center;
}

.skill-metadata-badge--attack {
  background: rgba(251, 146, 60, 0.1);
  color: #ea580c;
  border: 1px solid rgba(251, 146, 60, 0.3);
}

.skill-metadata-badge--armor {
  background: rgba(59, 130, 246, 0.1);
  color: #2563eb;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.skill-metadata-badge--destruction {
  background: rgba(139, 92, 246, 0.1);
  color: #7c3aed;
  border: 1px solid rgba(139, 92, 246, 0.3);
}

/* ===== 스킬 설명 스타일 ===== */
.skill-description {
  display: inline-flex;
  margin: 0;
  padding: 10px 12px;
  background: var(--surface-muted, #f9fafb);
  border-radius: 8px;
  color: var(--text-secondary, #4b5563);
  font-size: 0.85rem;
  line-height: 1.5;
  text-align: left;
  align-items: center;
  word-break: keep-all;
  white-space: pre-wrap;
  flex: 1 1 auto;
  height: fit-content;
}

.skill-card-icon {
  width: calc(64px - 15px);
  height: calc(64px - 15px);
}

.skill-rune-icon {
  width: fit-content;
  height: fit-content;
}

.skill-icon-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.skill-icon-wrapper:focus-visible .skill-icon-tooltip,
.skill-icon-wrapper:hover .skill-icon-tooltip {
  opacity: 1;
  pointer-events: auto;
  transform: translate(-50%, -4px);
}

.skill-icon-tooltip {
  position: absolute;
  bottom: calc(100% + 10px);
  left: 50%;
  transform: translate(-50%, -10px);
  width: min(340px, 80vw);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s ease, transform 0.15s ease;
  z-index: 10;
}

.skill-tooltip-title {
  margin: 0 0 6px;
  font-size: 0.95rem;
}

.skill-tooltip-desc {
  margin: 0 0 10px;
  font-size: 0.9rem;
  line-height: 1.5;
}

.skill-tooltip-tripods {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 8px;
}

.skill-tooltip-tripods ul {
  list-style: none;
  padding: 0;
  margin: 6px 0 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.skill-tooltip-tripods li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 0.82rem;
}

.tripod-tier-pill {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 32px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.75rem;
  position: absolute;
  z-index: 100;
  text-shadow: -1px 0px white, 0px 1px white, 1px 0px white, 0px -1px white;
}

.tripod-name {
  /* flex: 1; */
  min-width: 130px;
  padding: 0px 10px;
  font-weight: 600;
  color: var(--text-primary, #1f2937);
}

.tripod-slot {
  font-weight: 600;
  color: white;
  border-radius: 999px;
}

.tripod-level {
  font-weight: 600;
  color: #c7d2fe;
}

.tripod-color-1 {
  background-color: #00a1e0
}

.tripod-color-2 {
  background-color: #7cca15
}

.tripod-color-3 {
  background-color: #ff9500
}

.tripod-inline-name {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  white-space: nowrap;
}

.tripod-inline-name .tripod-name {
  width: 100px;
  min-width: 100px;
  max-width: 100px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tripod-detail-inline .tripod-desc {
  text-overflow: ellipsis;
  min-width: 0;
}

.tripod-detail-inline .tripod-desc.tripod-desc--empty {
  visibility: hidden;
}

.tripod-detail-inline .tripod-slot {
  display: flex;
  flex-direction: column;
  text-align: center;
  align-items: center;
  min-height: 25px;
  max-height: 25px;
  min-width: 25px;
  max-width: 25px;
  margin-left: auto;
  padding-top: 2px;
}

.tripod-desc {
  display: block;
  color: var(--text-secondary, #4b5563);
  font-size: 0.8rem;
  line-height: 1.4;
}

.tripod-detail {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 12px;
  background: var(--surface-muted, #f3f4f6);
}

/* 트라이포드 상세 인라인 레이아웃 */
.tripod-detail-inline {
  display: grid;
  grid-template-columns: 40px 1fr;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-radius: 12px;
  background: var(--surface-muted, #f3f4f6);
}

.tripod-inline-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.tripod-detail-icon {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.tripod-detail-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tripod-detail-body {
  gap: 4px;
  width: 100%;
}

/* ===== 룬 툴팁 스타일 ===== */
.skill-tooltip-rune {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 8px;
  margin-top: 8px;
}

.skill-tooltip-rune-body {
  display: flex;
  gap: 8px;
  align-items: center;
}

.skill-tooltip-rune-name {
  margin: 0;
  font-weight: 700;
}

.skill-tooltip-rune-desc {
  margin: 2px 0 0;
  font-size: 0.82rem;
  color: #e5e7eb;
}

.tripod-desc {
  display: block;
  /* color: #e5e7eb; */
  font-size: 0.8rem;
  line-height: 1.4;
  word-break: keep-all;
  white-space: pre-wrap;
}

.skill-card-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.skill-card-name {
  margin: 0;
  font-size: 0.75rem;
  color: var(--text-primary, #1f2937);
  font-weight: 600;
  word-break: keep-all;
}

.skill-card-meta {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  color: var(--text-muted, #6b7280);
  font-size: 0.82rem;
  font-weight: 600;
}

.skill-affix-row {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.skill-affix {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--surface-muted, #f3f4f6);
  color: var(--text-secondary, #374151);
  font-size: 0.82rem;
}

.skill-affix--rune {
  background: rgba(37, 99, 235, 0.08);
}

.skill-affix--gem {
  background: rgba(16, 185, 129, 0.08);
}

.affix-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.affix-icon-image {
  width: 20px;
  height: 20px;
}

.affix-label {
  font-weight: 700;
  color: var(--text-primary, #111827);
}

.affix-text {
  color: var(--text-secondary, #374151);
}

.skill-card-description,
.skill-rune-description,
.gem-card-description {
  margin: 0;
  color: var(--text-primary, #374151);
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
  flex-direction: column;
  gap: 12px;
  width: 100%;
  /* flex: 1 1 260px; */
  align-items: stretch;
}

.skill-tripod-rail--compact {
  flex: 0 0 auto;
  flex-wrap: nowrap;
  align-items: center;
  gap: 8px;
}

.tripod-pill {
  display: inline-flex;
  align-items: center;
  /* gap: 6px; */
  padding: 4px;
  border-radius: 999px;
  background: var(--surface-muted, #f3f4f6);
  font-size: 0.82rem;
  color: var(--text-secondary, #374151);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.tripod-pill-slot {
  font-weight: 700;
}

.tripod-pill-level {
  color: var(--text-muted, #6b7280);
  font-weight: 600;
}

.tripod-pill[class*='slot-1'] {
  background: rgba(96, 165, 250, 0.12);
  border-color: rgba(96, 165, 250, 0.35);
  color: #1d4ed8;
}

.tripod-pill[class*='slot-2'] {
  background: rgba(52, 211, 153, 0.12);
  border-color: rgba(52, 211, 153, 0.35);
  color: #0f766e;
}

.tripod-pill[class*='slot-3'] {
  background: rgba(251, 191, 36, 0.12);
  border-color: rgba(251, 191, 36, 0.35);
  color: #b45309;
}

/* ===== 룬 표시 스타일 ===== */
.skill-rune {
  /* gap: 12px; */
  display: flex;
  /* padding: 4px 10px; */
  border-radius: 12px;
  /* background: rgba(37, 99, 235, 0.07); */
  text-align: center;
  width: fit-content;
}

.skill-rune--inline {
  margin-top: 8px;
  padding: 5px 10px;
}

.skill-tripod-rail .skill-rune--inline {
  margin-top: 0;
  align-items: center;
  text-align: left;
  gap: 8px;
}

.skill-tripod-rail .skill-rune--inline .skill-rune-name {
  display: block;
  width: 107px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.skill-tripod-rail .skill-rune--inline .skill-rune-description {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
  color: var(--text-secondary, #4b5563);
}

/* ===== 보석 표시 스타일 ===== */
.skill-gem-line {
  background: rgba(16, 185, 129, 0.08);
}

.skill-gem-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.skill-gem-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

.skill-gem-line .skill-rune-name {
  color: var(--text-primary, #1f2937);
}

.skill-gem-line .skill-rune-description {
  margin: 0;
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
  min-width: calc(64px - 15px);
}

.skill-card--compact .skill-tripod-rail {
  margin-left: auto;
}

.skill-rune-grade {
  margin: 0;
  font-size: 0.80rem;
  font-weight: 600;
  color: var(--text-muted, #6b7280);
}

.skill-rune-name {
  display: block;
  font-size: 0.8rem;
  color: var(--text-primary, #1f2937);
}

.skill-rune-description {
  font-size: 0.8rem;
}

/* ===== 보석 카드 그리드 (미사용) ===== */
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

.rune-image,
.gem-card-icon {
  width: calc(40px - 15px);
  height: calc(40px - 15px);
}

.gem-card-grade {
  margin: 0;
  font-size: 0.78rem;
  color: var(--text-muted, #6b7280);
}

.gem-card-name {
  font-size: 0.9rem;
  color: var(--text-primary, #1f2937);
}

.gem-card-level {
  font-size: 0.78rem;
  color: var(--accent-color, #2563eb);
  font-weight: 600;
}

.gem-card-skill {
  margin: 0;
  font-size: 0.82rem;
  color: var(--text-secondary, #374151);
}

/* ===== 보석 인라인 행 스타일 ===== */
.skill-gem-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skill-gem-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 5px 10px;
}

.skill-gem-main {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 175px;
  flex-shrink: 0;
}

.skill-gem-icon-img {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  flex-shrink: 0;
}

.skill-gem-icon-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.skill-gem-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.skill-gem-name {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-primary, #1f2937);
  line-height: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.skill-gem-level {
  font-size: 0.7rem;
  color: var(--text-muted, #6b7280);
  line-height: 1;
}

.skill-gem-effect {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 0.8rem;
  color: var(--text-secondary, #374151);
  line-height: 1.4;
  min-width: 0;
}

.skill-gem-effect-main {
  flex: 1;
  min-width: 0;
  word-break: keep-all;
}

@media (max-width: 1100px) {
  .skill-card-pair-columns {
    flex-wrap: wrap;
  }
  .skill-card {
    padding: 12px;
  }
  .skill-card-inline-row {
    grid-template-columns: repeat(3, minmax(70px, 1fr));
  }
}

@media (max-width: 780px) {
  .skill-panel-layout {
    gap: 20px;
  }
  .skill-card-grid {
    gap: 12px;
  }
  .skill-card-inline-row {
    grid-template-columns: repeat(2, minmax(100px, 1fr));
  }
  .skill-card--compact .skill-card-info {
    flex-direction: column;
    align-items: flex-start;
  }
  .skill-card--compact .skill-card-meta {
    flex-wrap: wrap;
  }
}

@media (max-width: 560px) {
  .skill-card {
    padding: 10px;
  }
  .skill-card-main {
    flex-direction: column;
  }
  .skill-card-hero {
    flex-wrap: wrap;
  }
  .skill-card-icon-block {
    width: 48px;
  }
  .skill-card-meta {
    gap: 4px;
  }
}

.skill-gem-extra {
  font-size: 0.75rem;
  color: var(--text-muted, #6b7280);
  line-height: 1.4;
  white-space: nowrap;
  flex-shrink: 0;
  text-align: right;
}

/* ===== 반응형 미디어 쿼리 (모바일) ===== */
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

  .skill-card-grid {
    flex-direction: column;
  }

  .gem-card-grid {
    grid-template-columns: 1fr;
  }

  .skill-card-pair-row {
    grid-template-columns: 1fr;
  }

  .skill-card-pair-columns {
    flex-direction: column;
  }
}
</style>
