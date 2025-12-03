/**
 * 숫자 포매팅 유틸 모음
 */
export const formatNumberLocalized = (value?: number | string, fractionDigits?: number): string => {
  if (value === undefined || value === null || value === '') return '—'

  const numeric =
    typeof value === 'number'
      ? value
      : Number(
          value
            .toString()
            .replace(/,/g, '')
            .trim()
        )

  if (Number.isNaN(numeric)) {
    return typeof value === 'string' && value.length ? value : '—'
  }

  const options: Intl.NumberFormatOptions = {}
  if (typeof fractionDigits === 'number') {
    options.minimumFractionDigits = fractionDigits
    options.maximumFractionDigits = fractionDigits
  }

  return numeric.toLocaleString(undefined, options)
}

export const formatItemLevel = (value?: number | string): string => formatNumberLocalized(value, 2)

export const formatInteger = (value?: number | string): string => formatNumberLocalized(value, 0)

export const formatCombatPower = (value?: number | string): string => formatNumberLocalized(value)
