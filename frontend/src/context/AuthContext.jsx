import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { authApi } from '../api/auth'
import {
  clearAuthStorage,
  getStoredUser,
  getToken,
  setStoredUser,
  setToken,
  setUnauthorizedHandler,
} from '../api/client'

const AuthContext = createContext(null)

function toStoredUser(source) {
  return {
    userId: source.userId,
    fullName: source.fullName,
    email: source.email,
    role: source.role,
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getStoredUser())
  const [token, setTokenState] = useState(() => getToken())
  const [initializing, setInitializing] = useState(() => Boolean(getToken()))

  const applySession = useCallback((auth) => {
    setToken(auth.accessToken)
    const stored = toStoredUser(auth)
    setStoredUser(stored)
    setUser(stored)
    setTokenState(auth.accessToken)
  }, [])

  const logout = useCallback(() => {
    clearAuthStorage()
    setUser(null)
    setTokenState(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser(null)
      setTokenState(null)
    })
  }, [])

  useEffect(() => {
    let active = true
    if (!getToken()) {
      setInitializing(false)
      return undefined
    }
    authApi
      .me()
      .then((me) => {
        if (!active) return
        const stored = toStoredUser(me)
        setStoredUser(stored)
        setUser(stored)
      })
      .catch(() => {
        if (!active) return
        clearAuthStorage()
        setUser(null)
        setTokenState(null)
      })
      .finally(() => {
        if (active) setInitializing(false)
      })
    return () => {
      active = false
    }
    // Run once on mount to validate any persisted token.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const login = useCallback(
    async (credentials) => {
      const auth = await authApi.login(credentials)
      applySession(auth)
      return auth
    },
    [applySession],
  )

  const register = useCallback(
    async (payload) => {
      const auth = await authApi.register(payload)
      applySession(auth)
      return auth
    },
    [applySession],
  )

  const value = useMemo(
    () => ({
      user,
      token,
      initializing,
      isAuthenticated: Boolean(token && user),
      login,
      register,
      logout,
    }),
    [user, token, initializing, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
