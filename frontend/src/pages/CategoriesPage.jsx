import { useState } from 'react'
import { useApi } from '../hooks/useApi'
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
import CategoryForm, { categoryToForm } from '../components/forms/CategoryForm'
import { titleCase } from '../utils/format'

const PAGE_SIZE = 12

export default function CategoriesPage() {
  const [page, setPage] = useState(0)
  const [modal, setModal] = useState({ open: false, mode: 'create', record: null })
  const [formError, setFormError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [notice, setNotice] = useState('')

  const list = useApi(
    () => categoriesApi.list({ page, size: PAGE_SIZE, sort: 'categoryName,asc' }),
    [page],
  )

  const rows = list.data?.content || []

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
        await categoriesApi.update(modal.record.categoryId, payload)
        flash('Category updated successfully.')
      } else {
        await categoriesApi.create(payload)
        flash('Category created successfully.')
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
      await categoriesApi.remove(deleteTarget.categoryId)
      setDeleteTarget(null)
      flash('Category deleted.')
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

  return (
    <div>
      <PageHeader
        title="Categories"
        subtitle="Organise your transactions with shared or custom categories."
        actions={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            <Icon name="plus" size={16} />
            Add category
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
              icon="categories"
              title="No categories yet"
              description="Create categories to group your income and expenses."
              action={
                <button type="button" className="btn btn-primary" onClick={openCreate}>
                  <Icon name="plus" size={16} />
                  Add category
                </button>
              }
            />
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th>Ownership</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row) => {
                    const predefined = Boolean(row.isPredefined)
                    return (
                      <tr key={row.categoryId}>
                        <td>
                          <strong>{row.categoryName}</strong>
                        </td>
                        <td>
                          <Badge variant={row.type === 'INCOME' ? 'success' : 'danger'}>
                            {titleCase(row.type)}
                          </Badge>
                        </td>
                        <td className="text-muted">{row.description || '—'}</td>
                        <td>
                          {predefined ? (
                            <Badge variant="info">Shared</Badge>
                          ) : (
                            <Badge variant="muted">Custom</Badge>
                          )}
                        </td>
                        <td className="actions">
                          {predefined ? (
                            <span className="text-small text-muted nowrap">Read-only</span>
                          ) : (
                            <span className="row-actions">
                              <button
                                type="button"
                                className="btn-icon"
                                onClick={() => openEdit(row)}
                                aria-label={`Edit ${row.categoryName}`}
                              >
                                <Icon name="edit" size={16} />
                              </button>
                              <button
                                type="button"
                                className="btn-icon danger"
                                onClick={() => setDeleteTarget(row)}
                                aria-label={`Delete ${row.categoryName}`}
                              >
                                <Icon name="trash" size={16} />
                              </button>
                            </span>
                          )}
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
          {rows.length > 0 ? (
            <Pagination
              page={page}
              totalPages={list.data?.totalPages ?? 0}
              totalElements={list.data?.totalElements ?? 0}
              size={PAGE_SIZE}
              onPageChange={setPage}
            />
          ) : null}
        </div>
      </div>

      <Modal
        open={modal.open}
        title={modal.mode === 'edit' ? 'Edit category' : 'Add category'}
        onClose={closeModal}
      >
        <CategoryForm
          initial={categoryToForm(modal.record)}
          onSubmit={handleSave}
          onCancel={closeModal}
          submitting={submitting}
          serverError={formError}
          fieldErrors={fieldErrors}
        />
      </Modal>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Delete category"
        message={
          deleteTarget
            ? `Are you sure you want to delete "${deleteTarget.categoryName}"? Categories in use by transactions cannot be deleted.`
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
