import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'
import { isRecord, isString } from '@/utils/typeGuards'

const MAX_RETRIES = 1
const BASE_RETRY_DELAY_MS = 350
const MAX_RETRY_DELAY_MS = 1200
const RETRYABLE_STATUS = [429, 502, 503, 504]

type RetryableConfig = AxiosRequestConfig & { __retryCount?: number }
type CsrfRetryableConfig = RetryableConfig & { __csrfRetried?: boolean }

const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

const isRetryableError = (error: AxiosError): error is AxiosError & { config: RetryableConfig } => {
  const config = error.config as RetryableConfig | undefined
  if (!config) return false
  const method = (config.method || 'get').toUpperCase()
  if (method !== 'GET') return false

  const status = error.response?.status
  const isNetworkError = !error.response && (error.code === 'ECONNABORTED' || /timeout/i.test(error.message))

  return Boolean(
    (status && RETRYABLE_STATUS.includes(status)) ||
      isNetworkError
  )
}

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN'
})

let csrfTokenFromApi: string | null = null
let csrfRefreshPromise: Promise<void> | null = null

const getCookie = (name: string) => {
  if (typeof document === 'undefined') return null
  const escaped = name.replace(/[$()*+./?[\\\]^{|}-]/g, '\\$&')
  const match = document.cookie.match(new RegExp(`(?:^|; )${escaped}=([^;]*)`))
  const value = match?.[1]
  return value ? decodeURIComponent(value) : null
}

const shouldAttachCsrf = (method?: string) => {
  const normalized = (method || 'get').toUpperCase()
  return normalized !== 'GET' && normalized !== 'HEAD' && normalized !== 'OPTIONS'
}

const refreshCsrfToken = async (): Promise<void> => {
  if (!csrfRefreshPromise) {
    csrfRefreshPromise = apiClient
      .get('/auth/csrf')
      .then(() => undefined)
      .finally(() => {
        csrfRefreshPromise = null
      })
  }
  return csrfRefreshPromise
}

apiClient.interceptors.request.use(config => {
  if (shouldAttachCsrf(config.method)) {
    const token = getCookie('XSRF-TOKEN') || csrfTokenFromApi
    if (token) {
      config.headers = config.headers ?? {}
      config.headers['X-XSRF-TOKEN'] = token
    }
  }
  return config
})

apiClient.interceptors.response.use(
  response => {
    const url = response.config?.url
    if (typeof url === 'string' && url.includes('/auth/csrf')) {
      const data: unknown = response.data
      if (isRecord(data) && isString(data.token)) {
        csrfTokenFromApi = data.token
      }
    }
    return response
  },
  async (error: AxiosError) => {
    const config = error.config as CsrfRetryableConfig | undefined
    const method = (config?.method || 'get').toUpperCase()
    const url = typeof config?.url === 'string' ? config.url : ''

    if (
      error.response?.status === 403 &&
      config &&
      shouldAttachCsrf(method) &&
      !config.__csrfRetried &&
      !url.includes('/auth/csrf')
    ) {
      config.__csrfRetried = true
      try {
        await refreshCsrfToken()
        return apiClient(config)
      } catch {
        // fall through
      }
    }

    if (!isRetryableError(error)) {
      return Promise.reject(error)
    }

    const retryConfig = error.config as RetryableConfig
    const currentRetry = retryConfig.__retryCount ?? 0

    if (currentRetry >= MAX_RETRIES) {
      return Promise.reject(error)
    }

    retryConfig.__retryCount = currentRetry + 1
    const delay = Math.min(BASE_RETRY_DELAY_MS * 2 ** currentRetry, MAX_RETRY_DELAY_MS)
    await sleep(delay)

    return apiClient(retryConfig)
  }
)
