import { useMemo, useState } from 'react'
import FormField from '../ui/FormField'
import Alert from '../ui/Alert'
import { TRANSACTION_TYPES, PAYMENT_METHODS } from '../../utils/constants'
import { todayISO } from '../../utils/format'

function blankForm() {
  return {
    title: '',
    amount: '',
    type: 'EXPENSE',
    transactionDate: todayISO(),
    notes: '',
    paymentMethod: 'CASH',
    categoryId: '',
  }
}

export function recordToForm(record) {
  if (!record) return blankForm()
  return {
    title: record.title || '',
    amount: record.amount != null ? String(record.amount) : '',
    type: record.type || 'EXPENSE',
    transactionDate: record.transactionDate || todayISO(),
    notes: record.notes || '',
    paymentMethod: record.paymentMethod || 'CASH',
    categoryId: record.categoryId ? String(record.categoryId) : '',
  }
}

export default function TransactionForm({
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

  const availableCategories = useMemo(
    () => (categories || []).filter((category) => category.type === form.type),
    [categories, form.type],
  )

  function update(field) {
    return (event) => {
      const value = event.target.value
      setForm((prev) => {
        const next = { ...prev, [field]: value }
        if (field === 'type') {
          next.categoryId = ''
        }
        return next
      })
      setErrors((prev) => ({ ...prev, [field]: undefined }))
    }
  }

  function validate() {
    const next = {}
    if (!form.title.trim()) next.title = 'Title is required'
    else if (form.title.trim().length > 150) next.title = 'Title must be at most 150 characters'
    if (form.amount === '' || form.amount === null) next.amount = 'Amount is required'
    else if (Number.isNaN(Number(form.amount))) next.amount = 'Enter a valid number'
    else if (Number(form.amount) <= 0) next.amount = 'Amount must be greater than 0'
    if (!form.transactionDate) next.transactionDate = 'Date is required'
    if (!form.type) next.type = 'Type is required'
    if (!form.paymentMethod) next.paymentMethod = 'Payment method is required'
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
      title: form.title.trim(),
      amount: Number(form.amount),
      type: form.type,
      transactionDate: form.transactionDate,
      notes: form.notes.trim() ? form.notes.trim() : null,
      paymentMethod: form.paymentMethod,
      categoryId: form.categoryId ? Number(form.categoryId) : null,
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
      <FormField className="full" label="Title" htmlFor="tx-title" error={errorOf('title')} required>
        <input
          id="tx-title"
          type="text"
          value={form.title}
          onChange={update('title')}
          placeholder="e.g. Groceries at the supermarket"
        />
      </FormField>
      <FormField label="Amount (₹)" htmlFor="tx-amount" error={errorOf('amount')} required>
        <input
          id="tx-amount"
          type="number"
          min="0.01"
          step="0.01"
          value={form.amount}
          onChange={update('amount')}
          placeholder="0.00"
        />
      </FormField>
      <FormField label="Type" htmlFor="tx-type" error={errorOf('type')} required>
        <select id="tx-type" value={form.type} onChange={update('type')}>
          {TRANSACTION_TYPES.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </FormField>
      <FormField label="Date" htmlFor="tx-date" error={errorOf('transactionDate')} required>
        <input
          id="tx-date"
          type="date"
          value={form.transactionDate}
          onChange={update('transactionDate')}
        />
      </FormField>
      <FormField label="Payment method" htmlFor="tx-method" error={errorOf('paymentMethod')} required>
        <select id="tx-method" value={form.paymentMethod} onChange={update('paymentMethod')}>
          {PAYMENT_METHODS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </FormField>
      <FormField
        label="Category"
        htmlFor="tx-category"
        error={errorOf('categoryId')}
        hint={availableCategories.length === 0 ? `No ${form.type.toLowerCase()} categories available` : 'Optional'}
      >
        <select id="tx-category" value={form.categoryId} onChange={update('categoryId')}>
          <option value="">Uncategorized</option>
          {availableCategories.map((category) => (
            <option key={category.categoryId} value={category.categoryId}>
              {category.categoryName}
            </option>
          ))}
        </select>
      </FormField>
      <FormField className="full" label="Notes" htmlFor="tx-notes" error={errorOf('notes')}>
        <textarea
          id="tx-notes"
          value={form.notes}
          onChange={update('notes')}
          maxLength={255}
          placeholder="Optional notes"
        />
      </FormField>
      <div className="full modal-footer" style={{ padding: 0, border: 'none' }}>
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Saving…' : 'Save transaction'}
        </button>
      </div>
    </form>
  )
}
