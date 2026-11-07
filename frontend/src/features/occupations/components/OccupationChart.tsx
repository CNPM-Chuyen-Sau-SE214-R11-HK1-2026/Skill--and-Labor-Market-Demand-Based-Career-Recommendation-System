interface OccupationChartProps {
  demandScore: number | null;
  growthRatePct: number | null;
  shortageLevel: string | null;
  openingsCount: number | null;
}

function clamp(value: number): number { return Math.min(100, Math.max(0, value)); }

export function OccupationChart({ demandScore, growthRatePct, shortageLevel, openingsCount }: OccupationChartProps) {
  const score = demandScore === null ? null : clamp(demandScore);
  const growth = growthRatePct === null ? null : clamp(growthRatePct);
  const circumference = 2 * Math.PI * 42;
  const dash = score === null ? 0 : (score / 100) * circumference;

  return (
    <section className="lmi-panel" aria-labelledby="lmi-heading">
      <div className="section-heading">
        <div><span className="eyebrow">LABOR MARKET INSIGHTS</span><h2 id="lmi-heading">Tín hiệu thị trường lao động</h2></div>
        <span className="live-dot">Chỉ số LMI</span>
      </div>
      <div className="lmi-grid">
        <div className="demand-gauge">
          <svg viewBox="0 0 112 112" role="img" aria-label={score === null ? 'Chưa có điểm nhu cầu' : `Điểm nhu cầu ${score} trên 100`}>
            <circle cx="56" cy="56" r="42" className="gauge-track" />
            {score !== null && <circle cx="56" cy="56" r="42" className="gauge-value" strokeDasharray={`${dash} ${circumference}`} />}
            <text x="56" y="53" className="gauge-number">{score === null ? '—' : Math.round(score)}</text>
            <text x="56" y="69" className="gauge-label">/ 100</text>
          </svg>
          <div><strong>Điểm nhu cầu</strong><span>Mức độ quan tâm tuyển dụng</span></div>
        </div>
        <div className="lmi-stat">
          <span className="lmi-stat__icon">↗</span><span className="lmi-stat__label">Tăng trưởng</span>
          <strong>{growthRatePct === null ? '—' : `${growthRatePct}%`}</strong>
          <div className="metric-track"><span style={{ width: `${growth ?? 0}%` }} /></div>
          <small>Tỷ lệ tăng trưởng nhu cầu</small>
        </div>
        <div className="lmi-stat">
          <span className="lmi-stat__icon">◎</span><span className="lmi-stat__label">Mức độ thiếu hụt</span>
          <strong className="lmi-stat__word">{shortageLevel || 'Chưa cập nhật'}</strong>
          <small>Đánh giá cung – cầu nhân lực</small>
        </div>
        <div className="lmi-stat">
          <span className="lmi-stat__icon">▤</span><span className="lmi-stat__label">Vị trí tuyển dụng</span>
          <strong>{openingsCount === null ? '—' : new Intl.NumberFormat('vi-VN').format(openingsCount)}</strong>
          <small>Số lượng tin/vị trí được ghi nhận</small>
        </div>
      </div>
      <p className="chart-note">Các chỉ số được trình bày theo dữ liệu API cung cấp; hệ thống không tự suy diễn giá trị còn thiếu.</p>
    </section>
  );
}
