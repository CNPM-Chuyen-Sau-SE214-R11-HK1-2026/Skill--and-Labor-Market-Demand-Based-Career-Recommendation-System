import { Badge } from './Badge';
import { Card } from './Card';
import type { OccupationSummary } from '../types/occupation.types';

interface OccupationCardProps { occupation: OccupationSummary; }

const money = (value: number | null): string => value === null ? 'Đang cập nhật' : `${new Intl.NumberFormat('vi-VN').format(value)} ₫`;

export function OccupationCard({ occupation }: OccupationCardProps) {
  return (
    <Card className="occupation-card">
      <a className="occupation-card__link" href={`/occupations/${occupation.id}`}>
      <div className="occupation-card__top">
        <span className="occupation-code">{occupation.code}</span>
        <Badge tone="success">{occupation.demandScore === null ? 'Chưa có điểm' : `Nhu cầu ${occupation.demandScore}/100`}</Badge>
      </div>
      <h3>{occupation.title}</h3>
      <p className="occupation-card__group">{occupation.groupName}</p>
      <div className="occupation-card__salary">
        <span>Mức lương tham khảo</span>
        <strong>{money(occupation.averageSalaryMin)} – {money(occupation.averageSalaryMax)}</strong>
      </div>
      <div className="occupation-card__bottom">
        <span>{occupation.growthRatePct === null ? 'Tăng trưởng: —' : `Tăng trưởng ${occupation.growthRatePct}%`}</span>
        <span className="text-link">Xem chi tiết <span aria-hidden="true">→</span></span>
      </div>
      </a>
    </Card>
  );
}
