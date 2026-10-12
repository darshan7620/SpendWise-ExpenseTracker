import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { extractErrorMessage, extractFieldErrors } from '../api/client'
import AuthShell from '../components/AuthShell'
import FormField from '../components/ui/FormField'
import Alert from '../components/ui/Alert'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  function update(field) {
    return (event) => {
      setForm((prev) => ({ ...prev, [field]: event.target.value }))
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

  function validate() {
    const next = {}
    if (!form.email.trim()) {
      next.email = 'Email is required'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
      next.email = 'Enter a valid email address'
    }
    if (!form.password) {
      next.password = 'Password is required'
    }
    return next
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setServerError('')
    const validation = validate()
    if (Object.keys(validation).length > 0) {
      setErrors(validation)
      return
    }
    setSubmitting(true)
    try {
      await login({ email: form.email.trim(), password: form.password })
      const redirectTo = location.state?.from || '/dashboard'
      navigate(redirectTo, { replace: true })
    } catch (error) {
      setErrors(extractFieldErrors(error))
      setServerError(extractErrorMessage(error))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Log in to continue to your dashboard."
      footer={
        <>
          Don&apos;t have an account? <Link to="/register">Create one</Link>
        </>
      }
    >
      <form className="form" onSubmit={handleSubmit} noValidate>
        {serverError ? <Alert variant="error">{serverError}</Alert> : null}
        <FormField label="Email" htmlFor="login-email" error={errors.email} required>
          <input
            id="login-email"
            type="email"
            autoComplete="email"
            value={form.email}
            onChange={update('email')}
            placeholder="you@example.com"
          />
        </FormField>
        <FormField label="Password" htmlFor="login-password" error={errors.password} required>
          <input
            id="login-password"
            type="password"
            autoComplete="current-password"
            value={form.password}
            onChange={update('password')}
            placeholder="Your password"
          />
        </FormField>
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
    </AuthShell>
  )
}
