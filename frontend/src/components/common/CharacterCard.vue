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
  gap: 12px;
  padding: 0;
  border-radius: 14px;
  background: var(--card-bg);
  border: 2px solid var(--border-color);
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.engraving-card-wrapper:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
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
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 이름: 16/Bold */
.character-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Sub: 12px (직업 • iLv) */
.character-sub {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.4;
}

/* 배지 행: Horizontal / Gap 8 / W,H=Hug */
.engraving-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 16px 16px 16px;
}

/* pill: Fill/Radius=999 → "알약모양" */
.engraving-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  background: var(--primary-color);
  color: var(--text-inverse);
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.2s;
}

.engraving-pill.debuff {
  background: #ff6b6b;
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
