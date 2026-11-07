import React from 'react';
import styles from './Footer.module.css';

export const Footer: React.FC = () => {
  return (
    <footer className={styles.footer}>
      <div className={`container ${styles.footerContainer}`}>
        <p className={styles.copyright}>
          &copy; {new Date().getFullYear()} Skill- & Labor Market Demand–Based Career Recommendation System.
        </p>
        <p className={styles.subtext}>Đồ án môn học CNPM Chuyên sâu — Nhóm 4</p>
      </div>
    </footer>
  );
};
