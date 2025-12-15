<template>
  <div class="raid-party-page">
    <header class="page-header panel-card">
      <div>
        <p class="eyebrow">레이드 모집</p>
        <h2>멤버 구성 · DM 초대 · 상태 확인</h2>
        <p class="subtitle">친구 목록에서 멤버를 추가하면 Discord DM 초대가 전송됩니다.</p>
        <p v-if="me" class="user-pill">
          <span class="user-pill__label">로그인</span>
          <span class="user-pill__value">{{ me.discordUsername || me.kakaoNickname || `User#${me.id}` }}</span>
        </p>
      </div>
      <div class="header-actions">
        <button v-if="!me" type="button" class="btn btn-primary" @click="startDiscordLogin">Discord로 로그인</button>
        <button v-else type="button" class="btn" @click="refreshAll" :disabled="loading">새로고침</button>
      </div>
    </header>

    <section class="panel-card">
      <div class="section-heading">
        <div>
          <h3>레이드 생성</h3>
          <p class="section-subtitle">레이드 생성 후 멤버를 추가해 초대하세요.</p>
        </div>
      </div>

      <form class="form-grid" @submit.prevent="createRaid">
        <label class="field">
          <span class="field-label">레이드명</span>
          <input v-model.trim="draft.raidName" class="input" type="text" placeholder="예: 베히모스, 카제로스 3막" required />
        </label>

        <label class="field">
          <span class="field-label">난이도</span>
          <select v-model="draft.difficulty" class="input">
            <option value="노말">노말</option>
            <option value="하드">하드</option>
          </select>
        </label>

        <label class="field">
          <span class="field-label">일시</span>
          <input v-model="draft.dateTimeLocal" class="input" type="datetime-local" required />
        </label>

        <label class="field">
          <span class="field-label">인원</span>
          <select v-model.number="draft.maxParticipants" class="input">
            <option :value="4">4</option>
            <option :value="8">8</option>
            <option :value="16">16</option>
          </select>
        </label>

        <label class="field field--wide">
          <span class="field-label">설명 (선택)</span>
          <input v-model.trim="draft.description" class="input" type="text" placeholder="예: 20:50 집합 / 1-4관" />
        </label>

        <label class="checkbox">
          <input v-model="draft.includeMe" type="checkbox" />
          본인도 멤버로 추가
        </label>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="!me || loading">생성</button>
        </div>
      </form>

      <p v-if="message" class="notice" role="status">{{ message }}</p>
      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    </section>

    <section class="panel-card">
      <div class="section-heading">
        <div>
          <h3>내 레이드</h3>
          <p class="section-subtitle">{{ raids.length }}건</p>
        </div>
      </div>

      <div v-if="!raids.length" class="empty-state">아직 생성된 레이드가 없습니다.</div>

      <div v-else class="raid-grid">
        <button
          v-for="raid in raids"
          :key="raid.id"
          type="button"
          class="raid-card"
          :class="{ active: selectedRaid?.id === raid.id }"
          @click="selectRaid(raid.id)"
        >
          <div class="raid-card__top">
            <p class="raid-title">{{ raid.raidName }} <span class="muted">({{ raid.difficulty || '-' }})</span></p>
            <span class="pill">{{ raid.confirmedCount ?? 0 }}/{{ raid.maxParticipants ?? '-' }}</span>
          </div>
          <p class="muted">{{ formatDateTime(raid.scheduledAt) }}</p>
        </button>
      </div>
    </section>

    <section v-if="selectedRaid" class="panel-card">
      <div class="section-heading">
        <div>
          <h3>멤버 구성</h3>
          <p class="section-subtitle">친구를 추가하면 DM 초대가 발송됩니다.</p>
        </div>
        <button type="button" class="btn" @click="refreshSelectedRaid" :disabled="loading">상태 새로고침</button>
      </div>

      <div class="member-actions">
        <label class="field">
          <span class="field-label">친구 선택</span>
          <select v-model.number="selectedFriendUserId" class="input" :disabled="loading || !friends.length">
            <option :value="0">선택하세요</option>
            <option v-for="f in friends" :key="f.friendshipId" :value="f.friend.userId">
              {{ displayFriend(f) }}
            </option>
          </select>
        </label>
        <button
          type="button"
          class="btn btn-primary"
          :disabled="loading || !selectedFriendUserId"
          @click="addMembers([selectedFriendUserId])"
        >
          멤버 추가
        </button>
        <button
          v-if="me && !isMeInSelectedRaid"
          type="button"
          class="btn"
          :disabled="loading"
          @click="addMembers([me.id])"
        >
          본인 추가
        </button>
      </div>

      <div v-if="!selectedRaid.participants?.length" class="empty-state">아직 멤버가 없습니다.</div>

      <div v-else class="member-list">
        <div class="member-row member-row--head">
          <span>멤버</span>
          <span>상태</span>
          <span>응답</span>
          <span></span>
        </div>

        <div v-for="p in selectedRaid.participants" :key="p.id" class="member-row">
          <div class="member-main">
            <p class="member-name">{{ p.discordUsername || `User#${p.userId}` }}</p>
            <p v-if="p.changeRequestReason" class="member-reason">{{ p.changeRequestReason }}</p>
          </div>
          <span class="pill" :data-status="p.status">{{ statusLabel(p.status) }}</span>
          <span class="muted">{{ formatDateTime(p.respondedAt) }}</span>
          <button type="button" class="btn btn-danger btn-sm" :disabled="loading" @click="removeMember(p.id)">삭제</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { apiClient } from '@/api/http'
import { getHttpErrorMessage } from '@/utils/httpError'

// TODO(2차): 추천 조합/추천 멤버 기능
// - 직업별 포지션(서폿/딜/기타) 분류 + 시너지(버프/디버프/중복 제한) 규칙 반영
// - 레이드별 권장 구성(파티 사이즈/권장 서폿 수/최소 아이템레벨 등) 정책 기반 추천
// - 후보 필터링(최소 아이템레벨 미만 제외) + 추천 정렬(부족 역할 우선 등)

type MeResponse = {
  id: number
  kakaoNickname?: string | null
  discordUsername?: string | null
  discordId?: string | null
}

type ParticipantResponse = {
  id: number
  userId: number
  discordUsername?: string | null
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'CHANGE_REQUESTED'
  changeRequestReason?: string | null
  respondedAt?: string | null
}

type RaidScheduleResponse = {
  id: number
  raidName: string
  difficulty?: string | null
  scheduledAt: string
  description?: string | null
  status?: string | null
  maxParticipants?: number | null
  confirmedCount?: number | null
  participants?: ParticipantResponse[]
}

type FriendUserResponse = {
  userId: number
  discordUserId?: string | null
  discordUsername?: string | null
}

type FriendResponse = {
  friendshipId: number
  friend: FriendUserResponse
}

const me = ref<MeResponse | null>(null)
const raids = ref<RaidScheduleResponse[]>([])
const selectedRaid = ref<RaidScheduleResponse | null>(null)
const friends = ref<FriendResponse[]>([])
const selectedFriendUserId = ref(0)

const loading = ref(false)
const message = ref('')
const errorMessage = ref('')
let messageTimer: number | undefined

const setMessage = (value: string) => {
  message.value = value
  if (messageTimer) window.clearTimeout(messageTimer)
  messageTimer = window.setTimeout(() => {
    message.value = ''
  }, 2500)
}

const draft = ref({
  raidName: '',
  difficulty: '노말',
  dateTimeLocal: '',
  maxParticipants: 8,
  description: '',
  includeMe: true
})

const apiBaseUrl = () => (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/+$/, '')

const startDiscordLogin = () => {
  window.location.href = `${apiBaseUrl()}/oauth2/authorization/discord`
}

const formatDateTime = (value?: string | null) => {
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

const statusLabel = (status: ParticipantResponse['status']) => {
  if (status === 'ACCEPTED') return '수락'
  if (status === 'DECLINED') return '거절'
  if (status === 'CHANGE_REQUESTED') return '시간 변경 요청'
  return '대기'
}

const displayFriend = (friend: FriendResponse) => {
  const u = friend.friend
  return u.discordUsername || (u.discordUserId ? `User#${u.discordUserId}` : `User#${u.userId}`)
}

const isMeInSelectedRaid = computed(() => {
  if (!me.value || !selectedRaid.value?.participants) return false
  return selectedRaid.value.participants.some(p => p.userId === me.value?.id)
})

const fetchMe = async () => {
  try {
    const response = await apiClient.get<MeResponse>('/auth/me')
    me.value = response.data
  } catch {
    me.value = null
  }
}

const loadFriends = async () => {
  const response = await apiClient.get<FriendResponse[]>('/me/friends')
  friends.value = response.data
}

const loadMyRaids = async () => {
  const response = await apiClient.get<RaidScheduleResponse[]>('/me/raids')
  raids.value = response.data
}

const loadRaid = async (raidId: number) => {
  const response = await apiClient.get<RaidScheduleResponse>(`/me/raids/${raidId}`)
  selectedRaid.value = response.data
}

const refreshAll = async () => {
  if (!me.value) return
  try {
    loading.value = true
    errorMessage.value = ''
    await Promise.all([loadFriends(), loadMyRaids()])
    if (selectedRaid.value?.id) {
      await loadRaid(selectedRaid.value.id)
    }
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '데이터를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

const setDefaultDateTime = () => {
  if (draft.value.dateTimeLocal) return
  const now = new Date()
  now.setMinutes(Math.ceil(now.getMinutes() / 10) * 10, 0, 0)
  const pad = (v: number) => String(v).padStart(2, '0')
  const yyyy = now.getFullYear()
  const mm = pad(now.getMonth() + 1)
  const dd = pad(now.getDate())
  const hh = pad(now.getHours())
  const min = pad(now.getMinutes())
  draft.value.dateTimeLocal = `${yyyy}-${mm}-${dd}T${hh}:${min}`
}

const createRaid = async () => {
  if (!me.value) return
  try {
    loading.value = true
    errorMessage.value = ''
    const response = await apiClient.post<RaidScheduleResponse>('/me/raids', {
      raidName: draft.value.raidName,
      difficulty: draft.value.difficulty,
      scheduledAt: draft.value.dateTimeLocal,
      description: draft.value.description || null,
      maxParticipants: draft.value.maxParticipants
    })
    const created = response.data
    await loadMyRaids()
    await loadRaid(created.id)
    setMessage('레이드를 생성했어요.')
    if (draft.value.includeMe) {
      await addMembers([me.value.id])
    }
    draft.value.raidName = ''
    draft.value.description = ''
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '레이드 생성에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const selectRaid = async (raidId: number) => {
  if (!me.value) return
  selectedFriendUserId.value = 0
  try {
    loading.value = true
    errorMessage.value = ''
    await loadRaid(raidId)
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '레이드를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

const addMembers = async (userIds: number[]) => {
  if (!selectedRaid.value) return
  const normalized = userIds.filter(id => Number.isFinite(id) && id > 0)
  if (!normalized.length) return
  try {
    loading.value = true
    errorMessage.value = ''
    const response = await apiClient.post<RaidScheduleResponse>(`/me/raids/${selectedRaid.value.id}/members`, {
      userIds: normalized
    })
    selectedRaid.value = response.data
    selectedFriendUserId.value = 0
    await loadMyRaids()
    setMessage('멤버를 추가했어요.')
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '멤버 추가에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const removeMember = async (participantId: number) => {
  if (!selectedRaid.value) return
  try {
    loading.value = true
    errorMessage.value = ''
    const response = await apiClient.delete<RaidScheduleResponse>(
      `/me/raids/${selectedRaid.value.id}/members/${participantId}`
    )
    selectedRaid.value = response.data
    await loadMyRaids()
    setMessage('멤버를 삭제했어요.')
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '멤버 삭제에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const refreshSelectedRaid = async () => {
  if (!selectedRaid.value) return
  try {
    loading.value = true
    errorMessage.value = ''
    await loadRaid(selectedRaid.value.id)
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '상태를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  setDefaultDateTime()
  await apiClient.get('/auth/csrf').catch(() => undefined)
  await fetchMe()
  if (me.value) {
    await refreshAll()
  }
})
</script>

<style scoped>
.raid-party-page {
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

.panel-card {
  border-radius: 16px;
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

.input {
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

.checkbox {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: var(--text-primary);
}

.empty-state {
  padding: 14px;
  border: 1px dashed var(--border-color);
  border-radius: 12px;
  color: var(--text-muted);
}

.notice {
  margin: 10px 0 0;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.error-text {
  margin: 10px 0 0;
  color: color-mix(in srgb, #ef4444 70%, var(--text-primary));
}

.raid-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.raid-card {
  text-align: left;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 12px;
  background: var(--card-bg);
  cursor: pointer;
}

.raid-card.active {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--primary-color) 25%, transparent);
}

.raid-card__top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.raid-title {
  margin: 0;
  font-weight: 800;
  color: var(--text-primary);
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 999px;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: 0.82rem;
}

.member-actions {
  display: flex;
  gap: 10px;
  align-items: end;
  flex-wrap: wrap;
}

.member-list {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}

.member-row {
  display: grid;
  grid-template-columns: 1.8fr 120px 170px 72px;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--card-bg);
}

.member-row--head {
  background: var(--bg-secondary);
  color: var(--text-muted);
  font-size: 0.85rem;
}

.member-main {
  display: grid;
  gap: 2px;
}

.member-name {
  margin: 0;
  font-weight: 700;
  color: var(--text-primary);
}

.member-reason {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.85rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.muted {
  margin: 0;
  color: var(--text-muted);
}

.pill[data-status='ACCEPTED'] {
  background: color-mix(in srgb, #22c55e 14%, var(--bg-secondary));
  color: color-mix(in srgb, #22c55e 70%, var(--text-primary));
}

.pill[data-status='DECLINED'] {
  background: color-mix(in srgb, #ef4444 14%, var(--bg-secondary));
  color: color-mix(in srgb, #ef4444 70%, var(--text-primary));
}

.pill[data-status='CHANGE_REQUESTED'] {
  background: color-mix(in srgb, #f59e0b 16%, var(--bg-secondary));
  color: color-mix(in srgb, #f59e0b 70%, var(--text-primary));
}

@media (max-width: 1024px) {
  .form-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .field--wide {
    grid-column: span 2;
  }

  .member-row {
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .member-row--head {
    display: none;
  }
}

@media (max-width: 640px) {
  .raid-party-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
  }
}
</style>
