<template>
  <div class="engraving-card">
    <!-- 이미지 (고정 높이 176px) -->
    <div class="card-image-wrapper">
      <LazyImage
        v-if="engraving.icon"
        :src="engraving.icon"
        :alt="engraving.name"
        width="288"
        height="176"
        imageClass="card-image"
        errorIcon="📜"
      />
      <div
        v-if="parsedData.level > 0"
        class="level-badge"
        :style="{ backgroundColor: levelColor }"
      >
        {{ levelText }}
      </div>
    </div>

    <!-- 텍스트 정보 -->
    <div class="card-content">
      <!-- 이름 (16px Bold) -->
      <h3 class="card-title" :class="{ debuff: parsedData.isDebuff }">
        {{ parsedData.name || engraving.name }}
      </h3>

      <!-- Sub: 직업 • iLv (12px) -->
      <div class="card-subtitle">
        <span>{{ engraving.class || '공용' }}</span>
        <span v-if="engraving.itemLevel">• Lv.{{ engraving.itemLevel }}</span>
      </div>

      <!-- 각인 뱃지 행 (pill 형태) -->
      <div class="badge-row">
        <span
          v-if="parsedData.effectValue"
          class="badge pill"
          :class="{ debuff: parsedData.isDebuff }"
        >
          {{ parsedData.effectValue }}
        </span>
        <span
          v-for="(badge, idx) in additionalBadges"
          :key="`badge-${idx}`"
          class="badge pill"
        >
          {{ badge }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { parseEngravingDescription, getEngravingLevelColor, getEngravingLevelText, type ParsedEngraving } from '@/utils/engravingParser'
import LazyImage from './LazyImage.vue'

interface Engraving {
  name: string
  icon: string
  description: string
  class?: string
  itemLevel?: string | number
  badges?: string[]
}

interface Props {
  engraving: Engraving
  showDescription?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showDescription: false
})

const parsedData = computed<ParsedEngraving>(() => {
  return parseEngravingDescription(props.engraving.description)
})

const levelColor = computed(() => {
  return getEngravingLevelColor(parsedData.value.level, parsedData.value.isDebuff)
})

const levelText = computed(() => {
  return getEngravingLevelText(parsedData.value.level)
})

const additionalBadges = computed(() => {
  return props.engraving.badges || []
})
</script>

<style scoped>
/* 카드 프레임: Vertical / Gap 12 / Padding 16 / Radius 14 / W 320(Fixed) / H Hug */
.engraving-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  width: 320px;
  background: var(--card-bg);
  border-radius: 14px;
  transition: all 0.3s;
  border: 2px solid transparent;
  box-shadow: var(--shadow-sm);
}

.engraving-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

/* 이미지: H=176(Fixed), W=Fill */
.card-image-wrapper {
  position: relative;
  width: 100%;
  height: 176px;
  border-radius: 10px;
  overflow: hidden;
  background: var(--bg-secondary);
}

:deep(.card-image) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-image-wrapper :deep(.lazy-image-wrapper) {
  width: 100%;
  height: 100%;
  display: block;
}

/* 레벨 배지 */
.level-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  min-width: 36px;
  height: 24px;
  padding: 0 10px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 700;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(4px);
}

/* 카드 컨텐츠 */
.card-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 이름: 16px Bold */
.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-title.debuff {
  color: #ff6b6b;
}

/* Sub: 직업 • iLv (12px) */
.card-subtitle {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
  line-height: 1.4;
}

/* 배지 행: Horizontal / Gap 8 / W,H=Hug */
.badge-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

/* pill: Fill/Radius=999 → 알약 모양 */
.badge.pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: var(--primary-color);
  color: white;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
  white-space: nowrap;
  transition: all 0.2s;
}

.badge.pill:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.badge.pill.debuff {
  background: #ff6b6b;
}

/* 모바일 반응형 */
@media (max-width: 640px) {
  .engraving-card {
    width: 100%;
    max-width: 320px;
  }

  .card-image-wrapper {
    height: 160px;
  }

  .card-title {
    font-size: 15px;
  }

  .card-subtitle {
    font-size: 11px;
  }

  .badge.pill {
    font-size: 11px;
    padding: 5px 10px;
  }
}
</style>
