import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function NotFoundPage() {
  const { isAuthenticated } = useAuth()
  return (
    <div className="empty-state" style={{ minHeight: '70vh', justifyContent: 'center' }}>
      <span className="badge badge-muted">404</span>
      <h1 style={{ fontSize: 28 }}>Page not found</h1>
      <p>The page you are looking for doesn&apos;t exist or has moved.</p>
      <div className="mt-8">
        <Link className="btn btn-primary" to={isAuthenticated ? '/dashboard' : '/login'}>
          {isAuthenticated ? 'Back to dashboard' : 'Go to login'}
        </Link>
      </div>
    </div>
  )
}
