import LoadingBlock from '../ui/LoadingBlock'
import EmptyState from '../ui/EmptyState'
import Alert from '../ui/Alert'
import { extractErrorMessage } from '../../api/client'

export default function ChartCard({
  title,
  subtitle,
  actions,
  loading,
  error,
  isEmpty,
  emptyTitle = 'No data yet',
  emptyDescription,
  children,
  className = '',
}) {
  return (
    <section className={`card ${className}`.trim()}>
      <div className="card-header">
        <div>
          <h2>{title}</h2>
          {subtitle ? <div className="card-subtitle">{subtitle}</div> : null}
        </div>
        {actions ? <div className="page-actions">{actions}</div> : null}
      </div>
      <div className="card-body">
        {error ? (
          <Alert variant="error">{extractErrorMessage(error)}</Alert>
        ) : loading ? (
          <LoadingBlock />
        ) : isEmpty ? (
          <EmptyState title={emptyTitle} description={emptyDescription} />
        ) : (
          children
        )}
      </div>
    </section>
  )
}
