<template>
  <router-view v-slot="{ Component }">
    <component :is="Component" />
  </router-view>
</template>

<script setup lang="ts">
// This component now serves as a wrapper for auction sub-routes
// Actual content is in:
// - MarketView.vue (거래소)
// - AuctionHouseView.vue (경매장)
</script>
