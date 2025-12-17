<template>
  <Teleport to="body">
    <div v-if="show" class="myinfo-overlay" @click="requestClose">
      <div
        class="myinfo-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="myinfo-title"
        @click.stop
      >
        <header class="myinfo-header">
          <h2 id="myinfo-title" class="myinfo-title">내정보</h2>
          <button type="button" class="myinfo-close" aria-label="닫기" @click="requestClose">×</button>
        </header>

        <section class="myinfo-body">
          <p v-if="notice" class="myinfo-notice" role="status">{{ notice }}</p>
          <p v-if="errorMessage" class="myinfo-error" role="alert">{{ errorMessage }}</p>

          <div class="myinfo-shortcuts" aria-label="내정보 바로가기">
            <p class="myinfo-shortcuts__title">바로가기</p>
            <div class="myinfo-shortcuts__grid">
              <button type="button" class="myinfo-shortcut" :disabled="loading" @click="navigateTo('friends')">친구</button>
              <button type="button" class="myinfo-shortcut" :disabled="loading" @click="navigateTo('characters')">내 캐릭터</button>
            </div>
          </div>

          <div v-if="me" class="myinfo-card">
            <div class="myinfo-row">
              <span class="myinfo-label">계정</span>
              <span class="myinfo-value">{{ me.discordUsername || me.kakaoNickname || `User#${me.id}` }}</span>
            </div>
            <div class="myinfo-row">
              <span class="myinfo-label">디스코드 ID</span>
              <span class="myinfo-value">{{ me.discordId || '-' }}</span>
            </div>
            <div class="myinfo-row">
              <span class="myinfo-label">대표 캐릭터</span>
              <span class="myinfo-value">{{ me.mainCharacterName || '-' }}</span>
            </div>

            <div class="myinfo-actions">
              <button type="button" class="btn" :disabled="loading" @click="logout">로그아웃</button>
              <button type="button" class="btn btn-primary" :disabled="loading" @click="requestClose">닫기</button>
            </div>
          </div>

          <form v-else class="myinfo-form" @submit.prevent="login">
            <label class="field">
              <span class="field-label">디스코드 ID</span>
              <input
                ref="inputRef"
                v-model.trim="discordUserId"
                class="input"
                type="text"
                inputmode="numeric"
                autocomplete="off"
                placeholder="예: 123456789012345678"
                :disabled="loading"
                required
              />
            </label>

            <div class="myinfo-actions">
              <button type="button" class="btn" :disabled="loading" @click="requestClose">취소</button>
              <button type="submit" class="btn btn-primary" :disabled="loading || !discordUserId">로그인</button>
            </div>
          </form>
        </section>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { apiClient } from '@/api/http'
import { getHttpErrorMessage, getHttpStatus } from '@/utils/httpError'

type MeResponse = {
  id: number
  kakaoNickname?: string | null
  discordUsername?: string | null
  discordId?: string | null
  mainCharacterName?: string | null
}

const props = defineProps<{
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

type ShortcutMenuKey = 'friends' | 'characters'

const router = useRouter()

const me = ref<MeResponse | null>(null)
const discordUserId = ref('')
const loading = ref(false)
const notice = ref('')
const errorMessage = ref('')
const inputRef = ref<HTMLInputElement | null>(null)
let noticeTimer: number | undefined

const setNotice = (value: string) => {
  notice.value = value
  if (noticeTimer) window.clearTimeout(noticeTimer)
  noticeTimer = window.setTimeout(() => {
    notice.value = ''
  }, 2500)
}

const clearMessages = () => {
  notice.value = ''
  errorMessage.value = ''
}

const requestClose = () => {
  emit('close')
}

const navigateTo = (menu: ShortcutMenuKey) => {
  requestClose()
  void router
    .push({
      name: 'main',
      params: { menu },
    })
    .catch(() => undefined)
}

const fetchMe = async () => {
  try {
    const response = await apiClient.get<MeResponse>('/auth/me')
    me.value = response.data
  } catch {
    me.value = null
  }
}

const primeCsrf = async () => {
  await apiClient.get('/auth/csrf').catch(() => undefined)
}

const login = async () => {
  const id = discordUserId.value.trim()
  if (!id) {
    errorMessage.value = '디스코드 ID를 입력해 주세요.'
    return
  }

  loading.value = true
  clearMessages()
  try {
    await primeCsrf()
    const response = await apiClient.post<MeResponse>('/auth/dev-login', { discordUserId: id })
    me.value = response.data
    setNotice('로그인했어요.')
    window.dispatchEvent(new CustomEvent('loap-auth-changed'))
  } catch (err: unknown) {
    const status = getHttpStatus(err)
    if (status === 404) {
      errorMessage.value = '현재 서버에서 ID 로그인 기능이 비활성화되어 있어요. (APP_SECURITY_DEV_LOGIN_ENABLED=true)'
      return
    }
    errorMessage.value = getHttpErrorMessage(err) || '로그인에 실패했어요.'
  } finally {
    loading.value = false
  }
}

const logout = async () => {
  loading.value = true
  clearMessages()
  try {
    await primeCsrf()
    await apiClient.post('/auth/logout')
    me.value = null
    setNotice('로그아웃했어요.')
    window.dispatchEvent(new CustomEvent('loap-auth-changed'))
  } catch (err: unknown) {
    errorMessage.value = getHttpErrorMessage(err) || '로그아웃에 실패했어요.'
  } finally {
    loading.value = false
  }
}

const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') requestClose()
}

watch(
  () => props.show,
  async show => {
    if (!show) {
      window.removeEventListener('keydown', handleKeyDown)
      return
    }

    window.addEventListener('keydown', handleKeyDown)
    clearMessages()
    await primeCsrf()
    await fetchMe()

    if (!me.value) {
      await nextTick()
      inputRef.value?.focus()
    } else if (me.value.discordId) {
      discordUserId.value = me.value.discordId
    }
  }
)

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeyDown)
  if (noticeTimer) window.clearTimeout(noticeTimer)
})
</script>

<style scoped>
.myinfo-overlay {
  position: fixed;
  inset: 0;
  background: var(--modal-overlay, rgba(15, 23, 42, 0.55));
  display: grid;
  place-items: center;
  padding: 18px;
  z-index: 1000;
}

.myinfo-modal {
  width: min(520px, 100%);
  border-radius: 16px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--modal-bg, #ffffff);
  box-shadow: 0 22px 60px rgba(0, 0, 0, 0.22);
  overflow: hidden;
}

.myinfo-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f3f4f6);
}

.myinfo-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 800;
  color: var(--text-primary, #111827);
}

.myinfo-close {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-primary, #ffffff);
  color: var(--text-primary, #111827);
  font-size: 1.2rem;
  line-height: 1;
  cursor: pointer;
}

.myinfo-body {
  padding: 16px;
  display: grid;
  gap: 12px;
}

.myinfo-notice {
  margin: 0;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(16, 185, 129, 0.35);
  background: rgba(16, 185, 129, 0.12);
  color: var(--text-primary, #111827);
  font-weight: 650;
}

.myinfo-error {
  margin: 0;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(239, 68, 68, 0.35);
  background: rgba(239, 68, 68, 0.1);
  color: var(--text-primary, #111827);
  font-weight: 650;
}

.myinfo-card {
  display: grid;
  gap: 10px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f3f4f6);
}

.myinfo-row {
  display: grid;
  grid-template-columns: 110px 1fr;
  gap: 10px;
  align-items: center;
}

.myinfo-label {
  color: var(--muted-text, #6b7280);
  font-weight: 700;
  font-size: 0.92rem;
}

.myinfo-value {
  color: var(--text-primary, #111827);
  font-weight: 750;
  word-break: break-word;
}

.myinfo-form {
  display: grid;
  gap: 12px;
}

.myinfo-shortcuts {
  display: grid;
  gap: 10px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-primary, #ffffff);
}

.myinfo-shortcuts__title {
  margin: 0;
  font-size: 0.92rem;
  font-weight: 800;
  color: var(--text-primary, #111827);
}

.myinfo-shortcuts__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.myinfo-shortcut {
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f3f4f6);
  color: var(--text-primary, #111827);
  font-weight: 750;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.myinfo-shortcut:hover:not(:disabled),
.myinfo-shortcut:focus-visible:not(:disabled) {
  border-color: var(--primary-color, #6366f1);
  transform: translateY(-1px);
}

.myinfo-shortcut:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.myinfo-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.input {
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 12px;
  padding: 10px 12px;
  margin-left:10px;
}
</style>
