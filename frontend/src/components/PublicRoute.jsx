import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { LoadingBlock } from './ui/LoadingBlock'

export default function PublicRoute() {
  const { isAuthenticated, initializing } = useAuth()

  if (initializing) {
    return <LoadingBlock label="Loading…" />
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}
