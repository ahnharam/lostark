<template>
  <div id="app">
    <div id="layout-top" class="layout-top" aria-label="상단 영역">
      <div id="layout-top-menu" class="layout-top__menu" aria-label="메인 메뉴"></div>
      <div id="layout-top-main" class="layout-top__main" aria-label="메뉴 헤더"></div>
    </div>
    <RouterView />
  </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: var(--page-bg, #f7f8fa);
}

#app {
  width: 100%;
  min-height: 100vh;
}
</style>
