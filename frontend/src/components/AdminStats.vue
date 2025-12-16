<template>
  <div class="admin-page">
    <TopPageHeader>
      <div class="admin-head">
        <div>
          <p class="eyebrow">관리자 · 테스트</p>
          <div class="layout-title-row">
            <MenuAnchor />
            <h3>거래소 일별 기록 모니터</h3>
          </div>
          <p class="muted">DB에 저장된 일별 스냅샷을 빠르게 확인하고, 수동 캡처를 실행해 보세요.</p>
        </div>
        <div class="actions">
          <button class="btn" type="button" :disabled="triggering" @click="triggerCapture()">
            {{ triggering ? '기록 중...' : '기록 시작하기 (전일)' }}
          </button>
          <div class="date-run">
            <input v-model="targetDate" type="date" class="input" />
            <button class="btn ghost" type="button" :disabled="triggering || !targetDate" @click="triggerCapture(targetDate)">
              지정일 캡처
            </button>
          </div>
          <button class="btn ghost" type="button" :disabled="loading" @click="loadStats">
            새로고침
          </button>
        </div>
      </div>
    </TopPageHeader>

    <section class="panel">
      <div v-if="gateEnabled && !unlocked" class="gate">
        <span class="muted">관리 접근</span>
        <input v-model="passwordInput" type="password" class="input" placeholder="비밀번호" />
        <button class="btn ghost" type="button" @click="unlock">입장</button>
        <p v-if="error" class="error-text">{{ error }}</p>
      </div>

      <div v-if="gateEnabled && !unlocked" class="muted">비밀번호가 없으면 env에 VITE_ADMIN_PASSWORD를 비워두세요.</div>

      <div class="panel-head">
        <div>
          <p class="eyebrow">최근 기록</p>
          <h4>총 {{ totalElements }}건</h4>
        </div>
        <span v-if="message" class="muted">{{ message }}</span>
      </div>

      <div class="filter-row">
        <input
          v-model.trim="query"
          class="input"
          type="text"
          placeholder="아이템 이름 또는 ID 검색"
          @keyup.enter="handleSearch"
        />
        <button class="btn ghost" type="button" @click="handleSearch">검색</button>
        <button class="btn ghost" type="button" @click="() => { query = ''; handleSearch() }">초기화</button>
      </div>

      <div v-if="running" class="overlay">
        <LoadingSpinner message="데이터 수집 중입니다. 완료되면 최신 기록을 보여드려요." />
      </div>

      <LoadingSpinner v-else-if="loading" message="불러오는 중..." />
      <p v-else-if="error" class="error-text">{{ error }}</p>
      <p v-else-if="!stats.length" class="empty">저장된 기록이 없습니다.</p>
      <div v-else class="table">
        <div class="table-head">
          <span>날짜</span>
          <span>아이템</span>
          <span>아이템ID</span>
          <span>카테고리</span>
          <span class="align-right">최저가</span>
          <span class="align-right">평균가</span>
          <span class="align-right">거래건수</span>
          <span class="align-right">거래량</span>
        </div>
        <div v-for="stat in stats" :key="`${stat.apiItemId}-${stat.statDate}-${stat.id}`" class="table-row">
          <span>{{ stat.statDate }}</span>
          <span class="item-col">
            <img v-if="stat.icon" :src="stat.icon" alt="" class="thumb" />
            <span>{{ stat.itemName || '이름 없음' }}</span>
          </span>
          <span>{{ stat.apiItemId }}</span>
          <span>{{ stat.categoryCode }}</span>
          <span class="align-right">{{ formatNumber(stat.minPrice) }}</span>
          <span class="align-right">{{ formatNumber(stat.avgPrice) }}</span>
          <span class="align-right">{{ formatNumber(stat.tradeCount) }}</span>
          <span class="align-right">{{ formatNumber(stat.tradeVolume) }}</span>
        </div>
      </div>

      <div v-if="totalPages > 1" class="pager">
        <button class="btn ghost" type="button" :disabled="page === 0 || loading" @click="goPage(page - 1)">이전</button>
        <span class="pager-label">{{ page + 1 }} / {{ totalPages }}</span>
        <button class="btn ghost" type="button" :disabled="page + 1 >= totalPages || loading" @click="goPage(page + 1)">
          다음
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, onBeforeUnmount } from 'vue'
import { lostarkApi } from '@/api/lostark'
import type { MarketDailyStat } from '@/api/types'
import { getHttpErrorMessage } from '@/utils/httpError'
import LoadingSpinner from './common/LoadingSpinner.vue'
import TopPageHeader from './common/TopPageHeader.vue'
import MenuAnchor from './common/MenuAnchor.vue'

const stats = ref<MarketDailyStat[]>([])
const page = ref(0)
const pageSize = ref(20)
const totalPages = ref(1)
const totalElements = ref(0)
const query = ref('')
const loading = ref(false)
const triggering = ref(false)
const error = ref('')
const message = ref('')
const targetDate = ref('')
const passwordInput = ref('')
const unlocked = ref(false)
const gateEnabled = Boolean(import.meta.env.VITE_ADMIN_PASSWORD)
const ADMIN_KEY = 'admin_stats_unlocked'
const running = ref(false)
let statusTimer: number | undefined

const formatNumber = (value?: number | null) => {
  if (value === null || value === undefined) return '-'
  return new Intl.NumberFormat('ko-KR').format(value)
}

const resolveErrorMessage = (err: unknown, fallback: string) => getHttpErrorMessage(err) ?? fallback

const loadStats = async () => {
  if (gateEnabled && !unlocked.value) return
  if (running.value) return
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    const data = await lostarkApi.getMarketDailyStatsRecent({ page: page.value, size: pageSize.value, q: query.value || undefined })
    stats.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
    message.value = `${data.totalElements}건 중 ${data.page + 1} / ${data.totalPages}페이지`
  } catch (err: unknown) {
    error.value = resolveErrorMessage(err, '기록을 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

const triggerCapture = async (date?: string) => {
  if (gateEnabled && !unlocked.value) return
  if (running.value) return
  triggering.value = true
  error.value = ''
  message.value = ''
  try {
    const resp = await lostarkApi.triggerMarketStatsCapture(date)
    message.value = resp || '기록 요청을 전송했습니다.'
    await loadStats()
  } catch (err: unknown) {
    error.value = resolveErrorMessage(err, '기록을 시작하지 못했습니다.')
  } finally {
    triggering.value = false
  }
}

const goPage = (next: number) => {
  if (next < 0 || next >= totalPages.value) return
  page.value = next
  loadStats()
}

const handleSearch = () => {
  page.value = 0
  loadStats()
}

const pollStatus = async () => {
  const wasRunning = running.value
  try {
    const status = await lostarkApi.getMarketStatsStatus()
    running.value = status.running
  } catch {
    // ignore polling errors
  }
  if (wasRunning && !running.value) {
    loadStats()
  }
}

const unlock = () => {
  if (!gateEnabled) {
    unlocked.value = true
    return
  }
  if (passwordInput.value && passwordInput.value === import.meta.env.VITE_ADMIN_PASSWORD) {
    unlocked.value = true
    localStorage.setItem(ADMIN_KEY, '1')
    message.value = '접속 허용됨'
    error.value = ''
    loadStats()
  } else {
    error.value = '비밀번호가 올바르지 않습니다.'
  }
}

onMounted(() => {
  if (!gateEnabled) {
    unlocked.value = true
    loadStats()
  } else {
    const saved = localStorage.getItem(ADMIN_KEY)
    if (saved === '1') {
      unlocked.value = true
      loadStats()
    }
  }
  pollStatus()
  statusTimer = window.setInterval(pollStatus, 5000)
})

onBeforeUnmount(() => {
  if (statusTimer) {
    window.clearInterval(statusTimer)
  }
})
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
}

.admin-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.date-run {
  display: flex;
  gap: 6px;
  align-items: center;
}

.panel {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  background: var(--card-bg, #ffffff);
  padding: 14px;
  display: grid;
  gap: 12px;
}

.filter-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.gate {
  border: 1px dashed var(--border-color, #e5e7eb);
  padding: 12px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.table {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 10px;
  overflow: hidden;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 1fr 1.2fr 0.8fr repeat(5, 1fr);
  gap: 8px;
  padding: 10px 12px;
  align-items: center;
}

.table-head {
  background: var(--bg-secondary, #f3f4f6);
  color: var(--text-secondary, #4b5563);
  font-weight: 600;
}

.table-row:nth-child(odd) {
  background: var(--bg-secondary, #f9fafb);
}

.item-col {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.thumb {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  border: 1px solid var(--border-color, #e5e7eb);
  object-fit: cover;
  background: var(--bg-secondary, #f3f4f6);
}

.align-right {
  text-align: right;
}

.pager {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pager-label {
  font-weight: 600;
}

.overlay {
  border: 1px dashed var(--border-color, #e5e7eb);
  border-radius: 10px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
}

.btn {
  padding: 9px 12px;
  border-radius: 10px;
  border: 1px solid var(--primary-color, #2563eb);
  background: linear-gradient(120deg, #2563eb, #1d4ed8);
  color: #fff;
  cursor: pointer;
}

.btn.ghost {
  background: var(--card-bg, #fff);
  color: var(--text-primary, #111827);
  border-color: var(--border-color, #e5e7eb);
}

.input {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid var(--border-color, #e5e7eb);
}

.empty {
  color: var(--text-secondary, #6b7280);
}

.error-text {
  color: #b91c1c;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.02em;
  font-size: 0.82rem;
  color: var(--text-secondary, #6b7280);
  margin: 0 0 4px;
}

.muted {
  color: var(--text-secondary, #6b7280);
}
</style>
