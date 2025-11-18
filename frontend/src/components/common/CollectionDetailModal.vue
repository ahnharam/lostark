<template>
  <Teleport to="body">
    <div v-if="show" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <div class="modal-title-section">
            <LazyImage
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
                <div class="status-label">수집 포인트</div>
                <div class="status-value">{{ formatNumber(collection.point || 0) }}</div>
              </div>
            </div>
            <div class="status-card remaining">
              <div class="status-icon">○</div>
              <div class="status-info">
                <div class="status-label">남은 포인트</div>
                <div class="status-value">{{ formatNumber(remainingPoints) }}</div>
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

          <!-- 다음 보상 정보 -->
          <div v-if="remainingPoints > 0" class="next-reward-section">
            <div class="section-title">
              <span class="title-icon">🎯</span>
              <h3>다음 목표</h3>
            </div>
            <div class="next-reward-card">
              <div class="reward-level">Lv. {{ (collection.collectibleLevel || 0) + 1 }}</div>
              <div class="reward-progress">
                <span>{{ remainingPoints }} 포인트 남음</span>
                <span class="reward-hint">계속 수집하여 다음 보상을 획득하세요!</span>
              </div>
            </div>
          </div>

          <!-- 완료 메시지 -->
          <div v-else class="completion-message">
            <div class="completion-icon">🎉</div>
            <h3>수집 완료!</h3>
            <p>이 수집품의 모든 포인트를 획득했습니다.</p>
          </div>

          <!-- 상세 정보 섹션 -->
          <div class="detail-section">
            <div class="section-title">
              <span class="title-icon">📊</span>
              <h3>상세 정보</h3>
            </div>
            <div class="detail-list">
              <div class="detail-item">
                <span class="detail-label">수집품 유형</span>
                <span class="detail-value">{{ collection.type || '알 수 없음' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">현재 포인트</span>
                <span class="detail-value">{{ formatNumber(collection.point || 0) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">최대 포인트</span>
                <span class="detail-value">{{ formatNumber(collection.maxPoint || 0) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">보상 단계</span>
                <span class="detail-value">{{ collection.collectibleLevel || 0 }}</span>
              </div>
            </div>
          </div>

          <!-- 안내 메시지 -->
          <div class="info-notice">
            <div class="notice-icon">ℹ️</div>
            <p>
              개별 수집 아이템의 상세 목록은 로스트아크 게임 내에서 확인할 수 있습니다.
              현재 화면에서는 전체 진행 상황을 표시합니다.
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
import LazyImage from '@/components/common/LazyImage.vue'
import type { Collectible } from '@/api/types'

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

const remainingPoints = computed(() => {
  const point = props.collection.point || 0
  const maxPoint = props.collection.maxPoint || point
  return Math.max(0, maxPoint - point)
})

const formatNumber = (value?: number) => {
  if (typeof value !== 'number' || Number.isNaN(value)) return '0'
  return value.toLocaleString()
}

const handleOverlayClick = () => {
  // 오버레이 클릭 시 모달 닫기
  // @click.stop으로 모달 내부 클릭은 전파되지 않음
  // 필요시 주석 처리하여 오버레이 클릭으로 닫기 비활성화
  // props.$emit('close')
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
  max-width: 600px;
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

.next-reward-section {
  padding: 1.25rem;
  background: var(--card-bg, rgba(255, 255, 255, 0.04));
  border-radius: 1rem;
  border: 1px dashed var(--border-color, rgba(255, 255, 255, 0.15));
}

.next-reward-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.03));
  border-radius: 0.75rem;
}

.reward-level {
  font-size: 1.5rem;
  font-weight: 700;
  color: #fbbf24;
  padding: 0.5rem 1rem;
  background: rgba(251, 191, 36, 0.1);
  border-radius: 0.5rem;
}

.reward-progress {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.reward-progress span {
  font-size: 0.9rem;
  color: var(--text-primary, #fff);
}

.reward-hint {
  font-size: 0.8rem !important;
  color: var(--text-secondary, rgba(255, 255, 255, 0.6)) !important;
}

.completion-message {
  text-align: center;
  padding: 2rem 1rem;
  background: linear-gradient(135deg, rgba(110, 231, 183, 0.1), rgba(59, 130, 246, 0.1));
  border-radius: 1rem;
  border: 1px solid rgba(110, 231, 183, 0.3);
}

.completion-icon {
  font-size: 3rem;
  margin-bottom: 0.75rem;
}

.completion-message h3 {
  margin: 0 0 0.5rem;
  font-size: 1.3rem;
  color: #6ee7b7;
}

.completion-message p {
  margin: 0;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.detail-section {
  padding: 1.25rem;
  background: var(--card-bg, rgba(255, 255, 255, 0.04));
  border-radius: 1rem;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.03));
  border-radius: 0.5rem;
}

.detail-label {
  font-size: 0.9rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.detail-value {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-primary, #fff);
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

@media (max-width: 640px) {
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
}
</style>
