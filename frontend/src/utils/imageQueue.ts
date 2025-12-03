type QueueTask = {
  url: string
  resolve: (value: string) => void
  reject: (reason?: any) => void
}

const MAX_CONCURRENT = 4
const MAX_CACHE_ENTRIES = 150
const RETRY_LIMIT = 2
const RETRY_DELAY_MS = 300

const taskQueue: QueueTask[] = []
const inflight = new Map<string, Promise<string>>()
const cache = new Map<string, string>()
let activeCount = 0

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

const trimCache = () => {
  while (cache.size > MAX_CACHE_ENTRIES) {
    const first = cache.entries().next().value as [string, string] | undefined
    if (!first) break
    const [key, blobUrl] = first
    URL.revokeObjectURL(blobUrl)
    cache.delete(key)
  }
}

const fetchWithRetry = async (url: string): Promise<string> => {
  let lastError: unknown

  for (let attempt = 0; attempt <= RETRY_LIMIT; attempt++) {
    try {
      const response = await fetch(url, { cache: 'force-cache' })
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      const blob = await response.blob()
      return URL.createObjectURL(blob)
    } catch (err) {
      lastError = err
      if (attempt < RETRY_LIMIT) {
        await delay(RETRY_DELAY_MS * (attempt + 1))
      }
    }
  }

  throw lastError ?? new Error('Image fetch failed')
}

const runTask = async (task: QueueTask) => {
  const cached = cache.get(task.url)
  if (cached) return cached

  const inflightPromise = inflight.get(task.url)
  if (inflightPromise) return inflightPromise

  const promise = fetchWithRetry(task.url)
  inflight.set(task.url, promise)

  try {
    const result = await promise
    cache.set(task.url, result)
    trimCache()
    return result
  } finally {
    inflight.delete(task.url)
  }
}

const processQueue = () => {
  while (activeCount < MAX_CONCURRENT && taskQueue.length) {
    const next = taskQueue.shift()
    if (!next) break
    activeCount++

    runTask(next)
      .then(next.resolve)
      .catch(next.reject)
      .finally(() => {
        activeCount--
        processQueue()
      })
  }
}

export const enqueueImageRequest = (url: string): Promise<string> => {
  if (typeof window === 'undefined' || typeof fetch === 'undefined') {
    return Promise.resolve(url)
  }

  return new Promise((resolve, reject) => {
    taskQueue.push({ url, resolve, reject })
    processQueue()
  })
}

export const invalidateImageCache = (url?: string) => {
  if (!url) {
    cache.forEach(blobUrl => URL.revokeObjectURL(blobUrl))
    cache.clear()
    return
  }

  const blobUrl = cache.get(url)
  if (blobUrl) {
    URL.revokeObjectURL(blobUrl)
    cache.delete(url)
  }
}
