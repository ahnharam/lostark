<template>
  <div class="friends-page">
    <header class="page-header panel-card">
      <div>
        <p class="eyebrow">친구 관리</p>
        <h2>디스코드 ID로 친구 추가</h2>
        <p class="subtitle">친구 요청은 상대가 DM에서 수락해야 완료됩니다.</p>
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
          <h3>친구 추가</h3>
          <p class="section-subtitle">상대의 Discord User ID(숫자)를 입력하세요.</p>
        </div>
      </div>

      <div class="add-row">
        <input
          v-model.trim="discordUserId"
          class="input"
          type="text"
          inputmode="numeric"
          placeholder="예: 123456789012345678"
          :disabled="!me || loading"
        />
        <button type="button" class="btn btn-primary" :disabled="!me || loading || !discordUserId" @click="sendRequest">
          요청 보내기
        </button>
      </div>
      <p v-if="message" class="notice" role="status">{{ message }}</p>
      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    </section>

    <section class="panel-card">
      <div class="section-heading">
        <div>
          <h3>받은 요청</h3>
          <p class="section-subtitle">{{ requests.incoming.length }}건</p>
        </div>
      </div>

      <div v-if="!requests.incoming.length" class="empty-state">받은 친구 요청이 없습니다.</div>

      <div v-else class="request-list">
        <div v-for="req in requests.incoming" :key="req.requestId" class="request-row">
          <div class="request-main">
            <p class="request-name">{{ displayUser(req.requester) }}</p>
            <p class="muted">{{ formatDateTime(req.createdAt) }}</p>
          </div>
          <div class="request-actions">
            <button type="button" class="btn btn-primary btn-sm" :disabled="loading" @click="accept(req.requestId)">
              수락
            </button>
            <button type="button" class="btn btn-danger btn-sm" :disabled="loading" @click="decline(req.requestId)">
              거절
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="panel-card">
      <div class="section-heading">
        <div>
          <h3>보낸 요청</h3>
          <p class="section-subtitle">{{ requests.outgoing.length }}건</p>
        </div>
      </div>

      <div v-if="!requests.outgoing.length" class="empty-state">보낸 친구 요청이 없습니다.</div>

      <div v-else class="request-list">
        <div v-for="req in requests.outgoing" :key="req.requestId" class="request-row">
          <div class="request-main">
            <p class="request-name">{{ displayUser(req.addressee) }}</p>
            <p class="muted">{{ formatDateTime(req.createdAt) }}</p>
          </div>
          <div class="request-actions">
            <button type="button" class="btn btn-danger btn-sm" :disabled="loading" @click="cancel(req.requestId)">
              취소
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="panel-card">
      <div class="section-heading">
        <div>
          <h3>친구 목록</h3>
          <p class="section-subtitle">{{ friends.length }}명</p>
        </div>
      </div>

      <div v-if="!friends.length" class="empty-state">친구가 없습니다.</div>

      <div v-else class="friend-grid">
        <div v-for="item in friends" :key="item.friendshipId" class="friend-card">
          <div>
            <p class="friend-name">{{ displayUser(item.friend) }}</p>
            <p class="muted">{{ item.friend.discordUserId || '-' }}</p>
          </div>
          <button type="button" class="btn btn-danger btn-sm" :disabled="loading" @click="remove(item.friendshipId)">
            삭제
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiClient } from '@/api/http'
import { getHttpErrorMessage, getHttpStatus } from '@/utils/httpError'

type MeResponse = {
  id: number
  kakaoNickname?: string | null
  discordUsername?: string | null
  discordId?: string | null
}

type FriendUserResponse = {
  userId: number
  discordUserId?: string | null
  discordUsername?: string | null
}

type FriendRequestResponse = {
  requestId: number
  requester: FriendUserResponse
  addressee: FriendUserResponse
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'CANCELLED'
  createdAt: string
  respondedAt?: string | null
}

type FriendRequestsResponse = {
  outgoing: FriendRequestResponse[]
  incoming: FriendRequestResponse[]
}

type FriendResponse = {
  friendshipId: number
  friend: FriendUserResponse
}

const me = ref<MeResponse | null>(null)
const loading = ref(false)
const discordUserId = ref('')
const message = ref('')
const errorMessage = ref('')
const friends = ref<FriendResponse[]>([])
const requests = ref<FriendRequestsResponse>({ outgoing: [], incoming: [] })

let messageTimer: number | undefined
const setMessage = (value: string) => {
  message.value = value
  if (messageTimer) window.clearTimeout(messageTimer)
  messageTimer = window.setTimeout(() => {
    message.value = ''
  }, 2500)
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

const displayUser = (user: FriendUserResponse) => {
  if (user.discordUsername) return user.discordUsername
  if (user.discordUserId) return `User#${user.discordUserId}`
  return `User#${user.userId}`
}

const fetchMe = async () => {
  try {
    const response = await apiClient.get<MeResponse>('/auth/me')
    me.value = response.data
  } catch (err: unknown) {
    me.value = null
    const status = getHttpStatus(err)
    if (status && status !== 401) {
      errorMessage.value = getHttpErrorMessage(err) || '로그인 상태를 확인하지 못했습니다.'
    }
  }
}

const loadRequests = async () => {
  const response = await apiClient.get<FriendRequestsResponse>('/me/friends/requests')
  requests.value = response.data
}

const loadFriends = async () => {
  const response = await apiClient.get<FriendResponse[]>('/me/friends')
  friends.value = response.data
}

const refreshAll = async () => {
  if (!me.value) return
  try {
    loading.value = true
    errorMessage.value = ''
    await Promise.all([loadFriends(), loadRequests()])
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '친구 정보를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

const sendRequest = async () => {
  if (!me.value) return
  try {
    loading.value = true
    errorMessage.value = ''
    await apiClient.post('/me/friends/requests', { discordUserId: discordUserId.value })
    discordUserId.value = ''
    setMessage('친구 요청을 보냈어요. 상대가 DM에서 수락하면 완료됩니다.')
    await refreshAll()
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '친구 요청에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const accept = async (requestId: number) => {
  if (!me.value) return
  try {
    loading.value = true
    errorMessage.value = ''
    await apiClient.post(`/me/friends/requests/${requestId}/accept`)
    setMessage('친구 요청을 수락했어요.')
    await refreshAll()
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '요청 수락에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const decline = async (requestId: number) => {
  if (!me.value) return
  try {
    loading.value = true
    errorMessage.value = ''
    await apiClient.post(`/me/friends/requests/${requestId}/decline`)
    setMessage('친구 요청을 거절했어요.')
    await refreshAll()
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '요청 거절에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const cancel = async (requestId: number) => {
  if (!me.value) return
  try {
    loading.value = true
    errorMessage.value = ''
    await apiClient.post(`/me/friends/requests/${requestId}/cancel`)
    setMessage('요청을 취소했어요.')
    await refreshAll()
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '요청 취소에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const remove = async (friendshipId: number) => {
  if (!me.value) return
  try {
    loading.value = true
    errorMessage.value = ''
    await apiClient.delete(`/me/friends/${friendshipId}`)
    setMessage('친구를 삭제했어요.')
    await refreshAll()
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '친구 삭제에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await apiClient.get('/auth/csrf').catch(() => undefined)
  await fetchMe()
  if (me.value) {
    await refreshAll()
  }
})
</script>

<style scoped>
.friends-page {
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

.add-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.input {
  flex: 1;
  min-width: 260px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  color: var(--text-primary, #111827);
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

.request-list {
  display: grid;
  gap: 10px;
}

.request-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--card-bg);
}

.request-main {
  display: grid;
  gap: 2px;
}

.request-name {
  margin: 0;
  font-weight: 700;
  color: var(--text-primary);
}

.request-actions {
  display: flex;
  gap: 8px;
}

.friend-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.friend-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--card-bg);
}

.friend-name {
  margin: 0;
  font-weight: 700;
  color: var(--text-primary);
}

.muted {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.9rem;
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

@media (max-width: 640px) {
  .friends-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
  }

  .add-row {
    flex-direction: column;
    align-items: stretch;
  }

  .input {
    min-width: 0;
  }
}
</style>

