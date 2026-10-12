import { formatCurrency, formatPercent } from '../../utils/format'

export default function CategoryBreakdownList({ items, emptyText = 'No data for this period.', tone = 'expense' }) {
  if (!items || items.length === 0) {
    return <p className="text-muted text-small">{emptyText}</p>
  }
  const barClass = tone === 'income' ? 'success' : ''
  return (
    <div className="breakdown-list">
      {items.map((item) => (
        <div className="breakdown-row" key={`${item.categoryName}-${item.categoryId ?? 'none'}`}>
          <div className="breakdown-top">
            <span className="breakdown-name">
              {item.categoryName}
              <span className="text-muted text-small"> · {item.transactionCount} txns</span>
            </span>
            <span className="breakdown-amount">
              {formatCurrency(item.totalAmount)} · {formatPercent(item.percentage)}
            </span>
          </div>
          <div className="progress">
            <div
              className={`progress-bar ${barClass}`}
              style={{ width: `${Math.min(100, Number(item.percentage) || 0)}%` }}
            />
          </div>
        </div>
      ))}
    </div>
  )
}
