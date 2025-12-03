<template>
  <LazyImage
    :src="src"
    :alt="alt"
    :width="resolvedWidth"
    :height="resolvedHeight"
    :image-class="imageClass"
    :error-icon="errorIcon"
    :use-proxy="useProxy"
    :use-queue="useQueue"
    :show-skeleton="showSkeleton"
    :lazy="lazy"
    :referrer-policy="referrerPolicy"
    :cross-origin="crossOrigin || undefined"
    :log-errors="logErrors"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LazyImage from './LazyImage.vue'

interface Props {
  src: string
  alt?: string
  width?: string | number
  height?: string | number
  size?: string | number
  imageClass?: string
  errorIcon?: string
  useProxy?: boolean
  useQueue?: boolean
  showSkeleton?: boolean
  lazy?: boolean
  referrerPolicy?: ReferrerPolicy
  crossOrigin?: '' | 'anonymous' | 'use-credentials'
  logErrors?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  alt: '',
  imageClass: '',
  errorIcon: '🖼️',
  useProxy: true,
  useQueue: true,
  showSkeleton: true,
  lazy: false,
  referrerPolicy: 'no-referrer',
  crossOrigin: '',
  logErrors: false
})

const resolvedWidth = computed(() => props.size ?? props.width ?? 'auto')
const resolvedHeight = computed(() => props.size ?? props.height ?? 'auto')
</script>
