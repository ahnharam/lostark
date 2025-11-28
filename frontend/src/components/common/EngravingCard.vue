<template>
  <article class="engraving-card">
    <div class="engraving-row">
      <div class="engraving-icon-box">
        <LazyImage
          v-if="iconSrc"
          :src="iconSrc"
          :alt="displayName"
          width="44"
          height="44"
          imageClass="engraving-icon-img"
          errorIcon="📘"
          :showSkeleton="false"
        />
        <div v-else class="engraving-icon-fallback">
          {{ engravingInitial }}
        </div>
      </div>
      <div class="engraving-left">
        <span class="engraving-level">Lv.{{ formatLevel(displayLevel) }}</span>
        <span v-if="engraving.grade" class="meta-chip grade-chip">{{ engraving.grade }}</span>
        <div class="ability-area">
          <span v-if="abilityStoneLabel" class="meta-chip ability-chip">
            🔷 {{ abilityStoneLabel }}
          </span>
        </div>
        <span class="engraving-name">{{ displayName }}</span>
        <span v-if="primaryDescription" class="engraving-description">{{ primaryDescription }}</span>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { parseEngravingDescription } from '@/utils/engravingParser'
import { ENGRAVING_ICON_MAP } from '@/assets/BuffImage'
import { stripHtml } from '@/utils/tooltipParser'
import { getEngravingDisplayName } from '@/data/engravingNames'
import type { Engraving } from '@/api/types'
import LazyImage from './LazyImage.vue'

interface Props {
  engraving: Engraving
}

const props = defineProps<Props>()

const parsedData = computed(() => parseEngravingDescription(props.engraving.description ?? ''))

const baseName = computed(() => props.engraving.name || parsedData.value.name || '각인')

const displayName = computed(() => getEngravingDisplayName(baseName.value))

const iconSrc = computed(() => {
  const explicit = props.engraving.icon?.trim()
  if (explicit) return explicit
  const key = baseName.value.trim()
  return key && ENGRAVING_ICON_MAP[key] ? ENGRAVING_ICON_MAP[key] : ''
})

const engravingInitial = computed(() => displayName.value.charAt(0) || '?')

const displayLevel = computed(() => {
  if (typeof props.engraving.level === 'number') {
    return props.engraving.level
  }
  if (typeof parsedData.value.level === 'number') {
    return parsedData.value.level
  }
  return 0
})

const abilityStoneLabel = computed(() => {
  const value = props.engraving.abilityStoneLevel
  if (value === null || value === undefined) return ''
  return `세공 ${value}`
})

const primaryDescription = computed(() => {
  const raw = props.engraving.description || parsedData.value.rawDescription
  if (!raw) return ''
  const firstLine = raw
    .replace(/<br\s*\/?>/gi, '\n')
    .split(/\r?\n/g)
    .map(line => stripHtml(line).replace(/\s+/g, ' ').trim())
    .find(Boolean)
  return firstLine || ''
})

const formatLevel = (value: number) => {
  return Number.isFinite(value) ? value : 0
}
</script>

<style scoped>
.engraving-card {
  padding: 6px 0;
  border-bottom: 1px solid var(--border-color, #e5e7eb);
}

.engraving-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.95rem;
  color: var(--text-primary, #1f2937);
}

.engraving-left {
  display: inline-flex;
  align-items: center;
  flex: 1;
  gap: 10px;
  overflow: hidden;
}

.engraving-name {
  min-width: 100px;
  font-weight: 600;
}

.engraving-description {
  color: var(--text-secondary, #4b5563);
  font-size: 0.85rem;
  min-width: 120px;
  max-width: 500px;
  white-space: pre-wrap;
  word-break: keep-all;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-left: 0;
  margin-right: auto;
}

.engraving-level {
  font-weight: 700;
  color: var(--text-primary, #1f2937);
  padding-left: 12px;
}

.meta-chip {
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid var(--border-color, #d1d5db);
  font-size: 0.78rem;
  color: var(--text-secondary, #4b5563);
  white-space: nowrap;
}

.grade-chip {
  text-transform: uppercase;
  font-weight: 700;
}

.ability-area{
  width: 100px;
}

.ability-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.engraving-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  border: 1px solid var(--border-color, #e5e7eb);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--bg-default, #f9fafb);
}

.engraving-icon-img {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  object-fit: cover;
  display: block;
}

.engraving-icon-fallback {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--bg-muted, #e5e7eb);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: var(--text-secondary, #4b5563);
}
</style>
