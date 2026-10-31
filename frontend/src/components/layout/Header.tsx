import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import styles from './Header.module.css';

export const Header: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <header className={styles.header}>
      <div className={`container ${styles.headerContainer}`}>
        <Link to="/" className={styles.brand}>
          <span className={styles.brandAccent}>Career</span>Rec
        </Link>

        <nav className={styles.nav}>
          <Link to="/" className={styles.navLink}>
            Trang chủ
          </Link>
          {isAuthenticated && (
            <Link to="/dashboard" className={styles.navLink}>
              Dashboard
            </Link>
          )}
        </nav>

        <div className={styles.authSection}>
          {isAuthenticated && user ? (
            <div className={styles.userSection}>
              <span className={styles.userName}>
                Xin chào, <strong>{user.fullName}</strong>
              </span>
              <button onClick={logout} className={styles.logoutBtn}>
                Đăng xuất
              </button>
            </div>
          ) : (
            <div className={styles.guestSection}>
              <Link to="/login" className={styles.loginBtn}>
                Đăng nhập
              </Link>
              <Link to="/register" className={styles.registerBtn}>
                Đăng ký
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
