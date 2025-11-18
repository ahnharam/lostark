import { createRouter, createWebHistory } from 'vue-router'
import CharacterSearch from '@/components/CharacterSearch.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'character-search',
      component: CharacterSearch,
    },
    {
      path: '/reforge',
      name: 'reforge',
      component: CharacterSearch,
    },
  ],
})

export default router
