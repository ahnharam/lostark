<template>
  <div ref="rootRef" class="custom-select" :class="{ 'is-open': open, 'is-disabled': disabled }">
    <button
      :id="triggerId"
      type="button"
      class="custom-select__trigger"
      :class="triggerClass"
      :style="triggerStyle"
      v-bind="passthroughAttrs"
      :disabled="disabled"
      :aria-label="ariaLabel"
      :aria-expanded="open"
      aria-haspopup="listbox"
      :aria-required="required || undefined"
      @click="toggleOpen"
      @keydown="handleTriggerKeydown"
    >
      <span class="custom-select__label">{{ displayLabel }}</span>
      <span class="custom-select__arrow" aria-hidden="true">▾</span>
    </button>

    <transition name="fade">
      <div v-if="open" ref="menuRef" class="custom-select__menu" :class="menuClass" role="listbox">
        <template v-if="groups && groups.length">
          <div v-for="group in groups" :key="group.label" class="custom-select__group">
            <div class="custom-select__group-label">{{ group.label }}</div>
            <button
              v-for="option in group.options"
              :key="optionKey(option)"
              type="button"
              class="custom-select__option"
              :class="{
                selected: isSelected(option.value),
                disabled: option.disabled,
                'is-group': option.kind === 'group',
                'is-child': (option.depth ?? 0) > 0
              }"
              :disabled="option.disabled"
              role="option"
              :aria-selected="isSelected(option.value)"
              @click="selectOption(option)"
            >
              {{ option.label }}
            </button>
          </div>
        </template>
        <template v-else>
          <button
            v-for="option in options"
            :key="optionKey(option)"
            type="button"
            class="custom-select__option"
            :class="{
              selected: isSelected(option.value),
              disabled: option.disabled,
              'is-group': option.kind === 'group',
              'is-child': (option.depth ?? 0) > 0
            }"
            :disabled="option.disabled"
            role="option"
            :aria-selected="isSelected(option.value)"
            @click="selectOption(option)"
          >
            {{ option.label }}
          </button>
        </template>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, useAttrs } from 'vue'

type SelectValue = string | number | null

export type SelectOption = {
  label: string
  value: SelectValue
  disabled?: boolean
  depth?: number
  kind?: 'group' | 'item'
}

export type SelectGroup = {
  label: string
  options: SelectOption[]
}

defineOptions({
  inheritAttrs: false
})

const props = withDefaults(
  defineProps<{
    modelValue: SelectValue
    options?: SelectOption[]
    groups?: SelectGroup[]
    disabled?: boolean
    placeholder?: string
    required?: boolean
    ariaLabel?: string
    layout?: 'list' | 'grid'
  }>(),
  {
    options: () => [],
    layout: 'list'
  }
)

const emit = defineEmits<{
  (event: 'update:modelValue', value: SelectValue): void
  (event: 'change', value: SelectValue): void
}>()

const attrs = useAttrs()
const open = ref(false)
const rootRef = ref<HTMLElement | null>(null)
const menuRef = ref<HTMLElement | null>(null)

const triggerClass = computed(() => attrs.class)
const triggerStyle = computed(() => attrs.style as string | Record<string, string> | undefined)
const triggerId = computed(() => (typeof attrs.id === 'string' ? attrs.id : undefined))
const passthroughAttrs = computed(() => {
  const { class: _class, style: _style, id: _id, ...rest } = attrs
  return rest
})

const allOptions = computed(() => {
  if (props.groups && props.groups.length) {
    return props.groups.flatMap(group => group.options)
  }
  return props.options
})

const displayLabel = computed(() => {
  const match = allOptions.value.find(option => option.value === props.modelValue)
  if (match) return match.label
  return props.placeholder ?? '선택'
})

const menuClass = computed(() =>
  props.layout === 'grid' ? 'custom-select__menu--grid' : 'custom-select__menu--list'
)

const isSelected = (value: SelectValue) => value === props.modelValue

const optionKey = (option: SelectOption) =>
  option.value === null ? `null:${option.label}` : `${option.value}:${option.label}`

const close = () => {
  open.value = false
}

const toggleOpen = () => {
  if (props.disabled) return
  open.value = !open.value
}

const focusFirstOption = () => {
  if (!menuRef.value) return
  const target = menuRef.value.querySelector<HTMLButtonElement>('.custom-select__option:not(:disabled)')
  target?.focus()
}

const handleTriggerKeydown = (event: KeyboardEvent) => {
  if (props.disabled) return
  if (event.key === 'Escape') {
    close()
    return
  }
  if (event.key === 'Enter' || event.key === ' ' || event.key === 'ArrowDown') {
    event.preventDefault()
    open.value = true
    requestAnimationFrame(focusFirstOption)
  }
}

const selectOption = (option: SelectOption) => {
  if (props.disabled || option.disabled) return
  if (option.value !== props.modelValue) {
    emit('update:modelValue', option.value)
  }
  emit('change', option.value)
  close()
}

const handleDocumentClick = (event: MouseEvent) => {
  const root = rootRef.value
  if (!root) return
  if (event.target instanceof Node && !root.contains(event.target)) {
    close()
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentClick)
})
</script>

<style scoped>
.custom-select {
  position: relative;
  display: inline-flex;
  min-width: 0;
}

.custom-select__trigger {
  width: 100%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: var(--bg-secondary, #f8fafc);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 10px;
  padding: 10px 12px;
  color: var(--text-primary, #111827);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.custom-select__trigger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.custom-select__trigger:focus {
  outline: none;
  border-color: var(--primary-color, #2563eb);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.custom-select__label {
  flex: 1 1 auto;
  text-align: center;
}

.custom-select__arrow {
  font-size: 0.7rem;
}

.custom-select__menu {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  width: 100%;
  max-height: 240px;
  overflow-y: auto;
  background: var(--card-bg, #ffffff);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
  padding: 6px;
  z-index: 40;
  display: grid;
  gap: 4px;
}

.custom-select__menu--list {
  grid-template-columns: 1fr;
}

.custom-select__menu--grid {
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
}

.custom-select__group {
  display: grid;
  gap: 4px;
  margin-bottom: 6px;
}

.custom-select__group:last-child {
  margin-bottom: 0;
}

.custom-select__group-label {
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--text-secondary, #6b7280);
  padding: 4px 6px;
  text-align: center;
}

.custom-select__option {
  display: block;
  width: 100%;
  border: none;
  background: transparent;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
  font-weight: 600;
  color: var(--text-primary, #111827);
}

.custom-select__option.is-group {
  display: block;
  width: 100%;
  text-align: left;
  font-weight: 700;
  color: var(--text-tertiary, #9ca3af);
  cursor: default;
}

.custom-select__menu--grid .custom-select__option.is-group {
  grid-column: 1 / -1;
}

.custom-select__option.selected {
  background: var(--primary-soft-bg, rgba(99, 102, 241, 0.16));
  color: var(--primary-color, #6366f1);
}

.custom-select__option:hover:not(:disabled) {
  background: var(--bg-secondary, #f3f4f6);
}

.custom-select__option:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.custom-select__option.is-group:disabled {
  opacity: 1;
  cursor: default;
}
</style>
