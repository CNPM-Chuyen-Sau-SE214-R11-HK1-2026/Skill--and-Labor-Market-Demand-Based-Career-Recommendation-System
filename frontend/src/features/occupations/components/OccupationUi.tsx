import type { ReactNode } from 'react';

export function Spinner({ label = 'Đang tải dữ liệu...' }: { label?: string }) {
  return <div className="state-panel" role="status"><span className="spinner" /><span>{label}</span></div>;
}
export function EmptyState({ title, description }: { title: string; description: string }) {
  return <div className="state-panel"><span className="state-icon">⌕</span><strong>{title}</strong><span>{description}</span></div>;
}
export function ErrorState({ message, onRetry }: { message: string; onRetry: () => void }) {
  return <div className="state-panel state-panel--error" role="alert"><strong>Không thể tải dữ liệu</strong><span>{message}</span><button className="button button--secondary" onClick={onRetry}>Thử lại</button></div>;
}
export function PaginationControl({ page, totalPages, onChange }: { page: number; totalPages: number; onChange: (page: number) => void }) {
  if (totalPages <= 1) return null;
  return <nav className="pagination" aria-label="Phân trang">
    <button className="page-button" disabled={page <= 0} onClick={() => onChange(page - 1)} aria-label="Trang trước">←</button>
    {Array.from({ length: totalPages }, (_, index) => index).slice(Math.max(0, Math.min(page - 2, totalPages - 5)), Math.max(5, Math.min(page - 2, totalPages - 5)) + 5).map((item) =>
      <button key={item} className={`page-button ${item === page ? 'is-active' : ''}`} aria-current={item === page ? 'page' : undefined} onClick={() => onChange(item)}>{item + 1}</button>)}
    <button className="page-button" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)} aria-label="Trang sau">→</button>
  </nav>;
}
export function PageShell({ children }: { children: ReactNode }) { return <main className="explorer-page"><div className="explorer-container">{children}</div></main>; }
