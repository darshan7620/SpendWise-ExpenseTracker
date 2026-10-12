import { useState } from 'react'
import FormField from '../ui/FormField'
import Alert from '../ui/Alert'
import { todayISO } from '../../utils/format'

function blankForm() {
  return { goalName: '', targetAmount: '', currentAmount: '', targetDate: todayISO() }
}

export function goalToForm(record) {
  if (!record) return blankForm()
  return {
    goalName: record.goalName || '',
    targetAmount: record.targetAmount != null ? String(record.targetAmount) : '',
    currentAmount: record.currentAmount != null ? String(record.currentAmount) : '',
    targetDate: record.targetDate || todayISO(),
  }
}

export default function GoalForm({
  initial,
  onSubmit,
  onCancel,
  submitting,
  serverError,
  fieldErrors = {},
}) {
  const [form, setForm] = useState(() => initial || blankForm())
  const [errors, setErrors] = useState({})

  function update(field) {
    return (event) => {
      const value = event.target.value
      setForm((prev) => ({ ...prev, [field]: value }))
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

  function validate() {
    const next = {}
    if (!form.goalName.trim()) next.goalName = 'Goal name is required'
    else if (form.goalName.trim().length > 150) next.goalName = 'Name must be at most 150 characters'
    if (form.targetAmount === '') next.targetAmount = 'Target amount is required'
    else if (Number.isNaN(Number(form.targetAmount))) next.targetAmount = 'Enter a valid number'
    else if (Number(form.targetAmount) <= 0) next.targetAmount = 'Target must be greater than 0'
    if (form.currentAmount !== '' && Number(form.currentAmount) < 0)
      next.currentAmount = 'Current amount cannot be negative'
    if (!form.targetDate) next.targetDate = 'Target date is required'
    return next
  }

  function handleSubmit(event) {
    event.preventDefault()
    const validation = validate()
    if (Object.keys(validation).length > 0) {
      setErrors(validation)
      return
    }
    onSubmit({
      goalName: form.goalName.trim(),
      targetAmount: Number(form.targetAmount),
      currentAmount: form.currentAmount === '' ? 0 : Number(form.currentAmount),
      targetDate: form.targetDate,
    })
  }

  const errorOf = (field) => errors[field] || fieldErrors[field]

  return (
    <form className="form-grid" onSubmit={handleSubmit} noValidate>
      {serverError ? (
        <div className="full">
          <Alert variant="error">{serverError}</Alert>
        </div>
      ) : null}
      <FormField
        className="full"
        label="Goal name"
        htmlFor="goal-name"
        error={errorOf('goalName')}
        required
      >
        <input
          id="goal-name"
          type="text"
          value={form.goalName}
          onChange={update('goalName')}
          placeholder="e.g. Emergency fund"
        />
      </FormField>
      <FormField label="Target amount (₹)" htmlFor="goal-target" error={errorOf('targetAmount')} required>
        <input
          id="goal-target"
          type="number"
          min="0.01"
          step="0.01"
          value={form.targetAmount}
          onChange={update('targetAmount')}
          placeholder="0.00"
        />
      </FormField>
      <FormField
        label="Current amount (₹)"
        htmlFor="goal-current"
        error={errorOf('currentAmount')}
        hint="Optional starting amount"
      >
        <input
          id="goal-current"
          type="number"
          min="0"
          step="0.01"
          value={form.currentAmount}
          onChange={update('currentAmount')}
          placeholder="0.00"
        />
      </FormField>
      <FormField label="Target date" htmlFor="goal-date" error={errorOf('targetDate')} required>
        <input id="goal-date" type="date" value={form.targetDate} onChange={update('targetDate')} />
      </FormField>
      <div className="full modal-footer" style={{ padding: 0, border: 'none' }}>
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Saving…' : 'Save goal'}
        </button>
      </div>
    </form>
  )
}
