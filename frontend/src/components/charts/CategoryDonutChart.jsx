import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { formatCurrency, formatPercent } from '../../utils/format'

const COLORS = [
  '#4f46e5',
  '#0ea5e9',
  '#14b8a6',
  '#f59e0b',
  '#ef4444',
  '#8b5cf6',
  '#ec4899',
  '#22c55e',
  '#f97316',
  '#06b6d4',
]

function TooltipContent({ active, payload }) {
  if (!active || !payload || payload.length === 0) return null
  const item = payload[0].payload
  return (
    <div className="card" style={{ padding: '10px 12px', boxShadow: 'var(--shadow)' }}>
      <div style={{ fontWeight: 700, marginBottom: 4 }}>{item.name}</div>
      <div className="text-small">{formatCurrency(item.value)}</div>
      <div className="text-small text-muted">{formatPercent(item.percentage)} of total</div>
    </div>
  )
}

export default function CategoryDonutChart({ data }) {
  const first = data[0]
  const summary = `${formatCurrency(first?.value)} in ${first?.name}`

  return (
    <div>
      <div className="chart-container">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data}
              dataKey="value"
              nameKey="name"
              innerRadius="58%"
              outerRadius="88%"
              paddingAngle={2}
              stroke="var(--surface)"
            >
              {data.map((entry, index) => (
                <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip content={<TooltipContent />} />
          </PieChart>
        </ResponsiveContainer>
      </div>
      <p className="text-small text-muted" style={{ textAlign: 'center', marginTop: -8 }}>
        {summary}
      </p>
      <ul className="breakdown-list mt-16" style={{ listStyle: 'none', padding: 0, margin: '16px 0 0' }}>
        {data.map((entry, index) => (
          <li className="flex-between gap-12" key={entry.name}>
            <span className="flex items-center gap-8">
              <i
                className="dot"
                style={{
                  width: 10,
                  height: 10,
                  borderRadius: 3,
                  background: COLORS[index % COLORS.length],
                  display: 'inline-block',
                }}
              />
              <span>{entry.name}</span>
            </span>
            <span className="text-muted nowrap">
              {formatCurrency(entry.value)} · {formatPercent(entry.percentage)}
            </span>
          </li>
        ))}
      </ul>
    </div>
  )
}
