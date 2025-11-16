<template>
  <article class="ranking-metric-card">
    <div class="metric-header">
      <div>
        <h4>{{ title }}</h4>
        <p class="metric-subtitle">{{ subtitle }}</p>
      </div>
      <span v-if="metric?.percentile" class="metric-chip">
        상위 {{ formatPercent(metric.percentile) }}%
      </span>
    </div>

    <div v-if="loading" class="metric-skeleton">
      <span v-for="n in 3" :key="n" class="skeleton-line"></span>
    </div>
    <div v-else-if="error" class="metric-error">
      {{ error }}
    </div>
    <div v-else-if="metric" class="metric-body">
      <p class="metric-rank">
        <strong>#{{ metric.rank ?? '-' }}</strong>
        <span v-if="metric.total">/ {{ metric.total }}</span>
      </p>
      <p class="metric-value">
        현재 수치:
        <strong>{{ formatValue(metric.metricValue, metric.unit) }}</strong>
      </p>
      <p class="metric-hint">
        비교 기준: {{ hint }}
      </p>
    </div>
    <div v-else class="metric-empty">
      <p>아직 충분한 데이터가 없어 랭킹을 계산할 수 없습니다.</p>
      <p class="metric-hint">다른 캐릭터 검색이 늘어날수록 더 정확해집니다.</p>
    </div>
  </article>
</template>

<script setup lang="ts">
import type { RankingMetric } from '@/api/types'

const props = defineProps<{
  title: string
  subtitle: string
  hint: string
  metric?: RankingMetric | null
  loading: boolean
  error?: string | null
}>()

const formatValue = (value?: number | null, unit?: string) => {
  if (value == null) return '-'
  const formatted = value >= 1000 ? value.toLocaleString(undefined, { maximumFractionDigits: 2 }) : value
  return unit ? `${formatted} ${unit}` : String(formatted)
}

const formatPercent = (value: number) => {
  return (value * 100).toFixed(1)
}
</script>

<style scoped>
.ranking-metric-card {
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.08));
  border-radius: 16px;
  padding: 1rem;
  background: var(--panel-bg, rgba(0, 0, 0, 0.4));
  min-height: 160px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  color: var(--text-primary, #1f2937);
}

.metric-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.5rem;
}

.metric-header h4 {
  margin: 0;
  font-size: 1rem;
}

.metric-subtitle {
  margin: 0.1rem 0 0;
  font-size: 0.8rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.7));
}

.metric-chip {
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.3));
  font-size: 0.75rem;
}

.metric-body {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.metric-rank {
  margin: 0;
  font-size: 1.4rem;
}

.metric-value,
.metric-hint {
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.75));
}

.metric-error {
  color: var(--error-color, #ff9f9f);
  font-size: 0.9rem;
}

.metric-empty {
  font-size: 0.85rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.7));
}

.metric-skeleton {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.skeleton-line {
  height: 14px;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--panel-bg, rgba(255, 255, 255, 0.05)), var(--accent-soft-bg, rgba(255, 255, 255, 0.15)), var(--panel-bg, rgba(255, 255, 255, 0.05)));
  background-size: 200% 100%;
  animation: pulse 1.4s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
