<template>
  <div class="spinner-container" :class="{ overlay: fullscreen }">
    <div class="spinner"></div>
    <p v-if="message" class="spinner-message">{{ message }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  message?: string
  fullscreen?: boolean
}>()
</script>

<style scoped>
.spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.spinner-container.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--primary-soft-bg, rgba(102, 126, 234, 0.12));
  border-top-color: var(--primary-color, #667eea);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.spinner-message {
  margin-top: 16px;
  color: var(--primary-color, #667eea);
  font-size: 1rem;
  font-weight: 500;
}

.overlay .spinner-message {
  color: var(--text-inverse, #ffffff);
}
</style>
