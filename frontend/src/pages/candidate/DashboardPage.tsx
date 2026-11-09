import React from 'react';
import { useAuth } from '../../hooks/useAuth';
import styles from './DashboardPage.module.css';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();

  return (
    <div className={`container ${styles.dashboardContainer}`}>
      <div className={styles.welcomeBanner}>
        <h1 className={styles.title}>Bảng điều khiển ứng viên</h1>
        <p className={styles.subtitle}>
          Chào mừng trở lại, <strong>{user?.fullName}</strong> ({user?.email})
        </p>
      </div>

      <div className={styles.infoGrid}>
        <div className={styles.infoCard}>
          <h3 className={styles.cardHeader}>Thông tin phiên làm việc</h3>
          <ul className={styles.detailList}>
            <li>
              <span>Mã người dùng (User ID):</span>
              <strong>{user?.id}</strong>
            </li>
            <li>
              <span>Email:</span>
              <strong>{user?.email}</strong>
            </li>
            <li>
              <span>Vai trò bảo mật (Role):</span>
              <span className={styles.roleBadge}>{user?.role}</span>
            </li>
            <li>
              <span>Trạng thái tài khoản:</span>
              <strong>{user?.status || 'ACTIVE'}</strong>
            </li>
          </ul>
        </div>

        <div className={styles.infoCard}>
          <h3 className={styles.cardHeader}>Trạng thái hệ thống</h3>
          <p className={styles.statusText}>
            Khung khởi tạo Frontend (Task W1-T2) đã thiết lập thành công.
          </p>
          <div className={styles.roadmapBox}>
            <p className={styles.roadmapTitle}>Lộ trình phát triển tiếp theo (Tuần 2):</p>
            <ul className={styles.roadmapList}>
              <li>Quản lý hồ sơ chi tiết ứng viên (W2-FE-CND-01)</li>
              <li>Bộ chọn kỹ năng cá nhân &amp; mức độ thành thạo (User Skills)</li>
              <li>Khám phá danh mục nghề nghiệp chuẩn O*NET (W2-FE-ADM-01)</li>
              <li>Bảng gợi ý nghề nghiệp có trọng số thị trường (Tuần 3)</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};
