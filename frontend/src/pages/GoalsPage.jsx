import { useState } from 'react'
import { useApi } from '../hooks/useApi'
import { goalsApi } from '../api/goals'
import { extractErrorMessage, extractFieldErrors } from '../api/client'
import PageHeader from '../components/ui/PageHeader'
import LoadingBlock from '../components/ui/LoadingBlock'
import EmptyState from '../components/ui/EmptyState'
import Alert from '../components/ui/Alert'
import Badge from '../components/ui/Badge'
import Modal from '../components/ui/Modal'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import Icon from '../components/Icon'
import GoalForm, { goalToForm } from '../components/forms/GoalForm'
import GoalProgressForm from '../components/forms/GoalProgressForm'
import { formatCurrency, formatDate, formatPercent, titleCase } from '../utils/format'

const STATUS = {
  IN_PROGRESS: { variant: 'info', label: 'In Progress' },
  COMPLETED: { variant: 'success', label: 'Completed' },
  CANCELLED: { variant: 'muted', label: 'Cancelled' },
}

export default function GoalsPage() {
  const [modal, setModal] = useState({ open: false, mode: 'create', record: null })
  const [progressGoal, setProgressGoal] = useState(null)
  const [formError, setFormError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [notice, setNotice] = useState('')

  const list = useApi(() => goalsApi.list(), [])
  const goals = list.data || []

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

  function openProgress(record) {
    setFormError('')
    setProgressGoal(record)
  }

  async function handleSave(payload) {
    setSubmitting(true)
    setFormError('')
    setFieldErrors({})
    try {
      if (modal.mode === 'edit' && modal.record) {
        await goalsApi.update(modal.record.goalId, payload)
        flash('Goal updated successfully.')
      } else {
        await goalsApi.create(payload)
        flash('Goal created successfully.')
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

  async function handleProgress(amountToAdd) {
    if (!progressGoal) return
    setSubmitting(true)
    setFormError('')
    try {
      await goalsApi.updateProgress(progressGoal.goalId, amountToAdd)
      setProgressGoal(null)
      flash('Progress updated successfully.')
      await list.reload()
    } catch (error) {
      setFormError(extractErrorMessage(error))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return
    setDeleting(true)
    try {
      await goalsApi.remove(deleteTarget.goalId)
      setDeleteTarget(null)
      flash('Goal deleted.')
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
        title="Savings Goals"
        subtitle="Plan for the future and track your progress toward each target."
        actions={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            <Icon name="plus" size={16} />
            New goal
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
      ) : !list.error && goals.length === 0 ? (
        <div className="card">
          <EmptyState
            icon="goals"
            title="No savings goals yet"
            description="Create a goal with a target amount and date, then track your progress as you save."
            action={
              <button type="button" className="btn btn-primary" onClick={openCreate}>
                <Icon name="plus" size={16} />
                New goal
              </button>
            }
          />
        </div>
      ) : (
        <div className="grid grid-3">
          {goals.map((goal) => {
            const status = STATUS[goal.status] || STATUS.IN_PROGRESS
            const percentage = Math.min(100, Number(goal.progressPercentage) || 0)
            const barClass = goal.status === 'COMPLETED' ? 'success' : 'default'
            const canAddProgress = goal.status !== 'CANCELLED' && goal.status !== 'COMPLETED'
            return (
              <article className="card goal-card" key={goal.goalId}>
                <div className="card-body">
                  <div className="goal-head">
                    <div>
                      <h3 style={{ fontSize: 16 }}>{goal.goalName}</h3>
                      <span className="text-small text-muted">
                        <Icon name="calendar" size={13} /> Target {formatDate(goal.targetDate)}
                      </span>
                    </div>
                    <Badge variant={status.variant}>{status.label}</Badge>
                  </div>

                  <div className="mt-16">
                    <div className="flex-between text-small">
                      <span className="text-muted">Saved</span>
                      <span>
                        <strong>{formatCurrency(goal.currentAmount)}</strong> of{' '}
                        {formatCurrency(goal.targetAmount)}
                      </span>
                    </div>
                    <div className="progress mt-8">
                      <div
                        className={`progress-bar ${barClass}`}
                        style={{ width: `${percentage}%` }}
                      />
                    </div>
                    <div className="flex-between text-small mt-8">
                      <span className="text-muted">{formatPercent(goal.progressPercentage)} complete</span>
                      <span className="text-muted">
                        Remaining {formatCurrency(goal.remainingAmount)}
                      </span>
                    </div>
                  </div>
                </div>
                <div className="modal-footer" style={{ borderTop: '1px solid var(--border)' }}>
                  <button
                    type="button"
                    className="btn btn-sm btn-primary"
                    onClick={() => openProgress(goal)}
                    disabled={!canAddProgress}
                    title={canAddProgress ? 'Add to goal' : titleCase(goal.status)}
                  >
                    <Icon name="plus" size={14} />
                    Add progress
                  </button>
                  <button type="button" className="btn btn-sm btn-secondary" onClick={() => openEdit(goal)}>
                    <Icon name="edit" size={14} />
                    Edit
                  </button>
                  <button
                    type="button"
                    className="btn btn-sm btn-ghost"
                    onClick={() => setDeleteTarget(goal)}
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
        title={modal.mode === 'edit' ? 'Edit savings goal' : 'New savings goal'}
        onClose={closeModal}
      >
        <GoalForm
          initial={goalToForm(modal.record)}
          onSubmit={handleSave}
          onCancel={closeModal}
          submitting={submitting}
          serverError={formError}
          fieldErrors={fieldErrors}
        />
      </Modal>

      <Modal open={Boolean(progressGoal)} title="Add progress" onClose={() => setProgressGoal(null)}>
        <GoalProgressForm
          goal={progressGoal}
          onSubmit={handleProgress}
          onCancel={() => setProgressGoal(null)}
          submitting={submitting}
          serverError={formError}
        />
      </Modal>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Delete savings goal"
        message={
          deleteTarget
            ? `Are you sure you want to delete "${deleteTarget.goalName}"? This action cannot be undone.`
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
