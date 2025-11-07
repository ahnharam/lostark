<template>
  <div class="lazy-image-wrapper" :class="{ loading: isLoading, error: hasError }" :style="wrapperStyle">
    <!-- 로딩 스켈레톤 -->
    <div v-if="isLoading && showSkeleton" class="skeleton-loader"></div>

    <!-- 실제 이미지 -->
    <img
      v-show="!isLoading && !hasError"
      ref="imgRef"
      :src="currentSrc"
      :alt="alt"
      :class="imageClass"
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
import { ref, onMounted, onUnmounted, computed } from 'vue'

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
}

const props = withDefaults(defineProps<Props>(), {
  alt: '',
  width: 'auto',
  height: 'auto',
  imageClass: '',
  placeholder: '',
  errorIcon: '🖼️',
  showSkeleton: true,
  lazy: true
})

const imgRef = ref<HTMLImageElement | null>(null)
const isLoading = ref(true)
const hasError = ref(false)
const currentSrc = ref(props.placeholder || '')
const observer = ref<IntersectionObserver | null>(null)

const wrapperStyle = computed(() => ({
  width: typeof props.width === 'number' ? `${props.width}px` : props.width,
  height: typeof props.height === 'number' ? `${props.height}px` : props.height,
}))

const loadImage = () => {
  currentSrc.value = props.src
}

const onLoad = () => {
  isLoading.value = false
  hasError.value = false
}

const onError = () => {
  isLoading.value = false
  hasError.value = true
}

onMounted(() => {
  if (!props.lazy) {
    // 레이지 로딩 비활성화 시 즉시 로드
    loadImage()
    return
  }

  // Intersection Observer 설정
  observer.value = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && isLoading.value && !hasError.value) {
          loadImage()
          // 한 번 로드되면 observer 해제
          if (observer.value && imgRef.value) {
            observer.value.unobserve(entry.target)
          }
        }
      })
    },
    {
      rootMargin: '50px', // 뷰포트에서 50px 전에 미리 로드
      threshold: 0.01
    }
  )

  if (imgRef.value) {
    observer.value.observe(imgRef.value.parentElement as Element)
  }
})

onUnmounted(() => {
  if (observer.value) {
    observer.value.disconnect()
  }
})
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
