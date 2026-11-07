import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import styles from './HomePage.module.css';

export const HomePage: React.FC = () => {
  const { isAuthenticated, user } = useAuth();

  return (
    <div className={`container ${styles.homeContainer}`}>
      <section className={styles.hero}>
        <span className={styles.badge}>Hệ thống gợi ý nghề nghiệp thông minh</span>
        <h1 className={styles.title}>
          Định hướng nghề nghiệp dựa trên <span className={styles.highlight}>Kỹ năng cá nhân</span> &amp; <span className={styles.highlight}>Nhu cầu thị trường</span>
        </h1>
        <p className={styles.description}>
          Kết nối hồ sơ năng lực của ứng viên với từ điển nghề nghiệp chuẩn O*NET / ESCO và dữ liệu thị trường lao động (LMI) theo thời gian thực.
        </p>

        <div className={styles.actions}>
          {isAuthenticated ? (
            <Link to="/dashboard" className={styles.primaryBtn}>
              Vào bảng điều khiển ({user?.fullName}) &rarr;
            </Link>
          ) : (
            <>
              <Link to="/register" className={styles.primaryBtn}>
                Bắt đầu ngay — Đăng ký ứng viên
              </Link>
              <Link to="/login" className={styles.secondaryBtn}>
                Đăng nhập tài khoản
              </Link>
            </>
          )}
        </div>
      </section>

      <section className={styles.features}>
        <div className={styles.featureCard}>
          <div className={styles.featureIcon}>🎯</div>
          <h3 className={styles.featureTitle}>Đối sánh kỹ năng lõi</h3>
          <p className={styles.featureDesc}>
            Phân tích mức độ phù hợp giữa kỹ năng tự khai báo và yêu cầu nghề nghiệp chuẩn hóa.
          </p>
        </div>
        <div className={styles.featureCard}>
          <div className={styles.featureIcon}>📈</div>
          <h3 className={styles.featureTitle}>Chỉ số thị trường (LMI)</h3>
          <p className={styles.featureDesc}>
            Cập nhật xu hướng tăng trưởng việc làm, mức lương tham chiếu và mức độ khan hiếm nhân lực.
          </p>
        </div>
        <div className={styles.featureCard}>
          <div className={styles.featureIcon}>🔍</div>
          <h3 className={styles.featureTitle}>Chẩn đoán khoảng cách</h3>
          <p className={styles.featureDesc}>
            Xác định rõ các kỹ năng còn thiếu (&Delta;) và đề xuất lộ trình học tập bổ trợ phù hợp.
          </p>
        </div>
      </section>
    </div>
  );
};
