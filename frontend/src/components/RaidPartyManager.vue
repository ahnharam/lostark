<template>
  <div class="raid-party-page">
    <Teleport to="#raid-submenu-actions" v-if="isActive">
      <div class="subheader-actions">
        <button v-if="!me" type="button" class="btn btn-primary" @click="startDiscordLogin">Discord로 로그인</button>
        <button v-else type="button" class="btn" @click="refreshAll" :disabled="loading">새로고침</button>
      </div>
    </Teleport>

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
          <select v-model="draft.raidId" class="input" required>
            <optgroup label="에픽 레이드">
              <option v-for="raid in epicRaids" :key="raid.id" :value="raid.id">
                {{ formatRaidOptionLabel(raid) }}
              </option>
            </optgroup>
            <optgroup label="카제로스 레이드">
              <option v-for="raid in kazerosRaids" :key="raid.id" :value="raid.id">
                {{ formatRaidOptionLabel(raid) }}
              </option>
            </optgroup>
          </select>
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
        <div
          v-for="raid in raids"
          :key="raid.id"
          class="raid-card"
          :class="{ active: selectedRaid?.id === raid.id }"
          role="button"
          tabindex="0"
          @click="selectRaid(raid.id)"
          @keydown.enter.prevent="selectRaid(raid.id)"
          @keydown.space.prevent="selectRaid(raid.id)"
        >
          <div class="raid-card__top">
            <p class="raid-title">{{ raid.raidName }} <span class="muted">({{ raid.difficulty || '-' }})</span></p>
          </div>
          <p class="muted">{{ formatDateTime(raid.scheduledAt) }}</p>
          <div class="raid-card__bottom">
            <span class="pill">{{ raid.confirmedCount ?? 0 }}/{{ raid.maxParticipants ?? '-' }}</span>
            <button
              type="button"
              class="btn btn-danger btn-sm"
              :disabled="loading"
              @click.stop="deleteRaid(raid.id)"
            >
              삭제
            </button>
          </div>
        </div>
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

      <div v-else class="member-grid">
        <div v-for="p in selectedRaid.participants" :key="p.id" class="member-card">
          <span class="member-cell member-cell--name">{{ p.discordUsername || `User#${p.userId}` }}</span>
          <select
            class="input member-select member-cell--character"
            :value="p.characterName || ''"
            :disabled="loading || rosterLoadingByUserId[p.userId] || !me"
            @focus="ensureRosterLoaded(p.userId)"
            @change="event => handleCharacterChange(p.id, p.userId, event)"
          >
            <option v-if="rosterLoadingByUserId[p.userId]" value="" disabled>불러오는 중...</option>
            <option v-else value="" disabled>캐릭터 선택</option>
            <option
              v-for="opt in characterOptionsForParticipant(p)"
              :key="`${p.id}:${opt.characterName}`"
              :value="opt.characterName"
              :disabled="opt.disabled"
            >
              {{ opt.label }}
            </option>
            <option v-if="!rosterLoadingByUserId[p.userId] && !characterOptionsForParticipant(p).length" value="" disabled>
              조건을 만족하는 캐릭터가 없어요
            </option>
          </select>
          <span class="member-cell member-cell--status">
            <span class="member-status" :data-status="p.status" :title="p.changeRequestReason || undefined">
              {{ statusLabel(p.status) }}
            </span>
          </span>
          <button type="button" class="btn btn-danger btn-sm member-cell--delete" :disabled="loading" @click="removeMember(p.id)">
            삭제
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onActivated, onDeactivated, onMounted, ref } from 'vue'
import { apiClient } from '@/api/http'
import { getHttpErrorMessage } from '@/utils/httpError'
import { T4_RAIDS, type RaidDefinition } from '@/data/t4Raids'

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
  characterName?: string | null
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'CHANGE_REQUESTED'
  changeRequestReason?: string | null
  respondedAt?: string | null
}

type ExpeditionCharacterResponse = {
  characterName: string
  serverName?: string | null
  characterClassName?: string | null
  itemAvgLevel?: string | null
  itemMaxLevel?: string | null
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
const isActive = ref(false)
const rosterByUserId = ref<Record<number, ExpeditionCharacterResponse[]>>({})
const rosterLoadingByUserId = ref<Record<number, boolean>>({})

const setMessage = (value: string) => {
  message.value = value
  if (messageTimer) window.clearTimeout(messageTimer)
  messageTimer = window.setTimeout(() => {
    message.value = ''
  }, 2500)
}

const draft = ref({
  raidId: T4_RAIDS[0]?.id ?? '',
  difficulty: '노말',
  dateTimeLocal: '',
  maxParticipants: 8,
  description: '',
  includeMe: true
})

const epicRaids = computed(() => T4_RAIDS.filter(raid => raid.category === 'epic'))
const kazerosRaids = computed(() => T4_RAIDS.filter(raid => raid.category === 'kazeros'))

const getRaidMinItemLevel = (raid: RaidDefinition) => {
  const levels = raid.difficulties.map(difficulty => difficulty.minItemLevel)
  return levels.length ? Math.min(...levels) : null
}

const formatRaidOptionLabel = (raid: RaidDefinition) => {
  const min = getRaidMinItemLevel(raid)
  if (!min) return raid.name
  return `${raid.name} (${min}+)`
}

const selectedRaidName = computed(() => {
  const selected = T4_RAIDS.find(raid => raid.id === draft.value.raidId)
  return selected?.name ?? ''
})

const parseItemLevel = (value?: string | null) => {
  if (!value) return -Infinity
  const numeric = Number(String(value).replace(/[^\d.]/g, ''))
  return Number.isFinite(numeric) ? numeric : -Infinity
}

const getRequiredMinItemLevel = (schedule: RaidScheduleResponse | null) => {
  if (!schedule) return null
  const raid = T4_RAIDS.find(item => item.name === schedule.raidName)
  if (!raid) return null
  const difficultyLabel = (schedule.difficulty || '').trim()
  const difficulty = raid.difficulties.find(d => d.label === difficultyLabel)
  if (difficulty) return difficulty.minItemLevel
  const levels = raid.difficulties.map(d => d.minItemLevel)
  return levels.length ? Math.min(...levels) : null
}

const requiredMinItemLevel = computed(() => getRequiredMinItemLevel(selectedRaid.value))

type CharacterOption = {
  characterName: string
  label: string
  disabled: boolean
}

const characterOptionsForParticipant = (participant: ParticipantResponse): CharacterOption[] => {
  const roster = rosterByUserId.value[participant.userId] ?? []
  const min = requiredMinItemLevel.value
  const current = participant.characterName ?? ''

  const eligible = roster
    .filter(item => {
      if (!item.characterName) return false
      if (current && item.characterName === current) return true
      if (min == null) return true
      const level = parseItemLevel(item.itemMaxLevel ?? item.itemAvgLevel)
      return level >= min
    })
    .map(item => {
      const level = parseItemLevel(item.itemMaxLevel ?? item.itemAvgLevel)
      const hasMin = min != null && Number.isFinite(level) && level >= 0
      const suffix = hasMin ? ` (iLv ${Math.floor(level)})` : ''
      return {
        characterName: item.characterName,
        label: `${item.characterName}${suffix}`,
        disabled: min != null && current !== item.characterName && hasMin ? level < min : false
      }
    })

  const seen = new Set<string>()
  return eligible.filter(option => {
    if (seen.has(option.characterName)) return false
    seen.add(option.characterName)
    return true
  })
}

const ensureRosterLoaded = async (userId: number) => {
  if (!selectedRaid.value) return
  if (rosterByUserId.value[userId]) return
  if (rosterLoadingByUserId.value[userId]) return

  rosterLoadingByUserId.value = { ...rosterLoadingByUserId.value, [userId]: true }
  try {
    const response = await apiClient.get<ExpeditionCharacterResponse[]>(
      `/me/raids/${selectedRaid.value.id}/members/${userId}/expedition-characters`
    )
    rosterByUserId.value = { ...rosterByUserId.value, [userId]: response.data ?? [] }
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '원정대 캐릭터 목록을 불러오지 못했습니다.'
    rosterByUserId.value = { ...rosterByUserId.value, [userId]: [] }
  } finally {
    rosterLoadingByUserId.value = { ...rosterLoadingByUserId.value, [userId]: false }
  }
}

const handleCharacterChange = async (participantId: number, userId: number, event: Event) => {
  if (!selectedRaid.value) return
  const target = event.target
  if (!(target instanceof HTMLSelectElement)) return
  const next = target.value
  if (!next) return

  try {
    loading.value = true
    errorMessage.value = ''
    const response = await apiClient.patch<RaidScheduleResponse>(`/me/raids/${selectedRaid.value.id}/members/${participantId}`, {
      characterName: next
    })
    selectedRaid.value = response.data
    await loadMyRaids()
    setMessage('캐릭터를 변경했어요.')
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '캐릭터 변경에 실패했습니다.'
    await loadRaid(selectedRaid.value.id).catch(() => undefined)
  } finally {
    loading.value = false
  }
}

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
  if (!selectedRaidName.value) {
    errorMessage.value = '레이드명을 선택해 주세요.'
    return
  }
  try {
    loading.value = true
    errorMessage.value = ''
    const response = await apiClient.post<RaidScheduleResponse>('/me/raids', {
      raidKey: draft.value.raidId,
      raidName: selectedRaidName.value,
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

const deleteRaid = async (raidId: number) => {
  if (!me.value) return
  if (loading.value) return
  const ok = window.confirm('이 레이드를 삭제할까요?')
  if (!ok) return

  try {
    loading.value = true
    errorMessage.value = ''
    await apiClient.delete(`/me/raids/${raidId}`)
    if (selectedRaid.value?.id === raidId) {
      selectedRaid.value = null
    }
    await loadMyRaids()
    setMessage('레이드를 삭제했어요.')
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '레이드 삭제에 실패했습니다.'
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

onActivated(() => {
  isActive.value = true
})

onDeactivated(() => {
  isActive.value = false
})
</script>

<style scoped>
.raid-party-page {
  display: grid;
  gap: 16px;
}

.subheader-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
}

.user-pill {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  margin: 0;
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
  grid-column: span 2;
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
  grid-template-columns: repeat(auto-fit, minmax(220px, 0.25fr));
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

.raid-card__bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
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

.member-grid {
  display: grid;
  gap: 10px;
  margin-top: 12px;
  grid-template-columns: repeat(2, minmax(220px, 1fr));
}

.member-card {
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 12px;
  background: var(--card-bg);
  display: grid;
  grid-template-columns: minmax(0, 0.8fr) minmax(0, 1fr) auto auto;
  gap: 12px;
  align-items: center;
}

.member-cell {
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-cell--name {
  font-weight: 800;
}

.member-cell--character {
  color: var(--text-secondary);
  font-weight: 750;
}

.member-select {
  width: 100%;
  min-width: 0;
  padding: 6px 8px;
  border-radius: 10px;
}

.member-cell--status {
  display: flex;
  justify-content: flex-end;
}

.member-cell--delete {
  justify-self: end;
}

.member-status {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-weight: 800;
}

.member-status[data-status='PENDING'],
.member-status[data-status='CHANGE_REQUESTED'] {
  border-color: color-mix(in srgb, #9ca3af 45%, var(--border-color));
  background: color-mix(in srgb, #9ca3af 12%, var(--bg-secondary));
  color: color-mix(in srgb, #6b7280 80%, var(--text-primary));
}

.member-status[data-status='ACCEPTED'] {
  border-color: color-mix(in srgb, #16a34a 45%, var(--border-color));
  background: color-mix(in srgb, #16a34a 12%, var(--bg-secondary));
  color: color-mix(in srgb, #16a34a 78%, var(--text-primary));
}

.member-status[data-status='DECLINED'] {
  border-color: color-mix(in srgb, #dc2626 45%, var(--border-color));
  background: color-mix(in srgb, #dc2626 12%, var(--bg-secondary));
  color: color-mix(in srgb, #dc2626 78%, var(--text-primary));
}

@media (max-width: 720px) {
  .member-card {
    grid-template-columns: 1fr 1fr;
    grid-template-areas:
      'name status'
      'character delete';
    row-gap: 8px;
  }

  .member-cell--name {
    grid-area: name;
  }

  .member-cell--character {
    grid-area: character;
  }

  .member-cell--status {
    grid-area: status;
  }

  .member-cell--delete {
    grid-area: delete;
  }
}

@media (max-width: 1100px) {
  .member-grid {
    grid-template-columns: repeat(3, minmax(220px, 1fr));
  }
}

@media (max-width: 820px) {
  .member-grid {
    grid-template-columns: repeat(2, minmax(220px, 1fr));
  }
}

@media (max-width: 520px) {
  .member-grid {
    grid-template-columns: 1fr;
  }
}

.muted {
  margin: 0;
  color: var(--text-muted);
}

@media (max-width: 1024px) {
  .form-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .field--wide {
    grid-column: span 2;
  }
}
</style>
