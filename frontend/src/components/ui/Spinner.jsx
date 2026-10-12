export default function Spinner({ size = 'md', label }) {
  const className = size === 'lg' ? 'spinner lg' : 'spinner'
  return (
    <span className="inline-loading" role="status" aria-live="polite">
      <span className={className} />
      {label ? <span>{label}</span> : <span className="sr-only">Loading</span>}
    </span>
  )
}
