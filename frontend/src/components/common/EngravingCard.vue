<template>
  <article class="engraving-card">
    <div class="engraving-row">
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
import { stripHtml } from '@/utils/tooltipParser'
import type { Engraving } from '@/api/types'

interface Props {
  engraving: Engraving
}

const props = defineProps<Props>()

const parsedData = computed(() => parseEngravingDescription(props.engraving.description ?? ''))

const displayName = computed(() => props.engraving.name || parsedData.value.name || '각인')

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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-left: auto;
  margin-right: 0;
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
</style>
