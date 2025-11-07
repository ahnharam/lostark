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
    <div v-else class="modal-container" @click.stop>
        <!-- 헤더: 캐릭터명/직업/레벨 + 닫기 버튼 -->
        <div class="modal-header">
          <div class="character-info-header">
            <LazyImage
              v-if="character.characterImage"
              :src="character.characterImage"
              :alt="character.characterName"
              width="60"
              height="60"
              imageClass="header-avatar"
              errorIcon="👤"
            />
            <div class="header-text">
              <h2>{{ character.characterName }}</h2>
              <p>{{ character.characterClassName }} • iLv. {{ character.itemMaxLevel }}</p>
            </div>
          </div>
        </div>

        <!-- 탭: 장비 / 각인 / 세트효과 -->
        <div class="modal-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            :class="['tab-button', { active: currentTab === tab.id }]"
            @click="currentTab = tab.id"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- 본문 스크롤 영역 -->
        <div class="modal-body">
          <!-- 장비 탭 -->
          <div v-if="currentTab === 'equipment'" class="equipment-grid">
            <!-- 좌: 슬롯/아이콘/베이스스펙 -->
            <div class="equipment-slots">
              <div
                v-for="item in equipment"
                :key="item.name"
                class="equipment-slot"
                @click="selectEquipment(item)"
                :class="{ selected: selectedEquipment?.name === item.name }"
              >
                <LazyImage
                  v-if="item.icon"
                  :src="item.icon"
                  :alt="item.name"
                  width="56"
                  height="56"
                  imageClass="equipment-icon"
                  errorIcon="⚔️"
                />
                <div class="equipment-slot-info">
                  <div class="equipment-type">{{ item.type }}</div>
                  <div class="equipment-name" :class="item.grade">{{ item.name }}</div>
                </div>
              </div>
            </div>

            <!-- 우: 세부 옵션/품질/세트/트포 -->
            <div class="equipment-detail" v-if="selectedEquipment">
              <h3>{{ selectedEquipment.name }}</h3>
              <div class="equipment-grade">{{ selectedEquipment.grade }}</div>
              
              <div class="detail-section">
                <h4>기본 정보</h4>
                <div class="detail-item">
                  <span>부위</span>
                  <span>{{ selectedEquipment.type }}</span>
                </div>
                <div class="detail-item" v-if="selectedEquipment.quality">
                  <span>품질</span>
                  <span class="quality-value">{{ selectedEquipment.quality }}</span>
                </div>
              </div>

              <div class="detail-section" v-if="selectedEquipment.tooltip">
                <h4>상세 정보</h4>
                <div class="tooltip-content" v-html="selectedEquipment.tooltip"></div>
              </div>
            </div>
            <div v-else class="equipment-placeholder">
              <p>장비를 선택하여 상세 정보를 확인하세요</p>
            </div>
          </div>

          <!-- 각인 탭 -->
          <div v-if="currentTab === 'engravings'" class="engravings-list">
            <div
              v-for="eng in engravings"
              :key="eng.name"
              class="engraving-item"
            >
              <LazyImage
                v-if="eng.icon"
                :src="eng.icon"
                :alt="eng.name"
                width="48"
                height="48"
                imageClass="engraving-icon"
                errorIcon="📜"
              />
              <div class="engraving-info">
                <div class="engraving-name">{{ eng.name }}</div>
                <div class="engraving-description">{{ eng.description }}</div>
              </div>
            </div>
          </div>

          <!-- 세트효과 탭 -->
          <div v-if="currentTab === 'sets'" class="sets-content">
            <p class="placeholder-text">세트 효과 정보가 여기에 표시됩니다.</p>
          </div>
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import LazyImage from './LazyImage.vue'
import LoadingSpinner from './LoadingSpinner.vue'

interface Character {
  characterName: string
  characterClassName: string
  characterImage?: string
  itemMaxLevel: string
}

interface Equipment {
  name: string
  type: string
  icon?: string
  grade?: string
  quality?: number
  tooltip?: string
}

interface Engraving {
  name: string
  icon?: string
  description: string
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

const currentTab = ref<'equipment' | 'engravings' | 'sets'>('equipment')
const selectedEquipment = ref<Equipment | null>(null)

const tabs = [
  { id: 'equipment', label: '장비' },
  { id: 'engravings', label: '각인' },
  { id: 'sets', label: '세트효과' }
]

// 첫 장비 자동 선택
watch(() => props.equipment, (newEquipment) => {
  selectedEquipment.value = newEquipment.length > 0 ? newEquipment[0] : null
}, { immediate: true })

const selectEquipment = (item: Equipment) => {
  selectedEquipment.value = item
}

watch(() => props.character, () => {
  currentTab.value = 'equipment'
  selectedEquipment.value = props.equipment.length > 0 ? props.equipment[0] : null
}, { immediate: true })
</script>

<style scoped>
/* 인라인 컨테이너 */
.character-detail-panel {
  width: 100%;
  background: transparent;
}

.detail-placeholder {
  padding: 40px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: var(--card-bg);
  text-align: center;
  color: var(--text-secondary);
}

/* 모달 컨테이너: 960×800 */
.modal-container {
  width: 100%;
  max-width: 960px;
  min-height: 400px;
  background: var(--card-bg);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: 1px solid var(--border-color);
  overflow: hidden;
  margin: 0 auto;
}

/* 헤더 */
.modal-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 24px;
  border-bottom: 2px solid var(--border-color);
  background: var(--bg-secondary);
  gap: 16px;
}

.character-info-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.character-info-header :deep(.header-avatar) {
  border-radius: 12px;
  object-fit: cover;
  border: 2px solid var(--border-color);
}

.header-text h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.header-text p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* 탭 */
.modal-tabs {
  display: flex;
  gap: 8px;
  padding: 16px 24px;
  background: var(--bg-secondary);
  border-bottom: 2px solid var(--border-color);
}

.tab-button {
  padding: 10px 20px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;
}

.tab-button:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.tab-button.active {
  background: var(--primary-color);
  color: white;
}

/* 본문 */
.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* 장비 그리드 (좌우 분할) */
.equipment-grid {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 24px;
  height: 100%;
}

.equipment-slots {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.equipment-slot {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 10px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.equipment-slot:hover {
  background: var(--bg-hover);
  border-color: var(--primary-color);
}

.equipment-slot.selected {
  border-color: var(--primary-color);
  background: var(--primary-color);
  color: white;
}

.equipment-slot.selected .equipment-type,
.equipment-slot.selected .equipment-name {
  color: white;
}

.equipment-slot :deep(.equipment-icon) {
  border-radius: 8px;
  object-fit: contain;
}

.equipment-slot-info {
  flex: 1;
  min-width: 0;
}

.equipment-type {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 4px;
}

.equipment-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 장비 상세 */
.equipment-detail {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 20px;
  overflow-y: auto;
}

.equipment-detail h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.equipment-grade {
  display: inline-block;
  padding: 4px 12px;
  background: var(--primary-color);
  color: white;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 16px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-secondary);
  margin: 0 0 12px 0;
  text-transform: uppercase;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
  font-size: 14px;
}

.detail-item span:first-child {
  color: var(--text-secondary);
}

.detail-item span:last-child {
  color: var(--text-primary);
  font-weight: 600;
}

.quality-value {
  color: var(--primary-color) !important;
}

.tooltip-content {
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.equipment-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: var(--bg-secondary);
  border-radius: 12px;
  color: var(--text-tertiary);
}

/* 각인 리스트 */
.engravings-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.engraving-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 12px;
}

.engraving-item :deep(.engraving-icon) {
  border-radius: 8px;
  flex-shrink: 0;
}

.engraving-info {
  flex: 1;
}

.engraving-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.engraving-description {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
}

/* 세트 컨텐츠 */
.sets-content {
  text-align: center;
  padding: 40px;
  color: var(--text-tertiary);
}

.placeholder-text {
  font-size: 14px;
}

/* 애니메이션 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-active .modal-container,
.modal-fade-leave-active .modal-container {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-container,
.modal-fade-leave-to .modal-container {
  transform: scale(0.9);
  opacity: 0;
}

/* 반응형 */
@media (max-width: 1024px) {
  .modal-container {
    max-width: 100%;
    height: 100%;
    max-height: 100%;
    border-radius: 0;
  }

  .equipment-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .modal-header {
    padding: 16px;
  }

  .modal-tabs {
    padding: 12px 16px;
    overflow-x: auto;
  }

  .modal-body {
    padding: 16px;
  }

  .character-info-header {
    gap: 12px;
  }

  .header-text h2 {
    font-size: 16px;
  }

  .header-text p {
    font-size: 12px;
  }
}
</style>
