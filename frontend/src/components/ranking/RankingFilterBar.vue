<template>
  <div class="ranking-filter-bar">
    <div class="filter-group">
      <label class="filter-label">
        <span>리더보드</span>
        <CustomSelect
          class="filter-select"
          :model-value="leaderboardCode"
          :options="leaderboardOptions"
          @change="handleLeaderboardChange"
        />
      </label>
      <p class="filter-hint">
        {{ selectedOption?.description || '원하는 리더보드를 선택하세요.' }}
      </p>
    </div>

    <div class="filter-group">
      <label class="filter-label">
        <span>시즌</span>
        <input
          class="filter-input"
          type="text"
          placeholder="현재 시즌"
          :value="seasonId || ''"
          @change="$emit('update:seasonId', ($event.target as HTMLInputElement).value || undefined)"
        />
      </label>
      <p class="filter-hint">지정하지 않으면 현재 시즌으로 조회합니다.</p>
    </div>

    <div class="filter-group filter-group--pager">
      <div class="pager-controls">
        <button
          type="button"
          class="pager-button"
          :disabled="page <= 1 || loading"
          @click="$emit('update:page', Math.max(1, page - 1))"
        >
          이전
        </button>
        <span class="pager-label">Page {{ page }}</span>
        <button
          type="button"
          class="pager-button"
          :disabled="loading"
          @click="$emit('update:page', page + 1)"
        >
          다음
        </button>
      </div>
      <button
        type="button"
        class="refresh-button"
        :disabled="loading"
        @click="$emit('refresh')"
      >
        새로고침
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CustomSelect, { type SelectOption } from '../common/CustomSelect.vue'

interface LeaderboardOption extends SelectOption {
  value: string
  label: string
  description: string
}

const props = defineProps<{
  leaderboardCode: string
  seasonId?: string
  page: number
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'update:leaderboardCode', value: string): void
  (e: 'update:seasonId', value: string | undefined): void
  (e: 'update:page', value: number): void
  (e: 'refresh'): void
}>()

const leaderboardOptions: LeaderboardOption[] = [
  { value: '0101', label: '경쟁전 (0101)', description: 'PvP 경쟁전 종합 순위' },
  { value: '0201', label: '증명의 전장 - 팀 데스매치 (0201)', description: '3:3 PvP 팀 데스매치' },
  { value: '0202', label: '증명의 전장 - 대장전 (0202)', description: '대장전 모드 랭킹' },
  { value: '0203', label: '증명의 전장 - 섬 점령전 (0203)', description: '섬 점령전 점수 기반 랭킹' }
]

const selectedOption = computed(() =>
  leaderboardOptions.find(option => option.value === props.leaderboardCode)
)

const handleLeaderboardChange = (value: string | number | null) => {
  if (typeof value !== 'string') return
  emit('update:leaderboardCode', value)
}
</script>

<style scoped>
.ranking-filter-bar {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
  padding: 1rem;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
  border-radius: 12px;
  background: var(--panel-bg, rgba(255, 255, 255, 0.02));
  margin-bottom: 1rem;
  color: var(--text-primary, #1f2937);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.filter-label {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.85rem;
}

.filter-label span {
  font-weight: 600;
}

.filter-select,
:deep(.filter-select),
.filter-input {
  padding: 0.4rem 0.6rem;
  border-radius: 8px;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
  background: var(--input-bg, rgba(0, 0, 0, 0.2));
  color: inherit;
}

.filter-select:focus,
:deep(.filter-select:focus),
.filter-input:focus {
  outline: none;
  border-color: var(--accent-color, #80b2ff);
}

.filter-hint {
  margin: 0;
  font-size: 0.75rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.6));
}

.filter-group--pager {
  align-items: flex-end;
}

.pager-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.pager-button,
.refresh-button {
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.2));
  background: transparent;
  color: inherit;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.pager-button:disabled,
.refresh-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pager-button:not(:disabled):hover,
.refresh-button:not(:disabled):hover {
  background: var(--accent-color, rgba(128, 178, 255, 0.15));
}

.pager-label {
  font-weight: 600;
}
</style>
