<template>
  <section class="expedition-section">
    <div class="section-header-bar">
      <div>
        <h3>원정대 보유 캐릭터</h3>
        <p class="section-subtitle">클릭하면 해당 캐릭터의 통합 화면이 열립니다.</p>
      </div>
      <div class="section-actions">
        <div class="expedition-sort">
          <label for="expedition-sort">캐릭터 정렬 기준</label>
          <CustomSelect
            id="expedition-sort"
            :model-value="sortKey"
            class="expedition-sort__select"
            :options="sortOptions"
            @update:model-value="(value) => $emit('update:sortKey', String(value))"
          />
        </div>
        <span class="count-pill">{{ totalCount }}명</span>
      </div>
    </div>

    <template v-if="groups.length">
      <div
        v-for="group in groups"
        :key="group.server"
        class="expedition-group"
      >
        <h4>{{ group.server }}</h4>
        <div class="expedition-grid">
          <article
            v-for="member in group.members"
            :key="member.characterName"
            class="expedition-card"
            :class="{ active: selectedCharacterName === member.characterName }"
            @click="$emit('select', member)"
          >
            <div class="member-top">
              <span class="member-level">Lv. {{ formatInteger(member.characterLevel) }}</span>
              <span class="member-class">{{ member.characterClassName }}</span>
            </div>
            <strong class="member-name">{{ member.characterName }}</strong>
            <span class="member-ilvl">
              iLv. {{ formatItemLevel(member.itemAvgLevel || member.itemMaxLevel) }}
            </span>
            <span class="member-detail">상세 보기</span>
          </article>
        </div>
      </div>
    </template>
    <p v-else class="empty-message">원정대 캐릭터가 없습니다.</p>
  </section>
</template>

<script setup lang="ts">
import CustomSelect from '@/components/common/CustomSelect.vue'
import type { SelectOption } from '@/components/common/CustomSelect.vue'
import type { SiblingCharacter } from '@/api/types/armory'

/**
 * 서버별 그룹
 */
export interface ExpeditionGroup {
  server: string
  members: SiblingCharacter[]
}

const props = defineProps<{
  /**
   * 서버별 그룹화된 원정대 캐릭터 목록
   */
  groups: ExpeditionGroup[]

  /**
   * 현재 선택된 캐릭터 이름
   */
  selectedCharacterName?: string

  /**
   * 전체 캐릭터 수
   */
  totalCount: number

  /**
   * 현재 정렬 기준
   */
  sortKey: string

  /**
   * 정렬 옵션 목록
   */
  sortOptions: SelectOption[]
}>()

defineEmits<{
  /**
   * 캐릭터 선택 이벤트
   */
  select: [character: SiblingCharacter]

  /**
   * 정렬 기준 변경 이벤트
   */
  'update:sortKey': [key: string]
}>()

/**
 * 정수 포맷 (천 단위 구분자)
 */
const formatInteger = (value?: number): string => {
  if (value === undefined || value === null) return '0'
  return Math.floor(value).toLocaleString()
}

/**
 * 아이템 레벨 포맷 (소수점 2자리)
 */
const formatItemLevel = (value?: string): string => {
  if (!value) return '0.00'
  const numeric = parseFloat(String(value).replace(/[^\d.]/g, ''))
  if (!Number.isFinite(numeric)) return '0.00'
  return numeric.toFixed(2)
}
</script>

<style scoped>
.expedition-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
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
}

.section-subtitle {
  margin: 0.25rem 0 0 0;
  font-size: 0.875rem;
  color: #888;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.expedition-sort {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.expedition-sort label {
  font-size: 0.875rem;
  color: #666;
  white-space: nowrap;
}

.expedition-sort__select,
:deep(.expedition-sort__select) {
  min-width: 140px;
}

.count-pill {
  padding: 0.25rem 0.75rem;
  background: #f0f0f0;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #333;
}

.expedition-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.expedition-group h4 {
  margin: 0;
  font-size: 1rem;
  font-weight: 500;
  color: #555;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e0e0e0;
}

.expedition-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1rem;
}

.expedition-card {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.expedition-card:hover {
  border-color: #4a90e2;
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.2);
  transform: translateY(-2px);
}

.expedition-card.active {
  border-color: #4a90e2;
  background: #f0f7ff;
}

.member-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  color: #666;
}

.member-level {
  font-weight: 500;
}

.member-class {
  color: #888;
}

.member-name {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-ilvl {
  font-size: 0.875rem;
  font-weight: 500;
  color: #4a90e2;
}

.member-detail {
  font-size: 0.75rem;
  color: #999;
  text-align: center;
  padding-top: 0.25rem;
  border-top: 1px solid #f0f0f0;
}

.empty-message {
  text-align: center;
  padding: 3rem 1rem;
  color: #999;
  font-size: 0.875rem;
}
</style>
