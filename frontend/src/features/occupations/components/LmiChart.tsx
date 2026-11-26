import { useState } from 'react';
import { Card } from '../../../components/common/Card';
import type { OccupationMarketIndicators, OccupationMarketPoint } from '../types/occupation.types';
import { formatPercent } from '../utils/formatters';
import styles from './LmiChart.module.css';

type Metric = 'demandScore' | 'openingsCount';

const numberFormat = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 2 });
const labels: Record<Metric, string> = {
  demandScore: 'Điểm nhu cầu (0–100)',
  openingsCount: 'Số lượng tuyển dụng',
};

function MarketBars({ points, metric, maximum }: {
  points: OccupationMarketPoint[];
  metric: Metric;
  maximum: number;
}) {
  return (
    <figure className={styles.figure}>
      <figcaption>{labels[metric]}{metric === 'openingsCount' && <small> (0–{numberFormat.format(maximum)})</small>}</figcaption>
      <ul className={styles.bars} aria-label={labels[metric]}>
        {points.map((point) => {
          const value = point[metric];
          const height = Math.min(100, Math.max(0, ((value ?? 0) / maximum) * 100));

          return (
            <li key={point.period}>
              <strong>{value === null ? '—' : numberFormat.format(value)}</strong>
              <svg viewBox="0 0 24 100" preserveAspectRatio="none" aria-hidden="true">
                <rect width="24" height="100" className={styles.track} />
                {value !== null && (
                  <rect y={100 - height} width="24" height={height} className={styles[metric]} />
                )}
              </svg>
              <span>{point.period}</span>
            </li>
          );
        })}
      </ul>
    </figure>
  );
}

interface LmiChartProps {
  indicators: OccupationMarketIndicators | null;
  trend?: OccupationMarketPoint[];
}

export function LmiChart({ indicators, trend = [] }: LmiChartProps) {
  const [view, setView] = useState<'chart' | 'table'>('chart');
  const [selectedRegion, setSelectedRegion] = useState('');
  const regions = [...new Set(trend.flatMap(({ region }) => region ? [region] : []))];
  const region = regions.includes(selectedRegion) ? selectedRegion : regions[0];
  const snapshot = indicators && (indicators.demandScore !== null || indicators.openingsCount !== null)
    ? [{
        period: indicators.dataPeriod || 'Kỳ mới nhất',
        region: null,
        demandScore: indicators.demandScore,
        openingsCount: indicators.openingsCount,
      }]
    : [];
  const points = trend.length
    ? trend.filter((point) => point.region === (region ?? null))
      .sort((first, second) => first.period.localeCompare(second.period, 'vi', { numeric: true }))
    : snapshot;
  const availableMetrics = Object.keys(labels) as Metric[];
  const metrics = availableMetrics
    .filter((metric) => points.some((point) => point[metric] !== null));

  const visibleMetrics = view === 'table' ? availableMetrics : metrics;
  const hasMissingValues = points.some((point) => visibleMetrics.some((metric) => point[metric] === null));
  const maxOpenings = (trend.length ? trend : snapshot)
    .reduce((maximum, point) => Math.max(maximum, point.openingsCount ?? 0), 1);

  return (
    <Card as="section" className={styles.card}>
      <div className={styles.heading}>
        <h2>Xu hướng thị trường lao động</h2>
        {regions.length > 0 && (
          <label className={styles.region}>
            <span>Khu vực</span>
            <select value={region} onChange={(event) => setSelectedRegion(event.target.value)}>
              {regions.map((name) => <option key={name}>{name}</option>)}
            </select>
          </label>
        )}
      </div>
      {metrics.length > 0 ? (
        <>
          <div className={styles.segmented} role="group" aria-label="Cách hiển thị dữ liệu LMI">
            <button type="button" aria-pressed={view === 'chart'} onClick={() => setView('chart')}>Biểu đồ</button>
            <button type="button" aria-pressed={view === 'table'} onClick={() => setView('table')}>Bảng số liệu</button>
          </div>
          {view === 'chart' ? (
            <div className={styles.charts}>
              {metrics.map((metric) => (
                <MarketBars
                  key={metric}
                  points={points}
                  metric={metric}
                  maximum={metric === 'demandScore' ? 100 : maxOpenings}
                />
              ))}
            </div>
          ) : (
            <div className={styles.tableArea}>
              <table aria-label={region ? `Số liệu LMI — ${region}` : 'Số liệu LMI'}>
                <thead>
                  <tr>
                    <th scope="col">Kỳ dữ liệu</th>
                    <th scope="col">{labels.demandScore}</th>
                    <th scope="col">{labels.openingsCount}</th>
                  </tr>
                </thead>
                <tbody>
                  {points.map((point) => (
                    <tr key={point.period}>
                      <th scope="row">{point.period}</th>
                      <td>{point.demandScore === null ? '—' : numberFormat.format(point.demandScore)}</td>
                      <td>{point.openingsCount === null ? '—' : numberFormat.format(point.openingsCount)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      ) : <p className={styles.note}>Chưa có dữ liệu điểm nhu cầu hoặc số lượng tuyển dụng.</p>}
      {trend.length === 0 && snapshot.length > 0 && (
        <p className={styles.note}>Dữ liệu hiện có gồm kỳ mới nhất.</p>
      )}
      {indicators && (
        <dl className={styles.meta} aria-label="Chỉ số tổng quan kỳ mới nhất">
          {indicators.dataPeriod && <div><dt>Kỳ mới nhất</dt><dd>{indicators.dataPeriod}</dd></div>}
          {indicators.growthRatePct !== null && <div><dt>Tăng trưởng</dt><dd>{formatPercent(indicators.growthRatePct)}</dd></div>}
          {indicators.shortageLevel && <div><dt>Thiếu hụt nhân lực</dt><dd>{indicators.shortageLevel}</dd></div>}
          {indicators.sourceName && <div><dt>Nguồn</dt><dd>{indicators.sourceName}</dd></div>}
        </dl>
      )}
      {metrics.length > 0 && hasMissingValues && (
        <p className={styles.note}>Dấu — biểu thị giá trị chưa được cập nhật.</p>
      )}
    </Card>
  );
}
