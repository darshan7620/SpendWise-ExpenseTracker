import { useMemo, useState } from 'react'
import FormField from '../ui/FormField'
import Alert from '../ui/Alert'
import { monthOptions, yearOptions } from '../../utils/constants'

const YEARS = yearOptions()

function blankForm() {
  const now = new Date()
  return {
    categoryId: '',
    limitAmount: '',
    month: String(now.getMonth() + 1),
    year: String(now.getFullYear()),
  }
}

export function budgetToForm(record) {
  if (!record) return blankForm()
  return {
    categoryId: record.categoryId ? String(record.categoryId) : '',
    limitAmount: record.limitAmount != null ? String(record.limitAmount) : '',
    month: record.month != null ? String(record.month) : '',
    year: record.year != null ? String(record.year) : '',
  }
}

export default function BudgetForm({
  initial,
  categories,
  onSubmit,
  onCancel,
  submitting,
  serverError,
  fieldErrors = {},
}) {
  const [form, setForm] = useState(() => initial || blankForm())
  const [errors, setErrors] = useState({})

  const expenseCategories = useMemo(
    () => (categories || []).filter((category) => category.type === 'EXPENSE'),
    [categories],
  )

  function update(field) {
    return (event) => {
      const value = event.target.value
      setForm((prev) => ({ ...prev, [field]: value }))
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

  function validate() {
    const next = {}
    if (!form.categoryId) next.categoryId = 'Category is required'
    if (form.limitAmount === '') next.limitAmount = 'Limit is required'
    else if (Number.isNaN(Number(form.limitAmount))) next.limitAmount = 'Enter a valid number'
    else if (Number(form.limitAmount) <= 0) next.limitAmount = 'Limit must be greater than 0'
    if (!form.month) next.month = 'Month is required'
    if (!form.year) next.year = 'Year is required'
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
      categoryId: Number(form.categoryId),
      limitAmount: Number(form.limitAmount),
      month: Number(form.month),
      year: Number(form.year),
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
        label="Category"
        htmlFor="bud-category"
        error={errorOf('categoryId')}
        hint={expenseCategories.length === 0 ? 'Create an expense category first' : undefined}
        required
      >
        <select id="bud-category" value={form.categoryId} onChange={update('categoryId')}>
          <option value="">Select a category</option>
          {expenseCategories.map((category) => (
            <option key={category.categoryId} value={category.categoryId}>
              {category.categoryName}
            </option>
          ))}
        </select>
      </FormField>
      <FormField label="Limit amount (₹)" htmlFor="bud-limit" error={errorOf('limitAmount')} required>
        <input
          id="bud-limit"
          type="number"
          min="0.01"
          step="0.01"
          value={form.limitAmount}
          onChange={update('limitAmount')}
          placeholder="0.00"
        />
      </FormField>
      <FormField label="Month" htmlFor="bud-month" error={errorOf('month')} required>
        <select id="bud-month" value={form.month} onChange={update('month')}>
          <option value="">Select month</option>
          {monthOptions().map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </FormField>
      <FormField label="Year" htmlFor="bud-year" error={errorOf('year')} required>
        <select id="bud-year" value={form.year} onChange={update('year')}>
          <option value="">Select year</option>
          {YEARS.map((year) => (
            <option key={year} value={year}>
              {year}
            </option>
          ))}
        </select>
      </FormField>
      <div className="full modal-footer" style={{ padding: 0, border: 'none' }}>
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Saving…' : 'Save budget'}
        </button>
      </div>
    </form>
  )
}
