<template>
  <Teleport to="body">
    <div v-if="show" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <div class="modal-title-section">
            <IconImage
              v-if="collection.icon"
              :src="collection.icon"
              :alt="collection.type || '수집품'"
              width="56"
              height="56"
              image-class="detail-icon"
              error-icon="📦"
            />
            <div>
              <h2 class="modal-title">{{ collection.type || '수집품' }}</h2>
              <p class="modal-subtitle">보상 단계 {{ collection.collectibleLevel || 0 }}</p>
            </div>
          </div>
          <button class="close-button" @click="$emit('close')" aria-label="닫기">
            ✕
          </button>
        </div>

        <div class="modal-body">
          <!-- 진행 상황 요약 -->
          <div class="progress-summary">
            <div class="progress-info">
              <div class="progress-label">전체 진행도</div>
              <div class="progress-stats">
                <span class="progress-percent">{{ completionRate.toFixed(1) }}%</span>
                <span class="progress-points">
                  {{ formatNumber(collection.point || 0) }} / {{ formatNumber(collection.maxPoint || 0) }}
                </span>
              </div>
            </div>
            <div class="progress-bar-large">
              <div class="progress-bar-fill" :style="{ width: `${completionRate}%` }"></div>
            </div>
          </div>

          <!-- 수집 상태 정보 -->
          <div class="status-cards">
            <div class="status-card collected">
              <div class="status-icon">✓</div>
              <div class="status-info">
                <div class="status-label">수집 완료</div>
                <div class="status-value">{{ collectedItems.length }}개</div>
              </div>
            </div>
            <div class="status-card remaining">
              <div class="status-icon">○</div>
              <div class="status-info">
                <div class="status-label">미수집</div>
                <div class="status-value">{{ uncollectedItems.length }}개</div>
              </div>
            </div>
            <div class="status-card reward">
              <div class="status-icon">★</div>
              <div class="status-info">
                <div class="status-label">현재 보상 단계</div>
                <div class="status-value">Lv. {{ collection.collectibleLevel || 0 }}</div>
              </div>
            </div>
          </div>

          <!-- 수집 목록 (좌우 레이아웃) -->
          <div v-if="hasCollectiblePoints" class="collections-section">
            <div class="section-title">
              <span class="title-icon">📋</span>
              <h3>수집 목록</h3>
            </div>

            <div class="collections-grid">
              <!-- 수집 완료 목록 (왼쪽) -->
              <div class="collection-column collected-column">
                <div class="column-header">
                  <span class="column-icon">✓</span>
                  <h4>수집 완료</h4>
                  <span class="column-count">{{ collectedItems.length }}</span>
                </div>
                <div class="items-list">
                  <div
                    v-for="(item, index) in collectedItems"
                    :key="`collected-${index}`"
                    class="item-card collected-item"
                  >
                    <div class="item-name">{{ item.pointName }}</div>
                    <div class="item-progress">
                      <span class="item-points complete">{{ formatNumber(item.point) }} / {{ formatNumber(item.maxPoint) }}</span>
                      <span class="completion-badge">완료</span>
                    </div>
                  </div>
                  <div v-if="collectedItems.length === 0" class="empty-list">
                    <span class="empty-icon">📭</span>
                    <p>아직 수집한 항목이 없습니다</p>
                  </div>
                </div>
              </div>

              <!-- 미수집 목록 (오른쪽) -->
              <div class="collection-column uncollected-column">
                <div class="column-header">
                  <span class="column-icon">○</span>
                  <h4>미수집</h4>
                  <span class="column-count">{{ uncollectedItems.length }}</span>
                </div>
                <div class="items-list">
                  <div
                    v-for="(item, index) in uncollectedItems"
                    :key="`uncollected-${index}`"
                    class="item-card uncollected-item"
                  >
                    <div class="item-name">{{ item.pointName }}</div>
                    <div class="item-progress">
                      <span class="item-points incomplete">{{ formatNumber(item.point) }} / {{ formatNumber(item.maxPoint) }}</span>
                      <div class="mini-progress-bar">
                        <div class="mini-progress-fill" :style="{ width: `${getItemCompletion(item)}%` }"></div>
                      </div>
                    </div>
                  </div>
                  <div v-if="uncollectedItems.length === 0" class="empty-list">
                    <span class="empty-icon">🎉</span>
                    <p>모든 항목을 수집했습니다!</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- CollectiblePoints가 없는 경우 안내 -->
          <div v-else class="info-notice">
            <div class="notice-icon">ℹ️</div>
            <p>
              이 수집품의 상세 목록 정보는 현재 제공되지 않습니다.
              전체 진행 상황만 확인할 수 있습니다.
            </p>
          </div>
        </div>

        <div class="modal-footer">
          <button class="button-secondary" @click="$emit('close')">닫기</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import IconImage from '@/components/common/IconImage.vue'
import type { Collectible, CollectiblePoint } from '@/api/types'

const props = defineProps<{
  show: boolean
  collection: Collectible
}>()

defineEmits<{
  (event: 'close'): void
}>()

const completionRate = computed(() => {
  const point = props.collection.point || 0
  const maxPoint = props.collection.maxPoint || point
  if (maxPoint === 0) return 0
  return Math.min(100, (point / maxPoint) * 100)
})

const hasCollectiblePoints = computed(() => {
  return props.collection.collectiblePoints && props.collection.collectiblePoints.length > 0
})

// 수집 완료된 항목들 (Point === MaxPoint)
const collectedItems = computed(() => {
  if (!hasCollectiblePoints.value) return []
  return (props.collection.collectiblePoints || [])
    .filter(item => {
      const point = item.point || 0
      const maxPoint = item.maxPoint || 0
      return maxPoint > 0 && point >= maxPoint
    })
    .sort((a, b) => (a.pointName || '').localeCompare(b.pointName || ''))
})

// 미수집 또는 진행 중인 항목들 (Point < MaxPoint)
const uncollectedItems = computed(() => {
  if (!hasCollectiblePoints.value) return []
  return (props.collection.collectiblePoints || [])
    .filter(item => {
      const point = item.point || 0
      const maxPoint = item.maxPoint || 0
      return maxPoint > 0 && point < maxPoint
    })
    .sort((a, b) => {
      // 진행도가 높은 순으로 정렬 (진행 중인 것 우선)
      const progressA = ((a.point || 0) / (a.maxPoint || 1)) * 100
      const progressB = ((b.point || 0) / (b.maxPoint || 1)) * 100
      return progressB - progressA
    })
})

const getItemCompletion = (item: CollectiblePoint) => {
  const point = item.point || 0
  const maxPoint = item.maxPoint || 0
  if (maxPoint === 0) return 0
  return Math.min(100, (point / maxPoint) * 100)
}

const formatNumber = (value?: number) => {
  if (typeof value !== 'number' || Number.isNaN(value)) return '0'
  return value.toLocaleString()
}

const handleOverlayClick = () => {
  // 오버레이 클릭 시 모달 닫기 (선택사항)
  // $emit('close')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
  overflow-y: auto;
}

.modal-container {
  background: var(--card-bg, #1a1a2e);
  border-radius: 1.5rem;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
  width: 100%;
  max-width: 1000px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
}

.modal-title-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.detail-icon {
  width: 56px;
  height: 56px;
  object-fit: contain;
  border-radius: 0.75rem;
  border: 2px solid var(--border-color, rgba(255, 255, 255, 0.1));
}

.modal-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary, #fff);
}

.modal-subtitle {
  margin: 0.25rem 0 0;
  font-size: 0.9rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.close-button {
  width: 36px;
  height: 36px;
  border: none;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.05));
  color: var(--text-primary, #fff);
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-button:hover {
  background: var(--bg-tertiary, rgba(255, 255, 255, 0.1));
  transform: scale(1.05);
}

.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.progress-summary {
  padding: 1.5rem;
  background: linear-gradient(135deg, rgba(128, 178, 255, 0.1), rgba(168, 85, 247, 0.1));
  border-radius: 1rem;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.progress-label {
  font-size: 0.9rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.progress-stats {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.25rem;
}

.progress-percent {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary, #fff);
}

.progress-points {
  font-size: 0.85rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.progress-bar-large {
  width: 100%;
  height: 12px;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.08));
  border-radius: 999px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #80b2ff, #a855f7);
  border-radius: 999px;
  transition: width 0.3s ease;
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.status-card {
  padding: 1rem;
  border-radius: 1rem;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.status-card.collected {
  background: rgba(110, 231, 183, 0.08);
  border-color: rgba(110, 231, 183, 0.2);
}

.status-card.remaining {
  background: rgba(251, 191, 36, 0.08);
  border-color: rgba(251, 191, 36, 0.2);
}

.status-card.reward {
  background: rgba(168, 85, 247, 0.08);
  border-color: rgba(168, 85, 247, 0.2);
}

.status-icon {
  font-size: 1.5rem;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.5rem;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.05));
}

.status-info {
  flex: 1;
}

.status-label {
  font-size: 0.75rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
  margin-bottom: 0.25rem;
}

.status-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary, #fff);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.title-icon {
  font-size: 1.2rem;
}

.section-title h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--text-primary, #fff);
}

.collections-section {
  padding: 1.25rem;
  background: var(--card-bg, rgba(255, 255, 255, 0.02));
  border-radius: 1rem;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
}

.collections-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.collection-column {
  display: flex;
  flex-direction: column;
  min-height: 300px;
}

.column-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.05));
  border-radius: 0.75rem;
  margin-bottom: 1rem;
}

.collected-column .column-header {
  background: rgba(110, 231, 183, 0.1);
  border: 1px solid rgba(110, 231, 183, 0.2);
}

.uncollected-column .column-header {
  background: rgba(251, 191, 36, 0.1);
  border: 1px solid rgba(251, 191, 36, 0.2);
}

.column-icon {
  font-size: 1.1rem;
}

.column-header h4 {
  margin: 0;
  font-size: 1rem;
  color: var(--text-primary, #fff);
  flex: 1;
}

.column-count {
  font-size: 0.85rem;
  padding: 0.25rem 0.6rem;
  background: var(--bg-tertiary, rgba(255, 255, 255, 0.1));
  border-radius: 999px;
  font-weight: 600;
  color: var(--text-primary, #fff);
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  overflow-y: auto;
  max-height: 500px;
  padding-right: 0.5rem;
}

.items-list::-webkit-scrollbar {
  width: 6px;
}

.items-list::-webkit-scrollbar-track {
  background: var(--bg-secondary, rgba(255, 255, 255, 0.05));
  border-radius: 3px;
}

.items-list::-webkit-scrollbar-thumb {
  background: var(--bg-tertiary, rgba(255, 255, 255, 0.2));
  border-radius: 3px;
}

.items-list::-webkit-scrollbar-thumb:hover {
  background: var(--bg-tertiary, rgba(255, 255, 255, 0.3));
}

.item-card {
  padding: 0.75rem 1rem;
  border-radius: 0.75rem;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.03));
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.08));
  transition: all 0.2s;
}

.item-card:hover {
  background: var(--bg-tertiary, rgba(255, 255, 255, 0.06));
  border-color: var(--border-color, rgba(255, 255, 255, 0.15));
}

.collected-item {
  border-left: 3px solid rgba(110, 231, 183, 0.5);
}

.uncollected-item {
  border-left: 3px solid rgba(251, 191, 36, 0.5);
}

.item-name {
  font-size: 0.9rem;
  color: var(--text-primary, #fff);
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.item-progress {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.item-points {
  font-size: 0.8rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.item-points.complete {
  color: #6ee7b7;
}

.item-points.incomplete {
  color: var(--text-secondary, rgba(255, 255, 255, 0.6));
}

.completion-badge {
  font-size: 0.7rem;
  padding: 0.15rem 0.5rem;
  background: rgba(110, 231, 183, 0.2);
  border: 1px solid rgba(110, 231, 183, 0.3);
  border-radius: 999px;
  color: #6ee7b7;
  font-weight: 600;
}

.mini-progress-bar {
  flex: 1;
  height: 6px;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.08));
  border-radius: 999px;
  overflow: hidden;
}

.mini-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #fbbf24, #f59e0b);
  border-radius: 999px;
  transition: width 0.3s ease;
}

.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  text-align: center;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 0.75rem;
}

.empty-list p {
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.6));
}

.info-notice {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  background: rgba(59, 130, 246, 0.08);
  border-radius: 0.75rem;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.notice-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.info-notice p {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
  line-height: 1.5;
}

.modal-footer {
  padding: 1.25rem 1.5rem;
  border-top: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.button-secondary {
  padding: 0.65rem 1.5rem;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.2));
  background: var(--bg-secondary, rgba(255, 255, 255, 0.05));
  color: var(--text-primary, #fff);
  border-radius: 0.5rem;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.2s;
}

.button-secondary:hover {
  background: var(--bg-tertiary, rgba(255, 255, 255, 0.1));
  border-color: rgba(255, 255, 255, 0.3);
}

@media (max-width: 768px) {
  .modal-container {
    max-width: 100%;
    margin: 0;
    border-radius: 1rem;
  }

  .modal-header {
    padding: 1rem;
  }

  .modal-body {
    padding: 1rem;
  }

  .modal-title {
    font-size: 1.2rem;
  }

  .status-cards {
    grid-template-columns: 1fr;
  }

  .collections-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .items-list {
    max-height: 300px;
  }
}

@media (max-width: 768px) {
  .collection-grid {
    grid-template-columns: 1fr;
  }
}

</style>
