import styles from './SearchBar.module.css';

export interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  label?: string;
}

export function SearchBar({
  value,
  onChange,
  placeholder = 'Tìm theo tên nghề nghiệp hoặc mã nghề...',
  label = 'Tìm kiếm nghề nghiệp',
}: SearchBarProps) {
  return (
    <label className={styles.wrapper}>
      <span className={styles.icon} aria-hidden="true">
        <svg viewBox="0 0 24 24" role="presentation">
          <circle cx="10.8" cy="10.8" r="6.8" />
          <path d="m16 16 4.2 4.2" />
        </svg>
      </span>
      <span className={styles.srOnly}>{label}</span>
      <input
        className={styles.input}
        type="search"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        autoComplete="off"
        spellCheck={false}
      />
      {value ? (
        <button
          className={styles.clearButton}
          type="button"
          onClick={() => onChange('')}
          aria-label="Xóa nội dung tìm kiếm"
        >
          ×
        </button>
      ) : null}
    </label>
  );
}

export default SearchBar;
