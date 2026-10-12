const inrFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

export function formatCurrency(value) {
  const number = Number(value)
  if (value === null || value === undefined || Number.isNaN(number)) {
    return inrFormatter.format(0)
  }
  return inrFormatter.format(number)
}

export function formatCompactCurrency(value) {
  const number = Number(value)
  if (!number) return '₹0'
  const abs = Math.abs(number)
  const sign = number < 0 ? '-' : ''
  if (abs >= 1e7) return `${sign}₹${(abs / 1e7).toFixed(1)}Cr`
  if (abs >= 1e5) return `${sign}₹${(abs / 1e5).toFixed(1)}L`
  if (abs >= 1e3) return `${sign}₹${(abs / 1e3).toFixed(0)}k`
  return `${sign}₹${abs.toFixed(0)}`
}

export function formatNumber(value) {
  const number = Number(value)
  if (Number.isNaN(number)) return '0'
  return new Intl.NumberFormat('en-IN').format(number)
}

export function formatPercent(value) {
  const number = Number(value)
  if (value === null || value === undefined || Number.isNaN(number)) {
    return '0%'
  }
  return `${number.toFixed(number % 1 === 0 ? 0 : 1)}%`
}

export function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

export function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function todayISO() {
  const now = new Date()
  const offset = now.getTimezoneOffset()
  const local = new Date(now.getTime() - offset * 60 * 1000)
  return local.toISOString().slice(0, 10)
}

export function titleCase(value) {
  if (!value) return ''
  return String(value)
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}
