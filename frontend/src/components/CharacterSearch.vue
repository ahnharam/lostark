<template>
  <div class="character-search">
    <div class="search-container">
      <h1>로스트아크 캐릭터 검색</h1>
      
      <div class="search-box">
        <input
          v-model="characterName"
          @keyup.enter="searchCharacter"
          type="text"
          placeholder="캐릭터명을 입력하세요"
          class="search-input"
        />
        <button @click="searchCharacter" :disabled="loading" class="search-button">
          {{ loading ? '검색 중...' : '검색' }}
        </button>
      </div>

      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <div v-if="character" class="character-info">
        <div class="character-header">
          <img 
            v-if="character.characterImage" 
            :src="character.characterImage" 
            :alt="character.characterName"
            class="character-image"
          />
          <div class="character-basic">
            <h2>{{ character.characterName }}</h2>
            <p class="server">{{ character.serverName }}</p>
          </div>
        </div>

        <div class="character-details">
          <div class="detail-item">
            <span class="label">클래스:</span>
            <span class="value">{{ character.characterClassName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">아이템 레벨:</span>
            <span class="value highlight">{{ character.itemMaxLevel }}</span>
          </div>
          <div class="detail-item">
            <span class="label">평균 아이템 레벨:</span>
            <span class="value">{{ character.itemAvgLevel }}</span>
          </div>
          <div class="detail-item" v-if="character.expeditionLevel">
            <span class="label">원정대 레벨:</span>
            <span class="value">{{ character.expeditionLevel }}</span>
          </div>
          <div class="detail-item" v-if="character.guildName">
            <span class="label">길드:</span>
            <span class="value">{{ character.guildName }}</span>
          </div>
          <div class="detail-item" v-if="character.pvpGradeName">
            <span class="label">PVP 등급:</span>
            <span class="value">{{ character.pvpGradeName }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { lostarkApi, type CharacterProfile } from '@/api/lostark'

const characterName = ref('')
const character = ref<CharacterProfile | null>(null)
const loading = ref(false)
const error = ref('')

const searchCharacter = async () => {
  if (!characterName.value.trim()) {
    error.value = '캐릭터명을 입력해주세요.'
    return
  }

  loading.value = true
  error.value = ''
  character.value = null

  try {
    const response = await lostarkApi.getCharacter(characterName.value.trim())
    character.value = response.data
  } catch (err: any) {
    if (err.response?.status === 404) {
      error.value = '캐릭터를 찾을 수 없습니다.'
    } else {
      error.value = '검색 중 오류가 발생했습니다.'
    }
    console.error('검색 실패:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.character-search {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.search-container {
  max-width: 800px;
  margin: 0 auto;
}

h1 {
  text-align: center;
  color: white;
  font-size: 2.5rem;
  margin-bottom: 40px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 15px 20px;
  font-size: 1.1rem;
  border: none;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.search-input:focus {
  outline: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.search-button {
  padding: 15px 40px;
  font-size: 1.1rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.search-button:hover:not(:disabled) {
  background-color: #45a049;
}

.search-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.error-message {
  background-color: #ff6b6b;
  color: white;
  padding: 15px;
  border-radius: 10px;
  text-align: center;
  margin-bottom: 20px;
}

.character-info {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  animation: fadeIn 0.5s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.character-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.character-image {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.character-basic h2 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 2rem;
}

.server {
  color: #666;
  font-size: 1.1rem;
  margin: 0;
}

.character-details {
  display: grid;
  gap: 15px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.label {
  font-weight: 600;
  color: #555;
}

.value {
  color: #333;
  font-weight: 500;
}

.value.highlight {
  color: #667eea;
  font-size: 1.2rem;
  font-weight: 700;
}
</style>
