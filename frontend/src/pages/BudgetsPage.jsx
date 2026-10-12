import { useMemo, useState } from 'react'
import { useApi } from '../hooks/useApi'
import { budgetsApi } from '../api/budgets'
import { categoriesApi } from '../api/categories'
import { extractErrorMessage, extractFieldErrors } from '../api/client'
import PageHeader from '../components/ui/PageHeader'
import LoadingBlock from '../components/ui/LoadingBlock'
import EmptyState from '../components/ui/EmptyState'
import Alert from '../components/ui/Alert'
import Badge from '../components/ui/Badge'
import Modal from '../components/ui/Modal'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import Icon from '../components/Icon'
import BudgetForm, { budgetToForm } from '../components/forms/BudgetForm'
import { MONTHS } from '../utils/constants'
import { formatCurrency, formatPercent } from '../utils/format'

const STATUS = {
  EXCEEDED: { variant: 'danger', label: 'Over budget', bar: 'exceeded' },
  WARNING: { variant: 'warning', label: 'Near limit', bar: 'warning' },
  NORMAL: { variant: 'success', label: 'On track', bar: 'success' },
}

export default function BudgetsPage() {
  const [modal, setModal] = useState({ open: false, mode: 'create', record: null })
  const [formError, setFormError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [notice, setNotice] = useState('')

  const list = useApi(() => budgetsApi.list(), [])
  const categoriesQuery = useApi(() => categoriesApi.list({ size: 1000, sort: 'categoryName,asc' }), [])

  const categories = useMemo(
    () => categoriesQuery.data?.content || [],
    [categoriesQuery.data],
  )
  const categoryMap = useMemo(() => {
    const map = new Map()
    categories.forEach((category) => map.set(category.categoryId, category.categoryName))
    return map
  }, [categories])

  const budgets = list.data || []

  function flash(message) {
    setNotice(message)
    window.setTimeout(() => setNotice(''), 3500)
  }

  function openCreate() {
    setFormError('')
    setFieldErrors({})
    setModal({ open: true, mode: 'create', record: null })
  }

  function openEdit(record) {
    setFormError('')
    setFieldErrors({})
    setModal({ open: true, mode: 'edit', record })
  }

  function closeModal() {
    setModal({ open: false, mode: 'create', record: null })
  }

  async function handleSave(payload) {
    setSubmitting(true)
    setFormError('')
    setFieldErrors({})
    try {
      if (modal.mode === 'edit' && modal.record) {
        await budgetsApi.update(modal.record.budgetId, payload)
        flash('Budget updated successfully.')
      } else {
        await budgetsApi.create(payload)
        flash('Budget created successfully.')
      }
      closeModal()
      await list.reload()
    } catch (error) {
      setFieldErrors(extractFieldErrors(error))
      setFormError(extractErrorMessage(error))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await budgetsApi.remove(deleteTarget.budgetId)
      setDeleteTarget(null)
      flash('Budget deleted.')
      await list.reload()
    } catch (error) {
      setDeleteTarget(null)
      flash(`Could not delete: ${extractErrorMessage(error)}`)
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div>
      <PageHeader
        title="Budgets"
        subtitle="Set monthly limits per category and track your spending against them."
        actions={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            <Icon name="plus" size={16} />
            New budget
          </button>
        }
      />

      {notice ? (
        <div className="mb-16">
          <Alert variant="success" onDismiss={() => setNotice('')}>
            {notice}
          </Alert>
        </div>
      ) : null}

      {list.error ? <Alert variant="error">{extractErrorMessage(list.error)}</Alert> : null}

      {list.loading ? (
        <div className="card">
          <LoadingBlock />
        </div>
      ) : !list.error && budgets.length === 0 ? (
        <div className="card">
          <EmptyState
            icon="budgets"
            title="No budgets yet"
            description="Create a budget for a category and month to keep your spending in check."
            action={
              <button type="button" className="btn btn-primary" onClick={openCreate}>
                <Icon name="plus" size={16} />
                New budget
              </button>
            }
          />
        </div>
      ) : (
        <div className="grid grid-3">
          {budgets.map((budget) => {
            const status = STATUS[budget.warningStatus] || STATUS.NORMAL
            const percentage = Math.min(100, Number(budget.percentageUsed) || 0)
            const categoryName = categoryMap.get(budget.categoryId) || 'Category'
            return (
              <article className="card" key={budget.budgetId}>
                <div className="card-body">
                  <div className="flex-between">
                    <div>
                      <h3 style={{ fontSize: 16 }}>{categoryName}</h3>
                      <span className="text-small text-muted">
                        {MONTHS[(budget.month || 1) - 1]} {budget.year}
                      </span>
                    </div>
                    <Badge variant={status.variant}>{status.label}</Badge>
                  </div>

                  <div className="mt-16">
                    <div className="flex-between text-small">
                      <span className="text-muted">Spent</span>
                      <span>
                        <strong>{formatCurrency(budget.spentAmount)}</strong> of{' '}
                        {formatCurrency(budget.limitAmount)}
                      </span>
                    </div>
                    <div className="progress mt-8">
                      <div className={`progress-bar ${status.bar}`} style={{ width: `${percentage}%` }} />
                    </div>
                    <div className="flex-between text-small mt-8">
                      <span className="text-muted">{formatPercent(budget.percentageUsed)} used</span>
                      <span className="text-muted">
                        Remaining {formatCurrency(budget.remainingAmount)}
                      </span>
                    </div>
                  </div>
                </div>
                <div className="modal-footer" style={{ borderTop: '1px solid var(--border)' }}>
                  <button type="button" className="btn btn-sm btn-secondary" onClick={() => openEdit(budget)}>
                    <Icon name="edit" size={14} />
                    Edit
                  </button>
                  <button
                    type="button"
                    className="btn btn-sm btn-ghost"
                    onClick={() => setDeleteTarget(budget)}
                  >
                    <Icon name="trash" size={14} />
                    Delete
                  </button>
                </div>
              </article>
            )
          })}
        </div>
      )}

      <Modal
        open={modal.open}
        title={modal.mode === 'edit' ? 'Edit budget' : 'New budget'}
        onClose={closeModal}
      >
        <BudgetForm
          initial={budgetToForm(modal.record)}
          categories={categories}
          onSubmit={handleSave}
          onCancel={closeModal}
          submitting={submitting}
          serverError={formError}
          fieldErrors={fieldErrors}
        />
      </Modal>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Delete budget"
        message="Are you sure you want to delete this budget? This action cannot be undone."
        confirmLabel="Delete"
        busy={deleting}
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}
