import { Link } from 'react-router-dom';
import type { OccupationSummary } from '../types/occupation.types';
import { formatPercent, formatVndRange } from '../utils/formatters';

const number = new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 2 });

export function OccupationCard({ occupation }: { occupation: OccupationSummary }) {
  const { id, code, title, groupName, demandScore, growthRatePct, averageSalaryMin, averageSalaryMax } = occupation;

  return (
    <Link className="occupation-card" to={`/occupations/${id}`} aria-label={`Xem chi tiết ${title}`}>
      <div className="occupation-card__top">
        <span className="occupation-code">{code}</span>
        {demandScore !== null && <span className="occupation-demand">Nhu cầu {number.format(demandScore)}/100</span>}
      </div>
      <div>
        <h3>{title}</h3>
        <p className="occupation-card__group">{groupName}</p>
      </div>
      <div className="occupation-card__salary">
        <span>Mức lương tham chiếu</span>
        <strong>{formatVndRange(averageSalaryMin, averageSalaryMax)}</strong>
      </div>
      <div className="occupation-card__bottom">
        {growthRatePct !== null && <span>Tăng trưởng {formatPercent(growthRatePct)}</span>}
        <span className="text-link">Xem chi tiết <span aria-hidden="true">→</span></span>
      </div>
    </Link>
  );
}
