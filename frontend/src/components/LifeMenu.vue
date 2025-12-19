<template>
  <div class="life-page">
    <div class="placeholder-card">
      <h2>생활</h2>
      <p>생활 컨텐츠 수익 분석과 동선 추천 기능을 준비하고 있습니다.</p>
      <p class="placeholder-meta">업데이트를 기대해 주세요.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.life-page {
  padding: 24px;
}

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
