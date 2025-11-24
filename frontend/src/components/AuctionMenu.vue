<template>
  <div class="placeholder-card">
    <h2>경매</h2>
    <p>경매장 시세와 수익 계산 도구를 준비하고 있습니다.</p>
    <p class="placeholder-meta">조금만 기다려 주세요!</p>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.placeholder-card {
  padding: 32px;
  border-radius: 16px;
  border: 1px dashed var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  color: var(--text-primary, #1f2937);
  display: grid;
  gap: 10px;
  justify-items: start;
}

.placeholder-meta {
  color: var(--text-muted, #6b7280);
}
</style>
