<template>
  <div class="ranking-highlight-card">
    <template v-if="loading">
      <div class="skeleton-line title"></div>
      <div class="skeleton-line subtitle"></div>
      <div class="skeleton-grid">
        <span v-for="n in 4" :key="n" class="skeleton-line small"></span>
      </div>
    </template>
    <template v-else-if="highlight">
      <div class="highlight-header">
        <span class="highlight-pill">현재 순위</span>
        <strong class="highlight-rank">#{{ highlight.rank }}</strong>
      </div>
      <p class="highlight-name">
        {{ highlight.characterName }}
        <span v-if="highlight.serverName" class="highlight-meta">· {{ highlight.serverName }}</span>
      </p>
      <dl class="highlight-stats">
        <div>
          <dt>평균 아이템 레벨</dt>
          <dd>{{ highlight.itemAvgLevel || '정보 없음' }}</dd>
        </div>
        <div>
          <dt>레이팅</dt>
          <dd>{{ highlight.rating ?? '정보 없음' }}</dd>
        </div>
        <div>
          <dt>티어</dt>
          <dd>
            <span v-if="highlight.tier">T{{ highlight.tier }}</span>
            <span v-else>정보 없음</span>
          </dd>
        </div>
        <div>
          <dt>디비전</dt>
          <dd>
            <span v-if="highlight.division">D{{ highlight.division }}</span>
            <span v-else>정보 없음</span>
          </dd>
        </div>
      </dl>
    </template>
    <template v-else>
      <p class="highlight-empty">
        <strong>{{ characterName || '이 캐릭터' }}</strong>는 아직 상위 랭킹에 진입하지 않았거나 데이터가
        제공되지 않았어요.
      </p>
      <p class="highlight-hint">상위 600위 이하 캐릭터는 표시되지 않을 수 있습니다.</p>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { RankingHighlight } from '@/api/types'

defineProps<{
  highlight?: RankingHighlight | null
  loading: boolean
  characterName?: string
}>()
</script>

<style scoped>
.ranking-highlight-card {
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.08));
  border-radius: 16px;
  padding: 1rem;
  background: linear-gradient(125deg, var(--panel-bg, rgba(255, 255, 255, 0.05)), var(--accent-soft-bg, rgba(128, 178, 255, 0.08)));
  margin-bottom: 1.25rem;
  min-height: 150px;
  color: var(--text-primary, #1f2937);
}

.highlight-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.4rem;
}

.highlight-pill {
  font-size: 0.7rem;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.3));
}

.highlight-rank {
  font-size: 1.5rem;
}

.highlight-name {
  font-size: 1.1rem;
  margin: 0 0 0.35rem;
  font-weight: 600;
}

.highlight-meta {
  font-size: 0.9rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.7));
}

.highlight-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 0.5rem;
  margin: 0;
}

.highlight-stats dt {
  font-size: 0.7rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.6));
}

.highlight-stats dd {
  margin: 0.2rem 0 0;
  font-weight: 600;
}

.highlight-empty {
  margin: 0;
  font-size: 0.95rem;
}

.highlight-hint {
  margin: 0.35rem 0 0;
  font-size: 0.75rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.6));
}

.skeleton-line {
  border-radius: 6px;
  background: linear-gradient(90deg, var(--panel-bg, rgba(255, 255, 255, 0.05)), var(--accent-soft-bg, rgba(255, 255, 255, 0.12)), var(--panel-bg, rgba(255, 255, 255, 0.05)));
  background-size: 200% 100%;
  animation: pulse 1.4s ease-in-out infinite;
}

.skeleton-line.title {
  height: 18px;
  width: 40%;
  margin-bottom: 0.5rem;
}

.skeleton-line.subtitle {
  height: 14px;
  width: 55%;
  margin-bottom: 0.75rem;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 0.5rem;
}

.skeleton-line.small {
  height: 12px;
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
