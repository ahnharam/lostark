<template>
  <div
    ref="wrapperRef"
    class="lazy-image-wrapper"
    :class="{ loading: isLoading, error: hasError }"
    :style="wrapperStyle"
  >
    <!-- 로딩 스켈레톤 -->
    <div v-if="isLoading && showSkeleton" class="skeleton-loader"></div>

    <!-- 실제 이미지 -->
    <img
      v-show="!isLoading && !hasError"
      ref="imgRef"
      :src="currentSrc"
      :alt="alt"
      :class="imageClass"
      :referrerpolicy="referrerPolicy"
      @load="onLoad"
      @error="onError"
    />

    <!-- 에러 플레이스홀더 -->
    <div v-if="hasError" class="error-placeholder">
      <span class="error-icon">{{ errorIcon }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'

interface Props {
  src: string
  alt?: string
  width?: string | number
  height?: string | number
  imageClass?: string
  placeholder?: string
  errorIcon?: string
  showSkeleton?: boolean
  lazy?: boolean
  referrerPolicy?: ReferrerPolicy
}

const props = withDefaults(defineProps<Props>(), {
  alt: '',
  width: 'auto',
  height: 'auto',
  imageClass: '',
  placeholder: '',
  errorIcon: '🖼️',
  showSkeleton: true,
  lazy: true,
  referrerPolicy: 'no-referrer'
})

const imgRef = ref<HTMLImageElement | null>(null)
const wrapperRef = ref<HTMLElement | null>(null)
const isLoading = ref(true)
const hasError = ref(false)
const currentSrc = ref(props.placeholder || '')
const observer = ref<IntersectionObserver | null>(null)

const formatSize = (value?: string | number) => {
  if (typeof value === 'number') {
    return `${value}px`
  }
  if (!value || value === 'auto') {
    return 'auto'
  }
  // "96"처럼 단위가 없는 문자열도 px로 간주
  if (/^\d+(\.\d+)?$/.test(value)) {
    return `${value}px`
  }
  return value
}

const wrapperStyle = computed(() => ({
  width: formatSize(props.width),
  height: formatSize(props.height)
}))

const normalizeSrc = (value: string) => {
  if (!value) return ''
  let normalized = value.trim()
  if (normalized.startsWith('//')) {
    normalized = `https:${normalized}`
  } else if (normalized.startsWith('http://')) {
    normalized = normalized.replace('http://', 'https://')
  }
  return normalized
}

const loadImage = () => {
  if (!props.src) return
  currentSrc.value = normalizeSrc(props.src)
}

const onLoad = () => {
  isLoading.value = false
  hasError.value = false
}

const onError = () => {
  isLoading.value = false
  hasError.value = true
}

const destroyObserver = () => {
  if (observer.value) {
    observer.value.disconnect()
    observer.value = null
  }
}

const setupObserver = () => {
  if (!props.lazy) {
    loadImage()
    return
  }

  if (typeof window === 'undefined' || !('IntersectionObserver' in window)) {
    loadImage()
    return
  }

  if (!observer.value) {
    observer.value = new IntersectionObserver(
      entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting && isLoading.value && !hasError.value) {
            loadImage()
            if (observer.value) {
              observer.value.unobserve(entry.target)
            }
          }
        })
      },
      {
        rootMargin: '50px',
        threshold: 0.01
      }
    )
  }

  if (wrapperRef.value) {
    observer.value.observe(wrapperRef.value)
  } else {
    // DOM 참조를 못 잡으면 즉시 로드
    loadImage()
  }
}

onMounted(() => {
  setupObserver()
})

onUnmounted(() => {
  destroyObserver()
})

watch(
  () => props.src,
  () => {
    isLoading.value = true
    hasError.value = false
    currentSrc.value = props.placeholder || ''
    setupObserver()
  }
)
</script>

<style scoped>
.lazy-image-wrapper {
  position: relative;
  display: inline-block;
  overflow: hidden;
  background: var(--bg-secondary);
}

.lazy-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s ease-in-out;
}

/* 로딩 스켈레톤 */
.skeleton-loader {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    var(--bg-secondary) 25%,
    var(--bg-hover) 50%,
    var(--bg-secondary) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 에러 플레이스홀더 */
.error-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  color: var(--text-tertiary);
}

.error-icon {
  font-size: 2rem;
  opacity: 0.5;
}

/* 페이드인 효과 */
.lazy-image-wrapper.loading img {
  opacity: 0;
}

.lazy-image-wrapper:not(.loading) img {
  opacity: 1;
}
</style>
