<template>
  <div class="auction-page">
    <header class="auction-hero">
      <div>
        <p class="eyebrow">경매 데이터 뷰어</p>
        <h2>DB에 저장된 거래소 아이템을 한눈에</h2>
        <p class="hero-desc">
          백엔드에서 동기화한 거래소 스냅샷을 필터링하고, 빠르게 찾아보세요. 카테고리를 바꾸거나 페이지를 넘기면 즉시 새로 불러옵니다.
        </p>
      </div>
      <div class="hero-meta">
        <div class="meta-chip">
          <span class="meta-label">총 카테고리</span>
          <strong>{{ categoryCount }}</strong>
        </div>
        <div class="meta-chip">
          <span class="meta-label">수집된 아이템</span>
          <strong>{{ formatNumber(totalItems) }}</strong>
        </div>
        <div class="meta-chip accent" :class="{ muted: !lastFetchedLabel }">
          <span class="meta-label">최근 수집</span>
          <strong>{{ lastFetchedLabel || '정보 없음' }}</strong>
        </div>
      </div>
    </header>

    <section class="control-panel panel-card">
      <div class="control-row">
        <label class="field">
          <span class="field-label">카테고리</span>
          <select v-model="selectedCategory" class="input" :disabled="loadingCategories">
            <option :value="null">전체 보기</option>
            <option v-for="cat in leafCategories" :key="cat.code" :value="cat.code">
              {{ cat.code }} · {{ cat.codeName }}
            </option>
          </select>
        </label>

        <label class="field">
          <span class="field-label">검색 (이름)</span>
          <input
            v-model.trim="searchText"
            class="input"
            type="text"
            placeholder="예: 파괴석, 각인서..."
            @keyup.enter="handleSearch"
          />
        </label>

        <label class="field field--small">
          <span class="field-label">페이지 크기</span>
          <select v-model.number="pageSize" class="input" @change="resetAndLoad">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="30">30</option>
            <option :value="50">50</option>
          </select>
        </label>

        <div class="actions">
          <button type="button" class="btn" @click="resetAndLoad">새로고침</button>
          <button type="button" class="btn ghost" @click="clearSearch">검색 초기화</button>
        </div>
      </div>

      <p v-if="!leafCategories.length" class="inline-hint">
        백엔드에서 `/api/markets/options/sync` → `/api/markets/items/sync` 호출 후 다시 시도하세요.
      </p>
    </section>

    <section class="items-panel panel-card">
      <div class="items-head">
        <div>
          <p class="eyebrow">아이템 목록</p>
          <h3>거래소 스냅샷 {{ filteredItems.length ? `(${filteredItems.length}개 표시 중)` : '' }}</h3>
        </div>
        <div class="pager">
          <button class="pager-btn" type="button" :disabled="page === 1 || loadingItems" @click="changePage(page - 1)">
            이전
          </button>
          <span class="pager-page">{{ page }} / {{ totalPages }}</span>
          <button
            class="pager-btn"
            type="button"
            :disabled="page >= totalPages || loadingItems"
            @click="changePage(page + 1)"
          >
            다음
          </button>
        </div>
      </div>

      <LoadingSpinner v-if="loadingItems && !items.length" message="아이템을 불러오는 중..." />
      <p v-else-if="errorMessage" class="error-text">{{ errorMessage }}</p>
      <p v-else-if="!items.length" class="empty-state">저장된 아이템이 없습니다.</p>

      <div v-else class="table">
        <div class="table-head">
          <span>아이템</span>
          <span class="align-right">현재 최저가</span>
          <span class="align-right">최근 거래가</span>
          <span class="align-right">전일 평균</span>
          <span class="align-right">묶음/거래</span>
          <span class="align-right">카테고리</span>
          <span class="align-right">수집시각</span>
        </div>
        <div
          v-for="item in filteredItems"
          :key="item.id"
          class="table-row"
        >
          <div class="item-cell">
            <div class="thumb" :data-grade="item.grade">
              <span>{{ gradeInitial(item.grade) }}</span>
            </div>
            <div>
              <p class="item-name">{{ item.name }}</p>
              <p class="item-sub">{{ item.grade || '등급 없음' }}</p>
            </div>
          </div>
          <span class="align-right strong">{{ formatGold(item.currentMinPrice) }}</span>
          <span class="align-right">{{ formatGold(item.recentPrice) }}</span>
          <span class="align-right muted">{{ formatGold(item.yDayAvgPrice) }}</span>
          <span class="align-right muted">
            {{ item.bundleCount ? `${item.bundleCount}개` : '-' }}
            <span v-if="item.tradeRemainCount !== undefined && item.tradeRemainCount !== null">
              / {{ item.tradeRemainCount }}
            </span>
          </span>
          <span class="align-right badge">{{ item.categoryCode ?? '전체' }}</span>
          <span class="align-right muted">{{ formatDateTime(item.fetchedAt) }}</span>
        </div>
      </div>

      <div v-if="loadingItems && items.length" class="inline-loading">새 페이지를 불러오는 중...</div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { lostarkApi } from '@/api/lostark'
import type { StoredMarketCategory, StoredMarketItem } from '@/api/types'
import LoadingSpinner from './common/LoadingSpinner.vue'

const categories = ref<StoredMarketCategory[]>([])
const items = ref<StoredMarketItem[]>([])
const selectedCategory = ref<number | null>(null)
const searchText = ref('')
const page = ref(1)
const totalPages = ref(1)
const totalItems = ref(0)
const pageSize = ref(20)
const loadingCategories = ref(false)
const loadingItems = ref(false)
const errorMessage = ref('')

const leafCategories = computed(() => categories.value.filter(cat => !cat.hasSubs))
const categoryCount = computed(() => categories.value.length)

const filteredItems = computed(() => {
  const keyword = searchText.value.toLowerCase()
  if (!keyword) return items.value
  return items.value.filter(item => item.name.toLowerCase().includes(keyword))
})

const lastFetchedLabel = computed(() => {
  const latest = items.value.reduce<string | null>((acc, cur) => {
    if (!cur.fetchedAt) return acc
    if (!acc || new Date(cur.fetchedAt) > new Date(acc)) return cur.fetchedAt
    return acc
  }, null)
  return latest ? formatDateTime(latest) : ''
})

const formatNumber = (value?: number | null) => {
  if (value === null || value === undefined) return '-'
  return new Intl.NumberFormat('ko-KR').format(value)
}

const formatGold = (value?: number | null) => {
  if (value === null || value === undefined) return '-'
  return `${formatNumber(value)} G`
}

const formatDateTime = (value?: string) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return new Intl.DateTimeFormat('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const gradeInitial = (grade?: string | null) => {
  if (!grade) return 'N'
  return grade.slice(0, 1).toUpperCase()
}

const handleSearch = () => {
  // computed가 알아서 필터링하므로 별도 호출은 필요 없지만 UX를 위해 페이지를 유지
}

const clearSearch = () => {
  searchText.value = ''
}

const changePage = (nextPage: number) => {
  if (nextPage < 1 || nextPage > totalPages.value) return
  page.value = nextPage
  loadItems()
}

const resetAndLoad = () => {
  page.value = 1
  loadItems()
}

const loadCategories = async () => {
  try {
    loadingCategories.value = true
    const data = await lostarkApi.getMarketCategories()
    categories.value = data
    if (!selectedCategory.value && leafCategories.value.length) {
      selectedCategory.value = leafCategories.value[0].code ?? null
    }
  } catch (error) {
    console.error(error)
    errorMessage.value = '카테고리를 불러오지 못했습니다. 백엔드 동기화 상태를 확인해 주세요.'
  } finally {
    loadingCategories.value = false
  }
}

const loadItems = async () => {
  try {
    loadingItems.value = true
    errorMessage.value = ''
    const data = await lostarkApi.getStoredMarketItems({
      categoryCode: selectedCategory.value ?? undefined,
      page: page.value,
      size: pageSize.value
    })
    items.value = data.content ?? []
    totalPages.value = data.totalPages ?? 1
    totalItems.value = data.totalElements ?? items.value.length
    // Spring에서 보정한 number는 1-based
    page.value = data.number ?? page.value
  } catch (error) {
    console.error(error)
    errorMessage.value = '아이템을 불러오지 못했습니다.'
  } finally {
    loadingItems.value = false
  }
}

watch(selectedCategory, () => resetAndLoad())

onMounted(async () => {
  await loadCategories()
  await loadItems()
})
</script>

<style scoped>
.auction-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.auction-hero {
  padding: 22px;
  border-radius: 18px;
  background: linear-gradient(120deg, #0f172a, #111827 55%, #0b2b4d);
  color: #f8fafc;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.18);
}

.auction-hero h2 {
  margin: 2px 0 6px;
  font-size: 1.6rem;
}

.hero-desc {
  margin: 0;
  color: rgba(255, 255, 255, 0.78);
  max-width: 720px;
}

.hero-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.meta-chip {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  padding: 10px 12px;
  display: grid;
  gap: 4px;
  min-width: 120px;
}

.meta-chip strong {
  font-size: 1.1rem;
}

.meta-chip.accent {
  background: linear-gradient(120deg, rgba(125, 211, 252, 0.15), rgba(255, 255, 255, 0.08));
  border-color: rgba(125, 211, 252, 0.4);
}

.meta-chip.muted {
  opacity: 0.7;
}

.meta-label {
  font-size: 0.82rem;
  color: rgba(255, 255, 255, 0.8);
}

.control-panel {
  display: grid;
  gap: 12px;
  border-radius: 16px;
  border: 1px solid var(--border-color, #e5e7eb);
}

.control-row {
  display: grid;
  grid-template-columns: 1.1fr 1fr 0.4fr auto;
  gap: 12px;
  align-items: end;
}

.field {
  display: grid;
  gap: 6px;
}

.field-label {
  font-size: 0.86rem;
  color: var(--text-secondary, #4b5563);
}

.input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f8fafc);
  color: var(--text-primary, #111827);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.input:focus {
  border-color: var(--primary-color, #2563eb);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
  outline: none;
}

.actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  align-items: center;
}

.btn {
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid var(--primary-color, #2563eb);
  background: linear-gradient(120deg, #2563eb, #1d4ed8);
  color: #ffffff;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 20px rgba(37, 99, 235, 0.2);
}

.btn.ghost {
  border-color: var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  color: var(--text-primary, #111827);
}

.inline-hint {
  margin: 0;
  color: var(--text-secondary, #4b5563);
  font-size: 0.95rem;
}

.items-panel {
  display: grid;
  gap: 12px;
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 16px;
}

.items-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.pager {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pager-btn {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f3f4f6);
  cursor: pointer;
}

.pager-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pager-page {
  font-weight: var(--font-semibold, 600);
}

.table {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  overflow: hidden;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 1.8fr repeat(6, 1fr);
  gap: 8px;
  align-items: center;
  padding: 10px 12px;
}

.table-head {
  background: var(--bg-secondary, #f3f4f6);
  color: var(--text-secondary, #4b5563);
  font-size: 0.9rem;
}

.table-row:nth-child(odd) {
  background: var(--card-bg, #ffffff);
}

.table-row:nth-child(even) {
  background: var(--bg-secondary, #f8fafc);
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.thumb {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-weight: 700;
  color: #0b1225;
  background: linear-gradient(135deg, #e5e7eb, #f9fafb);
  border: 1px solid var(--border-color, #e5e7eb);
}

.thumb[data-grade*="영웅"] {
  background: linear-gradient(135deg, #c084fc, #a855f7);
  color: #fff;
}

.thumb[data-grade*="전설"] {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: #2b1a00;
}

.thumb[data-grade*="유물"] {
  background: linear-gradient(135deg, #fb7185, #f97316);
  color: #fff;
}

.thumb[data-grade*="고대"] {
  background: linear-gradient(135deg, #22d3ee, #0ea5e9);
  color: #062b3b;
}

.item-name {
  margin: 0;
  font-weight: 700;
  color: var(--text-primary, #111827);
}

.item-sub {
  margin: 2px 0 0;
  color: var(--text-secondary, #4b5563);
  font-size: 0.88rem;
}

.align-right {
  text-align: right;
}

.strong {
  font-weight: 700;
}

.muted {
  color: var(--text-secondary, #6b7280);
}

.badge {
  display: inline-flex;
  justify-content: flex-end;
  padding: 4px 8px;
  background: var(--bg-secondary, #f3f4f6);
  border-radius: 10px;
  color: var(--text-secondary, #4b5563);
  font-size: 0.9rem;
}

.empty-state,
.error-text,
.inline-loading {
  margin: 6px 0 0;
  color: var(--text-secondary, #4b5563);
}

.error-text {
  color: #b91c1c;
}

.inline-loading {
  font-size: 0.95rem;
}

@media (max-width: 1024px) {
  .control-row {
    grid-template-columns: 1fr 1fr;
  }

  .table-head,
  .table-row {
    grid-template-columns: 1.6fr repeat(3, 1fr);
  }

  .table-head span:nth-child(n+5),
  .table-row span:nth-child(n+5) {
    display: none;
  }
}

@media (max-width: 640px) {
  .auction-hero {
    padding: 16px;
  }

  .control-row {
    grid-template-columns: 1fr;
  }

  .items-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
