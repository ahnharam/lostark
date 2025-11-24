<template>
  <div class="character-overview-card" ref="cardRef">
    <div class="overview-toolbar">
      <button
        type="button"
        class="refresh-button"
        :disabled="loading || !activeCharacter"
        @click="$emit('refresh')"
      >
        갱신하기
      </button>
      <span class="refresh-timestamp">
        {{ lastRefreshedLabel }}
      </span>
    </div>

    <div class="hero-row hero-row--levels" v-if="activeCharacter">
      <div class="profile-stats-grid hero-levels-grid">
        <div class="profile-stat">
          <span>전투력</span>
          <strong>{{ formatCombatPower(activeCharacter?.combatPower) }}</strong>
        </div>
        <div class="profile-stat">
          <span>전투 레벨</span>
          <strong>Lv. {{ formatInteger(activeCharacter?.characterLevel) }}</strong>
        </div>
        <div class="profile-stat">
          <span>아이템 레벨</span>
          <strong>{{ formatItemLevel(activeCharacter?.itemAvgLevel) }}</strong>
        </div>
        <div class="profile-stat">
          <span>원정대 레벨</span>
          <strong>{{ formatInteger(activeCharacter?.expeditionLevel) }}</strong>
        </div>
      </div>
    </div>

    <div class="hero-row hero-row--image">
      <div class="hero-avatar-column">
        <div v-if="hasCharacterImage" class="hero-avatar-controls">
          <button
            type="button"
            class="hero-avatar-btn"
            @click="$emit('expand-hero')"
            :disabled="isHeroImageLarge"
          >
            확대
          </button>
          <button
            type="button"
            class="hero-avatar-btn"
            @click="$emit('shrink-hero')"
            :disabled="!isHeroImageLarge"
          >
            축소
          </button>
          <button
            type="button"
            class="hero-avatar-btn"
            @click="$emit('open-hero-popup')"
          >
            팝업뷰
          </button>
        </div>
        <div class="hero-avatar-wrapper" :class="{ 'is-large': isHeroImageLarge }">
          <LazyImage
            :src="activeCharacter?.characterImage || ''"
            :alt="activeCharacter?.characterName || ''"
            :width="isHeroImageLarge ? 300 : 140"
            :height="isHeroImageLarge ? 350 : 140"
            imageClass="hero-avatar"
            errorIcon="👤"
          />
        </div>
      </div>
      <div class="hero-text">
        <div class="hero-text__header">
          <h2>{{ activeCharacter?.characterName || '캐릭터 이름' }}</h2>
          <button
            v-if="activeCharacter"
            type="button"
            class="favorite-toggle-btn"
            :class="{ 'is-active': isCharacterFavorite }"
            :aria-pressed="isCharacterFavorite"
            @click="$emit('toggle-favorite')"
            aria-label="즐겨찾기 토글"
          >
            <span class="favorite-star" aria-hidden="true">★</span>
          </button>
        </div>
        <span class="hero-title" v-if="activeCharacter?.title">{{ activeCharacter?.title }}</span>
      </div>
    </div>

    <div class="hero-row hero-row--meta">
      <div class="hero-meta-grid">
        <div class="meta-item">
          <span>직업</span>
          <strong>{{ activeCharacter?.characterClassName || '미상' }}</strong>
        </div>
        <div class="meta-item">
          <span>서버</span>
          <strong>{{ activeCharacter?.serverName || '미상' }}</strong>
        </div>
        <div class="meta-item" v-if="activeCharacter?.guildName">
          <span>길드</span>
          <strong>{{ activeCharacter?.guildName }}</strong>
        </div>
        <div class="meta-item" v-if="activeCharacter?.pvpGradeName">
          <span>PVP</span>
          <strong>{{ activeCharacter?.pvpGradeName }}</strong>
        </div>
      </div>
    </div>

    <div class="hero-row hero-row--profile-stats" v-if="displayStats.length">
      <h3>전투 특성</h3>
      <div class="profile-stats-grid">
        <div
          v-for="stat in displayStats"
          :key="`${stat.type}-${stat.value}`"
          class="profile-stat"
        >
          <span>{{ stat.type }}</span>
          <strong>{{ formatProfileStat(stat.value) }}</strong>
        </div>
      </div>
    </div>

    <div class="hero-row hero-row--paradise" v-if="paradiseInfo.power || paradiseInfo.season">
      <h3>낙원</h3>
      <div class="paradise-info">
        <div class="paradise-item" v-if="paradiseInfo.season">
          <span>시즌</span>
          <strong>{{ paradiseInfo.season }}</strong>
        </div>
        <div class="paradise-item" v-if="paradiseInfo.power">
          <span>낙원력</span>
          <strong>{{ formatInteger(paradiseInfo.power) }}</strong>
        </div>
      </div>
    </div>

    <div class="hero-row hero-row--special" v-if="specialEquipments.length">
      <div class="special-header">
        <h3>기타</h3>
      </div>
      <div class="special-grid special-grid--icons">
        <div
          v-for="special in specialEquipments"
          :key="special.item.name"
          class="special-icon-wrapper"
          :class="{ 'is-hovered': hoveredSpecialName === special.item.name }"
          tabindex="0"
          @mouseenter="handleHover(special.item.name)"
          @mouseleave="scheduleHoverClear"
          @focus="handleHover(special.item.name)"
          @blur="scheduleHoverClear"
        >
          <div class="special-icon-box">
            <LazyImage
              v-if="special.item.icon"
              :src="special.item.icon"
              :alt="special.item.name"
              width="56"
              height="56"
              imageClass="special-icon"
              errorIcon="🧭"
              :useProxy="true"
            />
            <div v-else class="special-icon special-icon--fallback" aria-hidden="true">
              {{ special.item.name ? special.item.name[0] : '?' }}
            </div>
          </div>
          <span class="special-label">
            {{ special.label }}
          </span>
          <div
            v-if="hoveredSpecialName === special.item.name"
            class="special-tooltip popup-surface popup-surface--tooltip"
            :style="tooltipWidthStyle"
            role="tooltip"
            @mouseenter="cancelHoverTimeout"
            @mouseleave="scheduleHoverClear"
          >
            <p class="popup-surface__title special-tooltip-title">
              {{ special.item.name }}
            </p>
            <div v-if="special.highlights.length" class="special-highlights">
              <span
                v-for="(line, idx) in special.highlights"
                :key="`${special.item.name}-${idx}`"
              >
                {{ line }}
              </span>
            </div>
            <p v-else class="special-tooltip-empty">추가 설명이 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import LazyImage from './LazyImage.vue'
import type { CharacterProfile, CharacterStat, Equipment } from '@/api/types'

interface ParadiseInfo {
  season?: string
  power?: string
}

interface SpecialEquipmentDisplay {
  item: Equipment
  highlights: string[]
  label: string
}

defineProps<{
  activeCharacter: CharacterProfile | null
  hasCharacterImage: boolean
  isHeroImageLarge: boolean
  isCharacterFavorite: boolean
  lastRefreshedLabel: string
  displayStats: CharacterStat[]
  paradiseInfo: ParadiseInfo
  specialEquipments: SpecialEquipmentDisplay[]
  loading: boolean
  formatCombatPower: (value: unknown) => string
  formatInteger: (value: unknown) => string
  formatItemLevel: (value: unknown) => string
  formatProfileStat: (value: unknown) => string
}>()

defineEmits<{
  (e: 'refresh'): void
  (e: 'expand-hero'): void
  (e: 'shrink-hero'): void
  (e: 'open-hero-popup'): void
  (e: 'toggle-favorite'): void
}>()

const cardRef = ref<HTMLElement | null>(null)
const cardWidth = ref(0)
const hoveredSpecialName = ref<string | null>(null)
const hoverTimeout = ref<number | null>(null)
let observer: ResizeObserver | null = null

const tooltipWidthValue = computed(() => {
  if (!cardWidth.value) return 320
  return Math.max(Math.round(cardWidth.value - 20), 240)
})

const tooltipWidthStyle = computed(() => ({
  '--tooltip-width': `${tooltipWidthValue.value}px`
}))

const observeWidth = () => {
  if (typeof window === 'undefined') return
  observer?.disconnect()
  const el = cardRef.value
  if (el && 'ResizeObserver' in window) {
    observer = new ResizeObserver(entries => {
      const width = entries[0]?.contentRect.width ?? el.getBoundingClientRect().width
      cardWidth.value = width || cardWidth.value
    })
    observer.observe(el)
  } else if (el) {
    cardWidth.value = el.getBoundingClientRect().width
  }
}

const handleHover = (name: string | null) => {
  cancelHoverTimeout()
  hoveredSpecialName.value = name
}

const cancelHoverTimeout = () => {
  if (hoverTimeout.value !== null && typeof window !== 'undefined') {
    window.clearTimeout(hoverTimeout.value)
  }
  hoverTimeout.value = null
}

const scheduleHoverClear = () => {
  if (typeof window === 'undefined') {
    hoveredSpecialName.value = null
    return
  }
  cancelHoverTimeout()
  hoverTimeout.value = window.setTimeout(() => {
    hoveredSpecialName.value = null
    hoverTimeout.value = null
  }, 120)
}

onMounted(() => {
  observeWidth()
})

watch(
  () => cardRef.value,
  () => observeWidth()
)

onBeforeUnmount(() => {
  observer?.disconnect()
  cancelHoverTimeout()
})

</script>

<style scoped>
.character-overview-card {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  background: var(--card-bg);
  border-radius: 20px;
  padding: 15px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
  gap: 25px;
  flex: 0 0 380px;
  height: fit-content;
  overflow: visible;
  max-width: 350px;
}

.overview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.refresh-button {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.refresh-button:hover,
.refresh-button:focus-visible {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-timestamp {
  margin-left: auto;
  color: var(--text-secondary);
  font-size: calc(0.95rem - 2px);
  white-space: nowrap;
}

.hero-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-levels-grid {
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.hero-row--image {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-avatar-column {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.hero-avatar-wrapper {
  width: 140px;
  height: 140px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  background: linear-gradient(145deg, rgba(99, 102, 241, 0.08), rgba(14, 165, 233, 0.06));
}

.hero-avatar-wrapper.is-large {
  width: 300px;
  height: 350px;
}

.hero-avatar-wrapper :deep(.hero-avatar) {
  object-fit: cover;
}

.hero-avatar-controls {
  display: flex;
  align-items: center;
  gap: 6px;
}

.hero-avatar-btn {
  padding: 6px 10px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.hero-avatar-btn + .hero-avatar-btn {
  margin-left: 2px;
}

.hero-avatar-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.hero-avatar-btn:not(:disabled):hover,
.hero-avatar-btn:not(:disabled):focus-visible {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.hero-text {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 180px;
  flex: 1;
}

.hero-text h2 {
  margin: 0;
  font-size: 1.35rem;
}

.hero-text__header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero-title {
  color: var(--text-secondary);
  font-weight: 500;
}

.hero-meta-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
}

.meta-item span {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.meta-item strong {
  display: block;
  font-size: 1rem;
  color: var(--text-primary);
}

.profile-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 10px;
}

.profile-stat {
  padding: 12px 10px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.profile-stat span {
  display: block;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.profile-stat strong {
  display: block;
  font-size: 1.2rem;
  color: var(--text-primary);
}

.hero-row--paradise h3 {
  margin: 0;
}

.paradise-info {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.paradise-item {
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.paradise-item span {
  display: block;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.paradise-item strong {
  display: block;
  color: var(--text-primary);
}

.hero-row--special {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.special-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.special-grid {
  display: grid;
  gap: 10px;
}

.special-grid--icons {
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
}

.special-icon-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  position: relative;
}

.special-icon-box {
  padding: 8px;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.special-icon {
  border-radius: 10px;
}

.special-icon--fallback {
  width: 56px;
  height: 56px;
  display: grid;
  place-items: center;
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 10px;
}

.special-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  text-align: center;
}

.special-tooltip {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  transform: translate(-50%, 6px);
  min-width: var(--tooltip-width, 260px);
  opacity: 1;
  pointer-events: auto;
  z-index: 30;
}

.special-tooltip-title {
  margin: 0 0 6px;
}

.special-highlights {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.95rem;
}

.special-tooltip-empty {
  margin: 0;
  color: var(--text-secondary);
}

@media (max-width: 1200px) {
  .character-overview-card {
    max-width: 100%;
    flex: 1;
  }
}
</style>
