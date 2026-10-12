import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { formatCompactCurrency, formatCurrency } from '../../utils/format'

function TooltipContent({ active, payload, label }) {
  if (!active || !payload || payload.length === 0) return null
  return (
    <div className="card" style={{ padding: '10px 12px', boxShadow: 'var(--shadow)' }}>
      <div style={{ fontWeight: 700, marginBottom: 6 }}>{label}</div>
      {payload.map((entry) => (
        <div key={entry.name} className="flex-between gap-16 text-small">
          <span style={{ color: entry.color }}>{entry.name}</span>
          <strong>{formatCurrency(entry.value)}</strong>
        </div>
      ))}
    </div>
  )
}

export default function IncomeExpenseTrendChart({ data }) {
  return (
    <div className="chart-container tall">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
          <XAxis
            dataKey="name"
            tick={{ fill: 'var(--muted)', fontSize: 12 }}
            axisLine={{ stroke: 'var(--border)' }}
            tickLine={false}
          />
          <YAxis
            tickFormatter={formatCompactCurrency}
            tick={{ fill: 'var(--muted)', fontSize: 12 }}
            axisLine={false}
            tickLine={false}
            width={70}
          />
          <Tooltip content={<TooltipContent />} cursor={{ fill: 'var(--surface-2)' }} />
          <Bar dataKey="income" name="Income" fill="var(--income)" radius={[5, 5, 0, 0]} maxBarSize={34} />
          <Bar dataKey="expenses" name="Expenses" fill="var(--expense)" radius={[5, 5, 0, 0]} maxBarSize={34} />
        </BarChart>
      </ResponsiveContainer>
      <div className="chart-legend" style={{ justifyContent: 'center', marginTop: 8 }}>
        <span>
          <i className="dot" style={{ background: 'var(--income)' }} /> Income
        </span>
        <span>
          <i className="dot" style={{ background: 'var(--expense)' }} /> Expenses
        </span>
      </div>
    </div>
  )
}
