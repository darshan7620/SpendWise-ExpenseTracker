export default function Alert({ variant = 'error', children, onDismiss }) {
  if (!children) return null
  return (
    <div className={`alert alert-${variant}`} role={variant === 'error' ? 'alert' : 'status'}>
      <div className="grow">{children}</div>
      {onDismiss ? (
        <button type="button" className="btn-icon" onClick={onDismiss} aria-label="Dismiss message">
          ×
        </button>
      ) : null}
    </div>
  )
}
