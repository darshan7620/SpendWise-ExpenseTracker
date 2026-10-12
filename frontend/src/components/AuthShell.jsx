import { Link } from 'react-router-dom'
import Icon from './Icon'

const BENEFITS = [
  'Track income and expenses effortlessly',
  'Set monthly budgets and never overspend',
  'Save toward goals with visual progress',
  'Understand spending with clear reports',
]

export default function AuthShell({ title, subtitle, children, footer }) {
  return (
    <div className="auth-page">
      <aside className="auth-aside">
        <Link to="/" className="brand" style={{ color: '#fff' }}>
          <span className="brand-mark">
            <Icon name="wallet" size={18} />
          </span>
          SpendWise
        </Link>
        <div>
          <h2>Money clarity starts here.</h2>
          <p>
            Join SpendWise to plan, track, and grow your finances with confidence.
          </p>
          <ul>
            {BENEFITS.map((benefit) => (
              <li key={benefit}>
                <Icon name="check" size={16} />
                {benefit}
              </li>
            ))}
          </ul>
        </div>
        <p style={{ fontSize: 13, color: 'rgba(255,255,255,0.7)' }}>
          Secure JWT authentication · Your data stays yours
        </p>
      </aside>

      <main className="auth-main">
        <div className="auth-card">
          <h1>{title}</h1>
          {subtitle ? <p className="subtitle">{subtitle}</p> : null}
          {children}
          {footer ? <div className="auth-footer">{footer}</div> : null}
        </div>
      </main>
    </div>
  )
}
