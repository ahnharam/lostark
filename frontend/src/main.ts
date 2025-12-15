import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { apiClient } from './api/http'
import './assets/theme.css'
import './assets/global.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')

// Prime CSRF cookie for any subsequent state-changing requests (POST/PUT/PATCH/DELETE).
void apiClient.get('/auth/csrf').catch(() => undefined)
