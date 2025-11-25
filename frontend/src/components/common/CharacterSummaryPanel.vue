<template>
  <section class="detail-panel summary-panel">
    <div v-if="activeCharacter" class="summary-grid summary-grid--modules summary-grid--stacked">
      <article class="summary-card summary-card--module summary-card--equipment">
        <div class="summary-card__head">
          <p class="summary-eyebrow">장비</p>
          <h4>무기 | 방어구 | 장신구</h4>
        </div>
        <p v-if="detailLoading" class="summary-note">장비 정보를 정리하는 중입니다...</p>
        <p v-else-if="detailError" class="summary-note summary-note--warning">{{ detailError }}</p>
        <div v-else class="equipment-row-list">
          <div v-for="row in equipmentRows" :key="row.key" class="equipment-row">
            <template v-if="row.right?.isBracelet && !row.left">
              <div class="equipment-side equipment-side--bracelet">
                <div class="equipment-icon-stack">
                  <LazyImage
                    :src="row.right.icon"
                    :alt="row.right.name"
                    width="32"
                    height="32"
                    imageClass="summary-icon"
                    errorIcon="💍"
                    :useProxy="true"
                  />
                  <span
                    v-if="row.right.quality && !row.right.isAbilityStone && !row.right.isBracelet"
                    class="equipment-quality-badge"
                    :style="qualityStyle(row.right.quality)"
                  >
                    {{ row.right.quality }}
                  </span>
                  <span class="equipment-slot-label">{{ row.right.typeLabel }}</span>
                  <span v-if="row.right.itemLevel" class="equipment-item-level">{{ row.right.itemLevel }}</span>
                </div>
                <div class="equipment-badge-stack">
                  <div v-if="row.right.effects?.length" class="equipment-effect-badges equipment-effect-badges--grid">
                    <span
                      v-for="(effect, idx) in row.right.effects"
                      :key="`effect-${row.key}-${idx}`"
                      class="equipment-badge equipment-badge--effect"
                      :title="effect.full || effect.label"
                      :style="{
                        backgroundColor: effect.bgColor,
                      }"
                    >
                      {{ effect.label }}
                    </span>
                  </div>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="equipment-side equipment-side--left">
                <template v-if="row.left">
                  <div class="equipment-icon-stack">
                    <LazyImage
                      :src="row.left.icon"
                      :alt="row.left.name"
                      width="32"
                      height="32"
                      imageClass="summary-icon"
                      errorIcon="🗡️"
                      :useProxy="true"
                    />
                    <span
                      v-if="row.left.quality"
                      class="equipment-quality-badge"
                      :style="qualityStyle(row.left.quality)"
                    >
                      {{ row.left.quality }}
                    </span>
                    <span class="equipment-slot-label">{{ row.left.typeLabel }}</span>
                    <span v-if="row.left.itemLevel" class="equipment-item-level">{{ row.left.itemLevel }}</span>
                  </div>
                  <div class="equipment-badge-stack">
                    <span v-if="row.left.enhancement" class="equipment-badge equipment-badge--enhance">
                      강화 {{ row.left.enhancement }}
                    </span>
                    <span v-if="row.left.harmony" class="equipment-badge equipment-badge--harmony">
                      상재 {{ row.left.harmony }}
                    </span>
                    <span v-if="row.left.transcend" class="equipment-badge equipment-badge--transcend">
                      초월 {{ row.left.transcend }}
                    </span>
                  </div>
                </template>
                <p v-else class="equipment-empty">—</p>
              </div>

              <div class="equipment-side">
                <template v-if="row.right">
                  <div class="equipment-icon-stack">
                    <LazyImage
                      :src="row.right.icon"
                      :alt="row.right.name"
                      width="32"
                      height="32"
                      imageClass="summary-icon"
                      errorIcon="💍"
                      :useProxy="true"
                    />
                    <span
                      v-if="row.right.quality && !row.right.isAbilityStone && !row.right.isBracelet"
                      class="equipment-quality-badge"
                      :style="qualityStyle(row.right.quality)"
                    >
                      {{ row.right.quality }}
                    </span>
                    <span class="equipment-slot-label">{{ row.right.typeLabel }}</span>
                    <span v-if="row.right.itemLevel" class="equipment-item-level">{{ row.right.itemLevel }}</span>
                  </div>
                  <div class="equipment-badge-stack">
                    <div
                      v-if="row.right.effects?.length"
                      class="equipment-effect-badges"
                      :class="{ 'equipment-effect-badges--grid': row.right.isBracelet }"
                    >
                      <span
                        v-for="(effect, idx) in row.right.effects"
                        :key="`effect-${row.key}-${idx}`"
                        class="equipment-badge equipment-badge--effect"
                        :title="effect.full || effect.label"
                        :style="{
                          backgroundColor: effect.bgColor,
                        }"
                      >
                        {{ effect.label }}
                      </span>
                    </div>
                  </div>
                </template>
                <p v-else class="equipment-empty"> </p>
              </div>
            </template>
          </div>
        </div>
      </article>

      <article class="summary-card summary-card--module summary-card--ark">
        <div class="ark-section">
          <div class="ark-section__block">
            <div class="summary-card__head">
              <p class="summary-eyebrow">아크 그리드</p>
            </div>
            <p v-if="arkGridLoading" class="summary-note">아크 그리드 정보를 불러오는 중입니다...</p>
            <p v-else-if="arkGridError" class="summary-note summary-note--warning">{{ arkGridError }}</p>
            <div v-else class="ark-core-layout">
              <div
                v-if="arkSummary.coreMatrix.rows.length"
                class="ark-core-matrix"
                :style="{ '--ark-core-col-count': arkSummary.coreMatrix.headers.length }"
              >
                <div class="ark-core-matrix__header">
                  <div class="ark-core-matrix__corner">구분</div>
                  <div
                    v-for="header in arkSummary.coreMatrix.headers"
                    :key="`core-header-${header.key}`"
                    class="ark-core-matrix__header-cell"
                  >
                    {{ header.label }}
                  </div>
                </div>
                <div
                  v-for="row in arkSummary.coreMatrix.rows"
                  :key="`core-row-${row.key}`"
                  class="ark-core-matrix__row"
                >
                  <div class="ark-core-matrix__row-label">{{ row.label }}</div>
                  <div
                    v-for="cell in row.cells"
                    :key="`core-cell-${cell.key}`"
                    class="ark-core-matrix__cell"
                    :class="{ 'ark-core-matrix__cell--empty': !cell.slots.length }"
                  >
                    <div v-if="cell.slots.length" class="ark-core-cell-grid">
                      <div
                        v-for="slot in cell.slots"
                        :key="slot.key"
                        class="ark-core"
                      >
                        <div class="ark-core__thumb">
                          <LazyImage
                            v-if="slot.icon"
                            :src="slot.icon"
                            :alt="slot.name"
                            width="55"
                            height="55"
                            imageClass="ark-core__image"
                            errorIcon="🧩"
                            :useProxy="true"
                          />
                          <div v-else class="ark-core__placeholder" aria-hidden="true">
                            {{ slot.initial }}
                          </div>
                          <span v-if="slot.pointLabel" class="ark-core__point">{{ slot.pointLabel }}</span>
                        </div>
                        <p class="ark-core__name">{{ slot.name }}</p>
                      </div>
                    </div>
                    <p v-else class="summary-note">-</p>
                  </div>
                </div>
              </div>
              <p v-else class="summary-note">
                표시할 아크 그리드 정보가 없습니다.
              </p>
            </div>
          </div>

          <div class="ark-section__block ark-section__block--passive">
            <div class="summary-card__head">
              <p class="summary-eyebrow">아크 패시브</p>
              <h4>{{ arkSummary.passiveTitle || '아크 루트 정보' }}</h4>
            </div>
            <div v-if="arkSummary.passiveMatrix?.length" class="ark-passive-summary">
              <div class="ark-passive-grid">
                <div class="ark-passive-grid-header">
                  <span class="ark-passive-header-cell ark-passive-header-tier">티어</span>
                  <span
                    v-for="point in arkSummary.appliedPoints"
                    :key="point.key"
                    class="ark-passive-header-cell"
                  >
                    {{ point.label }} · {{ point.value }}
                  </span>
                </div>
                <div
                  v-for="row in arkSummary.passiveMatrix.slice(0, 4)"
                  :key="row.id"
                  class="ark-passive-grid-row"
                >
                  <span class="ark-passive-tier">{{ row.label }}</span>
                  <div
                    v-for="section in row.sections"
                    :key="`${row.id}-${section.key}`"
                    class="ark-passive-cell"
                  >
                    <div v-if="section.effects.length" class="ark-passive-cell-list">
                      <div
                        v-for="effect in section.effects"
                        :key="effect.key"
                        class="ark-passive-chip"
                      >
                        <div class="ark-passive-chip-visual">
                          <LazyImage
                            v-if="effect.icon"
                            :src="effect.icon"
                            :alt="effect.name"
                            width="32"
                            height="32"
                            imageClass="summary-icon"
                            errorIcon="🌟"
                            :useProxy="true"
                          />
                          <div v-else class="summary-icon summary-icon--fallback" aria-hidden="true">★</div>
                          <span v-if="effect.levelDisplay" class="ark-passive-level">
                            {{ effect.levelDisplay }}
                          </span>
                        </div>
                      </div>
                    </div>
                    <p v-else class="summary-note"> </p>
                  </div>
                </div>
              </div>
            </div>
            <p v-else class="summary-note">패시브 정보가 없습니다.</p>
          </div>
        </div>
      </article>

      <article class="summary-card summary-card--module summary-card--engravings">
        <div class="summary-card__head">
          <p class="summary-eyebrow">각인</p>
          <!-- <h4>전설 · 유물 · 고대</h4> -->
        </div>
        <div v-if="engravingSummary.length" class="summary-list summary-list--flat">
          <div
            v-for="engrave in engravingSummary"
            :key="engrave.key"
            class="summary-list-item summary-list-item--plain"
          >
            <LazyImage
              v-if="engrave.icon || engravingIcon(engrave.name)"
              :src="engrave.icon || engravingIcon(engrave.name)"
              :alt="engrave.name"
              width="45"
              height="45"
              imageClass="summary-icon"
              errorIcon="🔮"
              :useProxy="true"
            />
            <div v-else class="summary-icon summary-icon--fallback" aria-hidden="true">
              {{ engrave.gradeLabel?.[0] || 'E' }}
            </div>
            <div class="summary-list-text">
              <p class="summary-title">{{ engrave.name }}</p>
              <p class="summary-sub">{{ engrave.gradeLabel }}</p>
            </div>
            <div class="summary-pill-row summary-pill-row--wrap">
              <span v-if="engrave.levelLabel" class="summary-pill summary-pill--primary">
                {{ engrave.levelLabel }}
              </span>
              <span v-if="engrave.craftLabel" class="summary-pill summary-pill--ghost">
                {{ engrave.craftLabel }}
              </span>
            </div>
          </div>
        </div>
        <p v-else class="summary-note">각인 정보가 없습니다.</p>
      </article>

      <article class="summary-card summary-card--module summary-card--skills">
        <div class="summary-card__head">
          <div>
            <p class="summary-eyebrow">스킬</p>
            <h4>핵심 스킬 라인업</h4>
          </div>
          <span class="summary-chip" :class="{ 'summary-chip--muted': !skillHighlights.length }">
            {{ skillHighlights.length ? `${skillHighlights.length}개` : '데이터 없음' }}
          </span>
        </div>
        <p v-if="skillLoading" class="summary-note">스킬 정보를 불러오는 중입니다...</p>
        <p v-else-if="skillError" class="summary-note summary-note--warning">{{ skillError }}</p>
        <ul v-else-if="skillHighlights.length" class="summary-list summary-list--flat">
          <li
            v-for="skill in skillHighlights"
            :key="skill.key"
            class="summary-list-item summary-list-item--plain"
          >
            <LazyImage
              :src="skill.icon"
              :alt="skill.name"
              width="34"
              height="34"
              imageClass="summary-icon"
              errorIcon="🎯"
              :useProxy="true"
            />
            <div class="summary-list-text">
              <p class="summary-title">{{ skill.name }}</p>
              <p class="summary-sub">스킬 포인트 {{ skill.levelLabel }}</p>
              <div class="summary-pill-row summary-pill-row--wrap">
                <span v-if="skill.levelLabel" class="summary-pill summary-pill--primary">
                  {{ skill.levelLabel }}
                </span>
                <span v-if="skill.rune" class="summary-pill summary-pill--accent">
                  {{ skill.rune }}
                </span>
                <span v-if="skill.gemLabel" class="summary-pill summary-pill--ghost">
                  {{ skill.gemLabel }}
                </span>
                <span v-if="skill.tripodLabel" class="summary-pill summary-pill--ghost">
                  {{ skill.tripodLabel }}
                </span>
              </div>
            </div>
          </li>
        </ul>
        <p v-else class="summary-note">요약할 스킬 정보가 없습니다.</p>
      </article>

      <article class="summary-card summary-card--module summary-card--collection">
        <div class="summary-card__head">
          <div>
            <p class="summary-eyebrow">수집</p>
            <h4>주요 포인트</h4>
          </div>
          <span
            class="summary-chip"
            :class="{ 'summary-chip--muted': !collectionSummary.length }"
          >
            {{ collectionSummary.length ? `${collectionSummary.length}종` : '데이터 없음' }}
          </span>
        </div>
        <p v-if="collectiblesLoading" class="summary-note">수집 정보를 정리하는 중입니다...</p>
        <p v-else-if="collectiblesError" class="summary-note summary-note--warning">
          {{ collectiblesError }}
        </p>
        <div v-else-if="collectionSummary.length" class="summary-progress-list summary-progress-list--dense">
          <div
            v-for="item in collectionSummary"
            :key="item.key"
            class="summary-progress summary-progress--compact"
          >
            <div class="summary-progress__head">
              <p class="summary-title">{{ item.name }}</p>
              <span v-if="item.levelLabel" class="summary-pill summary-pill--ghost">
                {{ item.levelLabel }}
              </span>
            </div>
            <div class="summary-progress__bar">
              <span :style="{ width: item.percentLabel }"></span>
            </div>
            <p class="summary-progress__meta">{{ item.pointLabel }}</p>
          </div>
        </div>
        <p v-else class="summary-note">표시할 수집 포인트가 없습니다.</p>
      </article>
    </div>
    <EmptyState
      v-else
      icon="ℹ️"
      title="캐릭터를 먼저 선택하세요"
      description="검색 후 내 정보 간소화 탭에서 핵심 정보를 요약해 드립니다."
    />
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LazyImage from './LazyImage.vue'
import EmptyState from './EmptyState.vue'
import type { CharacterProfile } from '@/api/types'
import { getQualityColor } from '@/utils/tooltipParser'
import { getEngravingIcon } from '@/assets/BuffImage'

const props = defineProps<{
  activeCharacter: CharacterProfile | null
  equipmentSummary: any
  detailLoading: boolean
  detailError: string | null
  arkSummary: any
  arkGridLoading: boolean
  arkGridError: string | null
  skillHighlights: any[]
  skillLoading: boolean
  skillError: string | null
  engravingSummary: any[]
  collectionSummary: any[]
  collectiblesLoading: boolean
  collectiblesError: string | null
}>()

const equipmentRows = computed(() => {
  const left = props.equipmentSummary?.left || []
  const right = props.equipmentSummary?.right || []
  const max = Math.max(left.length, right.length)
  return Array.from({ length: max }).map((_, index) => ({
    key: `equipment-row-${index}`,
    left: left[index],
    right: right[index]
  }))
})

const qualityBadgeBackground = 'var(--quality-badge-bg, rgba(15, 23, 42, 0.333))'

const qualityStyle = (quality?: number | string) => {
  const num = typeof quality === 'number' ? quality : Number(quality)
  const color = getQualityColor(Number.isFinite(num) ? num : undefined)
  const fallbackText = '#f9fafb'
  return {
    color: color || fallbackText,
    backgroundColor: qualityBadgeBackground,
    borderColor: color ? `${color}55` : 'rgba(255,255,255,0.15)'
  }
}

const engravingIcon = (name?: string) => getEngravingIcon(name || '')
</script>
