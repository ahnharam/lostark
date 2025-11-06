<template>
  <div id="app">
    <CharacterSearch />
  </div>
</template>

<script setup lang="ts">
import CharacterSearch from './components/CharacterSearch.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

#app {
  width: 100%;
  min-height: 100vh;
}
</style>
