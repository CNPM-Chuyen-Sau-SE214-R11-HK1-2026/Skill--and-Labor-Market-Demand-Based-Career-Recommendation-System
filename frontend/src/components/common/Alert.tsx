import React from 'react';
import styles from './Alert.module.css';

export interface AlertProps {
  type?: 'error' | 'success' | 'warning' | 'info';
  message: string;
  className?: string;
}

export const Alert: React.FC<AlertProps> = ({ type = 'error', message, className = '' }) => {
  if (!message) return null;

  return (
    <div className={`${styles.alert} ${styles[type]} ${className}`} role="alert">
      <span>{message}</span>
    </div>
  );
};
