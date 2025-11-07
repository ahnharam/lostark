<template>
  <div class="equipment-modal" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <button class="modal-close" @click="$emit('close')">×</button>

      <!-- 아이템 헤더 -->
      <div class="item-header">
        <img v-if="equipment.icon" :src="equipment.icon" :alt="equipment.name" class="item-icon" />
        <div class="item-title-section">
          <h3 :style="{ color: gradeColor }" class="item-name">{{ equipment.name }}</h3>
          <div class="item-meta">
            <span class="item-type">{{ equipment.type }}</span>
            <span class="item-grade" :style="{ color: gradeColor }">{{ equipment.grade }}</span>
          </div>
        </div>
      </div>

      <!-- 품질 바 -->
      <div v-if="parsedData.quality !== undefined" class="quality-section">
        <div class="quality-label">품질</div>
        <div class="quality-bar-container">
          <div
            class="quality-bar"
            :style="{
              width: parsedData.quality + '%',
              backgroundColor: qualityColor
            }"
          >
            <span class="quality-value">{{ parsedData.quality }}</span>
          </div>
        </div>
      </div>

      <!-- 아이템 레벨 -->
      <div v-if="parsedData.itemLevel" class="item-level-section">
        <span class="section-label">아이템 레벨</span>
        <span class="item-level-value">{{ parsedData.itemLevel }}</span>
      </div>

      <!-- 기본 스탯 -->
      <div v-if="parsedData.basicStats && parsedData.basicStats.length > 0" class="stats-section">
        <div class="section-title">기본 효과</div>
        <div class="stat-list">
          <div v-for="(stat, idx) in parsedData.basicStats" :key="`basic-${idx}`" class="stat-item">
            <span class="stat-type">{{ stat.type }}</span>
            <span class="stat-value">{{ stat.value }}</span>
          </div>
        </div>
      </div>

      <!-- 추가 스탯 -->
      <div v-if="parsedData.additionalStats && parsedData.additionalStats.length > 0" class="stats-section">
        <div class="section-title">추가 효과</div>
        <div class="stat-list">
          <div v-for="(stat, idx) in parsedData.additionalStats" :key="`additional-${idx}`" class="stat-item">
            <span class="stat-type">{{ stat.type }}</span>
            <span class="stat-value">{{ stat.value }}</span>
          </div>
        </div>
      </div>

      <!-- 엘릭서 효과 -->
      <div v-if="parsedData.elixirEffects && parsedData.elixirEffects.length > 0" class="elixir-section">
        <div class="section-title">🧪 엘릭서 효과</div>
        <div class="effect-list">
          <div v-for="(effect, idx) in parsedData.elixirEffects" :key="`elixir-${idx}`" class="effect-item elixir">
            {{ effect }}
          </div>
        </div>
      </div>

      <!-- 각인 효과 -->
      <div v-if="parsedData.engravingEffects && parsedData.engravingEffects.length > 0" class="engraving-section">
        <div class="section-title">📜 각인 효과</div>
        <div class="effect-list">
          <div v-for="(effect, idx) in parsedData.engravingEffects" :key="`engraving-${idx}`" class="effect-item engraving">
            {{ effect }}
          </div>
        </div>
      </div>

      <!-- 세트 효과 -->
      <div v-if="parsedData.setEffects && parsedData.setEffects.length > 0" class="set-section">
        <div class="section-title">⚡ 세트 효과</div>
        <div v-for="(setEffect, idx) in parsedData.setEffects" :key="`set-${idx}`" class="set-item">
          <div class="set-name">{{ setEffect.setName }}</div>
          <div class="set-effects">
            <div v-for="(effect, effectIdx) in setEffect.effects" :key="`set-effect-${effectIdx}`" class="set-effect">
              {{ effect }}
            </div>
          </div>
        </div>
      </div>

      <!-- 원본 데이터 (디버그용 - 개발 중에만 표시) -->
      <div v-if="showRawData && parsedData.rawElements" class="raw-data-section">
        <details>
          <summary>원본 데이터 보기</summary>
          <pre>{{ JSON.stringify(parsedData.rawElements, null, 2) }}</pre>
        </details>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { parseTooltip, getQualityColor, getGradeColor, type ParsedTooltip } from '@/utils/tooltipParser'

interface Equipment {
  type: string
  name: string
  icon: string
  grade: string
  tooltip: string
}

interface Props {
  equipment: Equipment
  showRawData?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showRawData: false
})

defineEmits<{
  close: []
}>()

const parsedData = computed<ParsedTooltip>(() => {
  return parseTooltip(props.equipment.tooltip)
})

const qualityColor = computed(() => {
  return getQualityColor(parsedData.value.quality)
})

const gradeColor = computed(() => {
  return getGradeColor(props.equipment.grade)
})
</script>

<style scoped>
.equipment-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--modal-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: var(--modal-bg);
  border-radius: 15px;
  padding: 30px;
  max-width: 600px;
  width: 90%;
  max-height: 85vh;
  overflow-y: auto;
  position: relative;
  box-shadow: var(--shadow-xl);
  animation: slideUp 0.3s;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-close {
  position: absolute;
  top: 10px;
  right: 15px;
  font-size: 2rem;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  line-height: 1;
  transition: color 0.2s, transform 0.2s;
}

.modal-close:hover {
  color: var(--text-primary);
  transform: scale(1.2);
}

/* 아이템 헤더 */
.item-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 2px solid var(--border-color);
}

.item-icon {
  width: 80px;
  height: 80px;
  border-radius: 10px;
  border: 2px solid var(--border-color);
  background: var(--bg-secondary);
}

.item-title-section {
  flex: 1;
}

.item-name {
  margin: 0 0 10px 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.item-meta {
  display: flex;
  gap: 15px;
  align-items: center;
}

.item-type {
  padding: 4px 12px;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border-radius: 5px;
  font-size: 0.9rem;
  font-weight: 600;
}

.item-grade {
  font-weight: 700;
  font-size: 1rem;
}

/* 품질 바 */
.quality-section {
  margin-bottom: 20px;
}

.quality-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-bottom: 8px;
  font-weight: 600;
}

.quality-bar-container {
  width: 100%;
  height: 30px;
  background: var(--bg-secondary);
  border-radius: 15px;
  overflow: hidden;
  position: relative;
  border: 1px solid var(--border-color);
}

.quality-bar {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 12px;
  transition: width 0.5s ease-in-out, background-color 0.3s;
  position: relative;
}

.quality-value {
  font-weight: 700;
  font-size: 0.95rem;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  z-index: 1;
}

/* 아이템 레벨 */
.item-level-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background: var(--bg-secondary);
  border-radius: 8px;
  margin-bottom: 20px;
}

.section-label {
  font-weight: 600;
  color: var(--text-secondary);
}

.item-level-value {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--primary-color);
}

/* 섹션 스타일 */
.stats-section,
.elixir-section,
.engraving-section,
.set-section {
  margin-bottom: 25px;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--border-color-light);
}

/* 스탯 리스트 */
.stat-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 15px;
  background: var(--bg-secondary);
  border-radius: 6px;
  transition: background 0.2s;
}

.stat-item:hover {
  background: var(--bg-hover);
}

.stat-type {
  color: var(--text-secondary);
  font-weight: 500;
}

.stat-value {
  color: var(--text-primary);
  font-weight: 700;
}

/* 효과 리스트 */
.effect-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.effect-item {
  padding: 12px 15px;
  border-radius: 8px;
  font-size: 0.95rem;
  line-height: 1.5;
  border-left: 4px solid transparent;
}

.effect-item.elixir {
  background: rgba(147, 51, 234, 0.1);
  border-left-color: #9333ea;
  color: var(--text-primary);
}

.effect-item.engraving {
  background: rgba(59, 130, 246, 0.1);
  border-left-color: #3b82f6;
  color: var(--text-primary);
}

/* 세트 효과 */
.set-item {
  margin-bottom: 15px;
  padding: 15px;
  background: var(--bg-secondary);
  border-radius: 10px;
  border: 1px solid var(--border-color);
}

.set-name {
  font-weight: 700;
  font-size: 1rem;
  color: var(--primary-color);
  margin-bottom: 10px;
}

.set-effects {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.set-effect {
  padding: 8px 12px;
  background: var(--card-bg);
  border-radius: 6px;
  font-size: 0.9rem;
  color: var(--text-secondary);
  border-left: 3px solid var(--primary-color);
}

/* 원본 데이터 */
.raw-data-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px dashed var(--border-color);
}

.raw-data-section details {
  cursor: pointer;
}

.raw-data-section summary {
  font-weight: 600;
  color: var(--text-secondary);
  padding: 10px;
  background: var(--bg-secondary);
  border-radius: 5px;
  user-select: none;
}

.raw-data-section summary:hover {
  background: var(--bg-hover);
}

.raw-data-section pre {
  margin-top: 10px;
  padding: 15px;
  background: var(--bg-secondary);
  border-radius: 8px;
  overflow-x: auto;
  font-size: 0.85rem;
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

/* 모바일 반응형 */
@media (max-width: 640px) {
  .modal-content {
    width: 95%;
    padding: 20px;
    max-height: 90vh;
  }

  .item-header {
    flex-direction: column;
    text-align: center;
  }

  .item-icon {
    width: 70px;
    height: 70px;
  }

  .item-name {
    font-size: 1.3rem;
  }

  .item-meta {
    justify-content: center;
  }

  .stat-item {
    flex-direction: column;
    gap: 5px;
  }

  .quality-bar {
    justify-content: center;
  }
}
</style>
