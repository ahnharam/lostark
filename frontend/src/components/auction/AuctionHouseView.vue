<template>
  <div class="auction-page">
    <section class="panel-card auction-house-placeholder">
      <h3>경매장</h3>
      <p class="muted">준비 중입니다.</p>
    </section>
  </div>
</template>

<script setup lang="ts">
// Auction House functionality will be implemented here
</script>

<style scoped>
.auction-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.auction-house-placeholder {
  display: grid;
  gap: 8px;
}

.panel-card {
  padding: 20px;
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 16px;
  background: var(--card-bg, #ffffff);
}

.muted {
  color: var(--text-secondary, #6b7280);
  font-size: var(--font-sm);
}
</style>
