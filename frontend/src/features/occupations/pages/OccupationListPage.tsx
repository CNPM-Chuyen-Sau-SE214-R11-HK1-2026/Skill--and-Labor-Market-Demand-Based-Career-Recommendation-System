import '../occupations.css';
import { useEffect, useState } from 'react';
import { OccupationCard } from '../components/OccupationCard';
import { SearchBar } from '../components/SearchBar';
import { EmptyState, ErrorState, PaginationControl, PageShell, Spinner } from '../components/OccupationUi';
import { getOccupations } from '../services/occupationService';
import type { OccupationSummary, PageResponse } from '../types/occupation.types';

const PAGE_SIZE = 9;

export default function OccupationListPage() {
  const [query, setQuery] = useState('');
  const [submittedQuery, setSubmittedQuery] = useState('');
  const [page, setPage] = useState(0);
  const [result, setResult] = useState<PageResponse<OccupationSummary> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true); setError('');
    getOccupations({ query: submittedQuery, page, size: PAGE_SIZE }, controller.signal)
      .then(setResult)
      .catch((cause: unknown) => {
        if (cause instanceof Error && cause.name === 'AbortError') return;
        setError(cause instanceof Error ? cause.message : 'Đã xảy ra lỗi không xác định.');
      })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [submittedQuery, page, reloadKey]);

  function handleSearch() { setPage(0); setSubmittedQuery(query.trim()); }

  return <PageShell>
    <header className="explorer-hero">
      <div className="hero-copy">
        <span className="eyebrow">CAREER EXPLORER · LABOR MARKET DATA</span>
        <h1>Khám phá con đường <em>nghề nghiệp</em> của bạn</h1>
        <p>Khám phá các nhóm nghề, mức lương tham khảo và tín hiệu nhu cầu nhân lực để đưa ra lựa chọn phát triển phù hợp.</p>
        <SearchBar value={query} onChange={setQuery} onSubmit={handleSearch} onClear={() => { setQuery(''); setSubmittedQuery(''); setPage(0); }} />
      </div>
      <div className="hero-art" aria-hidden="true"><div className="hero-orbit hero-orbit--one" /><div className="hero-orbit hero-orbit--two" /><div className="hero-glass"><span>CAREER</span><strong>01</strong><small>Find your direction</small></div><span className="hero-spark hero-spark--one">✳</span><span className="hero-spark hero-spark--two">✦</span></div>
    </header>
    <section className="catalog-section">
      <div className="catalog-heading"><div><span className="eyebrow">THE OPPORTUNITY INDEX</span><h2>Danh mục nghề nghiệp</h2><p>Những lựa chọn nghề nghiệp được tổng hợp từ dữ liệu thị trường.</p></div>
        {result && <span className="result-count">{result.totalElements.toLocaleString('vi-VN')} nghề nghiệp</span>}
      </div>
      {loading ? <Spinner label="Đang tải danh mục nghề nghiệp..." /> : error ? <ErrorState message={error} onRetry={() => setReloadKey((value) => value + 1)} /> :
        result && result.content.length > 0 ? <>
          <div className="occupation-grid">{result.content.map((occupation) => <OccupationCard key={occupation.id} occupation={occupation} />)}</div>
          <PaginationControl page={page} totalPages={result.totalPages} onChange={setPage} />
        </> : <EmptyState title="Không tìm thấy nghề nghiệp phù hợp" description="Thử thay đổi từ khóa tìm kiếm hoặc xóa bộ lọc để xem toàn bộ danh mục." />}
    </section>
    <footer className="explorer-footer"><span>CAREER PATH · LABOR MARKET INSIGHTS</span><span>Dữ liệu hiển thị theo nguồn cung cấp của hệ thống</span></footer>
  </PageShell>;
}
