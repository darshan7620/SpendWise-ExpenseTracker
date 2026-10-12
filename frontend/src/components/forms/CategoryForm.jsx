import { useState } from 'react'
import FormField from '../ui/FormField'
import Alert from '../ui/Alert'
import { TRANSACTION_TYPES } from '../../utils/constants'

function blankForm() {
  return { categoryName: '', type: 'EXPENSE', description: '' }
}

export function categoryToForm(record) {
  if (!record) return blankForm()
  return {
    categoryName: record.categoryName || '',
    type: record.type || 'EXPENSE',
    description: record.description || '',
  }
}

export default function CategoryForm({
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
    if (!form.categoryName.trim()) next.categoryName = 'Name is required'
    else if (form.categoryName.trim().length > 100)
      next.categoryName = 'Name must be at most 100 characters'
    if (!form.type) next.type = 'Type is required'
    if (form.description && form.description.length > 255)
      next.description = 'Description must be at most 255 characters'
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
      categoryName: form.categoryName.trim(),
      type: form.type,
      description: form.description.trim() ? form.description.trim() : null,
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
        label="Category name"
        htmlFor="cat-name"
        error={errorOf('categoryName')}
        required
      >
        <input
          id="cat-name"
          type="text"
          value={form.categoryName}
          onChange={update('categoryName')}
          placeholder="e.g. Groceries"
        />
      </FormField>
      <FormField label="Type" htmlFor="cat-type" error={errorOf('type')} required>
        <select id="cat-type" value={form.type} onChange={update('type')}>
          {TRANSACTION_TYPES.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </FormField>
      <FormField className="full" label="Description" htmlFor="cat-desc" error={errorOf('description')}>
        <textarea
          id="cat-desc"
          value={form.description}
          onChange={update('description')}
          maxLength={255}
          placeholder="Optional description"
        />
      </FormField>
      <div className="full modal-footer" style={{ padding: 0, border: 'none' }}>
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Saving…' : 'Save category'}
        </button>
      </div>
    </form>
  )
}
