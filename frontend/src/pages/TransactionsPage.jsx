import { useMemo, useState } from 'react'
import { useApi } from '../hooks/useApi'
import { transactionsApi } from '../api/transactions'
import { categoriesApi } from '../api/categories'
import { extractErrorMessage, extractFieldErrors } from '../api/client'
import PageHeader from '../components/ui/PageHeader'
import LoadingBlock from '../components/ui/LoadingBlock'
import EmptyState from '../components/ui/EmptyState'
import Alert from '../components/ui/Alert'
import Badge from '../components/ui/Badge'
import Modal from '../components/ui/Modal'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import Pagination from '../components/ui/Pagination'
import Icon from '../components/Icon'
import TransactionForm, { recordToForm } from '../components/forms/TransactionForm'
import { TRANSACTION_TYPES } from '../utils/constants'
import { formatCurrency, formatDate, titleCase } from '../utils/format'

const PAGE_SIZE = 10

const EMPTY_FILTERS = {
  type: '',
  categoryId: '',
  title: '',
  startDate: '',
  endDate: '',
}

export default function TransactionsPage() {
  const [page, setPage] = useState(0)
  const [draft, setDraft] = useState(EMPTY_FILTERS)
  const [applied, setApplied] = useState(EMPTY_FILTERS)
  const [modal, setModal] = useState({ open: false, mode: 'create', record: null })
  const [formError, setFormError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [notice, setNotice] = useState('')

  const params = useMemo(() => {
    const value = { page, size: PAGE_SIZE, sort: 'transactionDate,desc' }
    if (applied.type) value.type = applied.type
    if (applied.categoryId) value.categoryId = applied.categoryId
    if (applied.title.trim()) value.title = applied.title.trim()
    if (applied.startDate) value.startDate = applied.startDate
    if (applied.endDate) value.endDate = applied.endDate
    return value
  }, [page, applied])

  const list = useApi(() => transactionsApi.list(params), [params])
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

  const rows = list.data?.content || []

  function flash(message) {
    setNotice(message)
    window.setTimeout(() => setNotice(''), 3500)
  }

  function applyFilters() {
    setApplied(draft)
    setPage(0)
  }

  function resetFilters() {
    setDraft(EMPTY_FILTERS)
    setApplied(EMPTY_FILTERS)
    setPage(0)
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
        await transactionsApi.update(modal.record.transactionId, payload)
        flash('Transaction updated successfully.')
      } else {
        await transactionsApi.create(payload)
        flash('Transaction created successfully.')
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
      await transactionsApi.remove(deleteTarget.transactionId)
      setDeleteTarget(null)
      flash('Transaction deleted.')
      if (rows.length === 1 && page > 0) {
        setPage((prev) => prev - 1)
      } else {
        await list.reload()
      }
    } catch (error) {
      setDeleteTarget(null)
      flash(`Could not delete: ${extractErrorMessage(error)}`)
    } finally {
      setDeleting(false)
    }
  }

  const totalElements = list.data?.totalElements ?? 0
  const totalPages = list.data?.totalPages ?? 0

  return (
    <div>
      <PageHeader
        title="Transactions"
        subtitle="Record, filter, and manage all your income and expenses."
        actions={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            <Icon name="plus" size={16} />
            Add transaction
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

      <div className="card mb-16">
        <div className="card-body">
          <div className="filter-bar">
            <div className="field">
              <label htmlFor="f-type">Type</label>
              <select
                id="f-type"
                value={draft.type}
                onChange={(e) => setDraft((prev) => ({ ...prev, type: e.target.value }))}
              >
                <option value="">All types</option>
                {TRANSACTION_TYPES.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="f-category">Category</label>
              <select
                id="f-category"
                value={draft.categoryId}
                onChange={(e) => setDraft((prev) => ({ ...prev, categoryId: e.target.value }))}
              >
                <option value="">All categories</option>
                {categories.map((category) => (
                  <option key={category.categoryId} value={category.categoryId}>
                    {category.categoryName}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="f-start">From</label>
              <input
                id="f-start"
                type="date"
                value={draft.startDate}
                onChange={(e) => setDraft((prev) => ({ ...prev, startDate: e.target.value }))}
              />
            </div>
            <div className="field">
              <label htmlFor="f-end">To</label>
              <input
                id="f-end"
                type="date"
                value={draft.endDate}
                onChange={(e) => setDraft((prev) => ({ ...prev, endDate: e.target.value }))}
              />
            </div>
            <div className="field" style={{ flex: 1, minWidth: 200 }}>
              <label htmlFor="f-title">Search title</label>
              <input
                id="f-title"
                type="search"
                value={draft.title}
                onChange={(e) => setDraft((prev) => ({ ...prev, title: e.target.value }))}
                onKeyDown={(e) => e.key === 'Enter' && applyFilters()}
                placeholder="Search by title"
              />
            </div>
            <div className="flex items-center gap-8">
              <button type="button" className="btn btn-primary" onClick={applyFilters}>
                Apply
              </button>
              <button type="button" className="btn btn-secondary" onClick={resetFilters}>
                Reset
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-body flush">
          {list.error ? (
            <div style={{ padding: 18 }}>
              <Alert variant="error">{extractErrorMessage(list.error)}</Alert>
            </div>
          ) : list.loading ? (
            <LoadingBlock />
          ) : rows.length === 0 ? (
            <EmptyState
              icon="transactions"
              title="No transactions found"
              description={
                totalElements === 0
                  ? 'Add your first transaction to start tracking your finances.'
                  : 'No transactions match the current filters. Try adjusting them.'
              }
              action={
                <button type="button" className="btn btn-primary" onClick={openCreate}>
                  <Icon name="plus" size={16} />
                  Add transaction
                </button>
              }
            />
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Title</th>
                    <th>Category</th>
                    <th>Type</th>
                    <th>Payment</th>
                    <th className="num">Amount</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row) => (
                    <tr key={row.transactionId}>
                      <td className="nowrap">{formatDate(row.transactionDate)}</td>
                      <td>
                        <strong>{row.title}</strong>
                        {row.notes ? (
                          <div className="text-small text-muted">{row.notes}</div>
                        ) : null}
                      </td>
                      <td>{row.categoryId ? categoryMap.get(row.categoryId) || '—' : 'Uncategorized'}</td>
                      <td>
                        <Badge variant={row.type === 'INCOME' ? 'success' : 'danger'}>
                          {titleCase(row.type)}
                        </Badge>
                      </td>
                      <td>{titleCase(row.paymentMethod)}</td>
                      <td className={`num ${row.type === 'INCOME' ? 'value-income' : 'value-expense'}`}>
                        {row.type === 'INCOME' ? '+' : '−'}
                        {formatCurrency(row.amount)}
                      </td>
                      <td className="actions">
                        <span className="row-actions">
                          <button
                            type="button"
                            className="btn-icon"
                            onClick={() => openEdit(row)}
                            aria-label={`Edit ${row.title}`}
                          >
                            <Icon name="edit" size={16} />
                          </button>
                          <button
                            type="button"
                            className="btn-icon danger"
                            onClick={() => setDeleteTarget(row)}
                            aria-label={`Delete ${row.title}`}
                          >
                            <Icon name="trash" size={16} />
                          </button>
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {rows.length > 0 ? (
            <Pagination
              page={page}
              totalPages={totalPages}
              totalElements={totalElements}
              size={PAGE_SIZE}
              onPageChange={setPage}
            />
          ) : null}
        </div>
      </div>

      <Modal
        open={modal.open}
        title={modal.mode === 'edit' ? 'Edit transaction' : 'Add transaction'}
        onClose={closeModal}
        size="lg"
      >
        <TransactionForm
          initial={recordToForm(modal.record)}
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
        title="Delete transaction"
        message={
          deleteTarget
            ? `Are you sure you want to delete "${deleteTarget.title}"? This action cannot be undone.`
            : ''
        }
        confirmLabel="Delete"
        busy={deleting}
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}
