<template>
  <div class="equipment-modal" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <!-- 헤더: 캐릭터명/직업/레벨 + 닫기 -->
      <div class="modal-header">
        <div class="character-info">
          <h2 class="character-name">{{ characterName }}</h2>
          <span class="character-meta">{{ characterClass }} • Lv.{{ characterLevel }}</span>
        </div>
        <button class="modal-close" @click="$emit('close')">×</button>
      </div>

      <!-- 탭 메뉴: 장비 / 각인 / 세트효과 -->
      <div class="modal-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          :class="['tab-button', { active: activeTab === tab.id }]"
          @click="activeTab = tab.id"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 탭 컨텐츠 -->
      <div class="modal-body">
        <!-- 장비 탭 -->
        <div v-show="activeTab === 'equipment'" class="tab-content equipment-layout">
          <!-- 좌측: 슬롯/아이콘/베이스스펙 -->
          <div class="left-section">
            <div class="item-header">
              <LazyImage
                v-if="equipment.icon"
                :src="equipment.icon"
                :alt="equipment.name"
                width="80"
                height="80"
                imageClass="item-icon"
                errorIcon="⚔️"
                :lazy="false"
              />
              <div class="item-title-section">
                <h3 :style="{ color: gradeColor }" class="item-name">{{ equipment.name }}</h3>
                <div class="item-meta">
                  <span class="item-type">{{ equipment.type }}</span>
                  <span class="item-grade" :style="{ color: gradeColor }">{{ equipment.grade }}</span>
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
          </div>

          <!-- 우측: 세부 옵션/품질/세트/트포 -->
          <div class="right-section">
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
          </div>
        </div>

        <!-- 각인 탭 -->
        <div v-show="activeTab === 'engravings'" class="tab-content">
          <div class="section-title">활성 각인 효과</div>
          <div v-if="parsedData.engravingEffects && parsedData.engravingEffects.length > 0" class="effect-list">
            <div v-for="(effect, idx) in parsedData.engravingEffects" :key="`engraving-tab-${idx}`" class="effect-item engraving">
              {{ effect }}
            </div>
          </div>
          <div v-else class="empty-state">
            각인 정보가 없습니다.
          </div>
        </div>

        <!-- 세트효과 탭 -->
        <div v-show="activeTab === 'setEffects'" class="tab-content">
          <div v-if="parsedData.setEffects && parsedData.setEffects.length > 0">
            <div v-for="(setEffect, idx) in parsedData.setEffects" :key="`set-${idx}`" class="set-item">
              <div class="set-name">{{ setEffect.setName }}</div>
              <div class="set-effects">
                <div v-for="(effect, effectIdx) in setEffect.effects" :key="`set-effect-${effectIdx}`" class="set-effect">
                  {{ effect }}
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            세트 효과가 없습니다.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { parseTooltip, getQualityColor, getGradeColor, type ParsedTooltip } from '@/utils/tooltipParser'
import LazyImage from './LazyImage.vue'

interface Equipment {
  type: string
  name: string
  icon: string
  grade: string
  tooltip: string
}

interface Props {
  equipment: Equipment
  characterName?: string
  characterClass?: string
  characterLevel?: string | number
  showRawData?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  characterName: '캐릭터명',
  characterClass: '직업',
  characterLevel: '0',
  showRawData: false
})

defineEmits<{
  close: []
}>()

// 탭 설정
const tabs = [
  { id: 'equipment', label: '장비' },
  { id: 'engravings', label: '각인' },
  { id: 'setEffects', label: '세트효과' }
]

const activeTab = ref('equipment')

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

/* 960×800 모달 크기 */
.modal-content {
  background: var(--modal-bg);
  border-radius: 15px;
  padding: 0;
  width: 960px;
  height: 800px;
  position: relative;
  box-shadow: var(--shadow-xl);
  animation: slideUp 0.3s;
  display: flex;
  flex-direction: column;
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

/* 헤더 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  border-bottom: 2px solid var(--border-color);
}

.character-info {
  flex: 1;
}

.character-name {
  margin: 0 0 5px 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.character-meta {
  font-size: 0.95rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.modal-close {
  font-size: 2rem;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  line-height: 1;
  transition: color 0.2s, transform 0.2s;
  padding: 0 10px;
}

.modal-close:hover {
  color: var(--text-primary);
  transform: scale(1.2);
}

/* 탭 메뉴 */
.modal-tabs {
  display: flex;
  border-bottom: 2px solid var(--border-color);
  background: var(--bg-secondary);
}

.tab-button {
  flex: 1;
  padding: 15px 20px;
  background: none;
  border: none;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab-button:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.tab-button.active {
  color: var(--primary-color);
  background: var(--modal-bg);
}

.tab-button.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--primary-color);
}

/* 탭 본문 */
.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
}

.tab-content {
  animation: fadeIn 0.3s;
}

/* 장비 탭 레이아웃: 좌우 분할 */
.equipment-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.left-section,
.right-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 아이템 헤더 */
.item-header {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: var(--bg-secondary);
  border-radius: 10px;
}

:deep(.item-icon) {
  border-radius: 8px;
  object-fit: cover;
}

.item-header :deep(.lazy-image-wrapper) {
  border-radius: 8px;
  border: 2px solid var(--border-color);
  background: var(--card-bg);
}

.item-title-section {
  flex: 1;
}

.item-name {
  margin: 0 0 8px 0;
  font-size: 1.2rem;
  font-weight: 700;
}

.item-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.item-type {
  padding: 3px 10px;
  background: var(--card-bg);
  color: var(--text-secondary);
  border-radius: 5px;
  font-size: 0.85rem;
  font-weight: 600;
}

.item-grade {
  font-weight: 700;
  font-size: 0.9rem;
}

/* 품질 바 */
.quality-section {
  background: var(--bg-secondary);
  padding: 15px;
  border-radius: 10px;
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
  background: var(--card-bg);
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
}

.quality-value {
  font-weight: 700;
  font-size: 0.95rem;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

/* 아이템 레벨 */
.item-level-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background: var(--bg-secondary);
  border-radius: 8px;
}

.section-label {
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.item-level-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--primary-color);
}

/* 섹션 스타일 */
.stats-section,
.elixir-section,
.engraving-section,
.set-section {
  background: var(--bg-secondary);
  padding: 15px;
  border-radius: 10px;
}

.section-title {
  font-size: 1rem;
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
  padding: 10px 12px;
  background: var(--card-bg);
  border-radius: 6px;
  transition: background 0.2s;
}

.stat-item:hover {
  background: var(--bg-hover);
}

.stat-type {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 0.9rem;
}

.stat-value {
  color: var(--text-primary);
  font-weight: 700;
  font-size: 0.9rem;
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
  font-size: 0.9rem;
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

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-tertiary);
  font-size: 0.95rem;
}

/* 스크롤바 */
.modal-body::-webkit-scrollbar {
  width: 8px;
}

.modal-body::-webkit-scrollbar-track {
  background: var(--bg-secondary);
}

.modal-body::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

/* 모바일 반응형 */
@media (max-width: 1024px) {
  .modal-content {
    width: 95%;
    height: 90vh;
  }

  .equipment-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .modal-header {
    padding: 15px 20px;
  }

  .character-name {
    font-size: 1.2rem;
  }

  .modal-tabs {
    overflow-x: auto;
  }

  .tab-button {
    padding: 12px 15px;
    font-size: 0.9rem;
    white-space: nowrap;
  }

  .modal-body {
    padding: 20px;
  }
}
</style>
