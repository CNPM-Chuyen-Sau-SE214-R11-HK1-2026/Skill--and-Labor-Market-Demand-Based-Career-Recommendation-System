import React from 'react';
import { Link, NavLink, Outlet } from 'react-router-dom';
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

          <nav className={styles.navGroup} aria-label="Điều hướng quản trị">
            <NavLink end to="/admin" className={styles.navLink}>Tổng quan</NavLink>
            <NavLink to="/admin/lmi" className={styles.navLink}>LMI</NavLink>
            <NavLink to="/admin/taxonomy" className={styles.navLink}>Danh mục</NavLink>
            <NavLink to="/admin/users" className={styles.navLink}>Người dùng</NavLink>
            <Link to="/occupations" className={styles.navLink}>Tra cứu nghề</Link>
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
