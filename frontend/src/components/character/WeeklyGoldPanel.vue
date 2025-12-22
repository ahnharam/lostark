<template>
  <section class="weekly-gold-section">
    <div class="section-header-bar">
      <div>
        <h3>주간 골드 획득</h3>
        <p class="section-subtitle">캐릭터별 레이드 완료 현황을 체크하여 주간 골드를 계산합니다.</p>
      </div>
      <div class="gold-total">
        <span>골드</span>
        <div class="gold-total-values">
          <span class="total-amount">{{ formatGold(selectedTotalGold) }}</span>
          <span
            v-if="hasSplitGold(selectedTotalGoldTrade, selectedTotalGoldBound)"
            class="gold-breakdown"
          >
            거래 {{ formatGold(selectedTotalGoldTrade) }} / 귀속 {{ formatGold(selectedTotalGoldBound) }}
          </span>
        </div>
        <!-- <button
          type="button"
          class="toggle-expand-btn"
          @click="isExpanded = !isExpanded"
        >
          {{ isExpanded ? '▲' : '▼' }}
        </button> -->
      </div>
    </div>

    <div v-if="isExpanded" class="weekly-gold-content">
      <div class="server-group-list">
        <section
          v-for="group in groupedServers"
          :key="group.serverName"
          class="server-group"
        >
          <button
            type="button"
            class="server-group-toggle"
            @click="toggleServer(group.serverName)"
          >
            <span class="server-name">{{ group.serverName }}</span>
            <div class="server-total-group">
              <span class="server-total">{{ formatGold(group.totalGold) }}</span>
              <span
                v-if="hasSplitGold(group.totalGoldTrade, group.totalGoldBound)"
                class="server-total-breakdown"
              >
                거래 {{ formatGold(group.totalGoldTrade) }} / 귀속 {{ formatGold(group.totalGoldBound) }}
              </span>
            </div>
            <span class="server-arrow">{{ isServerExpanded(group.serverName) ? '▴' : '▾' }}</span>
          </button>
          <div v-if="isServerExpanded(group.serverName)" class="table-wrapper">
            <table class="weekly-gold-table">
              <thead>
                <tr>
                  <th class="col-checkbox"></th>
                  <th class="col-character">캐릭터</th>
                  <th class="col-gold">골드</th>
                  <th
                    v-for="raid in raidDifficulties"
                    :key="raid.raidKey"
                    class="col-raid"
                  >
                      <div class="raid-header">
                        <div class="raid-name">{{ raid.name }}</div>
                        <div class="raid-level">{{ raid.itemLevel }}</div>
                        <div class="raid-gold">
                          <template v-if="hasSplitGold(raid.goldTrade, raid.goldBound)">
                            <span class="raid-gold-part">거래 {{ formatGold(raid.goldTrade ?? 0) }}</span>
                            <span class="raid-gold-part">귀속 {{ formatGold(raid.goldBound ?? 0) }}</span>
                          </template>
                          <template v-else>
                            <span class="raid-gold-total">{{ formatGold(raid.goldReward) }}</span>
                          </template>
                        </div>
                      </div>
                    </th>
                  </tr>
                </thead>
              <tbody>
                <tr
                  v-for="char in group.characters"
                  :key="`${group.serverName}-${char.characterName}`"
                  :class="{ selected: char.selected }"
                >
                  <td class="col-checkbox">
                    <input
                      type="checkbox"
                      :checked="char.selected"
                      @change="$emit('toggle-character', char.characterName)"
                    />
                  </td>
                  <td class="col-character">
                    <div class="character-info">
                      <strong class="char-name">{{ char.characterName }}</strong>
                      <span class="char-level">{{ formatItemLevel(char.itemLevel) }}</span>
                    </div>
                  </td>
                  <td class="col-gold">
                    <div class="gold-stack">
                      <span class="gold-amount">{{ formatGold(char.totalGold) }}</span>
                      <span
                        v-if="hasSplitGold(char.totalGoldTrade, char.totalGoldBound)"
                        class="gold-breakdown"
                      >
                        거래 {{ formatGold(char.totalGoldTrade) }} / 귀속 {{ formatGold(char.totalGoldBound) }}
                      </span>
                    </div>
                  </td>
                  <td
                    v-for="raid in raidDifficulties"
                    :key="`${char.characterName}-${raid.raidKey}`"
                    class="col-raid"
                  >
                    <input
                      type="checkbox"
                      :checked="char.completedRaids.includes(raid.raidKey)"
                      :disabled="char.itemLevel < raid.itemLevel"
                      @change="$emit('toggle-raid', char.characterName, raid.raidKey)"
                    />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { RaidDifficulty, CharacterWeeklyGold } from '@/api/types/weekly-gold'

const props = defineProps<{
  /**
   * 레이드 난이도 목록
   */
  raidDifficulties: RaidDifficulty[]

  /**
   * 캐릭터별 주간 골드 정보
   */
  characters: CharacterWeeklyGold[]

  /**
   * 선택된 캐릭터들의 총 골드 합계
   */
  selectedTotalGold: number

  /**
   * 선택된 캐릭터들의 거래 가능 골드 합계
   */
  selectedTotalGoldTrade: number

  /**
   * 선택된 캐릭터들의 귀속 골드 합계
   */
  selectedTotalGoldBound: number
}>()

defineEmits<{
  /**
   * 레이드 완료 토글
   */
  'toggle-raid': [characterName: string, raidKey: string]

  /**
   * 캐릭터 선택 토글
   */
  'toggle-character': [characterName: string]

  /**
   * 전체 캐릭터 선택/해제 토글
   */
  'toggle-all': []
}>()

/**
 * 패널 확장/축소 상태
 */
const isExpanded = ref(true)

const groupedServers = computed(() => {
  const map = new Map<string, CharacterWeeklyGold[]>()
  props.characters.forEach((character) => {
    const serverName = character.serverName?.trim() || '알 수 없음'
    if (!map.has(serverName)) {
      map.set(serverName, [])
    }
    map.get(serverName)?.push(character)
  })
  return Array.from(map.entries()).map(([serverName, characters]) => ({
    serverName,
    characters,
    totalGold: characters.filter(c => c.selected).reduce((sum, c) => sum + c.totalGold, 0),
    totalGoldTrade: characters.filter(c => c.selected).reduce((sum, c) => sum + c.totalGoldTrade, 0),
    totalGoldBound: characters.filter(c => c.selected).reduce((sum, c) => sum + c.totalGoldBound, 0)
  }))
})

const expandedServers = ref<Record<string, boolean>>({})

const isServerExpanded = (serverName: string) => expandedServers.value[serverName] ?? false

const toggleServer = (serverName: string) => {
  expandedServers.value = {
    ...expandedServers.value,
    [serverName]: !isServerExpanded(serverName)
  }
}

watch(groupedServers, (groups) => {
  const next = { ...expandedServers.value }
  groups.forEach(group => {
    if (next[group.serverName] === undefined) {
      next[group.serverName] = false
    }
  })
  expandedServers.value = next
}, { immediate: true })

/**
 * 골드 포맷 (천 단위 구분자)
 */
const formatGold = (value: number): string => {
  return `${value.toLocaleString()}G`
}

const hasSplitGold = (trade?: number | null, bound?: number | null) => {
  return (trade ?? 0) > 0 || (bound ?? 0) > 0
}

/**
 * 아이템 레벨 포맷 (소수점 2자리)
 */
const formatItemLevel = (value: number): string => {
  return value.toFixed(2)
}
</script>

<style scoped>
.weekly-gold-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background: var(--card-bg, #ffffff);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  padding: 1.5rem;
}

.section-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
}

.section-header-bar h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary, #111827);
}

.section-subtitle {
  margin: 0.25rem 0 0 0;
  font-size: 0.875rem;
  color: var(--text-muted, #9ca3af);
}

.gold-total {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary, #111827);
}

.gold-total-values {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  align-items: flex-end;
}

.total-amount {
  font-size: 1.5rem;
  font-weight: 700;
  color: #f59e0b;
}

.toggle-expand-btn {
  padding: 0.25rem 0.5rem;
  background: var(--bg-secondary, #f3f4f6);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 6px;
  font-size: 0.875rem;
  color: var(--text-primary, #111827);
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-expand-btn:hover {
  background: var(--border-color, #e5e7eb);
}

.weekly-gold-content {
  overflow-x: auto;
}

.server-group-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.server-group {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  overflow: hidden;
  background: var(--card-bg, #ffffff);
}

.table-wrapper {
  min-width: 100%;
  overflow-x: auto;
}

.weekly-gold-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.weekly-gold-table thead {
  background: var(--bg-secondary, #f8f9fa);
  position: sticky;
  top: 0;
  z-index: 10;
}

.weekly-gold-table th {
  padding: 0.75rem 0.5rem;
  text-align: center;
  font-weight: 600;
  color: var(--text-primary, #111827);
  border-bottom: 2px solid var(--border-color, #e5e7eb);
  white-space: nowrap;
}

.weekly-gold-table td {
  padding: 0.75rem 0.5rem;
  text-align: center;
  border-bottom: 1px solid var(--border-color, #e5e7eb);
}

.server-group-toggle {
  width: 100%;
  /* display: flex; */
  display: grid;
  grid-template-columns: 100px 1fr 30px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0.7rem 0.9rem;
  border: none;
  border-bottom: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s, border-color 0.2s;
}

.server-group-toggle:hover {
  background: var(--bg-secondary, #f3f4f6);
}

.server-name {
  font-weight: 700;
  color: #2563eb;
}

.server-total {
  font-weight: 700;
  color: #f59e0b;
}

.server-total-group {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.1rem;
}

.server-total-breakdown {
  font-size: 0.7rem;
  color: var(--text-secondary, #6b7280);
}

.server-arrow {
  color: var(--text-secondary, #6b7280);
}

.col-checkbox {
  width: 40px;
}

.col-character {
  min-width: 150px;
  text-align: left !important;
}

.col-gold {
  min-width: 100px;
  font-weight: 600;
  color: #f59e0b;
}

.col-raid {
  min-width: 60px;
}

.raid-header {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.raid-name {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-primary, #111827);
}

.raid-level {
  font-size: 0.75rem;
  color: var(--text-secondary, #6b7280);
}

.raid-gold {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  font-size: 0.7rem;
  color: #f59e0b;
}

.raid-gold-part,
.raid-gold-total {
  white-space: nowrap;
}

.character-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  align-items: flex-start;
}

.char-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary, #111827);
}

.char-level {
  font-size: 0.75rem;
  color: var(--text-secondary, #6b7280);
}

.gold-amount {
  font-weight: 600;
}

.gold-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.1rem;
}

.gold-breakdown {
  font-size: 0.7rem;
  color: var(--text-secondary, #6b7280);
}

.weekly-gold-table tbody tr {
  transition: background 0.2s;
}

.weekly-gold-table tbody tr:hover {
  background: var(--bg-secondary, #f9fafb);
}

.weekly-gold-table tbody tr.selected {
  background: #eff6ff;
}

.weekly-gold-table input[type='checkbox'] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #3b82f6;
}

.weekly-gold-table input[type='checkbox']:disabled {
  cursor: not-allowed;
  opacity: 0.3;
}

/* 다크 모드 */
:global(.dark) .weekly-gold-section {
  background: var(--card-bg, #1f2937);
  border-color: var(--border-color, #374151);
}

:global(.dark) .section-header-bar h3 {
  color: var(--text-primary, #f3f4f6);
}

:global(.dark) .gold-total {
  color: var(--text-primary, #f3f4f6);
}

:global(.dark) .toggle-expand-btn {
  background: var(--bg-secondary, #374151);
  border-color: var(--border-color, #4b5563);
  color: var(--text-primary, #f3f4f6);
}

:global(.dark) .toggle-expand-btn:hover {
  background: var(--border-color, #4b5563);
}

:global(.dark) .weekly-gold-table thead {
  background: var(--bg-secondary, #374151);
}

:global(.dark) .weekly-gold-table th {
  color: var(--text-primary, #f3f4f6);
  border-bottom-color: var(--border-color, #4b5563);
}

:global(.dark) .weekly-gold-table td {
  border-bottom-color: var(--border-color, #374151);
}

:global(.dark) .weekly-gold-table tbody tr:hover {
  background: var(--bg-secondary, #374151);
}

:global(.dark) .weekly-gold-table tbody tr.selected {
  background: #1e3a5f;
}

:global(.dark) .server-group-toggle {
  background: var(--card-bg, #1f2937);
  border-color: var(--border-color, #374151);
}

:global(.dark) .server-group-toggle:hover {
  background: var(--bg-secondary, #374151);
}

:global(.dark) .char-name {
  color: var(--text-primary, #f3f4f6);
}

:global(.dark) .raid-name {
  color: var(--text-primary, #f3f4f6);
}
</style>
