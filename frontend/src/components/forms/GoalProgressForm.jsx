import { useState } from 'react'
import FormField from '../ui/FormField'
import Alert from '../ui/Alert'
import { formatCurrency } from '../../utils/format'

export default function GoalProgressForm({ goal, onSubmit, onCancel, submitting, serverError }) {
  const [amount, setAmount] = useState('')
  const [error, setError] = useState('')

  function handleSubmit(event) {
    event.preventDefault()
    const value = Number(amount)
    if (amount === '' || Number.isNaN(value)) {
      setError('Enter an amount to add')
      return
    }
    if (value <= 0) {
      setError('Amount must be greater than 0')
      return
    }
    setError('')
    onSubmit(value)
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      {serverError ? (
        <div className="mb-16">
          <Alert variant="error">{serverError}</Alert>
        </div>
      ) : null}
      {goal ? (
        <p className="text-small text-muted mb-16">
          Add to <strong>{goal.goalName}</strong>. Currently {formatCurrency(goal.currentAmount)} of{' '}
          {formatCurrency(goal.targetAmount)} saved.
        </p>
      ) : null}
      <FormField label="Amount to add (₹)" htmlFor="goal-progress" error={error} required>
        <input
          id="goal-progress"
          type="number"
          min="0.01"
          step="0.01"
          value={amount}
          onChange={(e) => {
            setAmount(e.target.value)
            setError('')
          }}
          placeholder="0.00"
          autoFocus
        />
      </FormField>
      <div className="modal-footer" style={{ padding: '16px 0 0', border: 'none' }}>
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Updating…' : 'Add to goal'}
        </button>
      </div>
    </form>
  )
}
