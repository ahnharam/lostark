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
  background: #fee;
  border: 2px solid #f88;
}

.error-container.warning {
  background: #fff9db;
  border: 2px solid #ffd93d;
}

.error-container.info {
  background: #e3f2fd;
  border: 2px solid #64b5f6;
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
  color: #333;
  font-size: 1.1rem;
  font-weight: 700;
}

.error-message {
  margin: 0;
  color: #555;
  line-height: 1.6;
}

.retry-btn {
  margin-top: 12px;
  padding: 8px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s;
}

.retry-btn:hover {
  background: #5568d3;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #999;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 24px;
  height: 24px;
}

.close-btn:hover {
  color: #333;
}
</style>
