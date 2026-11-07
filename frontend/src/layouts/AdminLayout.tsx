import React from 'react';
import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import styles from './AdminLayout.module.css';

/**
 * Dedicated administrative layout with dark control header and role branding.
 */
export const AdminLayout: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <div className={styles.adminWrapper}>
      <header className={styles.adminHeader}>
        <div className={styles.headerContainer}>
          <div className={styles.brandGroup}>
            <Link to="/admin" className={styles.brand}>
              <span className={styles.brandAccent}>Career</span>Rec
            </Link>
            <span className={styles.adminBadge}>Admin Portal</span>
          </div>

          <nav className={styles.navGroup}>
            <Link to="/" className={styles.navLink}>
              Trang chủ
            </Link>
            <Link to="/occupations" className={styles.navLink}>
              Khám phá nghề nghiệp
            </Link>
            <Link to="/dashboard" className={styles.navLink}>
              Bảng điều khiển
            </Link>
          </nav>

          <div className={styles.userGroup}>
            <span className={styles.userName}>{user?.fullName}</span>
            <button onClick={logout} className={styles.logoutBtn} type="button">
              Đăng xuất
            </button>
          </div>
        </div>
      </header>
      <main className={styles.adminContent}>
        <Outlet />
      </main>
    </div>
  );
};

export default AdminLayout;
