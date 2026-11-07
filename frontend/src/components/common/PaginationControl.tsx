import styles from './PaginationControl.module.css';

export interface PaginationControlProps {
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function PaginationControl({
  pageNumber,
  pageSize,
  totalElements,
  totalPages,
  onPageChange,
}: PaginationControlProps) {
  const safeTotalPages = Math.max(0, totalPages);
  const start = totalElements === 0 ? 0 : pageNumber * pageSize + 1;
  const end = Math.min(totalElements, (pageNumber + 1) * pageSize);
  const canGoPrevious = pageNumber > 0;
  const canGoNext = pageNumber + 1 < safeTotalPages;

  if (totalElements === 0 || safeTotalPages <= 1) return null;

  return (
    <nav className={styles.wrapper} aria-label="Phân trang danh mục nghề nghiệp">
      <span className={styles.summary}>
        Hiển thị <strong>{start}–{end}</strong> trong <strong>{totalElements}</strong> nghề nghiệp
      </span>
      <div className={styles.actions}>
        <button
          className={styles.button}
          type="button"
          onClick={() => onPageChange(pageNumber - 1)}
          disabled={!canGoPrevious}
        >
          <span aria-hidden="true">‹</span> Trang trước
        </button>
        <span className={styles.pageIndicator} aria-live="polite">
          {pageNumber + 1} / {safeTotalPages}
        </span>
        <button
          className={styles.button}
          type="button"
          onClick={() => onPageChange(pageNumber + 1)}
          disabled={!canGoNext}
        >
          Trang sau <span aria-hidden="true">›</span>
        </button>
      </div>
    </nav>
  );
}

export default PaginationControl;
