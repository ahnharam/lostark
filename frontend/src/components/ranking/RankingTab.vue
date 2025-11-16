<template>
  <section class="detail-panel ranking-panel">
    <div class="section-header-bar">
      <div>
        <h3>랭킹 대시보드</h3>
        <p class="section-subtitle">경쟁전부터 자체 통계까지 다양한 기준으로 내 위치를 확인하세요.</p>
      </div>
      <span v-if="activeCategory === 'pvp' && summary?.seasonId" class="badge">시즌 {{ summary.seasonId }}</span>
    </div>

    <EmptyState
      v-if="!characterName"
      icon="🏆"
      title="캐릭터를 먼저 검색해 주세요"
      description="캐릭터를 선택하면 경쟁전 및 내부 통계 기반 랭킹을 확인할 수 있어요."
    />
    <template v-else>
      <div class="ranking-category-tabs" role="tablist">
        <button
          v-for="category in rankingCategories"
          :key="category.key"
          type="button"
          class="ranking-category-tab"
          :class="{ active: category.key === activeCategory }"
          @click="setActiveCategory(category.key)"
        >
          {{ category.label }}
        </button>
      </div>

      <div v-if="activeCategory === 'pvp'">
        <RankingFilterBar
          :leaderboard-code="filters.leaderboardCode"
          :season-id="filters.seasonId"
          :page="filters.page"
          :loading="loading"
          @update:leaderboard-code="handleLeaderboardChange"
          @update:season-id="value => handleFilterChange({ seasonId: value })"
          @update:page="value => handleFilterChange({ page: value })"
          @refresh="refreshRanking"
        />

        <ErrorMessage
          v-if="error"
          :title="'랭킹 정보를 불러오지 못했습니다.'"
          :message="error"
          type="error"
          :retry="true"
          @retry="refreshRanking"
        />

        <RankingHighlightCard
          :highlight="summary?.highlightedCharacter || null"
          :loading="loading"
          :character-name="characterName"
        />

        <RankingTable :entries="entries" :loading="loading" />

        <EmptyState
          v-if="!loading && !error && entries.length === 0"
          icon="📊"
          title="데이터가 없습니다"
          description="선택한 리더보드에서 데이터를 찾을 수 없습니다."
        />

        <p v-if="summary?.lastUpdated" class="ranking-updated">
          최근 업데이트: {{ formatUpdated(summary.lastUpdated) }}
        </p>
      </div>
      <div v-else class="ranking-profile-panel">
        <RankingMetricCard
          :title="activeCategoryMeta?.label || ''"
          :subtitle="activeCategoryMeta?.description || ''"
          :hint="activeCategoryMeta?.hint || ''"
          :metric="activeMetric"
          :loading="profileLoading"
          :error="profileError"
        />
        <p class="ranking-data-hint">
          내부 데이터베이스에 저장된 캐릭터(즐겨찾기/검색 기록 기반)를 토대로 계산된 상대 순위입니다.
        </p>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import EmptyState from '@/components/common/EmptyState.vue'
import ErrorMessage from '@/components/common/ErrorMessage.vue'
import RankingFilterBar from './RankingFilterBar.vue'
import RankingHighlightCard from './RankingHighlightCard.vue'
import RankingTable from './RankingTable.vue'
import RankingMetricCard from './RankingMetricCard.vue'
import { useRanking } from '@/composables/useRanking'
import type { RankingFilters } from '@/composables/useRanking'
import { useProfileRanking } from '@/composables/useProfileRanking'
import type { RankingMetric } from '@/api/types'

const props = defineProps<{
  characterName?: string
}>()

const { entries, summary, loading, error, filters, setCharacter, updateFilters, fetchRanking, resetState } =
  useRanking()

type RankingCategoryKey =
  | 'pvp'
  | 'globalItem'
  | 'globalClass'
  | 'serverOverall'
  | 'serverClass'
  | 'expedition'
  | 'collection'

const rankingCategories: Array<{
  key: RankingCategoryKey
  label: string
  description: string
  hint: string
}> = [
  { key: 'pvp', label: '경쟁전', description: 'Lost Ark 공식 경쟁전/증명의 전장 리더보드', hint: '공식 리더보드' },
  {
    key: 'globalItem',
    label: '전체 서버 · 아이템',
    description: '아이템 레벨 기준 전체 서버 평균 비교',
    hint: '전체 서버 내 모든 캐릭터'
  },
  {
    key: 'globalClass',
    label: '전체 서버 · 직업',
    description: '같은 직업 중 내 위치를 확인하세요.',
    hint: '전체 서버, 동일 직업'
  },
  {
    key: 'serverOverall',
    label: '내 서버 · 전체',
    description: '같은 서버 모든 직업과 비교한 순위',
    hint: '내 서버, 전 직업'
  },
  {
    key: 'serverClass',
    label: '내 서버 · 직업',
    description: '같은 서버 & 직업 내 세부 순위',
    hint: '내 서버, 동일 직업'
  },
  {
    key: 'expedition',
    label: '원정대',
    description: '원정대 레벨로 본 상대적 위치',
    hint: '전체 서버, 원정대 레벨'
  },
  {
    key: 'collection',
    label: '수집품',
    description: '수집 포인트 합산 기준 순위',
    hint: '전체 서버, 수집 포인트'
  }
]

const activeCategory = ref<RankingCategoryKey>('pvp')

const {
  data: profileRanking,
  loading: profileLoading,
  error: profileError,
  setCharacter: setProfileCharacter,
  fetchProfileRanking,
  reset: resetProfileRanking
} = useProfileRanking()

const formatUpdated = (value: string) => {
  try {
    const date = new Date(value)
    return date.toLocaleString()
  } catch {
    return value
  }
}

const handleLeaderboardChange = (value: string) => {
  updateFilters({ leaderboardCode: value, page: 1 })
  fetchRanking(true)
}

const handleFilterChange = (next: Partial<RankingFilters>) => {
  updateFilters(next)
  fetchRanking()
}

const refreshRanking = () => {
  fetchRanking(true)
}

const setActiveCategory = (category: RankingCategoryKey) => {
  activeCategory.value = category
}

const activeCategoryMeta = computed(() =>
  rankingCategories.find(category => category.key === activeCategory.value)
)

const metricMap = computed<Record<Exclude<RankingCategoryKey, 'pvp'>, RankingMetric | null | undefined>>(() => ({
  globalItem: profileRanking.value?.globalItemLevel,
  globalClass: profileRanking.value?.globalClassItemLevel,
  serverOverall: profileRanking.value?.serverItemLevel,
  serverClass: profileRanking.value?.serverClassItemLevel,
  expedition: profileRanking.value?.expeditionLevel,
  collection: profileRanking.value?.collectionScore
}))

const activeMetric = computed<RankingMetric | null>(() => {
  if (activeCategory.value === 'pvp') return null
  return (metricMap.value as Record<string, RankingMetric | null | undefined>)[activeCategory.value] ?? null
})

watch(
  () => props.characterName,
  name => {
    setCharacter(name ?? null)
    setProfileCharacter(name ?? null)
    if (name) {
      fetchRanking(true)
      fetchProfileRanking(true)
    } else {
      resetState()
      resetProfileRanking()
    }
  },
  { immediate: true }
)

watch(
  activeCategory,
  category => {
    if (!props.characterName) return
    if (category !== 'pvp' && !profileRanking.value) {
      fetchProfileRanking()
    }
  },
  { immediate: false }
)
</script>

<style scoped>
.ranking-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  color: var(--text-primary, #1f2937);
}

.ranking-category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.ranking-category-tab {
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.2));
  border-radius: 999px;
  padding: 0.35rem 0.9rem;
  background: transparent;
  color: var(--text-primary, #1f2937);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.ranking-category-tab.active {
  background: var(--accent-color, rgba(128, 178, 255, 0.2));
  color: var(--text-inverse, #ffffff);
  border-color: transparent;
}

.badge {
  align-self: flex-start;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  background: var(--accent-soft-bg, rgba(128, 178, 255, 0.15));
  color: var(--accent-color, #80b2ff);
  font-weight: 600;
  font-size: 0.75rem;
}

.ranking-updated {
  margin: 0;
  font-size: 0.75rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.6));
}

.ranking-profile-panel {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.ranking-data-hint {
  margin: 0;
  font-size: 0.78rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.65));
}
</style>
