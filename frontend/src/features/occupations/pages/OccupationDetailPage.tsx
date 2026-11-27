import { Link, useParams } from 'react-router-dom';
import { Card } from '../../../components/common/Card';
import { LmiChart } from '../components/LmiChart';
import { useOccupationDetail } from '../hooks/useOccupationDetail';
import type { OccupationDetail, OccupationSkill } from '../types/occupation.types';
import '../occupationTheme.css';
import styles from './OccupationDetailPage.module.css';

const percent = new Intl.NumberFormat('vi-VN', { style: 'percent', maximumFractionDigits: 1 });

function salaryLabel({ min, max, currency }: OccupationDetail['salaryRange']) {
  const money = new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: currency?.trim() || 'VND',
    maximumFractionDigits: 0,
  });
  if (min === null) return max === null ? 'Chưa cập nhật' : `Đến ${money.format(max)}`;
  if (max === null) return `Từ ${money.format(min)}`;
  return `${money.format(min)} – ${money.format(max)}`;
}

function SkillGroup({ title, skills, core = false }: {
  title: string;
  skills: OccupationSkill[];
  core?: boolean;
}) {
  return (
    <section className={styles.skillGroup}>
      <h3>{title}</h3>
      {skills.length > 0 ? (
        <ul className={styles.skills}>
          {[...skills].sort((first, second) => (second.weight ?? -1) - (first.weight ?? -1)).map((skill) => (
            <li className={core ? styles.coreSkill : undefined} key={skill.skillId}>
              <div className={styles.skillInfo}>
                <strong>{skill.skillName}</strong>
                <span>{skill.weight === null ? 'Chưa có trọng số' : percent.format(skill.weight)}</span>
              </div>
              {skill.weight !== null && (
                <meter min={0} max={1} value={skill.weight} aria-label={`Trọng số ${skill.skillName}`} />
              )}
            </li>
          ))}
        </ul>
      ) : <p className={styles.muted}>Chưa có kỹ năng trong nhóm này.</p>}
    </section>
  );
}

function DetailContent({ id }: { id: number | null }) {
  const { state, retry } = useOccupationDetail(id);

  if (state.status === 'loading') {
    return (
      <div role="status" aria-busy="true" className={styles.loading}>
        <span>Đang tải thông tin nghề nghiệp…</span>
        <div className={styles.skeleton} aria-hidden="true" />
        <div className={styles.skeleton} aria-hidden="true" />
      </div>
    );
  }

  if (state.status === 'notFound') {
    return (
      <div className={styles.state}>
        <h1>404 · Không tìm thấy nghề nghiệp</h1>
        <p>Kiểm tra mã nghề hoặc chọn một nghề trong danh mục.</p>
      </div>
    );
  }

  if (state.status === 'error') {
    return (
      <div className={styles.state} role="alert">
        <h1>Không thể tải thông tin nghề nghiệp</h1>
        <p>{state.message}</p>
        <button type="button" onClick={retry}>Thử lại</button>
      </div>
    );
  }

  const { data } = state;
  const skills = data.skills ?? [];
  const required = skills.filter((skill) => skill.importanceType === 'ESSENTIAL' || skill.importanceType === 'CORE');
  const optional = skills.filter((skill) => skill.importanceType === 'OPTIONAL');
  const unclassified = skills.filter((skill) => !required.includes(skill) && !optional.includes(skill));

  return (
    <div className={styles.content}>
      <header className={styles.hero}>
        <div>
          <div className={styles.category}>
            <span>{data.groupName}</span>
            <span>{data.code}</span>
          </div>
          <h1>{data.title}</h1>
        </div>
        <div className={styles.salary}>
          <span>Mức lương tham chiếu</span>
          <strong>{data.salaryRange ? salaryLabel(data.salaryRange) : 'Chưa cập nhật'}</strong>
        </div>
      </header>
      <Card as="section" className={styles.description}>
        <h2>Mô tả công việc</h2>
        <p>{data.description?.trim() || 'Chưa có mô tả công việc.'}</p>
      </Card>
      <LmiChart indicators={data.marketIndicators} trend={data.marketTrend} />
      <Card as="section" className={styles.skillCard}>
        <h2>Kỹ năng yêu cầu</h2>
        <div className={styles.skillGroups}>
          <SkillGroup title="Kỹ năng cốt lõi · Bắt buộc" skills={required} core />
          <SkillGroup title="Kỹ năng bổ trợ · Tùy chọn" skills={optional} />
          {unclassified.length > 0 && <SkillGroup title="Kỹ năng chưa phân loại" skills={unclassified} />}
        </div>
      </Card>
    </div>
  );
}

export default function OccupationDetailPage() {
  const { id } = useParams<'id'>();
  const parsed = id && /^\d+$/.test(id) ? Number(id) : NaN;
  const occupationId = Number.isSafeInteger(parsed) && parsed > 0 ? parsed : null;

  return (
    <div className="occ-theme">
      <div className={styles.page}>
        <Link to="/occupations" className={styles.backLink}><span aria-hidden="true">←</span> Quay lại danh mục nghề nghiệp</Link>
        <DetailContent key={occupationId} id={occupationId} />
      </div>
    </div>
  );
}
