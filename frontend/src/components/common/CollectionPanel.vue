<template>
  <section class="collection-panel">
    <div class="section-header-bar">
      <div>
        <h3>수집 현황</h3>
        <p class="section-subtitle">모코코, 섬의 마음 등 컬렉션 진행도를 한눈에 확인하세요.</p>
      </div>
      <button
        v-if="errorMessage"
        type="button"
        class="refresh-button"
        @click="$emit('retry')"
      >
        새로고침
      </button>
    </div>

    <div v-if="loading" class="collection-state">
      <LoadingSpinner message="수집 정보를 불러오는 중입니다..." />
    </div>

    <ErrorMessage
      v-else-if="errorMessage"
      :title="'수집 정보를 불러오지 못했습니다.'"
      :message="errorMessage"
      type="error"
      :retry="true"
      @retry="$emit('retry')"
    />

    <EmptyState
      v-else-if="!hasData"
      icon="📦"
      title="수집 정보가 없습니다"
      :description="emptyMessage"
    />

    <template v-else>
      <div class="collection-summary-grid">
        <div class="collection-summary-card">
          <span class="summary-label">총 수집 포인트</span>
          <strong class="summary-value">{{ formatNumber(totalPoints) }}</strong>
          <span class="summary-hint">최대 {{ formatNumber(totalMaxPoints) }} 포인트</span>
        </div>
        <div class="collection-summary-card">
          <span class="summary-label">완료율</span>
          <strong class="summary-value">{{ completionRate.toFixed(1) }}%</strong>
          <div class="summary-progress">
            <div class="summary-progress-track">
              <div class="summary-progress-fill" :style="{ width: `${completionRate}%` }"></div>
            </div>
            <span>{{ formatNumber(totalPoints) }} / {{ formatNumber(totalMaxPoints) }}</span>
          </div>
        </div>
        <div class="collection-summary-card">
          <span class="summary-label">평균 보상 단계</span>
          <strong class="summary-value">Lv. {{ averageLevel }}</strong>
          <span class="summary-hint">{{ completedCount }}개 완료</span>
        </div>
      </div>

      <div class="collection-insights" v-if="insights.length">
        <article
          v-for="insight in insights"
          :key="insight.id"
          class="insight-card"
        >
          <div class="insight-label">{{ insight.label }}</div>
          <strong class="insight-name">{{ insight.name }}</strong>
          <p class="insight-meta">{{ insight.meta }}</p>
        </article>
      </div>

      <div class="collection-grid">
        <article
          v-for="item in decoratedCollectibles"
          :key="item.collectibleId || item.type"
          class="collection-card"
        >
          <div class="collection-card-header">
            <LazyImage
              :src="item.icon || ''"
              :alt="item.label"
              width="48"
              height="48"
              image-class="collection-icon"
              error-icon="📦"
            />
            <div>
              <p class="collection-name">{{ item.label }}</p>
              <span class="collection-level">{{ item.levelLabel }}</span>
            </div>
            <span class="collection-rank" :class="{ complete: item.completion >= 100 }">
              {{ item.completion >= 100 ? '완료' : `${item.completion.toFixed(1)}%` }}
            </span>
          </div>
          <div class="collection-progress">
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: `${item.completion}%` }"></div>
            </div>
            <span class="progress-value">
              {{ formatNumber(item.point) }} / {{ formatNumber(item.maxPoint) }}
            </span>
          </div>
          <p class="collection-meta">
            <span>포인트 비중 {{ item.share }}%</span>
            <span>다음 보상 {{ item.nextLevelLabel }}</span>
          </p>
        </article>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ErrorMessage from '@/components/common/ErrorMessage.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LazyImage from '@/components/common/LazyImage.vue'
import type { Collectible } from '@/api/types'

const props = defineProps<{
  collectibles: Collectible[]
  loading: boolean
  errorMessage?: string | null
  characterName?: string
}>()

defineEmits<{
  (event: 'retry'): void
}>()

const hasData = computed(() => props.collectibles && props.collectibles.length > 0)

const decoratedCollectibles = computed(() => {
  const totalPoints = props.collectibles.reduce((sum, item) => sum + (item.point ?? 0), 0)
  return props.collectibles
    .map(item => {
      const point = item.point ?? 0
      const maxPoint = item.maxPoint ?? point
      const completion = maxPoint > 0 ? Math.min(100, (point / maxPoint) * 100) : 0
      return {
        ...item,
        point,
        maxPoint,
        completion,
        label: item.type || '수집품',
        levelLabel: item.collectibleLevel ? `보상 단계 ${item.collectibleLevel}` : '보상 정보 없음',
        nextLevelLabel:
          item.collectibleLevel && maxPoint
            ? `Lv.${item.collectibleLevel + 1} 까지 ${Math.max(maxPoint - point, 0)}`
            : '다음 목표 없음',
        share: totalPoints > 0 ? ((point / totalPoints) * 100).toFixed(1) : '0.0'
      }
    })
    .sort((a, b) => b.point - a.point)
})

const totalPoints = computed(() => decoratedCollectibles.value.reduce((sum, item) => sum + item.point, 0))
const totalMaxPoints = computed(() => decoratedCollectibles.value.reduce((sum, item) => sum + item.maxPoint, 0))
const completionRate = computed(() =>
  totalMaxPoints.value > 0 ? (totalPoints.value / totalMaxPoints.value) * 100 : 0
)
const completedCount = computed(() => decoratedCollectibles.value.filter(item => item.completion >= 100).length)
const averageLevel = computed(() => {
  const levels = decoratedCollectibles.value
    .map(item => item.collectibleLevel)
    .filter((value): value is number => typeof value === 'number' && value > 0)
  if (!levels.length) return '0.0'
  const avg = levels.reduce((sum, level) => sum + level, 0) / levels.length
  return avg.toFixed(1)
})

const formatNumber = (value?: number) => {
  if (typeof value !== 'number' || Number.isNaN(value)) return '0'
  return value.toLocaleString()
}

const insights = computed(() => {
  if (!decoratedCollectibles.value.length) return []
  const sortedByCompletion = [...decoratedCollectibles.value].sort((a, b) => b.completion - a.completion)
  const best = sortedByCompletion[0]
  const worst = sortedByCompletion[sortedByCompletion.length - 1]
  const result = []
  if (best) {
    result.push({
      id: 'best',
      label: '가장 앞선 컬렉션',
      name: best.label,
      meta: `${best.completion.toFixed(1)}% · ${formatNumber(best.point)} / ${formatNumber(best.maxPoint)}`
    })
  }
  if (worst && worst !== best) {
    result.push({
      id: 'focus',
      label: '집중 필요',
      name: worst.label,
      meta: `${worst.share}% 비중 · 다음 목표 ${worst.nextLevelLabel}`
    })
  }
  return result
})

const emptyMessage = computed(() => {
  if (!props.characterName) return '캐릭터를 검색하면 수집 진행도를 확인할 수 있어요.'
  return `'${props.characterName}'의 수집 데이터가 아직 없습니다. 잠시 후 다시 시도해 주세요.`
})
</script>

<style scoped>
.section-header-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.section-header-bar h3 {
  margin: 0;
  font-size: 1.2rem;
  color: var(--text-primary, #fff);
}

.section-subtitle {
  margin: 0.2rem 0 0;
  font-size: 0.85rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.collection-panel {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.collection-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2.5rem 0;
}

.refresh-button {
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.2));
  background: transparent;
  color: var(--text-primary, #fff);
  padding: 0.35rem 0.9rem;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.85rem;
}

.collection-summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.collection-summary-card {
  padding: 1rem;
  border-radius: 1rem;
  background: var(--card-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.summary-label {
  font-size: 0.85rem;
  color: var(--text-tertiary, rgba(255, 255, 255, 0.6));
}

.summary-value {
  font-size: 1.6rem;
  color: var(--text-primary, #fff);
}

.summary-hint {
  font-size: 0.78rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.6));
}

.summary-progress {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.78rem;
}

.summary-progress-track {
  width: 100%;
  height: 8px;
  border-radius: 4px;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.08));
}

.summary-progress-fill {
  height: 100%;
  border-radius: 4px;
  background: linear-gradient(90deg, #6dd5ed, #2193b0);
}

.collection-insights {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.insight-card {
  padding: 1rem;
  background: var(--card-bg, rgba(255, 255, 255, 0.04));
  border-radius: 1rem;
  border: 1px dashed var(--border-color, rgba(255, 255, 255, 0.1));
}

.insight-label {
  font-size: 0.75rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.65));
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.insight-name {
  display: block;
  margin: 0.4rem 0;
  font-size: 1.1rem;
  color: var(--text-primary, #fff);
}

.insight-meta {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

.collection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1rem;
}

.collection-card {
  padding: 1rem;
  border-radius: 1rem;
  background: var(--card-bg, rgba(255, 255, 255, 0.03));
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.collection-card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.collection-icon {
  width: 48px;
  height: 48px;
  object-fit: contain;
  border-radius: 0.75rem;
}

.collection-name {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #fff);
}

.collection-level {
  font-size: 0.78rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.65));
}

.collection-rank {
  margin-left: auto;
  font-size: 0.75rem;
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.15));
  color: var(--text-primary, #fff);
}

.collection-rank.complete {
  border-color: rgba(110, 231, 183, 0.5);
  color: #6ee7b7;
}

.collection-progress {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.progress-track {
  width: 100%;
  height: 10px;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.08));
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #80b2ff, #a855f7);
}

.progress-value {
  font-size: 0.82rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.65));
}

.collection-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.78rem;
  margin: 0;
  color: var(--text-secondary, rgba(255, 255, 255, 0.7));
}

@media (max-width: 768px) {
  .collection-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}
</style>
