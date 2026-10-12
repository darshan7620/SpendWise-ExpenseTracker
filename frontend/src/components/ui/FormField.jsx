export default function FormField({
  label,
  htmlFor,
  error,
  hint,
  required,
  className = '',
  children,
}) {
  return (
    <div className={`field ${error ? 'has-error' : ''} ${className}`.trim()}>
      {label ? (
        <label htmlFor={htmlFor}>
          {label}
          {required ? ' *' : ''}
        </label>
      ) : null}
      {children}
      {error ? (
        <span className="field-error" role="alert">
          {error}
        </span>
      ) : hint ? (
        <span className="hint">{hint}</span>
      ) : null}
    </div>
  )
}
