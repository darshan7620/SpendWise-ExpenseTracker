import { useMemo, useState } from 'react'
import { useApi } from '../hooks/useApi'
import { dashboardApi } from '../api/dashboard'
import { extractErrorMessage } from '../api/client'
import PageHeader from '../components/ui/PageHeader'
import ChartCard from '../components/charts/ChartCard'
import IncomeExpenseTrendChart from '../components/charts/IncomeExpenseTrendChart'
import CategoryDonutChart from '../components/charts/CategoryDonutChart'
import Icon from '../components/Icon'
import { MONTHS, monthOptions, yearOptions } from '../utils/constants'
import { formatCurrency, formatNumber, formatPercent } from '../utils/format'

const YEARS = yearOptions()

function StatCard({ icon, tone, label, value, foot, valueClass = '' }) {
  return (
    <article className="stat-card">
      <span className={`stat-icon ${tone}`}>
        <Icon name={icon} size={18} />
      </span>
      <span className="stat-label">{label}</span>
      <div className={`stat-value ${valueClass}`}>{value}</div>
      {foot ? <div className="stat-foot">{foot}</div> : null}
    </article>
  )
}

export default function DashboardPage() {
  const [year, setYear] = useState(() => new Date().getFullYear())
  const [month, setMonth] = useState(() => new Date().getMonth() + 1)
  const [trendMonths, setTrendMonths] = useState(6)
  const [breakdownType, setBreakdownType] = useState('EXPENSE')

  const periodParams = useMemo(() => {
    const params = { year }
    if (month) params.month = month
    return params
  }, [year, month])

  const summary = useApi(() => dashboardApi.summary(periodParams), [periodParams])
  const trends = useApi(() => dashboardApi.monthlyTrends(trendMonths), [trendMonths])
  const breakdown = useApi(
    () => dashboardApi.categoryBreakdown({ type: breakdownType, ...periodParams }),
    [breakdownType, periodParams],
  )

  const periodLabel = month ? `${MONTHS[month - 1]} ${year}` : `Year ${year}`

  const trendData = useMemo(
    () =>
      (trends.data || []).map((item) => ({
        name: item.label,
        income: Number(item.totalIncome),
        expenses: Number(item.totalExpenses),
        balance: Number(item.balance),
      })),
    [trends.data],
  )

  const breakdownData = useMemo(
    () =>
      (breakdown.data?.categories || [])
        .filter((item) => Number(item.totalAmount) > 0)
        .map((item) => ({
          name: item.categoryName,
          value: Number(item.totalAmount),
          percentage: Number(item.percentage),
          count: item.transactionCount,
        })),
    [breakdown.data],
  )

  const summaryData = summary.data

  return (
    <div>
      <PageHeader
        title="Dashboard"
        subtitle="An overview of your income, spending, and trends."
        actions={
          <div className="filter-bar">
            <div className="field">
              <label htmlFor="dash-month">Month</label>
              <select id="dash-month" value={month} onChange={(e) => setMonth(Number(e.target.value))}>
                <option value={0}>Full year</option>
                {monthOptions().map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="dash-year">Year</label>
              <select id="dash-year" value={year} onChange={(e) => setYear(Number(e.target.value))}>
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

      {summary.error ? (
        <div className="alert alert-error mb-16">{extractErrorMessage(summary.error)}</div>
      ) : null}

      <div className="grid grid-4">
        <StatCard
          icon="trendingUp"
          tone="income"
          label="Income"
          value={summary.loading ? '—' : formatCurrency(summaryData?.totalIncome)}
          valueClass="value-income"
          foot={summary.loading ? ' ' : `${formatNumber(summaryData?.incomeCount)} transactions`}
        />
        <StatCard
          icon="trendingDown"
          tone="expense"
          label="Expenses"
          value={summary.loading ? '—' : formatCurrency(summaryData?.totalExpenses)}
          valueClass="value-expense"
          foot={summary.loading ? ' ' : `${formatNumber(summaryData?.expenseCount)} transactions`}
        />
        <StatCard
          icon="scale"
          tone="balance"
          label="Balance"
          value={summary.loading ? '—' : formatCurrency(summaryData?.balance)}
          valueClass={Number(summaryData?.balance) < 0 ? 'value-negative' : ''}
          foot={periodLabel}
        />
        <StatCard
          icon="transactions"
          tone="neutral"
          label="Transactions"
          value={summary.loading ? '—' : formatNumber(summaryData?.transactionCount)}
          foot="in selected period"
        />
      </div>

      <div className="grid grid-2 mt-24">
        <ChartCard
          title="Income vs Expenses"
          subtitle="Monthly trend over the recent months"
          loading={trends.loading}
          error={trends.error}
          isEmpty={!trends.loading && trendData.length === 0}
          emptyTitle="No trend data"
          emptyDescription="Add transactions to see your monthly income and expense trends."
          actions={
            <div className="segmented" role="group" aria-label="Trend range">
              {[6, 12].map((value) => (
                <button
                  key={value}
                  type="button"
                  className={trendMonths === value ? 'active' : ''}
                  onClick={() => setTrendMonths(value)}
                >
                  {value}M
                </button>
              ))}
            </div>
          }
        >
          <IncomeExpenseTrendChart data={trendData} />
        </ChartCard>

        <ChartCard
          title="Category breakdown"
          subtitle={`${periodLabel} · total ${formatCurrency(breakdown.data?.totalAmount)}`}
          loading={breakdown.loading}
          error={breakdown.error}
          isEmpty={!breakdown.loading && breakdownData.length === 0}
          emptyTitle="No category data"
          emptyDescription="There are no categorised transactions for the selected period."
          actions={
            <div className="segmented" role="group" aria-label="Breakdown type">
              <button
                type="button"
                className={breakdownType === 'EXPENSE' ? 'active' : ''}
                onClick={() => setBreakdownType('EXPENSE')}
              >
                Expense
              </button>
              <button
                type="button"
                className={breakdownType === 'INCOME' ? 'active' : ''}
                onClick={() => setBreakdownType('INCOME')}
              >
                Income
              </button>
            </div>
          }
        >
          <CategoryDonutChart data={breakdownData} />
        </ChartCard>
      </div>

      <div className="card mt-24">
        <div className="card-header">
          <h2>Top categories</h2>
          <span className="text-small text-muted">{periodLabel}</span>
        </div>
        <div className="card-body">
          {breakdownData.length === 0 ? (
            <p className="text-muted text-small">No category spending to show.</p>
          ) : (
            <div className="breakdown-list">
              {breakdownData.slice(0, 5).map((item) => (
                <div className="breakdown-row" key={item.name}>
                  <div className="breakdown-top">
                    <span className="breakdown-name">{item.name}</span>
                    <span className="breakdown-amount">
                      {formatCurrency(item.value)} · {formatPercent(item.percentage)}
                    </span>
                  </div>
                  <div className="progress">
                    <div className="progress-bar" style={{ width: `${Math.min(100, item.percentage)}%` }} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
