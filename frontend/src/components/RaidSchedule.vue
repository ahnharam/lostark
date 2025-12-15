<template>
  <div class="raid-schedule-page">
    <header class="page-header panel-card">
      <div>
        <p class="eyebrow">4티어 레이드</p>
        <h2>레이드 일정 생성 · 공유</h2>
        <p class="subtitle">에픽 레이드 / 카제로스 레이드 일정표를 만들고 링크로 공유할 수 있어요.</p>
        <p v-if="me" class="user-pill">
          <span class="user-pill__label">로그인</span>
          <span class="user-pill__value">{{ me.discordUsername || me.kakaoNickname || `User#${me.id}` }}</span>
        </p>
      </div>
      <div class="header-actions">
        <button v-if="!me" type="button" class="btn btn-primary" @click="startDiscordLogin">Discord로 로그인</button>
        <button v-else type="button" class="btn" @click="logout">로그아웃</button>
        <button
          v-if="me"
          type="button"
          class="btn"
          @click="loadFromServer"
        >
          서버에서 불러오기
        </button>
        <button
          v-if="me"
          type="button"
          class="btn btn-primary"
          @click="saveToServer"
          :disabled="saving"
        >
          서버에 저장
        </button>
        <button type="button" class="btn" @click="resetSchedule" :disabled="entries.length === 0">초기화</button>
        <button type="button" class="btn btn-primary" @click="copyShareLink" :disabled="entries.length === 0">
          공유 링크 복사
        </button>
      </div>
    </header>

    <section class="panel-card">
      <div class="section-heading">
        <div>
          <h3>일정 추가</h3>
          <p class="section-subtitle">레이드 / 난이도 / 관문 / 시간만 선택하면 됩니다.</p>
        </div>
        <span v-if="selectedDifficulty" class="inline-pill">
          최소 입장 레벨 {{ selectedDifficulty.minItemLevel }}
        </span>
      </div>

      <form class="form-grid" @submit.prevent="addEntry">
        <label class="field">
          <span class="field-label">레이드</span>
          <select v-model="draft.raidId" class="field-input" required>
            <optgroup label="에픽 레이드">
              <option v-for="raid in epicRaids" :key="raid.id" :value="raid.id">
                {{ raid.name }}
              </option>
            </optgroup>
            <optgroup label="카제로스 레이드">
              <option v-for="raid in kazerosRaids" :key="raid.id" :value="raid.id">
                {{ raid.name }}
              </option>
            </optgroup>
          </select>
        </label>

        <label class="field">
          <span class="field-label">난이도</span>
          <select v-model="draft.difficultyKey" class="field-input" required>
            <option v-for="difficulty in difficultiesForSelectedRaid" :key="difficulty.key" :value="difficulty.key">
              {{ difficulty.label }} ({{ difficulty.minItemLevel }}+)
            </option>
          </select>
        </label>

        <label class="field">
          <span class="field-label">관문</span>
          <select v-model.number="draft.gate" class="field-input">
            <option :value="0">전체</option>
            <option v-for="gateNumber in gatesForSelectedRaid" :key="gateNumber" :value="gateNumber">{{ gateNumber }}관문</option>
          </select>
        </label>

        <label class="field">
          <span class="field-label">일시</span>
          <input v-model="draft.dateTimeLocal" type="datetime-local" class="field-input" required />
        </label>

        <label class="field field--wide">
          <span class="field-label">메모 (선택)</span>
          <input v-model.trim="draft.memo" type="text" class="field-input" placeholder="예: 20:50 집합 / 디코 필참" />
        </label>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="!canAdd">추가</button>
        </div>
      </form>
    </section>

    <section class="panel-card">
      <div class="section-heading">
        <div>
          <h3>내 일정</h3>
          <p class="section-subtitle">{{ entries.length }}건</p>
        </div>
      </div>

      <div v-if="entries.length === 0" class="empty-state">아직 일정이 없습니다. 위에서 일정을 추가해 주세요.</div>

      <div v-else class="entry-list">
        <div class="entry-row entry-row--head">
          <span>일시</span>
          <span>레이드</span>
          <span>난이도</span>
          <span>관문</span>
          <span>인원</span>
          <span></span>
        </div>

        <div v-for="entry in sortedEntries" :key="entry.id" class="entry-row">
          <span class="mono">{{ formatDateTimeLocal(entry.dateTimeLocal) }}</span>
          <div class="entry-raid">
            <span class="entry-raid__name">{{ getRaidName(entry.raidId) }}</span>
            <span v-if="entry.memo" class="entry-raid__memo">{{ entry.memo }}</span>
          </div>
          <span>{{ getDifficultyLabel(entry.raidId, entry.difficultyKey) }}</span>
          <span>{{ entry.gate ? `${entry.gate}관문` : '전체' }}</span>
          <span>{{ getRaidPlayers(entry.raidId) }}명</span>
          <button type="button" class="btn btn-danger btn-sm" @click="removeEntry(entry.id)">삭제</button>
        </div>
      </div>
    </section>

    <p v-if="notice" class="notice" role="status">{{ notice }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { T4_RAIDS, type RaidDifficultyKey } from '@/data/t4Raids'
import { decodeBase64Url, encodeBase64Url } from '@/utils/base64url'
import { apiClient } from '@/api/http'

type RaidScheduleEntry = {
  id: string
  raidId: string
  difficultyKey: RaidDifficultyKey
  gate: number
  dateTimeLocal: string
  memo?: string
}

type RaidScheduleSharePayloadV1 = {
  v: 1
  entries: RaidScheduleEntry[]
}

type MeResponse = {
  id: number
  kakaoNickname?: string | null
  discordUsername?: string | null
  discordId?: string | null
}

type RaidCalendarEntryResponse = {
  id: number
  raidId: string
  difficultyKey: RaidDifficultyKey
  gate: number
  scheduledAt: string
  memo?: string | null
}

const STORAGE_KEY = 'loap.raidSchedule.v1'

const router = useRouter()
const route = useRoute()

const notice = ref('')
let noticeTimer: number | undefined
const me = ref<MeResponse | null>(null)
const saving = ref(false)
const loadingServer = ref(false)
let suppressPersist = false

const setNotice = (message: string) => {
  notice.value = message
  if (noticeTimer) window.clearTimeout(noticeTimer)
  noticeTimer = window.setTimeout(() => {
    notice.value = ''
  }, 2500)
}

const makeId = () => {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) return crypto.randomUUID()
  return `entry_${Date.now()}_${Math.random().toString(16).slice(2)}`
}

const epicRaids = computed(() => T4_RAIDS.filter(raid => raid.category === 'epic'))
const kazerosRaids = computed(() => T4_RAIDS.filter(raid => raid.category === 'kazeros'))

const entries = ref<RaidScheduleEntry[]>([])

const draft = ref<{
  raidId: string
  difficultyKey: RaidDifficultyKey
  gate: number
  dateTimeLocal: string
  memo: string
}>({
  raidId: T4_RAIDS[0]?.id ?? '',
  difficultyKey: 'normal',
  gate: 0,
  dateTimeLocal: '',
  memo: '',
})

const selectedRaid = computed(() => T4_RAIDS.find(raid => raid.id === draft.value.raidId))
const difficultiesForSelectedRaid = computed(() => selectedRaid.value?.difficulties ?? [])
const selectedDifficulty = computed(() =>
  difficultiesForSelectedRaid.value.find(difficulty => difficulty.key === draft.value.difficultyKey)
)
const gatesForSelectedRaid = computed(() => {
  const count = selectedRaid.value?.gates ?? 0
  return Array.from({ length: count }, (_, index) => index + 1)
})

watch(
  () => draft.value.raidId,
  () => {
    const nextDifficulty = difficultiesForSelectedRaid.value[0]
    draft.value.difficultyKey = nextDifficulty?.key ?? 'normal'
    draft.value.gate = 0
  }
)

const canAdd = computed(() => {
  if (!draft.value.raidId) return false
  if (!draft.value.dateTimeLocal) return false
  return difficultiesForSelectedRaid.value.some(difficulty => difficulty.key === draft.value.difficultyKey)
})

const formatDateTimeLocal = (dateTimeLocal: string) => dateTimeLocal.replace('T', ' ')

const getRaidName = (raidId: string) => T4_RAIDS.find(raid => raid.id === raidId)?.name ?? raidId
const getRaidPlayers = (raidId: string) => T4_RAIDS.find(raid => raid.id === raidId)?.players ?? 0
const getDifficultyLabel = (raidId: string, difficultyKey: RaidDifficultyKey) => {
  const raid = T4_RAIDS.find(item => item.id === raidId)
  const difficulty = raid?.difficulties.find(item => item.key === difficultyKey)
  return difficulty?.label ?? difficultyKey
}

const sortedEntries = computed(() =>
  [...entries.value].sort((a, b) => a.dateTimeLocal.localeCompare(b.dateTimeLocal))
)

const persist = () => {
  if (me.value) return
  const payload: RaidScheduleSharePayloadV1 = { v: 1, entries: entries.value }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
}

watch(
  entries,
  () => {
    if (suppressPersist) return
    persist()
  },
  { deep: true }
)

const addEntry = () => {
  if (!canAdd.value) return
  entries.value.push({
    id: makeId(),
    raidId: draft.value.raidId,
    difficultyKey: draft.value.difficultyKey,
    gate: draft.value.gate,
    dateTimeLocal: draft.value.dateTimeLocal,
    memo: draft.value.memo || undefined,
  })
  draft.value.memo = ''
}

const removeEntry = (id: string) => {
  entries.value = entries.value.filter(entry => entry.id !== id)
}

const resetSchedule = () => {
  entries.value = []
  localStorage.removeItem(STORAGE_KEY)
  void router.replace({ query: { ...route.query, rs: undefined } })
  setNotice('일정을 초기화했어요.')
}

const encodeSharePayload = () => {
  const payload: RaidScheduleSharePayloadV1 = { v: 1, entries: entries.value }
  return encodeBase64Url(JSON.stringify(payload))
}

const loadSharePayload = (payload: RaidScheduleSharePayloadV1) => {
  if (payload.v !== 1) throw new Error('Unsupported payload version')
  if (!Array.isArray(payload.entries)) throw new Error('Invalid payload')
  entries.value = payload.entries
}

const maybeLoadFromQuery = () => {
  const rs = route.query.rs
  if (typeof rs !== 'string' || !rs) return false
  try {
    const decoded = decodeBase64Url(rs)
    const parsed = JSON.parse(decoded) as RaidScheduleSharePayloadV1
    loadSharePayload(parsed)
    setNotice('공유 링크에서 일정을 불러왔어요.')
    return true
  } catch {
    setNotice('공유 링크를 해석하지 못했어요.')
    return false
  }
}

const maybeLoadFromStorage = () => {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return
  try {
    const parsed = JSON.parse(raw) as RaidScheduleSharePayloadV1
    loadSharePayload(parsed)
  } catch {
    localStorage.removeItem(STORAGE_KEY)
  }
}

const apiBaseUrl = () => (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/+$/, '')

const startDiscordLogin = () => {
  window.location.href = `${apiBaseUrl()}/oauth2/authorization/discord`
}

const fetchMe = async () => {
  try {
    const response = await apiClient.get<MeResponse>('/auth/me')
    me.value = response.data
  } catch {
    me.value = null
  }
}

const loadFromServer = async () => {
  if (!me.value || loadingServer.value) return
  loadingServer.value = true
  try {
    const response = await apiClient.get<RaidCalendarEntryResponse[]>('/me/raid-calendar')
    suppressPersist = true
    entries.value =
      response.data?.map(item => ({
        id: String(item.id),
        raidId: item.raidId,
        difficultyKey: item.difficultyKey,
        gate: item.gate ?? 0,
        dateTimeLocal: (item.scheduledAt || '').slice(0, 16),
        memo: item.memo ?? undefined,
      })) ?? []
    setNotice('서버에서 일정을 불러왔어요.')
  } catch {
    setNotice('서버에서 일정을 불러오지 못했어요.')
  } finally {
    suppressPersist = false
    loadingServer.value = false
  }
}

const saveToServer = async () => {
  if (!me.value || saving.value) return
  saving.value = true
  try {
    await apiClient.put(
      '/me/raid-calendar',
      entries.value.map(entry => ({
        raidId: entry.raidId,
        difficultyKey: entry.difficultyKey,
        gate: entry.gate,
        scheduledAt: entry.dateTimeLocal,
        memo: entry.memo ?? null,
      }))
    )
    setNotice('서버에 저장했어요.')
  } catch {
    setNotice('서버 저장에 실패했어요.')
  } finally {
    saving.value = false
  }
}

const logout = async () => {
  try {
    await apiClient.post('/auth/logout')
  } catch {
    // ignore
  } finally {
    me.value = null
    setNotice('로그아웃했어요.')
  }
}

const copyShareLink = async () => {
  const rs = encodeSharePayload()
  const resolved = router.resolve({
    name: 'main',
    params: { menu: 'raid-schedule' },
    query: { rs },
  })

  const url = new URL(resolved.href, window.location.origin).toString()
  try {
    await navigator.clipboard.writeText(url)
    setNotice('공유 링크를 복사했어요.')
  } catch {
    window.prompt('아래 링크를 복사해 주세요.', url)
  }
}

const setDefaultDateTime = () => {
  if (draft.value.dateTimeLocal) return
  const now = new Date()
  now.setMinutes(Math.ceil(now.getMinutes() / 10) * 10, 0, 0)
  const pad = (value: number) => String(value).padStart(2, '0')
  const yyyy = now.getFullYear()
  const mm = pad(now.getMonth() + 1)
  const dd = pad(now.getDate())
  const hh = pad(now.getHours())
  const min = pad(now.getMinutes())
  draft.value.dateTimeLocal = `${yyyy}-${mm}-${dd}T${hh}:${min}`
}

onMounted(() => {
  setDefaultDateTime()
  const loadedFromQuery = maybeLoadFromQuery()
  void apiClient
    .get('/auth/csrf')
    .catch(() => undefined)
    .then(() => fetchMe())
    .then(async () => {
    if (me.value) {
      if (loadedFromQuery) {
        setNotice('공유 링크로 불러온 일정입니다. 필요하면 "서버에 저장"을 눌러주세요.')
        return
      }
      await loadFromServer()
      return
    }
    if (!loadedFromQuery) {
      maybeLoadFromStorage()
    }
  })
})
</script>

<style scoped>
.raid-schedule-page {
  display: grid;
  gap: 16px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 0.85rem;
  color: var(--text-muted);
}

h2 {
  margin: 0;
  color: var(--text-primary);
}

.subtitle {
  margin: 8px 0 0;
  color: var(--text-muted);
  font-size: 0.95rem;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.user-pill {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  margin: 10px 0 0;
  padding: 6px 10px;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  width: fit-content;
}

.user-pill__label {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.user-pill__value {
  font-weight: 700;
  font-size: 0.9rem;
}

.form-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  align-items: end;
}

.field {
  display: grid;
  gap: 6px;
}

.field--wide {
  grid-column: span 3;
}

.field-label {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.field-input {
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 10px 12px;
  background: var(--card-bg);
  color: var(--text-primary);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.btn {
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}

.btn:hover {
  background: var(--bg-hover);
  transform: translateY(-1px);
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

.btn-primary {
  border-color: var(--primary-color);
  background: var(--primary-color);
  color: #fff;
}

.btn-primary:hover {
  background: color-mix(in srgb, var(--primary-color) 88%, #000);
}

.btn-danger {
  border-color: color-mix(in srgb, #ef4444 55%, var(--border-color));
  background: color-mix(in srgb, #ef4444 10%, var(--bg-secondary));
  color: color-mix(in srgb, #ef4444 70%, var(--text-primary));
}

.btn-sm {
  padding: 6px 10px;
  border-radius: 8px;
}

.empty-state {
  padding: 14px;
  border: 1px dashed var(--border-color);
  border-radius: 12px;
  color: var(--text-muted);
}

.entry-list {
  display: grid;
  gap: 8px;
}

.entry-row {
  display: grid;
  grid-template-columns: 160px 1.6fr 120px 90px 70px 72px;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--card-bg);
}

.entry-row--head {
  background: var(--bg-secondary);
  font-size: 0.85rem;
  color: var(--text-muted);
  border-style: solid;
}

.entry-raid {
  display: grid;
  gap: 2px;
}

.entry-raid__name {
  color: var(--text-primary);
  font-weight: 600;
}

.entry-raid__memo {
  color: var(--text-muted);
  font-size: 0.85rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mono {
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.notice {
  margin: 0;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
}

@media (max-width: 1024px) {
  .form-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .field--wide {
    grid-column: span 2;
  }

  .entry-row {
    grid-template-columns: 140px 1fr 100px 80px 60px 72px;
  }
}

@media (max-width: 640px) {
  .raid-schedule-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
  }

  .entry-row,
  .entry-row--head {
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .entry-row--head {
    display: none;
  }
}
</style>
