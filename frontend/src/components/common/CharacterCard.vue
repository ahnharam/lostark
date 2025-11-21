<template>
  <div class="engraving-card-wrapper">
    <!-- 캐릭터 이미지 -->
    <div class="card-image">
      <LazyImage
        v-if="characterImage"
        :src="characterImage"
        :alt="characterName"
        width="100%"
        height="176"
        imageClass="character-img"
        errorIcon="👤"
      />
    </div>

    <!-- 텍스트 정보 -->
    <div class="card-content">
      <h3 class="character-name">{{ characterName }}</h3>
      <p class="character-sub">{{ characterClass }} • iLv. {{ itemLevel }}</p>
    </div>

    <!-- 각인 배지 행 -->
    <div class="engraving-badges">
      <span 
        v-for="(eng, idx) in engravings" 
        :key="idx" 
        class="engraving-pill"
        :class="{ debuff: eng.isDebuff }"
      >
        {{ eng.name }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import LazyImage from './LazyImage.vue'

interface Engraving {
  name: string
  isDebuff?: boolean
}

interface Props {
  characterImage?: string
  characterName: string
  characterClass: string
  itemLevel: string
  engravings?: Engraving[]
}

withDefaults(defineProps<Props>(), {
  characterImage: '',
  engravings: () => []
})
</script>

<style scoped>
/* 카드 래퍼: Vertical / Gap 12 / Padding 16 / Radius 14 / W 320(Fixed) / H Hug */
.engraving-card-wrapper {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  padding: 0;
  border-radius: var(--radius-lg);
  background: var(--card-bg);
  border: 2px solid var(--border-color);
  overflow: hidden;
  transition: all var(--transition-slow);
  cursor: pointer;
}

.engraving-card-wrapper:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

/* 이미지: H=176(Fixed), W=Fill */
.card-image {
  width: 100%;
  height: 176px;
  background: var(--bg-secondary);
  overflow: hidden;
  flex-shrink: 0;
}

.card-image :deep(.character-img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 텍스트 컨텐츠: Padding 내부 */
.card-content {
  padding: 0 var(--space-lg);
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

/* 이름: 16/Bold */
.character-name {
  font-size: var(--font-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Sub: 12px (직업 • iLv) */
.character-sub {
  font-size: var(--font-xs);
  font-weight: var(--font-normal);
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.4;
}

/* 배지 행: Horizontal / Gap 8 / W,H=Hug */
.engraving-badges {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-sm);
  padding: 0 var(--space-lg) var(--space-lg) var(--space-lg);
}

/* pill: Fill/Radius=999 → "알약모양" */
.engraving-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-xs) var(--space-md);
  background: var(--primary-color);
  color: var(--text-inverse);
  border-radius: var(--radius-full);
  font-size: var(--font-xs);
  font-weight: var(--font-semibold);
  white-space: nowrap;
  transition: all var(--transition-base);
}

.engraving-pill.debuff {
  background: var(--error-color);
}

.engraving-pill:hover {
  transform: scale(1.05);
}

/* 반응형 */
@media (max-width: 640px) {
  .engraving-card-wrapper {
    width: 100%;
    max-width: 320px;
  }
}
</style>
