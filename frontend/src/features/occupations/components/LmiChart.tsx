import { Card } from '../../../components/common/Card';

import type {
  OccupationMarketIndicators,
} from '../types/occupation.types';

import {
  formatInteger,
  formatPercent,
} from '../utils/formatters';

import styles from './LmiChart.module.css';

interface LmiChartProps {
  indicators: OccupationMarketIndicators;
}

function clampScore(value: number | null): number {
  if (value === null || !Number.isFinite(value)) {
    return 0;
  }

  return Math.min(100, Math.max(0, value));
}

export function LmiChart({
  indicators,
}: LmiChartProps) {
  const demand = indicators.demandScore;

  const visualScore = clampScore(demand);

  const growth = indicators.growthRatePct;

  const isPositiveGrowth =
    growth !== null && growth > 0;

  return (
    <Card
      as="section"
      className={styles.card}
    >
      <div className={styles.heading}>
        <div>
          <span className={styles.eyebrow}>
            LABOR MARKET INDICATORS
          </span>

          <h2>
            Toàn cảnh nhu cầu thị trường
          </h2>
        </div>

        {indicators.dataPeriod ? (
          <span className={styles.period}>
            {indicators.dataPeriod}
          </span>
        ) : null}
      </div>

      <div className={styles.chartArea}>
        <div className={styles.chartLabels}>
          <div>
            <span className={styles.chartTitle}>
              Điểm nhu cầu tuyển dụng
            </span>

            <span className={styles.chartSubtitle}>
              Thang tham chiếu 0–100
            </span>
          </div>

          <strong className={styles.score}>
            {demand === null
              ? '—'
              : demand.toLocaleString('vi-VN', {
                  maximumFractionDigits: 1,
                })}

            <small>/100</small>
          </strong>
        </div>

        <svg
          className={styles.svg}
          viewBox="0 0 640 54"
          role="img"
          aria-label={
            demand === null
              ? 'Chưa có dữ liệu điểm nhu cầu'
              : `Điểm nhu cầu tuyển dụng ${demand} trên 100`
          }
        >
          <rect
            x="0"
            y="12"
            width="640"
            height="18"
            rx="9"
            fill="#e9eff7"
          />

          <rect
            x="0"
            y="12"
            width={640 * visualScore / 100}
            height="18"
            rx="9"
            fill="#3569bd"
          />

          <line
            x1="160"
            y1="8"
            x2="160"
            y2="34"
            stroke="#ffffff"
            strokeWidth="2"
            opacity="0.9"
          />

          <line
            x1="320"
            y1="8"
            x2="320"
            y2="34"
            stroke="#ffffff"
            strokeWidth="2"
            opacity="0.9"
          />

          <line
            x1="480"
            y1="8"
            x2="480"
            y2="34"
            stroke="#ffffff"
            strokeWidth="2"
            opacity="0.9"
          />
        </svg>

        <div className={styles.scale}>
          <span>0</span>
          <span>25</span>
          <span>50</span>
          <span>75</span>
          <span>100</span>
        </div>
      </div>

      <div className={styles.metrics}>
        <div className={styles.metric}>
          <span className={styles.metricLabel}>
            Tăng trưởng
          </span>

          <strong
            className={
              isPositiveGrowth
                ? styles.positive
                : styles.metricValue
            }
          >
            {formatPercent(growth)}
          </strong>

          <span className={styles.metricHint}>
            Tỷ lệ tăng trưởng được API cung cấp
          </span>
        </div>

        <div className={styles.metric}>
          <span className={styles.metricLabel}>
            Vị trí đang tuyển
          </span>

          <strong className={styles.metricValue}>
            {formatInteger(indicators.openingsCount)}
          </strong>

          <span className={styles.metricHint}>
            Số lượng tin tuyển dụng mở
          </span>
        </div>
      </div>

      <div className={styles.footer}>
        <span>
          {indicators.shortageLevel
            ? `Mức độ thiếu hụt nhân lực: ${indicators.shortageLevel}`
            : 'Mức độ thiếu hụt nhân lực: chưa cập nhật'}
        </span>

        {indicators.sourceName ? (
          <span>
            Nguồn: {indicators.sourceName}
          </span>
        ) : null}
      </div>
    </Card>
  );
}

export default LmiChart;