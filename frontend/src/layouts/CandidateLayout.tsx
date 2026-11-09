import React from 'react';
import { Outlet } from 'react-router-dom';
import { Header } from '../components/layout/Header';
import { Footer } from '../components/layout/Footer';
import styles from './CandidateLayout.module.css';

/**
 * Primary layout for candidates and public browsing pages.
 * Integrates standard Header, responsive Main content area, and Footer.
 */
export const CandidateLayout: React.FC = () => {
  return (
    <div className={styles.layoutWrapper}>
      <Header />
      <main className={styles.mainContent}>
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};

export default CandidateLayout;
