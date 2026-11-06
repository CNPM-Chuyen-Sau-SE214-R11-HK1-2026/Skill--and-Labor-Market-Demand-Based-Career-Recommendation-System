import { useState } from 'react';
import { Badge } from '../../../components/common/Badge';
import { PaginationControl } from '../../../components/common/PaginationControl';
import { SearchBar } from '../../../components/common/SearchBar';
import { Spinner } from '../../../components/common/Spinner';
import { OccupationCard } from '../components/OccupationCard';
import { useDebouncedValue } from '../hooks/useDebouncedValue';
import { useOccupations } from '../hooks/useOccupations';
import { isOccupationMockModeEnabled } from '../services/occupationService';
import styles from './OccupationsPage.module.css';

const PAGE_SIZE = 9;

export function OccupationsPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(0);
  const query = useDebouncedValue(searchTerm.trim(), 300);
  const { data, loading, error, retry } = useOccupations({ query, page, size: PAGE_SIZE });

  const handleSearchChange = (value: string) => {
    setSearchTerm(value);
    setPage(0);
  };

  return (
    <main className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroContent}>
          <span className={styles.eyebrow}>CAREER EXPLORER · TAXONOMY</span>
          <h1>Khám phá con đường<br /><em>nghề nghiệp phù hợp</em></h1>
          <p className={styles.intro}>
            Tra cứu vai trò nghề nghiệp, kỹ năng cốt lõi và tín hiệu nhu cầu tuyển dụng để hiểu rõ hơn về thị trường lao động.
          </p>
          <SearchBar value={searchTerm} onChange={handleSearchChange} />
          <div className={styles.heroFootnote}>
            <span className={styles.pulse} aria-hidden="true" />
            <span>Tìm theo tên nghề, nhóm ngành hoặc mã nghề</span>
          </div>
        </div>
        <div className={styles.heroVisual} aria-hidden="true">
          <div className={`${styles.orbit} ${styles.orbitOne}`} />
          <div className={`${styles.orbit} ${styles.orbitTwo}`} />
          <div className={`${styles.orbit} ${styles.orbitThree}`} />
          <div className={styles.visualCore}><span>CAREER</span><strong>MAP</strong></div>
          <span className={`${styles.node} ${styles.nodeOne}`}>DATA</span>
          <span className={`${styles.node} ${styles.nodeTwo}`}>AI</span>
          <span className={`${styles.node} ${styles.nodeThree}`}>DEV</span>
        </div>
      </section>

      {isOccupationMockModeEnabled() ? (
        <div className={styles.mockNotice} role="status">
          <Badge tone="warning">DEMO MODE</Badge>
          <span>Đang sử dụng dữ liệu mô phỏng để phát triển giao diện. Không dùng các chỉ số này làm dữ liệu thị trường thực tế.</span>
        </div>
      ) : null}

      <section className={styles.resultsSection} aria-labelledby="occupations-heading">
        <div className={styles.sectionHeading}>
          <div>
            <span className={styles.sectionEyebrow}>OCCUPATION DIRECTORY</span>
            <h2 id="occupations-heading">Danh mục nghề nghiệp</h2>
            <p>Khám phá vai trò, mức lương tham khảo và tín hiệu thị trường.</p>
          </div>
          {data ? <span className={styles.resultCount}>{data.totalElements.toLocaleString('vi-VN')} kết quả</span> : null}
        </div>

        {loading ? (
          <Spinner label="Đang tải danh mục nghề nghiệp..." size="large" />
        ) : error ? (
          <div className={styles.statePanel} role="alert">
            <div className={styles.stateIcon} aria-hidden="true">!</div>
            <h3>Chưa thể tải danh mục</h3>
            <p>{error}</p>
            <button className={styles.retryButton} type="button" onClick={retry}>Thử tải lại</button>
          </div>
        ) : data && data.content.length > 0 ? (
          <>
            <div className={styles.grid}>
              {data.content.map((occupation) => (
                <OccupationCard key={occupation.id} occupation={occupation} />
              ))}
            </div>
            <PaginationControl
              pageNumber={data.pageNumber}
              pageSize={data.pageSize}
              totalElements={data.totalElements}
              totalPages={data.totalPages}
              onPageChange={setPage}
            />
          </>
        ) : (
          <div className={styles.statePanel}>
            <div className={styles.stateIcon} aria-hidden="true">⌕</div>
            <h3>Không tìm thấy nghề nghiệp phù hợp</h3>
            <p>Hãy thử từ khóa khác hoặc xóa bộ lọc tìm kiếm.</p>
            <button className={styles.retryButton} type="button" onClick={() => handleSearchChange('')}>Xóa tìm kiếm</button>
          </div>
        )}
      </section>
    </main>
  );
}

export default OccupationsPage;
