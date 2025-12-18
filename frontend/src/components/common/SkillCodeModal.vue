<template>
  <Teleport to="body">
    <div v-if="show" class="skill-code-modal__overlay" @click.self="emitClose">
      <div class="skill-code-modal__panel popup-surface" role="dialog" aria-modal="true" aria-label="스킬 코드">
        <header class="skill-code-modal__header">
          <h2 class="skill-code-modal__title">스킬 코드</h2>
          <button type="button" class="skill-code-modal__close" aria-label="닫기" @click="emitClose">
            ✕
          </button>
        </header>

        <div class="skill-code-modal__content">
          <p class="skill-code-modal__desc">
            다음 정보가 포함된 코드를 생성하여, 다른 모험가에게 공유할 수 있습니다.
          </p>
          <ul class="skill-code-modal__bullets">
            <li>스킬 레벨 및 트라이포드</li>
            <li>보석 효과 및 레벨</li>
            <li>각인</li>
            <li>아크 패시브</li>
          </ul>

          <div class="skill-code-modal__code popup-surface">
            <p v-if="loading" class="skill-code-modal__code-text skill-code-modal__code-text--muted">
              스킬 코드를 불러오는 중입니다...
            </p>
            <p v-else-if="errorMessage" class="skill-code-modal__code-text skill-code-modal__code-text--error">
              {{ errorMessage }}
            </p>
            <pre v-else class="skill-code-modal__code-text skill-code-modal__code-text--value">{{ skillCode || '' }}</pre>
          </div>

          <div class="skill-code-modal__actions">
            <button
              type="button"
              class="skill-code-modal__copy"
              :disabled="!canCopy"
              @click="copyCode"
            >
              {{ copyButtonLabel }}
            </button>
          </div>

          <p v-if="characterName" class="skill-code-modal__meta">
            {{ characterName }}
          </p>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps<{
  show: boolean
  characterName: string
  skillCode: string | null
  loading: boolean
  errorMessage: string | null
}>()

const emit = defineEmits<{
  close: []
}>()

const emitClose = () => emit('close')

const canCopy = computed(() => {
  return !props.loading && !props.errorMessage && typeof props.skillCode === 'string' && props.skillCode.trim().length > 0
})

const copyState = ref<'idle' | 'copied' | 'error'>('idle')
let copyTimer: number | null = null

const copyButtonLabel = computed(() => {
  if (copyState.value === 'copied') return '복사 완료'
  if (copyState.value === 'error') return '복사 실패'
  return '코드 복사하기'
})

const resetCopyState = () => {
  copyState.value = 'idle'
  if (copyTimer) window.clearTimeout(copyTimer)
  copyTimer = null
}

const copyViaExecCommand = (text: string) => {
  if (typeof document === 'undefined') return false
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', 'true')
  textarea.style.position = 'fixed'
  textarea.style.top = '-1000px'
  textarea.style.left = '-1000px'
  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()
  let ok = false
  try {
    ok = document.execCommand('copy')
  } catch {
    ok = false
  } finally {
    document.body.removeChild(textarea)
  }
  return ok
}

const copyCode = async () => {
  resetCopyState()
  const text = props.skillCode ?? ''
  if (!text.trim()) return

  try {
    if (typeof navigator !== 'undefined' && navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
      copyState.value = 'copied'
    } else if (copyViaExecCommand(text)) {
      copyState.value = 'copied'
    } else {
      copyState.value = 'error'
    }
  } catch {
    copyState.value = 'error'
  } finally {
    copyTimer = window.setTimeout(() => {
      copyState.value = 'idle'
      copyTimer = null
    }, 1400)
  }
}

const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') emitClose()
}

watch(
  () => props.show,
  show => {
    resetCopyState()
    if (!show) {
      window.removeEventListener('keydown', handleKeyDown)
      return
    }
    window.addEventListener('keydown', handleKeyDown)
  }
)

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeyDown)
  if (copyTimer) window.clearTimeout(copyTimer)
})
</script>

<style scoped>
.skill-code-modal__overlay {
  position: fixed;
  inset: 0;
  background: var(--modal-overlay);
  display: grid;
  place-items: center;
  padding: 18px;
  z-index: var(--z-modal, 1050);
}

.skill-code-modal__panel {
  width: min(640px, 100%);
  padding: 0;
  overflow: hidden;
}

.skill-code-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color, rgba(255, 255, 255, 0.12));
  background: var(--bg-secondary, #111827);
}

.skill-code-modal__title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 800;
  color: var(--text-primary, #f8fafc);
}

.skill-code-modal__close {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.12));
  background: transparent;
  color: var(--text-primary, #f8fafc);
  font-size: 1.1rem;
  line-height: 1;
  cursor: pointer;
}

.skill-code-modal__content {
  padding: 16px;
  display: grid;
  gap: 12px;
}

.skill-code-modal__desc {
  margin: 0;
  color: var(--text-secondary, rgba(229, 231, 235, 0.88));
  font-size: 0.95rem;
}

.skill-code-modal__bullets {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary, rgba(229, 231, 235, 0.85));
  display: grid;
  gap: 6px;
}

.skill-code-modal__code {
  padding: 14px;
}

.skill-code-modal__code-text {
  margin: 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 0.92rem;
  white-space: pre-wrap;
  word-break: break-all;
}

.skill-code-modal__code-text--value {
  color: var(--rarity-legendary, #fbbf24);
}

.skill-code-modal__code-text--muted {
  color: var(--text-tertiary, rgba(156, 163, 175, 0.9));
}

.skill-code-modal__code-text--error {
  color: var(--danger-color, #ef4444);
}

.skill-code-modal__actions {
  display: flex;
  justify-content: center;
  padding-top: 6px;
}

.skill-code-modal__copy {
  min-width: 200px;
  padding: 12px 18px;
  border-radius: 12px;
  border: 1px solid var(--border-color, rgba(255, 255, 255, 0.12));
  background: var(--surface-color, #ffffff);
  color: var(--text-primary, #111827);
  font-weight: 800;
  cursor: pointer;
}

.skill-code-modal__copy:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.skill-code-modal__meta {
  margin: 0;
  font-size: 0.8rem;
  color: var(--text-tertiary, rgba(156, 163, 175, 0.85));
  text-align: right;
}
</style>
