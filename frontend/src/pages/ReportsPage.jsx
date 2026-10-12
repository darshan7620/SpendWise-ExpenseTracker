import { useMemo, useState } from 'react'
import { useApi } from '../hooks/useApi'
import { reportsApi } from '../api/reports'
import { extractErrorMessage } from '../api/client'
import PageHeader from '../components/ui/PageHeader'
import Alert from '../components/ui/Alert'
import ChartCard from '../components/charts/ChartCard'
import IncomeExpenseTrendChart from '../components/charts/IncomeExpenseTrendChart'
import CategoryBreakdownList from '../components/charts/CategoryBreakdownList'
import Icon from '../components/Icon'
import { MONTHS, monthOptions, yearOptions } from '../utils/constants'
import { formatCurrency, formatNumber } from '../utils/format'

const YEARS = yearOptions()

function TotalCard({ icon, tone, label, value, valueClass }) {
  return (
    <article className="stat-card">
      <span className={`stat-icon ${tone}`}>
        <Icon name={icon} size={18} />
      </span>
      <span className="stat-label">{label}</span>
      <div className={`stat-value ${valueClass || ''}`}>{value}</div>
    </article>
  )
}

function MonthlyReport({ year, month }) {
  const report = useApi(() => reportsApi.monthly({ year, month }), [year, month])
  const data = report.data

  return (
    <>
      {report.error ? <Alert variant="error">{extractErrorMessage(report.error)}</Alert> : null}
      <div className="grid grid-4">
        <TotalCard
          icon="trendingUp"
          tone="income"
          label="Income"
          value={report.loading ? '—' : formatCurrency(data?.totalIncome)}
          valueClass="value-income"
        />
        <TotalCard
          icon="trendingDown"
          tone="expense"
          label="Expenses"
          value={report.loading ? '—' : formatCurrency(data?.totalExpenses)}
          valueClass="value-expense"
        />
        <TotalCard
          icon="scale"
          tone="balance"
          label="Balance"
          value={report.loading ? '—' : formatCurrency(data?.balance)}
          valueClass={Number(data?.balance) < 0 ? 'value-negative' : ''}
        />
        <TotalCard
          icon="transactions"
          tone="neutral"
          label="Transactions"
          value={report.loading ? '—' : formatNumber(data?.transactionCount)}
        />
      </div>

      <div className="grid grid-2 mt-24">
        <div className="card">
          <div className="card-header">
            <h2>Expenses by category</h2>
            <span className="text-small text-muted">
              {MONTHS[month - 1]} {year}
            </span>
          </div>
          <div className="card-body">
            {report.loading ? (
              <p className="text-muted text-small">Loading…</p>
            ) : (
              <CategoryBreakdownList items={data?.expenseByCategory} tone="expense" />
            )}
          </div>
        </div>
        <div className="card">
          <div className="card-header">
            <h2>Income by category</h2>
            <span className="text-small text-muted">
              {MONTHS[month - 1]} {year}
            </span>
          </div>
          <div className="card-body">
            {report.loading ? (
              <p className="text-muted text-small">Loading…</p>
            ) : (
              <CategoryBreakdownList items={data?.incomeByCategory} tone="income" />
            )}
          </div>
        </div>
      </div>
    </>
  )
}

function YearlyReport({ year }) {
  const report = useApi(() => reportsApi.yearly({ year }), [year])
  const data = report.data

  const trendData = useMemo(
    () =>
      (data?.monthlyBreakdown || []).map((item) => ({
        name: item.label,
        income: Number(item.totalIncome),
        expenses: Number(item.totalExpenses),
      })),
    [data],
  )

  return (
    <>
      {report.error ? <Alert variant="error">{extractErrorMessage(report.error)}</Alert> : null}
      <div className="grid grid-4">
        <TotalCard
          icon="trendingUp"
          tone="income"
          label="Income"
          value={report.loading ? '—' : formatCurrency(data?.totalIncome)}
          valueClass="value-income"
        />
        <TotalCard
          icon="trendingDown"
          tone="expense"
          label="Expenses"
          value={report.loading ? '—' : formatCurrency(data?.totalExpenses)}
          valueClass="value-expense"
        />
        <TotalCard
          icon="scale"
          tone="balance"
          label="Balance"
          value={report.loading ? '—' : formatCurrency(data?.balance)}
          valueClass={Number(data?.balance) < 0 ? 'value-negative' : ''}
        />
        <TotalCard
          icon="transactions"
          tone="neutral"
          label="Transactions"
          value={report.loading ? '—' : formatNumber(data?.transactionCount)}
        />
      </div>

      <div className="mt-24">
        <ChartCard
          title={`Monthly trend · ${year}`}
          subtitle="Income and expenses for each month of the year"
          loading={report.loading}
          error={report.error}
          isEmpty={!report.loading && trendData.length === 0}
          emptyTitle="No data for this year"
        >
          <IncomeExpenseTrendChart data={trendData} />
        </ChartCard>
      </div>

      <div className="grid grid-2 mt-24">
        <div className="card">
          <div className="card-header">
            <h2>Expenses by category</h2>
            <span className="text-small text-muted">{year}</span>
          </div>
          <div className="card-body">
            {report.loading ? (
              <p className="text-muted text-small">Loading…</p>
            ) : (
              <CategoryBreakdownList items={data?.expenseByCategory} tone="expense" />
            )}
          </div>
        </div>
        <div className="card">
          <div className="card-header">
            <h2>Income by category</h2>
            <span className="text-small text-muted">{year}</span>
          </div>
          <div className="card-body">
            {report.loading ? (
              <p className="text-muted text-small">Loading…</p>
            ) : (
              <CategoryBreakdownList items={data?.incomeByCategory} tone="income" />
            )}
          </div>
        </div>
      </div>
    </>
  )
}

export default function ReportsPage() {
  const [tab, setTab] = useState('monthly')
  const [year, setYear] = useState(() => new Date().getFullYear())
  const [month, setMonth] = useState(() => new Date().getMonth() + 1)

  return (
    <div>
      <PageHeader
        title="Reports"
        subtitle="Detailed monthly and yearly summaries of your finances."
        actions={
          <div className="filter-bar">
            {tab === 'monthly' ? (
              <div className="field">
                <label htmlFor="rep-month">Month</label>
                <select id="rep-month" value={month} onChange={(e) => setMonth(Number(e.target.value))}>
                  {monthOptions().map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
            ) : null}
            <div className="field">
              <label htmlFor="rep-year">Year</label>
              <select id="rep-year" value={year} onChange={(e) => setYear(Number(e.target.value))}>
                {YEARS.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </div>
          </div>
        }
      />

      <div className="tabs" role="tablist">
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'monthly'}
          className={`tab ${tab === 'monthly' ? 'active' : ''}`}
          onClick={() => setTab('monthly')}
        >
          Monthly
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'yearly'}
          className={`tab ${tab === 'yearly' ? 'active' : ''}`}
          onClick={() => setTab('yearly')}
        >
          Yearly
        </button>
      </div>

      {tab === 'monthly' ? (
        <MonthlyReport year={year} month={month} />
      ) : (
        <YearlyReport year={year} />
      )}
    </div>
  )
}
