<template>
  <div class="admin-raid-catalog">
    <Teleport to="#admin-submenu-actions" v-if="isActive">
      <div class="actions">
        <button class="btn ghost" type="button" :disabled="loading" @click="loadCatalog">
          새로고침
        </button>
      </div>
    </Teleport>

    <section class="panel">
      <div v-if="gateEnabled && !unlocked" class="gate">
        <span class="muted">관리 접근</span>
        <input v-model="passwordInput" type="password" class="input" placeholder="비밀번호" />
        <button class="btn ghost" type="button" @click="unlock">입장</button>
        <p v-if="error" class="error-text">{{ error }}</p>
      </div>

      <div v-if="gateEnabled && !unlocked" class="muted">비밀번호가 없으면 env에 VITE_ADMIN_PASSWORD를 비워두세요.</div>

      <template v-else>
        <div class="panel-head">
          <div>
            <p class="eyebrow">레이드 카탈로그</p>
            <h4>총 {{ rows.length }}건</h4>
          </div>
          <span v-if="message" class="muted">{{ message }}</span>
        </div>

        <form class="form-row" @submit.prevent="createRaid">
          <label class="field">
            <span class="field-label">raidKey</span>
            <input v-model.trim="draft.raidKey" class="input" type="text" placeholder="예: kazeros-act-5" required />
          </label>
          <label class="field field--wide">
            <span class="field-label">raidName</span>
            <input v-model.trim="draft.raidName" class="input" type="text" placeholder="예: 5막: ..." required />
          </label>
          <label class="checkbox">
            <input v-model="draft.active" type="checkbox" />
            활성
          </label>
          <button class="btn btn-primary" type="submit" :disabled="loading">추가</button>
        </form>

        <LoadingSpinner v-if="loading" message="불러오는 중..." />
        <p v-else-if="error" class="error-text">{{ error }}</p>
        <p v-else-if="!rows.length" class="empty">등록된 레이드가 없습니다.</p>
        <div v-else class="table">
          <div class="table-head">
            <span>raidKey</span>
            <span>raidName</span>
            <span>상태</span>
            <span>생성일</span>
            <span class="align-right">관리</span>
          </div>
          <div v-for="row in rows" :key="row.raidKey" class="table-row">
            <span class="mono">{{ row.raidKey }}</span>
            <span>{{ row.raidName }}</span>
            <span>
              <span class="pill" :class="{ off: !row.active }">{{ row.active ? '활성' : '비활성' }}</span>
            </span>
            <span class="muted">{{ formatDate(row.createdAt) }}</span>
            <span class="align-right">
              <button class="btn ghost btn-sm" type="button" :disabled="loading" @click="toggleActive(row)">
                {{ row.active ? '비활성화' : '활성화' }}
              </button>
            </span>
          </div>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onActivated, onDeactivated, onMounted, ref } from 'vue'
import { lostarkApi } from '@/api/lostark'
import { getHttpErrorMessage } from '@/utils/httpError'
import LoadingSpinner from './common/LoadingSpinner.vue'

type RaidCatalogEntry = {
  raidKey: string
  raidName: string
  active: boolean
  createdAt?: string | null
}

const rows = ref<RaidCatalogEntry[]>([])
const loading = ref(false)
const error = ref('')
const message = ref('')

const draft = ref({
  raidKey: '',
  raidName: '',
  active: true
})

const passwordInput = ref('')
const unlocked = ref(false)
const gateEnabled = Boolean(import.meta.env.VITE_ADMIN_PASSWORD)
const ADMIN_KEY = 'admin_unlocked'

const isActive = ref(false)

const resolveErrorMessage = (err: unknown, fallback: string) => getHttpErrorMessage(err) ?? fallback

const formatDate = (value?: string | null) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const loadCatalog = async () => {
  if (gateEnabled && !unlocked.value) return
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    rows.value = await lostarkApi.getRaidCatalog()
    message.value = `${rows.value.length}건`
  } catch (err: unknown) {
    error.value = resolveErrorMessage(err, '레이드 목록을 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

const createRaid = async () => {
  if (gateEnabled && !unlocked.value) return
  if (!draft.value.raidKey.trim() || !draft.value.raidName.trim()) return
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    await lostarkApi.createRaidCatalog({
      raidKey: draft.value.raidKey.trim(),
      raidName: draft.value.raidName.trim(),
      active: draft.value.active
    })
    draft.value.raidKey = ''
    draft.value.raidName = ''
    draft.value.active = true
    message.value = '추가했어요.'
    await loadCatalog()
  } catch (err: unknown) {
    error.value = resolveErrorMessage(err, '레이드 추가에 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const toggleActive = async (row: RaidCatalogEntry) => {
  if (gateEnabled && !unlocked.value) return
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    const updated = await lostarkApi.updateRaidCatalog(row.raidKey, { active: !row.active })
    rows.value = rows.value.map(item => (item.raidKey === updated.raidKey ? updated : item))
    message.value = '변경했어요.'
  } catch (err: unknown) {
    error.value = resolveErrorMessage(err, '상태 변경에 실패했습니다.')
  } finally {
    loading.value = false
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
    loadCatalog()
  } else {
    error.value = '비밀번호가 올바르지 않습니다.'
  }
}

onMounted(() => {
  if (!gateEnabled) {
    unlocked.value = true
    loadCatalog()
  } else {
    const saved = localStorage.getItem(ADMIN_KEY)
    if (saved === '1') {
      unlocked.value = true
      loadCatalog()
    }
  }
})

onActivated(() => {
  isActive.value = true
})

onDeactivated(() => {
  isActive.value = false
})
</script>

<style scoped>
.admin-raid-catalog {
  display: grid;
  gap: 16px;
}

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-end;
}

.panel {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  background: var(--card-bg, #ffffff);
  padding: 14px;
  display: grid;
  gap: 12px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 2fr auto auto;
  gap: 10px;
  align-items: end;
}

.field {
  display: grid;
  gap: 6px;
}

.field--wide {
  min-width: 220px;
}

.field-label {
  font-size: 0.85rem;
  color: var(--text-secondary, #4b5563);
}

.checkbox {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  user-select: none;
  color: var(--text-secondary, #4b5563);
}

.gate {
  border: 1px dashed var(--border-color, #e5e7eb);
  padding: 12px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.table {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 10px;
  overflow: hidden;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 1.2fr 1.5fr 0.7fr 1fr 0.8fr;
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

.pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(34, 197, 94, 0.35);
  background: rgba(34, 197, 94, 0.12);
  color: #15803d;
  font-weight: 700;
  font-size: 0.82rem;
}

.pill.off {
  border-color: rgba(107, 114, 128, 0.35);
  background: rgba(107, 114, 128, 0.12);
  color: #374151;
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

.btn-sm {
  padding: 7px 10px;
  border-radius: 10px;
  font-size: 0.85rem;
}

.btn-primary {
  border-color: var(--primary-color, #2563eb);
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

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.align-right {
  text-align: right;
}

@media (max-width: 860px) {
  .form-row {
    grid-template-columns: 1fr;
    align-items: stretch;
  }
  .field--wide {
    min-width: unset;
  }
  .align-right {
    text-align: left;
  }
  .table-head,
  .table-row {
    grid-template-columns: 1fr;
  }
}
</style>

