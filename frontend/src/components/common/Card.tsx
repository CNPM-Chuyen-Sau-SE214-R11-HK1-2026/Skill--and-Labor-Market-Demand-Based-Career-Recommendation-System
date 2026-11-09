import type { ReactNode } from 'react';
import styles from './Card.module.css';

export interface CardProps {
  children: ReactNode;
  className?: string;
  as?: 'article' | 'section' | 'div';
}

export function Card({ children, className, as = 'div' }: CardProps) {
  const classNames = [styles.card, className].filter(Boolean).join(' ');
  if (as === 'article') return <article className={classNames}>{children}</article>;
  if (as === 'section') return <section className={classNames}>{children}</section>;
  return <div className={classNames}>{children}</div>;
}

export default Card;
