<template>
  <div class="character-page">
    <TopPageHeader>
      <div class="header">
        <div>
          <div class="layout-title-row">
            <h3>내 캐릭터 등록</h3>
          </div>
        </div>
        <div></div>
        <div class="header-actions">
          <button v-if="!me" type="button" class="btn btn-primary" @click="startDiscordLogin">Discord로 로그인</button>
          <button v-else type="button" class="btn" :disabled="loading" @click="refresh">새로고침</button>
        </div>
      </div>
    </TopPageHeader>

    <section class="panel">
      <form class="form-row" @submit.prevent="registerCharacter">
        <label class="field">
          <span class="field-label">캐릭터명</span>
          <input
            v-model.trim="characterName"
            class="input"
            type="text"
            placeholder="예: 하람"
            :disabled="loading"
            required
          />
        </label>
        <label class="checkbox">
          <input v-model="forceRefresh" type="checkbox" :disabled="loading" />
          강제 동기화
        </label>
        <button type="submit" class="btn btn-primary" :disabled="!me || loading || !characterName">등록</button>
      </form>

      <p v-if="message" class="notice" role="status">{{ message }}</p>
      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    </section>

    <section v-if="me?.mainCharacterName" class="panel">
      <h3 class="panel-title">등록된 대표 캐릭터</h3>
      <p class="muted">{{ me.mainCharacterName }}</p>

      <div v-if="profile" class="profile-card">
        <img v-if="profile.characterImage" class="profile-image" :src="profile.characterImage" :alt="profile.characterName" />
        <div class="profile-main">
          <p class="profile-name">{{ profile.characterName }}</p>
          <p class="muted">
            {{ profile.serverName || '-' }} · {{ profile.characterClassName || '-' }} · Lv.{{ profile.characterLevel ?? '-' }}
          </p>
          <p class="muted">아이템 레벨: {{ profile.itemMaxLevel || profile.itemAvgLevel || '-' }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiClient } from '@/api/http'
import { getHttpErrorMessage } from '@/utils/httpError'
import TopPageHeader from './common/TopPageHeader.vue'

type MeResponse = {
  id: number
  kakaoNickname?: string | null
  discordUsername?: string | null
  discordId?: string | null
  mainCharacterName?: string | null
}

type CharacterProfilePreview = {
  characterName: string
  serverName?: string | null
  characterClassName?: string | null
  characterLevel?: number | null
  itemAvgLevel?: string | null
  itemMaxLevel?: string | null
  characterImage?: string | null
}

const me = ref<MeResponse | null>(null)
const profile = ref<CharacterProfilePreview | null>(null)

const characterName = ref('')
const forceRefresh = ref(false)
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

const fetchProfile = async (name: string) => {
  if (!name) {
    profile.value = null
    return
  }
  try {
    const response = await apiClient.get<CharacterProfilePreview>(`/characters/${encodeURIComponent(name)}`)
    profile.value = response.data
  } catch {
    profile.value = null
  }
}

const refresh = async () => {
  try {
    loading.value = true
    errorMessage.value = ''
    await fetchMe()
    await fetchProfile(me.value?.mainCharacterName ?? '')
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '데이터를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

const registerCharacter = async () => {
  if (!me.value) return
  try {
    loading.value = true
    errorMessage.value = ''

    await apiClient.post('/me/characters/register', {
      characterName: characterName.value,
      forceRefresh: forceRefresh.value
    })

    setMessage('캐릭터를 등록했어요.')
    await refresh()
    characterName.value = ''
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '캐릭터 등록에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await apiClient.get('/auth/csrf').catch(() => undefined)
  await refresh()
})
</script>

<style scoped>
.character-page {
  display: grid;
  gap: 16px;
  padding: 24px;
}

.eyebrow {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-muted);
}

h2 {
  margin: 0;
  color: var(--text-primary);
}

.subtitle {
  margin: 6px 0 0;
  color: var(--text-muted);
}

.header {
  display: grid;
  width:100%;
  height:100%;
  grid-template-columns: 1fr 1fr 1fr;
  align-items: center;
  gap: 12px;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.panel {
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 16px;
  background: var(--bg-secondary);
}

.form-row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.field {
  display: grid;
  gap: 6px;
  min-width: 260px;
}

.field-label {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.input {
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 12px;
  padding: 10px 12px;
}

.checkbox {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  color: var(--text-muted);
  padding: 0 6px 6px;
}

.btn {
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.btn-primary {
  border-color: var(--primary-color);
  background: var(--primary-color);
  color: #fff;
}

.notice {
  margin: 12px 0 0;
  color: var(--text-muted);
}

.error-text {
  margin: 12px 0 0;
  color: color-mix(in srgb, #ef4444 65%, var(--text-primary));
}

.muted {
  color: var(--text-muted);
}

.panel-title {
  margin: 0 0 10px;
  color: var(--text-primary);
}

.profile-card {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 12px;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  background: var(--bg-primary);
}

.profile-image {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  object-fit: cover;
}

.profile-name {
  margin: 0;
  font-weight: 800;
  color: var(--text-primary);
}

.profile-main p {
  margin: 2px 0 0;
}
</style>
