import '../occupationTheme.css';
import '../occupations.css';
import { useEffect, useState } from 'react';
import { OccupationCard } from '../components/OccupationCard';
import { SearchBar } from '../components/SearchBar';
import { EmptyState, ErrorState, PaginationControl, PageShell, Spinner } from '../components/OccupationUi';
import { getOccupations } from '../services/occupationService';
import { getApiErrorMessage } from '../utils/apiError';
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
      .then((response) => {
        if (!controller.signal.aborted) setResult(response);
      })
      .catch((cause: unknown) => {
        if (!controller.signal.aborted) setError(getApiErrorMessage(cause));
      })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [submittedQuery, page, reloadKey]);

  function handleSearch() { setPage(0); setSubmittedQuery(query.trim()); }

  return <PageShell>
    <header className="explorer-hero">
      <div className="hero-copy">
        <h1>Khám phá nghề nghiệp</h1>
        <p>Tra cứu mức lương, kỹ năng cần có và nhu cầu tuyển dụng của từng nghề.</p>
      </div>
      <SearchBar value={query} onChange={setQuery} onSubmit={handleSearch} onClear={() => { setQuery(''); setSubmittedQuery(''); setPage(0); }} />
    </header>
    <section className="catalog-section">
      <div className="catalog-heading"><h2>Danh mục nghề nghiệp</h2>
        {!loading && !error && result && <span className="result-count" role="status">{result.totalElements.toLocaleString('vi-VN')} nghề nghiệp</span>}
      </div>
      {loading ? <Spinner label="Đang tải danh mục nghề nghiệp..." /> : error ? <ErrorState message={error} onRetry={() => setReloadKey((value) => value + 1)} /> :
        result && result.content.length > 0 ? <>
          <div className="occupation-grid">{result.content.map((occupation) => <OccupationCard key={occupation.id} occupation={occupation} />)}</div>
          <PaginationControl page={page} totalPages={result.totalPages} onChange={setPage} />
        </> : <EmptyState title="Không tìm thấy nghề nghiệp phù hợp" description="Thử thay đổi từ khóa tìm kiếm hoặc xóa bộ lọc để xem toàn bộ danh mục." />}
    </section>
  </PageShell>;
}
