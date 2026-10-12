import Icon from '../Icon'

export default function Pagination({ page, totalPages, totalElements, size, onPageChange }) {
  const currentPage = Number(page) || 0
  const pages = Number(totalPages) || 0
  const total = Number(totalElements) || 0
  const pageSize = Number(size) || 0
  const from = total === 0 ? 0 : currentPage * pageSize + 1
  const to = total === 0 ? 0 : Math.min(total, (currentPage + 1) * pageSize)

  return (
    <div className="pagination">
      <span>
        Showing <strong>{from}</strong>–<strong>{to}</strong> of <strong>{total}</strong>
      </span>
      <div className="pager">
        <button
          type="button"
          className="btn btn-sm btn-secondary"
          onClick={() => onPageChange(Math.max(0, currentPage - 1))}
          disabled={currentPage <= 0}
        >
          <Icon name="chevronLeft" size={16} />
          Prev
        </button>
        <span className="nowrap">
          Page {pages === 0 ? 0 : currentPage + 1} of {pages === 0 ? 0 : pages}
        </span>
        <button
          type="button"
          className="btn btn-sm btn-secondary"
          onClick={() => onPageChange(Math.min(pages - 1, currentPage + 1))}
          disabled={pages === 0 || currentPage >= pages - 1}
        >
          Next
          <Icon name="chevronRight" size={16} />
        </button>
      </div>
    </div>
  )
}
