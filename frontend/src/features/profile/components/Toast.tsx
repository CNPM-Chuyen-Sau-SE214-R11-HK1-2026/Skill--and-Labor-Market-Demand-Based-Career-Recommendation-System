import React from 'react';
import type { ToastMessage } from '../types/profile.types';
import styles from './Toast.module.css';

export interface ToastContainerProps {
  toasts: ToastMessage[];
  onDismiss: (id: string) => void;
}

export const ToastContainer: React.FC<ToastContainerProps> = ({ toasts, onDismiss }) => {
  if (toasts.length === 0) {
    return null;
  }

  return (
    <div
      className={styles.toastViewport}
      aria-live="polite"
      aria-atomic="true"
    >
      {toasts.map((toast) => {
        const isError = toast.type === 'error';
        return (
          <div
            key={toast.id}
            className={`${styles.toast} ${styles[toast.type]}`}
            role={isError ? 'alert' : 'status'}
            aria-live={isError ? 'assertive' : 'polite'}
          >
            <span className={styles.icon} aria-hidden="true">
              {toast.type === 'success' ? '✓' : toast.type === 'error' ? '!' : 'i'}
            </span>
            <span className={styles.message}>{toast.message}</span>
            <button
              type="button"
              className={styles.closeBtn}
              onClick={() => onDismiss(toast.id)}
              aria-label="Đóng thông báo"
            >
              ×
            </button>
          </div>
        );
      })}
    </div>
  );
};
