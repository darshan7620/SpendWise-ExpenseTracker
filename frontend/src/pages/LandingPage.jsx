import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Icon from '../components/Icon'

const FEATURES = [
  {
    icon: 'transactions',
    title: 'Track every rupee',
    text: 'Record income and expenses across categories, payment methods, and dates in one place.',
  },
  {
    icon: 'budgets',
    title: 'Budgets that keep you honest',
    text: 'Set monthly limits per category and see exactly what is spent, remaining, and over budget.',
  },
  {
    icon: 'goals',
    title: 'Reach your savings goals',
    text: 'Define targets, log contributions, and watch your progress grow toward the finish line.',
  },
  {
    icon: 'reports',
    title: 'Insightful reports',
    text: 'Monthly and yearly reports with category breakdowns to understand where your money goes.',
  },
  {
    icon: 'dashboard',
    title: 'A dashboard at a glance',
    text: 'Income, expenses, balance, and trends visualised with clear, responsive charts.',
  },
  {
    icon: 'scale',
    title: 'Secure and private',
    text: 'JWT authentication keeps your financial data scoped to your account only.',
  },
]

export default function LandingPage() {
  const { isAuthenticated } = useAuth()

  return (
    <div className="landing">
      <header className="landing-nav">
        <span className="sidebar-brand" style={{ padding: 0 }}>
          <span className="brand-mark">S</span>
          SpendWise
        </span>
        <div className="actions">
          {isAuthenticated ? (
            <Link className="btn btn-primary" to="/dashboard">
              Go to dashboard
            </Link>
          ) : (
            <>
              <Link className="btn btn-secondary" to="/login">
                Log in
              </Link>
              <Link className="btn btn-primary" to="/register">
                Get started
              </Link>
            </>
          )}
        </div>
      </header>

      <section className="landing-hero">
        <div>
          <span className="badge badge-info">
            <Icon name="sparkles" size={14} /> Personal finance, simplified
          </span>
          <h1 style={{ marginTop: 16 }}>
            Take control of your money with SpendWise
          </h1>
          <p className="lead">
            Track expenses and income, set budgets, and grow your savings — all in
            one clean, secure dashboard built around how you actually spend.
          </p>
          <div className="cta">
            {isAuthenticated ? (
              <Link className="btn btn-primary" to="/dashboard">
                Open dashboard
              </Link>
            ) : (
              <>
                <Link className="btn btn-primary" to="/register">
                  Create free account
                </Link>
                <Link className="btn btn-secondary" to="/login">
                  I already have an account
                </Link>
              </>
            )}
          </div>
        </div>

        <div className="hero-visual" aria-hidden="true">
          <div className="flex-between" style={{ marginBottom: 16 }}>
            <span className="text-muted text-small">This month</span>
            <span className="badge badge-success">
              <Icon name="trendingUp" size={13} /> On track
            </span>
          </div>
          <div className="hero-note">
            <span>Income</span>
            <span>₹75,000.00</span>
          </div>
          <div className="bar income" style={{ width: '90%' }} />
          <div className="hero-note" style={{ marginTop: 16 }}>
            <span>Expenses</span>
            <span>₹42,500.00</span>
          </div>
          <div className="bar expense" style={{ width: '58%' }} />
          <div className="card mt-16" style={{ boxShadow: 'none' }}>
            <div className="card-body flex-between">
              <span className="text-small text-muted">Balance</span>
              <strong>₹32,500.00</strong>
            </div>
          </div>
        </div>
      </section>

      <section className="features">
        <span className="badge badge-muted">Everything you need</span>
        <h2 style={{ marginTop: 14 }}>A complete picture of your finances</h2>
        <div className="feature-grid">
          {FEATURES.map((feature) => (
            <article className="feature" key={feature.title}>
              <span className="feature-icon">
                <Icon name={feature.icon} size={22} />
              </span>
              <h3>{feature.title}</h3>
              <p>{feature.text}</p>
            </article>
          ))}
        </div>
      </section>

      <footer className="landing-footer">
        SpendWise — expense and budget tracker. Built with React, Vite, and Spring Boot.
      </footer>
    </div>
  )
}
