import { isNumber, isRecord, isString } from './typeGuards'

const getResponseRecord = (error: unknown): Record<string, unknown> | null => {
  if (!isRecord(error)) return null
  const response = error.response
  return isRecord(response) ? response : null
}

export const getHttpStatus = (error: unknown): number | null => {
  const response = getResponseRecord(error)
  if (!response) return null
  const status = response.status
  return isNumber(status) ? status : null
}

export const getHttpErrorMessage = (error: unknown): string | null => {
  const response = getResponseRecord(error)
  if (response) {
    const data = response.data
    if (isRecord(data) && isString(data.message)) {
      return data.message
    }
  }

  if (isRecord(error) && isString(error.message)) {
    return error.message
  }

  return null
}

