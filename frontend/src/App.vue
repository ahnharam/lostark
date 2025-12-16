<template>
  <div id="app">
    <div id="layout-top" class="layout-top" aria-label="상단 영역">
      <div class="layout-top__inner">
        <div id="layout-top-start" class="layout-top__start" aria-label="상단 좌측"></div>
        <div id="layout-top-main" class="layout-top__main" aria-label="상단 메인"></div>
      </div>
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

.layout-top {
  width: 100%;
  margin-bottom: 10px;
  background: var(--page-bg, #f7f8fa);
}

.layout-top__inner {
  display: flex;
  align-items: flex-start;
  gap: 0;
}

.layout-top__start {
  flex: 0 0 auto;
  display: flex;
  align-items: flex-start;
}

.layout-top__start .menu-trigger {
  margin-right: 12px;
}

.layout-top__main {
  flex: 1 1 auto;
  min-width: 0;
  display: grid;
  gap: 10px;
}

@media (max-width: 600px) {
  .layout-top {
    padding: 12px 0 0 0;
    margin-bottom: 10px;
  }

  .layout-top__inner {
    flex-wrap: wrap;
    padding: 0 12px;
  }

  .layout-top__main {
    flex-basis: 100%;
  }
}
</style>
