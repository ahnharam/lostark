<template>
  <div class="ranking-table">
    <table v-if="entries.length || loading">
      <thead>
        <tr>
          <th>순위</th>
          <th>캐릭터</th>
          <th>클래스</th>
          <th>서버</th>
          <th>아이템 레벨</th>
          <th>레이팅</th>
        </tr>
      </thead>
      <tbody>
        <template v-if="loading">
          <tr v-for="n in 5" :key="n" class="skeleton-row">
            <td colspan="6">
              <span class="skeleton-line"></span>
            </td>
          </tr>
        </template>
        <template v-else>
          <tr v-for="entry in entries" :key="entry.characterName + entry.rank">
            <td>#{{ entry.rank ?? '-' }}</td>
            <td>
              <strong>{{ entry.characterName }}</strong>
              <span v-if="entry.guildName" class="table-subtle">({{ entry.guildName }})</span>
            </td>
            <td>{{ entry.characterClassName || '-' }}</td>
            <td>{{ entry.serverName || '-' }}</td>
            <td>{{ formatItemLevel(entry.itemAvgLevel || entry.itemMaxLevel) }}</td>
            <td>{{ entry.rating ?? entry.score ?? '-' }}</td>
          </tr>
        </template>
      </tbody>
    </table>
    <p v-else class="ranking-table__empty">데이터가 없습니다.</p>
  </div>
</template>

<script setup lang="ts">
import type { RankingEntry } from '@/api/types'

defineProps<{
  entries: RankingEntry[]
  loading: boolean
}>()

const formatItemLevel = (value?: string) => {
  if (!value) return '-'
  const numeric = Number(value.replace(/[^0-9.]/g, ''))
  if (Number.isNaN(numeric)) return value
  return numeric.toFixed(2)
}
</script>

<style scoped>
.ranking-table {
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.05));
  border-radius: 12px;
  overflow: hidden;
  background: var(--panel-bg, rgba(0, 0, 0, 0.2));
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

th,
td {
  padding: 0.75rem;
  text-align: left;
}

thead {
  background: rgba(255, 255, 255, 0.04);
}

.table-subtle {
  display: inline-block;
  margin-left: 0.25rem;
  font-size: 0.75rem;
  color: var(--text-muted, rgba(255, 255, 255, 0.6));
}

.ranking-table__empty {
  margin: 0;
  padding: 1.5rem;
  text-align: center;
  color: var(--text-muted, rgba(255, 255, 255, 0.6));
}

.skeleton-row td {
  padding: 0.5rem 0.75rem;
}

.skeleton-line {
  display: block;
  width: 100%;
  height: 16px;
  border-radius: 6px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.04), rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0.04));
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
