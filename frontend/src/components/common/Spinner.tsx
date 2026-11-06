import styles from './Spinner.module.css';

export interface SpinnerProps {
  label?: string;
  size?: 'small' | 'medium' | 'large';
}

export function Spinner({ label = 'Đang tải dữ liệu...', size = 'medium' }: SpinnerProps) {
  return (
    <div className={styles.wrapper} role="status" aria-live="polite">
      <span className={`${styles.spinner} ${styles[size]}`} aria-hidden="true" />
      <span>{label}</span>
    </div>
  );
}

export default Spinner;
