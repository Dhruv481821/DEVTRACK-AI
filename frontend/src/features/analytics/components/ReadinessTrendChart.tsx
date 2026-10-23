import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  type TooltipProps,
} from 'recharts';
import type { ReadinessTrendPoint } from '../api/useReadiness';

// Chart colors come from the design tokens in tailwind.config (signal, border,
// text-muted). Recharts needs literal values for SVG attributes. signal on the
// surface color was checked with the dataviz palette validator (contrast >= 3:1).
const SERIES = '#6E5BFF';
const GRID = '#272B37';
const AXIS = '#8B90A0';

function shortDate(iso: string): string {
  return new Date(`${iso}T00:00`).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
  });
}

function TrendTooltip({ active, payload }: TooltipProps<number, string>) {
  if (!active || !payload?.length) return null;
  const point = payload[0].payload as ReadinessTrendPoint;
  return (
    <div className="rounded-lg border border-border bg-surface-raised px-3 py-2 font-body text-xs">
      <p className="mb-1 text-text-muted">{shortDate(point.date)}</p>
      <p className="text-text-primary">Readiness {point.overall}</p>
      <p className="text-text-muted">
        DSA {point.dsa} · Resume {point.resume} · GitHub {point.github}
      </p>
    </div>
  );
}

// Single series, so no legend: the card title names it. The sr-only table is
// the accessible alternative to the plot.
export function ReadinessTrendChart({ points }: { points: ReadinessTrendPoint[] }) {
  return (
    <figure>
      <div className="h-40 w-full" aria-hidden="true">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={points} margin={{ top: 8, right: 8, bottom: 0, left: -24 }}>
            <CartesianGrid stroke={GRID} vertical={false} />
            <XAxis
              dataKey="date"
              tickFormatter={shortDate}
              stroke={AXIS}
              tick={{ fontSize: 11 }}
              tickLine={false}
              axisLine={false}
              minTickGap={24}
            />
            <YAxis
              domain={[0, 100]}
              ticks={[0, 50, 100]}
              stroke={AXIS}
              tick={{ fontSize: 11 }}
              tickLine={false}
              axisLine={false}
            />
            <Tooltip
              content={<TrendTooltip />}
              cursor={{ stroke: AXIS, strokeDasharray: '3 3' }}
            />
            <Line
              type="monotone"
              dataKey="overall"
              stroke={SERIES}
              strokeWidth={2}
              dot={points.length <= 14 ? { r: 3, fill: SERIES, strokeWidth: 0 } : false}
              activeDot={{ r: 5, fill: SERIES, stroke: '#14171F', strokeWidth: 2 }}
              isAnimationActive={false}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <table className="sr-only">
        <caption>Readiness score by day</caption>
        <thead>
          <tr>
            <th scope="col">Date</th>
            <th scope="col">Readiness</th>
            <th scope="col">DSA</th>
            <th scope="col">Resume</th>
            <th scope="col">GitHub</th>
          </tr>
        </thead>
        <tbody>
          {points.map((p) => (
            <tr key={p.date}>
              <td>{p.date}</td>
              <td>{p.overall}</td>
              <td>{p.dsa}</td>
              <td>{p.resume}</td>
              <td>{p.github}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </figure>
  );
}
