<template>
  <div class="engraving-card" :class="{ debuff: parsedData.isDebuff }">
    <div class="engraving-icon-wrapper">
      <img v-if="engraving.icon" :src="engraving.icon" :alt="engraving.name" class="engraving-icon" />
      <div
        v-if="parsedData.level > 0"
        class="engraving-level-badge"
        :style="{ backgroundColor: levelColor }"
      >
        {{ levelText }}
      </div>
    </div>

    <div class="engraving-info">
      <div class="engraving-name" :class="{ debuff: parsedData.isDebuff }">
        {{ parsedData.name || engraving.name }}
      </div>

      <div v-if="parsedData.effectValue" class="engraving-effect">
        {{ parsedData.effectValue }}
      </div>

      <!-- 설명 툴팁 (호버 시 표시) -->
      <div v-if="showDescription && engraving.description" class="engraving-description">
        {{ engraving.description }}
      </div>
    </div>

    <!-- 레벨 인디케이터 (3개의 점) -->
    <div v-if="parsedData.level > 0" class="level-indicators">
      <div
        v-for="i in 3"
        :key="i"
        class="level-dot"
        :class="{ active: i <= parsedData.level, debuff: parsedData.isDebuff }"
        :style="i <= parsedData.level ? { backgroundColor: levelColor } : {}"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { parseEngravingDescription, getEngravingLevelColor, getEngravingLevelText, type ParsedEngraving } from '@/utils/engravingParser'

interface Engraving {
  name: string
  icon: string
  description: string
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
</script>

<style scoped>
.engraving-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: var(--bg-secondary);
  border-radius: 10px;
  transition: all 0.3s;
  border: 2px solid transparent;
  position: relative;
}

.engraving-card:hover {
  background: var(--bg-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.engraving-card.debuff {
  border-left: 4px solid #ff6b6b;
  background: rgba(255, 107, 107, 0.05);
}

.engraving-card.debuff:hover {
  border-color: #ff6b6b;
}

.engraving-icon-wrapper {
  position: relative;
  flex-shrink: 0;
}

.engraving-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  border: 2px solid var(--border-color);
  background: var(--card-bg);
  display: block;
}

.engraving-level-badge {
  position: absolute;
  bottom: -5px;
  right: -5px;
  min-width: 32px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  color: white;
  border: 2px solid var(--card-bg);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.engraving-info {
  flex: 1;
  min-width: 0;
}

.engraving-name {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.engraving-name.debuff {
  color: #ff6b6b;
}

.engraving-effect {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--primary-color);
}

.engraving-description {
  margin-top: 8px;
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.4;
  padding: 8px;
  background: var(--card-bg);
  border-radius: 5px;
  border-left: 3px solid var(--primary-color);
}

/* 레벨 인디케이터 */
.level-indicators {
  display: flex;
  gap: 5px;
  align-items: center;
}

.level-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--border-color);
  transition: all 0.3s;
}

.level-dot.active {
  transform: scale(1.2);
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.3);
}

.level-dot.debuff {
  background: rgba(255, 107, 107, 0.3);
}

/* 모바일 반응형 */
@media (max-width: 640px) {
  .engraving-card {
    padding: 12px;
    gap: 12px;
  }

  .engraving-icon {
    width: 50px;
    height: 50px;
  }

  .engraving-name {
    font-size: 0.95rem;
  }

  .engraving-effect {
    font-size: 0.85rem;
  }

  .level-indicators {
    flex-direction: column;
    gap: 3px;
  }

  .level-dot {
    width: 8px;
    height: 8px;
  }
}
</style>
