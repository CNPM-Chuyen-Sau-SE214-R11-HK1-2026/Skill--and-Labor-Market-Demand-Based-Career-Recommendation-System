import { Card } from '../../components/common/Card';
import styles from './AdminDashboardStub.module.css';

/** UI placeholder only. Admin route and protected layout remain under W2-T4 / later admin scope. */
export function AdminDashboardStub() {
  return (
    <main className={styles.page}>
      <span className={styles.eyebrow}>ADMINISTRATION</span>
      <Card className={styles.card}>
        <span className={styles.icon} aria-hidden="true">⌘</span>
        <h1>Khu vực quản trị</h1>
        <p>Khung giao diện quản trị đã được chuẩn bị. Các chức năng quản lý người dùng và nạp dữ liệu LMI sẽ được triển khai theo backlog của phân hệ Admin.</p>
        <span className={styles.status}>UI placeholder · chưa kết nối nghiệp vụ</span>
      </Card>
    </main>
  );
}

export default AdminDashboardStub;
