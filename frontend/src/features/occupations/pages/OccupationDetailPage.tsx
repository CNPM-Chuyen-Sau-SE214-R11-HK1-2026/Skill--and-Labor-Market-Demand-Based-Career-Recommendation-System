import '../occupations.css';
import { useEffect, useState } from 'react';
import { OccupationChart } from '../components/OccupationChart';
import { EmptyState, ErrorState, PageShell, Spinner } from '../components/OccupationUi';
import { getOccupationDetail } from '../services/occupationService';
import type { OccupationDetail } from '../types/occupation.types';

const money = (value: number | null): string => value === null ? 'Đang cập nhật' : `${new Intl.NumberFormat('vi-VN').format(value)} ₫`;

export default function OccupationDetailPage() {
  const occupationId = Number(window.location.pathname.split('/').filter(Boolean).slice(-1)[0]);
  const [data, setData] = useState<OccupationDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!Number.isSafeInteger(occupationId) || occupationId <= 0) { setError('Mã nghề nghiệp không hợp lệ.'); setLoading(false); return; }
    const controller = new AbortController();
    setLoading(true); setError('');
    getOccupationDetail(occupationId, controller.signal)
      .then(setData)
      .catch((cause: unknown) => {
        if (cause instanceof Error && cause.name === 'AbortError') return;
        setError(cause instanceof Error ? cause.message : 'Đã xảy ra lỗi không xác định.');
      })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [occupationId]);

  if (loading) return <PageShell><Spinner label="Đang tải thông tin nghề nghiệp..." /></PageShell>;
  if (error) return <PageShell><ErrorState message={error} onRetry={() => { setError(''); setLoading(true); getOccupationDetail(occupationId).then(setData).catch((cause: unknown) => setError(cause instanceof Error ? cause.message : 'Không thể tải dữ liệu.')).finally(() => setLoading(false)); }} /></PageShell>;
  if (!data) return <PageShell><EmptyState title="Không tìm thấy nghề nghiệp" description="Nghề nghiệp này không tồn tại hoặc đã bị gỡ khỏi danh mục." /></PageShell>;

  const market = data.marketIndicators;
  return <PageShell>
    <a className="back-link" href="/occupations">← Quay lại danh mục nghề nghiệp</a>
    <header className="detail-hero">
      <span className="eyebrow">{data.code} · {data.groupName}</span>
      <h1>{data.title}</h1>
      <p>{data.description || 'Chưa có mô tả chi tiết cho nghề nghiệp này.'}</p>
      <div className="detail-salary"><span>Mức lương tham khảo</span><strong>{money(data.salaryRange?.min ?? null)} – {money(data.salaryRange?.max ?? null)}</strong><small>{data.salaryRange?.currency || 'VND'}</small></div>
    </header>
    <OccupationChart demandScore={market?.demandScore ?? null} growthRatePct={market?.growthRatePct ?? null} shortageLevel={market?.shortageLevel ?? null} openingsCount={market?.openingsCount ?? null} />
    <section className="skills-panel">
      <div className="section-heading"><div><span className="eyebrow">SKILL REQUIREMENTS</span><h2>Kỹ năng cần có</h2></div><span className="result-count">{data.skills?.length ?? 0} kỹ năng</span></div>
      {data.skills?.length ? <div className="skills-list">{data.skills.map((skill) => <article className="skill-row" key={skill.skillId}><div><strong>{skill.skillName}</strong><span>{skill.skillCode}</span></div><div className="skill-meta"><span className={`skill-tag ${skill.importanceType === 'ESSENTIAL' ? 'skill-tag--essential' : ''}`}>{skill.importanceType === 'ESSENTIAL' ? 'Cốt lõi' : 'Bổ trợ'}</span>{skill.weight !== null && <span className="skill-weight">Trọng số {skill.weight}</span>}</div></article>)}</div> : <EmptyState title="Chưa có dữ liệu kỹ năng" description="Danh sách kỹ năng yêu cầu sẽ xuất hiện khi dữ liệu được cập nhật." />}
    </section>
    <p className="data-source">Kỳ dữ liệu: {market?.dataPeriod || 'Chưa cập nhật'}{market?.sourceName ? ` · Nguồn: ${market.sourceName}` : ''}</p>
  </PageShell>;
}
