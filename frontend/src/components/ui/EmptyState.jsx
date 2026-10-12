import Icon from '../Icon'

export default function EmptyState({ icon = 'inbox', title, description, action }) {
  return (
    <div className="empty-state">
      <span className="empty-icon">
        <Icon name={icon} size={24} />
      </span>
      {title ? <h3>{title}</h3> : null}
      {description ? <p>{description}</p> : null}
      {action ? <div className="mt-8">{action}</div> : null}
    </div>
  )
}
