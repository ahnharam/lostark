<template>
  <div class="blunt-calculator">
    <div class="calc-grid">
      <section class="panel-card input-card">
        <div class="card-head">
          <!-- <p class="muted">필요한 치명 스탯과 실시간 효율을 바로 확인해 보세요.</p> -->
          <div  class="card-head-title">
            <p class="eyebrow">뭉툭한 가시 {{ bluntLevelLabel }}</p>
            <div class="pill-note">치명 스탯 1 당 {{ critPerStat }}%</div>
          </div>
        </div>

        <div class="level-toggle">
          <button
            v-for="level in bluntLevels"
            :key="level.key"
            type="button"
            class="level-btn"
            :class="{ active: selectedBluntLevel === level.key }"
            @click="selectedBluntLevel = level.key"
          >
            {{ level.label }} ({{ level.value }}%)
          </button>
        </div>

        <p class="group-title">직접 입력</p>
        <div class="form-grid">
          <label class="input-field">
            <span>스탯</span>
            <input v-model.number="critStat" type="number" min="0" step="1" />
          </label>
          <label class="input-field">
            <span>직업 각인 (%)</span>
            <input v-model.number="classEngravingCrit" type="number" min="0" step="0.1" />
          </label>
          <label class="input-field">
            <span>트라이포드 (%)</span>
            <input v-model.number="tripodCrit" type="number" min="0" step="0.1" />
          </label>
          <label class="input-field">
            <span>팔찌 (%)</span>
            <input v-model.number="braceletCrit" type="number" min="0" step="0.1" />
          </label>
        </div>

        <p class="group-title">각인서</p>
        <div class="form-grid">
          <label class="input-field">
            <span>아드레날린</span>
            <select v-model.number="adrenalineCrit">
              <option v-for="option in adrenalineOptions" :key="option.value" :value="option.value">
                {{ option.label }} ({{ option.value }}%)
              </option>
            </select>
          </label>
          <label class="input-field">
            <span>정밀 단도</span>
            <select v-model.number="preciseDaggerCrit">
              <option v-for="option in preciseDaggerOptions" :key="option.value" :value="option.value">
                {{ option.label }} ({{ option.value }}%)
              </option>
            </select>
          </label>
          <div></div>
          <div></div>
        </div>

        <p class="group-title">아크패시브</p>
        <div class="form-grid form-grid-compact">
          <label class="input-field">
            <span>예리한 감각</span>
            <select v-model.number="sharpSenseCrit">
              <option v-for="option in sharpSenseOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
          <label class="input-field">
            <span>혼신의 강타</span>
            <select v-model.number="focusedStrikeCrit">
              <option v-for="option in focusedStrikeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
          <label class="input-field">
            <span>일격</span>
            <select v-model.number="fatalStrikeCrit">
              <option v-for="option in fatalStrikeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
          <label class="input-field">
            <span>달인</span>
            <select v-model.number="artisanElixirCrit">
              <option v-for="option in artisanElixirOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
        </div>

        <div class="inline-row">
          <div>
            <p class="group-title">아크 그리드</p>
            <div class="form-grid">
              <label class="input-field">
                <span>부수는 일격</span>
                <select v-model.number="destructStrikeCrit">
                  <option v-for="option in arcGridOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </label>
              <div></div>
            </div>
          </div>

          <div>
            <p class="group-title">반지</p>
            <div class="form-grid ring-grid">
              <label class="input-field">
                <span>반지 1</span>
                <select v-model.number="ringOneCrit">
                  <option v-for="option in ringOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </label>
              <label class="input-field">
                <span>반지 2</span>
                <select v-model.number="ringTwoCrit">
                  <option v-for="option in ringOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </label>
            </div>
          </div>
        </div>
      </section>

      <section class="panel-card support-synergy-card">
        <div class="card-head">
          <div class="card-head-title">
            <p class="eyebrow">서포터</p>
            <div class="pill-note">총 +{{ (supportCrit + beadBoyCrit + beadSupportCrit).toFixed(2) }}%</div>
          </div>
        </div>

        <div class="form-grid form-grid-compact">
          <label class="input-field">
            <span>서포터 팔찌 (%)</span>
            <select v-model.number="supportCrit">
              <option v-for="option in supportWeaknessOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
          <label class="input-field">
            <span>구슬 동자</span>
            <select v-model.number="beadBoyCrit">
              <option v-for="option in beadBoyOptions" :key="option.value" :value="option.value">
                {{ option.label }} ({{ option.value }}%)
              </option>
            </select>
          </label>
          <label class="input-field">
            <span>구슬동자 돌</span>
            <select v-model.number="beadSupportCrit">
              <option v-for="option in beadSupportOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
        </div>
      </section>

      <section class="panel-card synergy-card">
        <div class="card-head">
          <div class="card-head-title">
            <p class="eyebrow">시너지</p>
            <div class="pill-note">총 +{{ synergyCrit.toFixed(2) }}%</div>
          </div>
        </div>
        <div class="synergy-grid">
          <button
            v-for="synergy in synergyOptions"
            :key="synergy.key"
            type="button"
            class="synergy-chip"
            :class="{ active: activeSynergies.includes(synergy.key) }"
            @click="toggleSynergy(synergy.key)"
          >
            <span>{{ synergy.label }}</span>
            <span class="chip-value">+{{ synergy.value }}%</span>
          </button>
        </div>
      </section>

      <section class="panel-card result-card">
        <div class="result-grid">
          <div class="result-box primary">
            <p class="eyebrow">현재 치명타</p>
            <p class="result-value">{{ formatPercent(currentCritRate) }}%</p>
            <p class="muted">치명 스탯 환산 포함</p>
            <div class="progress">
              <span class="progress-bar" :style="{ width: efficiencyRate + '%' }"></span>
            </div>
          </div>
          <div class="result-box">
            <p class="eyebrow">뭉가 적용 값</p>
            <p class="result-value">{{ formatPercent(effectiveBluntValue) }}%</p>
            <p class="muted">최대 {{ maxBluntValue }}% 중 {{ formatPercent(efficiencyRate) }}%</p>
          </div>
          <div class="result-box">
            <p class="eyebrow">필요 치명 스탯</p>
            <p class="result-value">{{ formatNumber(requiredCritStat) }}</p>
            <p class="muted">
              남은 필요 스탯 {{ formatNumber(remainingCritStat) }} <br/>
              (부족 {{ formatPercent(remainingCritPercent) }}%)
            </p>
          </div>
        </div>

        <div class="breakdown">
          <div class="breakdown-head">
            <p class="eyebrow">치명 기여도</p>
            <span class="pill-note">총 {{ formatPercent(flatCritTotal) }}%</span>
          </div>
          <div class="breakdown-body">
            <div v-for="row in breakdownRows" :key="row.label" class="breakdown-row">
              <span>{{ row.label }}</span>
              <span class="value">+{{ formatPercent(row.value) }}%</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const critPerStat = 0.0357

type BluntLevelKey = 'level1' | 'level2'

const bluntLevels: Array<{ key: BluntLevelKey; label: string; value: number }> = [
  { key: 'level1', label: '1레벨', value: 115.42 },
  { key: 'level2', label: '2레벨', value: 119.28 }
]

type SynergyKey =
  | 'battleMaster'
  | 'gunslinger'
  | 'striker'
  | 'devilHunter'
  | 'aeromancer'
  | 'arcana'
  | 'backAttack'

const synergyOptions: Array<{ key: SynergyKey; label: string; value: number }> = [
  { key: 'battleMaster', label: '배틀마스터', value: 10 },
  { key: 'gunslinger', label: '건슬링어', value: 10 },
  { key: 'striker', label: '스트라이커', value: 10 },
  { key: 'devilHunter', label: '데빌헌터', value: 10 },
  { key: 'aeromancer', label: '기상술사', value: 10 },
  { key: 'arcana', label: '아르카나', value: 10 },
  { key: 'backAttack', label: '백어택', value: 10 }
]

const selectedBluntLevel = ref<BluntLevelKey>('level2')
const critStat = ref<number>(0)
const classEngravingCrit = ref<number>(0)
const tripodCrit = ref<number>(0)
const braceletCrit = ref<number>(0)
const supportCrit = ref<number>(0)
const adrenalineCrit = ref<number>(0)
const preciseDaggerCrit = ref<number>(0)
const beadBoyCrit = ref<number>(0)
const beadSupportCrit = ref<number>(0)
const sharpSenseCrit = ref<number>(0)
const focusedStrikeCrit = ref<number>(0)
const fatalStrikeCrit = ref<number>(0)
const artisanElixirCrit = ref<number>(0)
const destructStrikeCrit = ref<number>(0)
const ringOneCrit = ref<number>(0)
const ringTwoCrit = ref<number>(0)

const activeSynergies = ref<SynergyKey[]>([])

const toggleSynergy = (key: SynergyKey) => {
  if (activeSynergies.value.includes(key)) {
    activeSynergies.value = activeSynergies.value.filter(item => item !== key)
  } else {
    activeSynergies.value = [...activeSynergies.value, key]
  }
}

const maxBluntValue = computed(() => {
  const level = bluntLevels.find(item => item.key === selectedBluntLevel.value)
  const fallback = bluntLevels[0]?.value ?? 0
  return Number((level?.value ?? fallback).toFixed(2))
})

const bluntLevelLabel = computed(() => {
  const level = bluntLevels.find(item => item.key === selectedBluntLevel.value)
  return level?.label ?? '1레벨'
})

const adrenalineOptions = [
  { label: '없음', value: 0 },
  { label: '전설 4레벨', value: 14.5 },
  { label: '유물 1레벨', value: 15.5 },
  { label: '유물 2레벨', value: 17 },
  { label: '유물 3레벨', value: 18.5 },
  { label: '유물 4레벨', value: 18.5 }
]

const preciseDaggerOptions = [
  { label: '없음', value: 0 },
  { label: '전설 4레벨', value: 18.75 },
  { label: '유물 1레벨', value: 18 },
  { label: '유물 2레벨', value: 19.5 },
  { label: '유물 3레벨', value: 21 },
  { label: '유물 4레벨', value: 21 }
]

const beadBoyOptions = [
  { label: '없음', value: 0 },
  { label: '전설 4레벨', value: 15 },
  { label: '유물 1레벨', value: 15.6 },
  { label: '유물 2레벨', value: 16.2 },
  { label: '유물 3레벨', value: 16.8 },
  { label: '유물 4레벨', value: 17.4 }
]

const beadSupportOptions = [
  { label: '구슬동자 돌 없음', value: 0 },
  { label: '돌 6레벨 (+1.8%)', value: 1.8 },
  { label: '돌 7레벨 (+2.25%)', value: 2.25 },
  { label: '돌 9레벨 (+3.15%)', value: 3.15 },
  { label: '돌 10레벨 (+3.6%)', value: 3.6 }
]

const sharpSenseOptions = [
  { label: '미적용', value: 0 },
  { label: '1레벨 (+4%)', value: 4 },
  { label: '2레벨 (+8%)', value: 8 }
]

const focusedStrikeOptions = [
  { label: '미적용', value: 0 },
  { label: '1레벨 (+12%)', value: 12 },
  { label: '2레벨 (+24%)', value: 24 }
]

const fatalStrikeOptions = [
  { label: '미적용', value: 0 },
  { label: '1레벨 (+10%)', value: 10 },
  { label: '2레벨 (+20%)', value: 20 }
]

const artisanElixirOptions = [
  { label: '미적용', value: 0 },
  { label: '달인 엘릭서 (+7%)', value: 7 }
]

const arcGridOptions = [
  { label: '없음', value: 0 },
  { label: '전설 14P (+0.65%)', value: 0.65 },
  { label: '유물 14P (+0.65%)', value: 0.65 },
  { label: '유물 17P (+1.30%)', value: 1.3 },
  { label: '고대 14P (+0.65%)', value: 0.65 },
  { label: '고대 17P (+2.60%)', value: 2.6 }
]

const supportWeaknessOptions = [
  { label: '없음 (0%)', value: 0 },
  { label: '1.5%', value: 1.5 },
  { label: '1.8%', value: 1.8 },
  { label: '2.1%', value: 2.1 }
]

const ringOptions = [
  { label: '없음', value: 0 },
  { label: '하 (+0.40%)', value: 0.4 },
  { label: '중 (+0.95%)', value: 0.95 },
  { label: '상 (+1.55%)', value: 1.55 }
]

const synergyCrit = computed(() =>
  synergyOptions
    .filter(synergy => activeSynergies.value.includes(synergy.key))
    .reduce((sum, synergy) => sum + synergy.value, 0)
)

const arcPassiveTotal = computed(
  () =>
    sharpSenseCrit.value +
    focusedStrikeCrit.value +
    fatalStrikeCrit.value +
    artisanElixirCrit.value +
    destructStrikeCrit.value
)

const flatCritTotal = computed(
  () =>
    classEngravingCrit.value +
    tripodCrit.value +
    braceletCrit.value +
    supportCrit.value +
    synergyCrit.value +
    adrenalineCrit.value +
    preciseDaggerCrit.value +
    beadBoyCrit.value +
    beadSupportCrit.value +
    arcPassiveTotal.value +
    ringOneCrit.value +
    ringTwoCrit.value
)

const critFromStat = computed(() => critStat.value * critPerStat)
const currentCritRate = computed(() => Number((critFromStat.value + flatCritTotal.value).toFixed(2)))

const requiredCritStat = computed(() => {
  const neededCritRate = Math.max(0, maxBluntValue.value - flatCritTotal.value)
  return Math.ceil(neededCritRate / critPerStat)
})

const remainingCritStat = computed(() => Math.max(0, requiredCritStat.value - critStat.value))
const remainingCritPercent = computed(() =>
  Math.max(0, maxBluntValue.value - (flatCritTotal.value + critFromStat.value))
)

const effectiveBluntValue = computed(() =>
  Number(Math.min(maxBluntValue.value, currentCritRate.value).toFixed(2))
)

const efficiencyRate = computed(() => {
  if (!maxBluntValue.value) return 0
  return Number(Math.min(100, (effectiveBluntValue.value / maxBluntValue.value) * 100).toFixed(2))
})

const breakdownRows = computed(() =>
  [
    { label: '치명 스탯 환산', value: critFromStat.value },
    { label: '직업 각인', value: classEngravingCrit.value },
    { label: '트라이포드', value: tripodCrit.value },
    { label: '팔찌', value: braceletCrit.value },
    { label: '서포터 약점 노출', value: supportCrit.value },
    { label: '시너지 합계', value: synergyCrit.value },
    { label: '아드레날린', value: adrenalineCrit.value },
    { label: '정밀 단도', value: preciseDaggerCrit.value },
    { label: '구슬 동자', value: beadBoyCrit.value },
    { label: '구슬동자 돌', value: beadSupportCrit.value },
    { label: '아크패시브 합계', value: arcPassiveTotal.value },
    { label: '부수는 일격', value: destructStrikeCrit.value },
    { label: '달인 엘릭서', value: artisanElixirCrit.value },
    { label: '반지 1', value: ringOneCrit.value },
    { label: '반지 2', value: ringTwoCrit.value }
  ].filter(row => row.value > 0)
)

const formatPercent = (value: number) => Number(value.toFixed(2))
const formatNumber = (value: number) => value.toLocaleString('ko-KR')
</script>

<style scoped>
.blunt-calculator {
  width: 100%;
}

.eyebrow {
  font-size: 12px;
  color: var(--text-tertiary);
  letter-spacing: 0.02em;
  margin-bottom: 4px;
}

.muted {
  color: var(--text-secondary);
  font-size: 13px;
}

.calc-grid {
  display: grid;
  grid-template-columns: 3fr 1fr 1fr 1fr;
  gap: 14px;
  width: 100%;
}

.level-toggle {
  display: inline-flex;
  gap: 8px;
  flex-wrap: wrap;
}

.level-btn {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.level-btn.active {
  background: var(--primary-soft-bg);
  color: var(--primary-color);
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 0 8px 16px rgba(99, 102, 241, 0.12);
}

.group-title {
  margin: 14px 0 6px;
  font-weight: 800;
  color: var(--text-primary);
}

.panel-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 16px;
  /* box-shadow: var(--shadow-sm); */
}

.input-card .form-grid {
  margin-top: 12px;
}

.card-head {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.card-head-title{
  display: flex;
  justify-content: space-between;
  align-items: center;
  width:100%;
  margin-bottom: 10px;
}

.pill-note {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: var(--font-sm);
  font-weight: 700;
  border: 1px solid var(--border-color);
}

.form-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.form-grid-compact {
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.inline-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 14px;
  margin-top: 10px;
}

.ring-grid {
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
}

.input-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
  color: var(--text-secondary);
}

.input-field input,
.input-field select {
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 10px 12px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 700;
}

.input-field input:focus,
.input-field select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.support-synergy-card {
  grid-column: 2 / 3;
  display: flex;
  flex-direction: column;
}

.synergy-card {
  grid-column: 3 / 4;
}

.synergy-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 8px;
  margin-top: 14px;
}

.synergy-chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  cursor: pointer;
  transition: all 0.15s ease;
  font-weight: 700;
  color: var(--text-primary);
}

.synergy-chip .chip-value {
  color: var(--primary-color);
}

.synergy-chip.active {
  background: var(--primary-soft-bg);
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 0 8px 16px rgba(99, 102, 241, 0.12);
}

.result-card {
  grid-column: 4 / 5;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
}

.result-box {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 12px;
  background: var(--bg-secondary);
}

.result-box.primary {
  border-color: var(--primary-color);
  background: var(--primary-soft-bg);
}

.result-value {
  font-size: 1.4rem;
  font-weight: 800;
  margin: 4px 0;
}

.progress {
  position: relative;
  width: 100%;
  height: 8px;
  border-radius: 999px;
  background: var(--border-color);
  overflow: hidden;
  margin-top: 10px;
}

.progress-bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  background: linear-gradient(90deg, #6366f1, #22c55e);
  border-radius: 999px;
}

.breakdown {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 12px;
  background: var(--bg-secondary);
}

.breakdown-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.breakdown-body {
  display: grid;
  gap: 6px;
}

.breakdown-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 10px;
  background: var(--card-bg);
  border: 1px dashed var(--border-color);
  font-weight: 600;
}

.breakdown-row .value {
  color: var(--primary-color);
}

@media (max-width: 1100px) {
  .calc-grid {
    grid-template-columns: 1fr;
  }

  .support-synergy-card,
  .synergy-card,
  .result-card {
    grid-column: auto;
  }
}
</style>
