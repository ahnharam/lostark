<template>
  <div class="admin-raid-catalog">
    <Teleport to="#admin-submenu-actions" v-if="isActive">
      <div class="actions">
        <button class="btn ghost" type="button" :disabled="loading || isEditMode" @click="loadCatalog">
          새로고침
        </button>
      </div>
    </Teleport>

    <section class="panel">
      <div v-if="gateEnabled && !unlocked" class="gate">
        <span class="muted">관리 접근</span>
        <input v-model="passwordInput" type="password" class="input" placeholder="비밀번호" />
        <button class="btn ghost" type="button" @click="unlock">입장</button>
        <p v-if="error" class="error-text">{{ error }}</p>
      </div>

      <div v-if="gateEnabled && !unlocked" class="muted">비밀번호가 없으면 env에 VITE_ADMIN_PASSWORD를 비워두세요.</div>

      <template v-else>
        <div class="panel-head">
          <div>
            <p class="eyebrow">레이드 관리</p>
            <h4>총 {{ rows.length }}건</h4>
          </div>
          <div class="panel-actions">
            <span v-if="message" class="muted">{{ message }}</span>
            <div class="btn-group">
              <button
                v-if="!isEditMode"
                class="btn ghost"
                type="button"
                :disabled="loading || !rows.length"
                @click="startEditAll"
              >
                수정
              </button>
              <template v-else>
                <button
                  class="btn"
                  type="button"
                  :disabled="loading"
                  @click="saveAllEdits"
                >
                  저장
                </button>
                <button
                  class="btn ghost"
                  type="button"
                  :disabled="loading"
                  @click="cancelEditAll"
                >
                  취소
                </button>
              </template>
            </div>
          </div>
        </div>

        <form class="form-row" @submit.prevent="createRaid">
          <label class="field">
            <span class="field-label">raidKey</span>
            <input v-model.trim="draft.raidKey" class="input" type="text" placeholder="예: kazeros-act-5" required />
          </label>
          <label class="field field--wide">
            <span class="field-label">raidName</span>
            <input v-model.trim="draft.raidName" class="input" type="text" placeholder="예: 5막: ..." required />
          </label>
          <label class="field field--compact">
            <span class="field-label">약어</span>
            <input v-model.trim="draft.abbreviation" class="input" type="text" placeholder="예: 5막" />
          </label>
          <label class="field field--wide">
            <span class="field-label">난이도/입장/골드(거래/귀속)</span>
            <div class="checkbox-group">
              <label v-for="option in difficultyOptions" :key="option.id" class="checkbox checkbox--gold">
                <input v-model="draft.difficulties" type="checkbox" :value="option.id" />
                <span>{{ option.label }}</span>
                <input
                  v-model="draft.difficultyLevels[option.id]"
                  class="input input--xs"
                  type="number"
                  min="0"
                  placeholder="레벨"
                  :disabled="!draft.difficulties.includes(option.id)"
                />
                <input
                  v-model="draft.difficultyGoldsTrade[option.id]"
                  class="input input--xs"
                  type="number"
                  min="0"
                  placeholder="거래"
                  :disabled="!draft.difficulties.includes(option.id)"
                />
                <input
                  v-model="draft.difficultyGoldsBound[option.id]"
                  class="input input--xs"
                  type="number"
                  min="0"
                  placeholder="귀속"
                  :disabled="!draft.difficulties.includes(option.id)"
                />
              </label>
            </div>
          </label>
          <label class="field field--compact">
            <span class="field-label">인원</span>
            <select v-model="draft.partySize" class="select">
              <option value="">미설정</option>
              <option v-for="size in partySizeOptions" :key="size" :value="String(size)">{{ size }}명</option>
            </select>
          </label>
          <label class="checkbox">
            <input v-model="draft.active" type="checkbox" />
            활성
          </label>
          <button class="btn btn-primary" type="submit" :disabled="loading || isEditMode">추가</button>
        </form>

        <LoadingSpinner v-if="loading" message="불러오는 중..." />
        <p v-else-if="error" class="error-text">{{ error }}</p>
        <p v-else-if="!rows.length" class="empty">등록된 레이드가 없습니다.</p>
        <div v-else class="table">
          <div class="table-head">
            <span>raidKey</span>
            <span>raidName</span>
            <span>약어</span>
            <span>난이도/입장/골드(거래/귀속)</span>
            <span>인원</span>
            <span>상태</span>
            <span>생성일</span>
            <span class="align-right">관리</span>
          </div>
          <div v-for="row in rows" :key="row.raidKey" class="table-row">
            <span class="mono">{{ row.raidKey }}</span>
            <span v-if="!isEditMode">{{ row.raidName }}</span>
            <input
              v-else
              v-model.trim="editDrafts[row.raidKey].raidName"
              class="input input--inline"
              type="text"
              placeholder="레이드 이름"
            />
            <span v-if="!isEditMode">{{ row.abbreviation || '-' }}</span>
            <input
              v-else
              v-model.trim="editDrafts[row.raidKey].abbreviation"
              class="input input--inline"
              type="text"
              placeholder="약어"
            />
            <span v-if="!isEditMode">{{ formatDifficultyDetails(row) }}</span>
            <div v-else class="checkbox-group">
              <label v-for="option in difficultyOptions" :key="option.id" class="checkbox checkbox--gold">
                <input v-model="editDrafts[row.raidKey].difficulties" type="checkbox" :value="option.id" />
                <span>{{ option.label }}</span>
                <input
                  v-model="editDrafts[row.raidKey].difficultyLevels[option.id]"
                  class="input input--xs"
                  type="number"
                  min="0"
                  placeholder="레벨"
                  :disabled="!editDrafts[row.raidKey].difficulties.includes(option.id)"
                />
                <input
                  v-model="editDrafts[row.raidKey].difficultyGoldsTrade[option.id]"
                  class="input input--xs"
                  type="number"
                  min="0"
                  placeholder="거래"
                  :disabled="!editDrafts[row.raidKey].difficulties.includes(option.id)"
                />
                <input
                  v-model="editDrafts[row.raidKey].difficultyGoldsBound[option.id]"
                  class="input input--xs"
                  type="number"
                  min="0"
                  placeholder="귀속"
                  :disabled="!editDrafts[row.raidKey].difficulties.includes(option.id)"
                />
              </label>
            </div>
            <span v-if="!isEditMode">{{ formatPartySize(row.partySize) }}</span>
            <select v-else v-model="editDrafts[row.raidKey].partySize" class="select select--inline">
              <option value="">미설정</option>
              <option v-for="size in partySizeOptions" :key="size" :value="String(size)">{{ size }}명</option>
            </select>
            <span v-if="!isEditMode">
              <span class="pill" :class="{ off: !row.active }">{{ row.active ? '활성' : '비활성' }}</span>
            </span>
            <label v-else class="checkbox inline">
              <input v-model="editDrafts[row.raidKey].active" type="checkbox" />
              활성
            </label>
            <span class="muted">{{ formatDate(row.createdAt) }}</span>
            <span class="align-right">
              <div v-if="!isEditMode" class="btn-group">
                <button
                  class="btn ghost btn-sm"
                  type="button"
                  :disabled="loading"
                  @click="deleteRaid(row)"
                >
                  삭제
                </button>
              </div>
              <span v-else class="muted">-</span>
            </span>
          </div>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onActivated, onDeactivated, onMounted, ref, watch } from 'vue'
import { lostarkApi, type RaidCatalogEntry } from '@/api/lostark'
import { getHttpErrorMessage } from '@/utils/httpError'
import LoadingSpinner from './common/LoadingSpinner.vue'

const rows = ref<RaidCatalogEntry[]>([])
const loading = ref(false)
const error = ref('')
const message = ref('')
const isEditMode = ref(false)
const editDrafts = ref<Record<string, RaidDraft>>({})

const difficultyOptions = [
  { id: 'single', label: '싱글' },
  { id: 'normal', label: '노말' },
  { id: 'hard', label: '하드' },
  { id: 'nightmare', label: '나이트메어' }
] as const

const partySizeOptions = [4, 8, 16] as const

type DifficultyId = (typeof difficultyOptions)[number]['id']

const difficultyIdSet = new Set<DifficultyId>(difficultyOptions.map(option => option.id))
const difficultyLabelMap = new Map<DifficultyId, string>(difficultyOptions.map(option => [option.id, option.label]))

type DifficultyGoldsTrade = Record<DifficultyId, string>
type DifficultyGoldsBound = Record<DifficultyId, string>
type DifficultyLevels = Record<DifficultyId, string>

type RaidDraft = {
  raidKey: string
  raidName: string
  abbreviation: string
  active: boolean
  difficulties: DifficultyId[]
  partySize: string
  difficultyLevels: DifficultyLevels
  difficultyGoldsTrade: DifficultyGoldsTrade
  difficultyGoldsBound: DifficultyGoldsBound
}

const createEmptyDifficultyGoldsTrade = (): DifficultyGoldsTrade => ({
  single: '',
  normal: '',
  hard: '',
  nightmare: ''
})

const createEmptyDifficultyGoldsBound = (): DifficultyGoldsBound => ({
  single: '',
  normal: '',
  hard: '',
  nightmare: ''
})

const createEmptyDifficultyLevels = (): DifficultyLevels => ({
  single: '',
  normal: '',
  hard: '',
  nightmare: ''
})

const draft = ref<RaidDraft>({
  raidKey: '',
  raidName: '',
  abbreviation: '',
  active: true,
  difficulties: [],
  partySize: '',
  difficultyLevels: createEmptyDifficultyLevels(),
  difficultyGoldsTrade: createEmptyDifficultyGoldsTrade(),
  difficultyGoldsBound: createEmptyDifficultyGoldsBound()
})

const passwordInput = ref('')
const unlocked = ref(false)
const gateEnabled = Boolean(import.meta.env.VITE_ADMIN_PASSWORD)
const ADMIN_KEY = 'admin_unlocked'

const isActive = ref(false)

const resolveErrorMessage = (err: unknown, fallback: string) => getHttpErrorMessage(err) ?? fallback

const formatDate = (value?: string | null) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const formatNumber = (value?: number | null) => {
  if (value === null || value === undefined) return '-'
  return new Intl.NumberFormat('ko-KR').format(value)
}

const formatPartySize = (value?: number | null) => {
  if (!value) return '-'
  return `${value}명`
}

const formatDifficultyDetails = (entry: RaidCatalogEntry) => {
  const difficulties = normalizeDifficultyIds(entry.difficulties ?? [])
  if (difficulties.length === 0) {
    const baseParts = []
    if (entry.itemLevel != null && entry.itemLevel > 0) {
      baseParts.push(`Lv.${formatNumber(entry.itemLevel)}`)
    }
    if (entry.goldReward != null && entry.goldReward > 0) {
      baseParts.push(`${formatNumber(entry.goldReward)}G`)
    }
    return baseParts.length > 0 ? baseParts.join(' / ') : '-'
  }
  const parts = difficulties.map(difficulty => {
    const label = difficultyLabelMap.get(difficulty) ?? difficulty
    const itemLevel = resolveDifficultyItemLevel(entry, difficulty)
    const { trade, bound } = resolveDifficultyGoldParts(entry, difficulty)
    const legacyGold = resolveLegacyDifficultyGold(entry, difficulty)
    const detailParts = []
    if (itemLevel) {
      detailParts.push(`Lv.${formatNumber(itemLevel)}`)
    }
    if (trade !== null || bound !== null) {
      const tradeLabel = trade !== null ? `${formatNumber(trade)}G` : '-'
      const boundLabel = bound !== null ? `${formatNumber(bound)}G` : '-'
      detailParts.push(`거래 ${tradeLabel} / 귀속 ${boundLabel}`)
    } else if (legacyGold) {
      detailParts.push(`총 ${formatNumber(legacyGold)}G`)
    }
    return detailParts.length > 0 ? `${label} ${detailParts.join(' / ')}` : label
  })
  return parts.join(', ')
}

const normalizeText = (value: unknown) => {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

const parseOptionalNumber = (value: unknown) => {
  const trimmed = normalizeText(value)
  if (!trimmed) return null
  const parsed = Number(trimmed)
  return Number.isFinite(parsed) ? parsed : null
}

const isFiniteNumber = (value: unknown): value is number => {
  return typeof value === 'number' && Number.isFinite(value)
}

const normalizeDifficultyIds = (values: string[]) => {
  const selected = new Set<DifficultyId>()
  values.forEach(value => {
    if (typeof value !== 'string') return
    const normalized = value.trim().toLowerCase() as DifficultyId
    if (difficultyIdSet.has(normalized)) {
      selected.add(normalized)
    }
  })
  return difficultyOptions.map(option => option.id).filter(id => selected.has(id))
}

const resolveDifficultyGoldParts = (entry: RaidCatalogEntry, difficulty: DifficultyId) => {
  const tradeMap: Record<DifficultyId, number | null | undefined> = {
    single: entry.goldSingleTrade,
    normal: entry.goldNormalTrade,
    hard: entry.goldHardTrade,
    nightmare: entry.goldNightmareTrade
  }
  const boundMap: Record<DifficultyId, number | null | undefined> = {
    single: entry.goldSingleBound,
    normal: entry.goldNormalBound,
    hard: entry.goldHardBound,
    nightmare: entry.goldNightmareBound
  }
  const tradeValue = tradeMap[difficulty]
  const boundValue = boundMap[difficulty]
  const trade = typeof tradeValue === 'number' && Number.isFinite(tradeValue) && tradeValue > 0 ? tradeValue : null
  const bound = typeof boundValue === 'number' && Number.isFinite(boundValue) && boundValue > 0 ? boundValue : null
  return { trade, bound }
}

const resolveDifficultyItemLevel = (entry: RaidCatalogEntry, difficulty: DifficultyId) => {
  const levelMap: Record<DifficultyId, number | null | undefined> = {
    single: entry.itemLevelSingle,
    normal: entry.itemLevelNormal,
    hard: entry.itemLevelHard,
    nightmare: entry.itemLevelNightmare
  }
  const levelValue = levelMap[difficulty]
  if (typeof levelValue === 'number' && Number.isFinite(levelValue) && levelValue > 0) {
    return levelValue
  }
  if (typeof entry.itemLevel === 'number' && Number.isFinite(entry.itemLevel) && entry.itemLevel > 0) {
    return entry.itemLevel
  }
  return null
}

const resolveLegacyDifficultyGold = (entry: RaidCatalogEntry, difficulty: DifficultyId) => {
  const legacyMap: Record<DifficultyId, number | null | undefined> = {
    single: entry.goldSingle,
    normal: entry.goldNormal,
    hard: entry.goldHard,
    nightmare: entry.goldNightmare
  }
  const goldValue = legacyMap[difficulty]
  if (typeof goldValue === 'number' && Number.isFinite(goldValue) && goldValue > 0) {
    return goldValue
  }
  if (typeof entry.goldReward === 'number' && Number.isFinite(entry.goldReward) && entry.goldReward > 0) {
    return entry.goldReward
  }
  return null
}

const buildDifficultyGoldsTrade = (entry: RaidCatalogEntry): DifficultyGoldsTrade => {
  const tradeMap: Record<DifficultyId, number | null | undefined> = {
    single: entry.goldSingleTrade,
    normal: entry.goldNormalTrade,
    hard: entry.goldHardTrade,
    nightmare: entry.goldNightmareTrade
  }
  const boundMap: Record<DifficultyId, number | null | undefined> = {
    single: entry.goldSingleBound,
    normal: entry.goldNormalBound,
    hard: entry.goldHardBound,
    nightmare: entry.goldNightmareBound
  }
  const resolveValue = (difficulty: DifficultyId) => {
    const tradeValue = tradeMap[difficulty]
    const boundValue = boundMap[difficulty]
    if (isFiniteNumber(tradeValue)) return String(tradeValue)
    if (isFiniteNumber(boundValue)) return ''
    const legacyGold = resolveLegacyDifficultyGold(entry, difficulty)
    return legacyGold !== null ? String(legacyGold) : ''
  }
  return {
    single: resolveValue('single'),
    normal: resolveValue('normal'),
    hard: resolveValue('hard'),
    nightmare: resolveValue('nightmare')
  }
}

const buildDifficultyGoldsBound = (entry: RaidCatalogEntry): DifficultyGoldsBound => {
  const tradeMap: Record<DifficultyId, number | null | undefined> = {
    single: entry.goldSingleTrade,
    normal: entry.goldNormalTrade,
    hard: entry.goldHardTrade,
    nightmare: entry.goldNightmareTrade
  }
  const boundMap: Record<DifficultyId, number | null | undefined> = {
    single: entry.goldSingleBound,
    normal: entry.goldNormalBound,
    hard: entry.goldHardBound,
    nightmare: entry.goldNightmareBound
  }
  const resolveValue = (difficulty: DifficultyId) => {
    const boundValue = boundMap[difficulty]
    const tradeValue = tradeMap[difficulty]
    if (isFiniteNumber(boundValue)) return String(boundValue)
    if (isFiniteNumber(tradeValue)) return ''
    return ''
  }
  return {
    single: resolveValue('single'),
    normal: resolveValue('normal'),
    hard: resolveValue('hard'),
    nightmare: resolveValue('nightmare')
  }
}

const buildDifficultyLevels = (entry: RaidCatalogEntry): DifficultyLevels => ({
  single: entry.itemLevelSingle != null ? String(entry.itemLevelSingle) : '',
  normal: entry.itemLevelNormal != null ? String(entry.itemLevelNormal) : '',
  hard: entry.itemLevelHard != null ? String(entry.itemLevelHard) : '',
  nightmare: entry.itemLevelNightmare != null ? String(entry.itemLevelNightmare) : ''
})

const parseGoldValue = (value: unknown) => parseOptionalNumber(value)
const parseLevelValue = (value: unknown) => parseOptionalNumber(value)

const buildDifficultyGoldPayload = (
  difficulties: DifficultyId[],
  tradeGolds: DifficultyGoldsTrade,
  boundGolds: DifficultyGoldsBound
) => {
  const selected = new Set(difficulties)
  const payload: {
    goldSingleTrade?: number | null
    goldSingleBound?: number | null
    goldNormalTrade?: number | null
    goldNormalBound?: number | null
    goldHardTrade?: number | null
    goldHardBound?: number | null
    goldNightmareTrade?: number | null
    goldNightmareBound?: number | null
  } = {}

  const singleTrade = parseGoldValue(tradeGolds.single)
  if (selected.has('single') && singleTrade !== null) payload.goldSingleTrade = singleTrade
  const singleBound = parseGoldValue(boundGolds.single)
  if (selected.has('single') && singleBound !== null) payload.goldSingleBound = singleBound

  const normalTrade = parseGoldValue(tradeGolds.normal)
  if (selected.has('normal') && normalTrade !== null) payload.goldNormalTrade = normalTrade
  const normalBound = parseGoldValue(boundGolds.normal)
  if (selected.has('normal') && normalBound !== null) payload.goldNormalBound = normalBound

  const hardTrade = parseGoldValue(tradeGolds.hard)
  if (selected.has('hard') && hardTrade !== null) payload.goldHardTrade = hardTrade
  const hardBound = parseGoldValue(boundGolds.hard)
  if (selected.has('hard') && hardBound !== null) payload.goldHardBound = hardBound

  const nightmareTrade = parseGoldValue(tradeGolds.nightmare)
  if (selected.has('nightmare') && nightmareTrade !== null) payload.goldNightmareTrade = nightmareTrade
  const nightmareBound = parseGoldValue(boundGolds.nightmare)
  if (selected.has('nightmare') && nightmareBound !== null) payload.goldNightmareBound = nightmareBound

  return payload
}

const buildDifficultyLevelPayload = (difficulties: DifficultyId[], levels: DifficultyLevels) => {
  const selected = new Set(difficulties)
  const payload: {
    itemLevelSingle?: number | null
    itemLevelNormal?: number | null
    itemLevelHard?: number | null
    itemLevelNightmare?: number | null
  } = {}

  const singleLevel = parseLevelValue(levels.single)
  if (selected.has('single') && singleLevel !== null) payload.itemLevelSingle = singleLevel
  const normalLevel = parseLevelValue(levels.normal)
  if (selected.has('normal') && normalLevel !== null) payload.itemLevelNormal = normalLevel
  const hardLevel = parseLevelValue(levels.hard)
  if (selected.has('hard') && hardLevel !== null) payload.itemLevelHard = hardLevel
  const nightmareLevel = parseLevelValue(levels.nightmare)
  if (selected.has('nightmare') && nightmareLevel !== null) payload.itemLevelNightmare = nightmareLevel

  return payload
}

type RaidCatalogUpdatePayload = Parameters<typeof lostarkApi.updateRaidCatalog>[1]

const buildRaidDraft = (row: RaidCatalogEntry): RaidDraft => {
  const difficulties = Array.isArray(row.difficulties) ? row.difficulties : []
  return {
    raidKey: row.raidKey,
    raidName: row.raidName,
    abbreviation: row.abbreviation ?? '',
    active: row.active,
    difficulties: normalizeDifficultyIds(difficulties),
    partySize: row.partySize != null ? String(row.partySize) : '',
    difficultyLevels: buildDifficultyLevels(row),
    difficultyGoldsTrade: buildDifficultyGoldsTrade(row),
    difficultyGoldsBound: buildDifficultyGoldsBound(row)
  }
}

const startEditAll = () => {
  if (loading.value) return
  isEditMode.value = true
  const drafts: Record<string, RaidDraft> = {}
  rows.value.forEach((row) => {
    drafts[row.raidKey] = buildRaidDraft(row)
  })
  editDrafts.value = drafts
}

const cancelEditAll = () => {
  isEditMode.value = false
  editDrafts.value = {}
}

const buildUpdatePayload = (draft: RaidDraft): RaidCatalogUpdatePayload => {
  const raidName = normalizeText(draft.raidName)
  const abbreviation = normalizeText(draft.abbreviation)
  const normalizedDifficulties = normalizeDifficultyIds(draft.difficulties)
  return {
    raidName,
    abbreviation: abbreviation || null,
    active: draft.active,
    difficulties: normalizedDifficulties,
    partySize: parseOptionalNumber(draft.partySize),
    ...buildDifficultyLevelPayload(normalizedDifficulties, draft.difficultyLevels),
    ...buildDifficultyGoldPayload(
      normalizedDifficulties,
      draft.difficultyGoldsTrade,
      draft.difficultyGoldsBound
    )
  }
}

const saveAllEdits = async () => {
  if (gateEnabled && !unlocked.value) return
  if (!isEditMode.value) return
  const hasMissingName = rows.value.some((row) => {
    const draft = editDrafts.value[row.raidKey]
    return !draft || !normalizeText(draft.raidName)
  })
  if (hasMissingName) {
    error.value = 'raidName이 필요합니다.'
    return
  }
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    const updatedRows: RaidCatalogEntry[] = []
    for (const row of rows.value) {
      const draft = editDrafts.value[row.raidKey]
      if (!draft) continue
      const payload = buildUpdatePayload(draft)
      const updated = await lostarkApi.updateRaidCatalog(row.raidKey, payload)
      updatedRows.push(updated)
    }
    const updatedMap = new Map(updatedRows.map(item => [item.raidKey, item]))
    rows.value = rows.value.map(item => updatedMap.get(item.raidKey) ?? item)
    isEditMode.value = false
    editDrafts.value = {}
    message.value = '수정했어요.'
  } catch (err: unknown) {
    error.value = resolveErrorMessage(err, '레이드 수정에 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const loadCatalog = async () => {
  if (gateEnabled && !unlocked.value) return
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    rows.value = await lostarkApi.getRaidCatalog()
    message.value = `${rows.value.length}건`
  } catch (err: unknown) {
    error.value = resolveErrorMessage(err, '레이드 목록을 불러오지 못했습니다.')
  } finally {
    loading.value = false
  }
}

watch(rows, (nextRows) => {
  if (!isEditMode.value) return
  const nextDrafts: Record<string, RaidDraft> = {}
  nextRows.forEach((row) => {
    nextDrafts[row.raidKey] = editDrafts.value[row.raidKey] ?? buildRaidDraft(row)
  })
  editDrafts.value = nextDrafts
})

const createRaid = async () => {
  if (gateEnabled && !unlocked.value) return
  if (isEditMode.value) return
  const raidKey = normalizeText(draft.value.raidKey)
  const raidName = normalizeText(draft.value.raidName)
  const abbreviation = normalizeText(draft.value.abbreviation)
  if (!raidKey || !raidName) return
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    const normalizedDifficulties = normalizeDifficultyIds(draft.value.difficulties)
    await lostarkApi.createRaidCatalog({
      raidKey,
      raidName,
      abbreviation: abbreviation || null,
      active: draft.value.active,
      difficulties: normalizedDifficulties,
      partySize: parseOptionalNumber(draft.value.partySize),
      ...buildDifficultyLevelPayload(normalizedDifficulties, draft.value.difficultyLevels),
      ...buildDifficultyGoldPayload(
        normalizedDifficulties,
        draft.value.difficultyGoldsTrade,
        draft.value.difficultyGoldsBound
      )
    })
    draft.value.raidKey = ''
    draft.value.raidName = ''
    draft.value.abbreviation = ''
    draft.value.active = true
    draft.value.difficulties = []
    draft.value.partySize = ''
    draft.value.difficultyLevels = createEmptyDifficultyLevels()
    draft.value.difficultyGoldsTrade = createEmptyDifficultyGoldsTrade()
    draft.value.difficultyGoldsBound = createEmptyDifficultyGoldsBound()
    message.value = '추가했어요.'
    await loadCatalog()
  } catch (err: unknown) {
    error.value = resolveErrorMessage(err, '레이드 추가에 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const deleteRaid = async (row: RaidCatalogEntry) => {
  if (gateEnabled && !unlocked.value) return
  const label = normalizeText(row.raidName || row.raidKey)
  const confirmed = window.confirm(`"${label}" 레이드를 삭제할까요?`)
  if (!confirmed) return
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    await lostarkApi.deleteRaidCatalog(row.raidKey)
    rows.value = rows.value.filter(item => item.raidKey !== row.raidKey)
    if (editDrafts.value[row.raidKey]) {
      const nextDrafts = { ...editDrafts.value }
      delete nextDrafts[row.raidKey]
      editDrafts.value = nextDrafts
    }
    message.value = '삭제했어요.'
  } catch (err: unknown) {
    error.value = resolveErrorMessage(err, '레이드 삭제에 실패했습니다.')
  } finally {
    loading.value = false
  }
}

const unlock = () => {
  if (!gateEnabled) {
    unlocked.value = true
    return
  }
  if (passwordInput.value && passwordInput.value === import.meta.env.VITE_ADMIN_PASSWORD) {
    unlocked.value = true
    localStorage.setItem(ADMIN_KEY, '1')
    message.value = '접속 허용됨'
    error.value = ''
    loadCatalog()
  } else {
    error.value = '비밀번호가 올바르지 않습니다.'
  }
}

onMounted(() => {
  if (!gateEnabled) {
    unlocked.value = true
    loadCatalog()
  } else {
    const saved = localStorage.getItem(ADMIN_KEY)
    if (saved === '1') {
      unlocked.value = true
      loadCatalog()
    }
  }
})

onActivated(() => {
  isActive.value = true
})

onDeactivated(() => {
  isActive.value = false
})
</script>

<style scoped>
.admin-raid-catalog {
  display: grid;
  gap: 16px;
}

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-end;
}

.panel {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  background: var(--card-bg, #ffffff);
  padding: 14px;
  display: grid;
  gap: 12px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1.6fr 0.8fr 2fr 0.6fr auto auto;
  gap: 10px;
  align-items: end;
}

.field {
  display: grid;
  gap: 6px;
}

.field--wide {
  min-width: 220px;
}

.field--compact {
  min-width: 120px;
}
.field-label {
  font-size: 0.85rem;
  color: var(--text-secondary, #4b5563);
}

.checkbox {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  user-select: none;
  color: var(--text-secondary, #4b5563);
}

.checkbox.inline {
  margin: 0;
}

.checkbox--gold {
  gap: 6px;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.gate {
  border: 1px dashed var(--border-color, #e5e7eb);
  padding: 12px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.table {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 10px;
  overflow: hidden;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 1.1fr 1.2fr 0.7fr 1.8fr 0.6fr 0.6fr 1fr 0.8fr;
  gap: 8px;
  padding: 10px 12px;
  align-items: center;
}

.table-head {
  background: var(--bg-secondary, #f3f4f6);
  color: var(--text-secondary, #4b5563);
  font-weight: 600;
}

.table-row:nth-child(odd) {
  background: var(--bg-secondary, #f9fafb);
}

.pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(34, 197, 94, 0.35);
  background: rgba(34, 197, 94, 0.12);
  color: #15803d;
  font-weight: 700;
  font-size: 0.82rem;
}

.pill.off {
  border-color: rgba(107, 114, 128, 0.35);
  background: rgba(107, 114, 128, 0.12);
  color: #374151;
}

.btn {
  padding: 9px 12px;
  border-radius: 10px;
  border: 1px solid var(--primary-color, #2563eb);
  background: linear-gradient(120deg, #2563eb, #1d4ed8);
  color: #fff;
  cursor: pointer;
}

.btn.ghost {
  background: var(--card-bg, #fff);
  color: var(--text-primary, #111827);
  border-color: var(--border-color, #e5e7eb);
}

.btn-sm {
  padding: 7px 10px;
  border-radius: 10px;
  font-size: 0.85rem;
}

.btn-group {
  display: flex;
  gap: 6px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.btn-primary {
  border-color: var(--primary-color, #2563eb);
}

.input {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid var(--border-color, #e5e7eb);
}

.input--inline {
  width: 100%;
  min-width: 0;
}

.input--xs {
  width: 80px;
  min-width: 0;
  padding: 6px 8px;
  font-size: 0.82rem;
}

.select {
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
}

.select--inline {
  width: 100%;
  min-width: 0;
}

.empty {
  color: var(--text-secondary, #6b7280);
}

.error-text {
  color: #b91c1c;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.02em;
  font-size: 0.82rem;
  color: var(--text-secondary, #6b7280);
  margin: 0 0 4px;
}

.muted {
  color: var(--text-secondary, #6b7280);
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}

.align-right {
  text-align: right;
}

@media (max-width: 860px) {
  .form-row {
    grid-template-columns: 1fr;
    align-items: stretch;
  }
  .field--wide {
    min-width: unset;
  }
  .align-right {
    text-align: left;
  }
  .table-head,
  .table-row {
    grid-template-columns: 1fr;
  }
}
</style>
