<template>
  <div class="error-container" :class="type">
    <div class="error-icon">
      <span v-if="type === 'error'">⚠️</span>
      <span v-else-if="type === 'warning'">⚡</span>
      <span v-else>ℹ️</span>
    </div>
    <div class="error-content">
      <h4 v-if="title" class="error-title">{{ title }}</h4>
      <p class="error-message">{{ message }}</p>
      <button v-if="retry" @click="$emit('retry')" class="retry-btn">
        다시 시도
      </button>
    </div>
    <button v-if="dismissible" @click="$emit('dismiss')" class="close-btn">×</button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  message: string
  type?: 'error' | 'warning' | 'info'
  retry?: boolean
  dismissible?: boolean
}>()

defineEmits<{
  retry: []
  dismiss: []
}>()
</script>

<style scoped>
.error-container {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  position: relative;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.error-container.error {
  background: var(--error-soft-bg, #ffeeee);
  border: 2px solid var(--error-color, #ff8888);
}

.error-container.warning {
  background: var(--warning-soft-bg, #fff9db);
  border: 2px solid var(--warning-color, #ffd93d);
}

.error-container.info {
  background: var(--info-soft-bg, #e3f2fd);
  border: 2px solid var(--info-color, #64b5f6);
}

.error-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.error-content {
  flex: 1;
}

.error-title {
  margin: 0 0 8px 0;
  color: var(--text-primary, #333333);
  font-size: 1.1rem;
  font-weight: 700;
}

.error-message {
  margin: 0;
  color: var(--text-secondary, #555555);
  line-height: 1.6;
}

.retry-btn {
  margin-top: 12px;
  padding: 8px 20px;
  background: var(--primary-color, #667eea);
  color: var(--text-inverse, #ffffff);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s;
}

.retry-btn:hover {
  background: var(--primary-hover, #5568d3);
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted, #999999);
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 24px;
  height: 24px;
}

.close-btn:hover {
  color: var(--text-primary, #333333);
}
</style>
