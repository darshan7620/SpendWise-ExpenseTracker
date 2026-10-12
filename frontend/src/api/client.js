import axios from 'axios'

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

const TOKEN_KEY = 'spendwise.token'
const USER_KEY = 'spendwise.user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export function getStoredUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function setStoredUser(user) {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

export function clearAuthStorage() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let unauthorizedHandler = null

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const url = error?.config?.url || ''
    const isAuthEndpoint =
      url.includes('/auth/login') || url.includes('/auth/register')

    if (status === 401 && !isAuthEndpoint) {
      clearAuthStorage()
      if (typeof unauthorizedHandler === 'function') {
        unauthorizedHandler()
      }
    }
    return Promise.reject(error)
  },
)

export function extractErrorMessage(error) {
  const data = error?.response?.data
  if (data) {
    if (data.fieldErrors && typeof data.fieldErrors === 'object') {
      const messages = Object.values(data.fieldErrors).filter(Boolean)
      if (messages.length > 0) {
        return messages.join(', ')
      }
    }
    if (data.message) {
      return data.message
    }
  }
  if (error?.code === 'ERR_NETWORK') {
    return 'Unable to reach the server. Please make sure the backend is running.'
  }
  return error?.message || 'Something went wrong. Please try again.'
}

export function extractFieldErrors(error) {
  const data = error?.response?.data
  if (data?.fieldErrors && typeof data.fieldErrors === 'object') {
    return data.fieldErrors
  }
  return {}
}
