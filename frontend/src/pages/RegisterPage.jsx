import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { extractErrorMessage, extractFieldErrors } from '../api/client'
import AuthShell from '../components/AuthShell'
import FormField from '../components/ui/FormField'
import Alert from '../components/ui/Alert'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
  })
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
    if (!form.fullName.trim()) {
      next.fullName = 'Full name is required'
    } else if (form.fullName.trim().length > 100) {
      next.fullName = 'Full name must be at most 100 characters'
    }
    if (!form.email.trim()) {
      next.email = 'Email is required'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
      next.email = 'Enter a valid email address'
    }
    if (!form.password) {
      next.password = 'Password is required'
    } else if (form.password.length < 6) {
      next.password = 'Password must be at least 6 characters'
    } else if (form.password.length > 100) {
      next.password = 'Password must be at most 100 characters'
    }
    if (form.confirmPassword !== form.password) {
      next.confirmPassword = 'Passwords do not match'
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
      await register({
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
      })
      navigate('/dashboard', { replace: true })
    } catch (error) {
      setErrors(extractFieldErrors(error))
      setServerError(extractErrorMessage(error))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthShell
      title="Create your account"
      subtitle="Start tracking your income, expenses, and goals in minutes."
      footer={
        <>
          Already have an account? <Link to="/login">Log in</Link>
        </>
      }
    >
      <form className="form" onSubmit={handleSubmit} noValidate>
        {serverError ? <Alert variant="error">{serverError}</Alert> : null}
        <FormField label="Full name" htmlFor="reg-name" error={errors.fullName} required>
          <input
            id="reg-name"
            type="text"
            autoComplete="name"
            value={form.fullName}
            onChange={update('fullName')}
            placeholder="Jane Doe"
          />
        </FormField>
        <FormField label="Email" htmlFor="reg-email" error={errors.email} required>
          <input
            id="reg-email"
            type="email"
            autoComplete="email"
            value={form.email}
            onChange={update('email')}
            placeholder="you@example.com"
          />
        </FormField>
        <FormField
          label="Password"
          htmlFor="reg-password"
          error={errors.password}
          hint="At least 6 characters"
          required
        >
          <input
            id="reg-password"
            type="password"
            autoComplete="new-password"
            value={form.password}
            onChange={update('password')}
            placeholder="Create a password"
          />
        </FormField>
        <FormField
          label="Confirm password"
          htmlFor="reg-confirm"
          error={errors.confirmPassword}
          required
        >
          <input
            id="reg-confirm"
            type="password"
            autoComplete="new-password"
            value={form.confirmPassword}
            onChange={update('confirmPassword')}
            placeholder="Re-enter your password"
          />
        </FormField>
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? 'Creating account…' : 'Create account'}
        </button>
      </form>
    </AuthShell>
  )
}
