import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Icon from './Icon'

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard', icon: 'dashboard' },
  { to: '/transactions', label: 'Transactions', icon: 'transactions' },
  { to: '/categories', label: 'Categories', icon: 'categories' },
  { to: '/budgets', label: 'Budgets', icon: 'budgets' },
  { to: '/goals', label: 'Savings Goals', icon: 'goals' },
  { to: '/reports', label: 'Reports', icon: 'reports' },
]

function initials(name) {
  if (!name) return 'U'
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join('')
}

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-shell">
      <div
        className={`sidebar-backdrop ${menuOpen ? 'show' : ''}`}
        onClick={() => setMenuOpen(false)}
        aria-hidden="true"
      />
      <aside className={`sidebar ${menuOpen ? 'open' : ''}`} aria-label="Primary navigation">
        <div className="sidebar-brand">
          <span className="brand-mark">S</span>
          SpendWise
        </div>
        <nav className="sidebar-nav">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              onClick={() => setMenuOpen(false)}
            >
              <Icon name={item.icon} className="nav-icon" />
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="user-chip">
            <span className="user-avatar">{initials(user?.fullName)}</span>
            <span className="user-meta">
              <span className="user-name">{user?.fullName || 'User'}</span>
              <span className="user-email">{user?.email}</span>
            </span>
          </div>
          <button type="button" className="btn btn-secondary btn-block btn-sm" onClick={handleLogout}>
            <Icon name="logout" size={16} />
            Sign out
          </button>
        </div>
      </aside>

      <div className="app-main">
        <header className="topbar">
          <button
            type="button"
            className="btn-icon"
            onClick={() => setMenuOpen((open) => !open)}
            aria-label="Toggle navigation menu"
            aria-expanded={menuOpen}
          >
            <Icon name={menuOpen ? 'close' : 'menu'} size={22} />
          </button>
          <span className="topbar-brand">
            <span className="brand-mark" style={{ width: 28, height: 28, borderRadius: 8, background: 'linear-gradient(135deg, var(--brand), var(--brand-dark))', color: '#fff', display: 'grid', placeItems: 'center', fontWeight: 800, fontSize: 15 }}>
              S
            </span>
            SpendWise
          </span>
          <button type="button" className="btn-icon" onClick={handleLogout} aria-label="Sign out">
            <Icon name="logout" size={20} />
          </button>
        </header>

        <main className="app-content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
