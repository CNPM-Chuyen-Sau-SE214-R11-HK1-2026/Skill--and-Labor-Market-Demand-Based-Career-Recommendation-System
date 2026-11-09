import type { ReactNode } from 'react';
import styles from './Badge.module.css';

export type BadgeTone = 'neutral' | 'success' | 'warning' | 'danger' | 'info';

export interface BadgeProps {
  children: ReactNode;
  tone?: BadgeTone;
  className?: string;
}

export function Badge({ children, tone = 'neutral', className }: BadgeProps) {
  const classNames = [styles.badge, styles[tone], className].filter(Boolean).join(' ');
  return <span className={classNames}>{children}</span>;
}

export default Badge;
