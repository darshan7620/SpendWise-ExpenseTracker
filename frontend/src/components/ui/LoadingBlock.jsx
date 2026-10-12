export function LoadingBlock({ label = 'Loading…' }) {
  return (
    <div className="loading-block" role="status" aria-live="polite">
      <span className="spinner lg" />
      <span>{label}</span>
    </div>
  )
}

export default LoadingBlock
