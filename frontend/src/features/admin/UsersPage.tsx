import { useEffect, useRef, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { adminApi, adminError, demoMode } from './adminApi';
import type { AdminUser, Stats } from './model';
import type { PageResponse } from '../occupations/types/occupation.types';
import styles from './Admin.module.css';
const initialUsers: AdminUser[] = [
    { id: 900001, fullName: 'Quản trị viên mẫu', email: 'demo-admin@example.invalid', role: 'ROLE_ADMIN', active: true },
    { id: 900002, fullName: 'Ứng viên mẫu A', email: 'candidate-a@example.invalid', role: 'ROLE_CANDIDATE', active: true },
    { id: 900003, fullName: 'Ứng viên mẫu B', email: 'candidate-b@example.invalid', role: 'ROLE_CANDIDATE', active: false },
];
const metrics: [
    keyof Stats,
    string
][] = [['totalUsers', 'Tài khoản'], ['totalProfiles', 'Hồ sơ'], ['totalOccupations', 'Nghề nghiệp'], ['totalSkills', 'Kỹ năng'], ['totalLmiRecords', 'Bản ghi LMI'], ['totalRecommendationLogs', 'Lượt gợi ý']];
export default function UsersPage() {
    const { user } = useAuth();
    const [demoUsers, setDemoUsers] = useState(initialUsers);
    const [query, setQuery] = useState('');
    const [role, setRole] = useState('');
    const [active, setActive] = useState('');
    const [page, setPage] = useState(0);
    const [version, setVersion] = useState(0);
    const [data, setData] = useState<PageResponse<AdminUser> | null>(null);
    const [stats, setStats] = useState<Stats | null>(null);
    const [statsError, setStatsError] = useState('');
    const [error, setError] = useState('');
    const [actionError, setActionError] = useState('');
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const lock = useRef(false);
    useEffect(() => {
        const controller = new AbortController();
        setLoading(true);
        setError('');
        setData(null);
        const load = async () => {
            try {
                let response: PageResponse<AdminUser>;
                if (demoMode) {
                    const filtered = demoUsers.filter(u => u.email.toLowerCase().includes(query.toLowerCase()) && (!role || u.role === role) && (!active || String(u.active) === active));
                    response = { content: filtered.slice(page * 10, page * 10 + 10), pageNumber: page, pageSize: 10, totalElements: filtered.length, totalPages: Math.ceil(filtered.length / 10), last: (page + 1) * 10 >= filtered.length };
                }
                else
                    response = await adminApi.users({ query: query.trim(), role, active, page, size: 10 }, controller.signal);
                if (controller.signal.aborted)
                    return;
                if (page > 0 && response.content.length === 0) {
                    setPage(page - 1);
                    return;
                }
                setData(response);
            }
            catch (err) {
                if (!controller.signal.aborted)
                    setError(adminError(err));
            }
            finally {
                if (!controller.signal.aborted)
                    setLoading(false);
            }
        };
        const timer = window.setTimeout(() => void load(), 250);
        return () => { window.clearTimeout(timer); controller.abort(); };
    }, [query, role, active, page, version, demoUsers]);
    useEffect(() => { const controller = new AbortController(); setStats(null); setStatsError(''); if (demoMode)
        setStats({ totalUsers: demoUsers.length, totalProfiles: 2, totalOccupations: 0, totalSkills: 0, totalLmiRecords: 0, totalRecommendationLogs: 0 });
    else
        adminApi.stats(controller.signal).then(s => { if (!controller.signal.aborted)
            setStats(s); }).catch(e => { if (!controller.signal.aborted)
            setStatsError(adminError(e)); }); return () => controller.abort(); }, [version, demoUsers]);
    async function toggle(target: AdminUser) {
        if (lock.current || target.id === user?.id || (demoMode && target.role === 'ROLE_ADMIN') || typeof target.active !== 'boolean')
            return;
        if (!window.confirm(`${target.active ? 'Vô hiệu hóa' : 'Kích hoạt'} ${target.email}?`))
            return;
        lock.current = true;
        setBusy(true);
        setActionError('');
        setMessage('');
        try {
            if (demoMode)
                setDemoUsers(prev => prev.map(u => u.id === target.id ? { ...u, active: !u.active } : u));
            else
                await adminApi.status(target.id, !target.active);
            setMessage(demoMode ? 'Đã cập nhật dữ liệu demo.' : 'Đã cập nhật trạng thái.');
            setVersion(v => v + 1);
        }
        catch (e) {
            setActionError(adminError(e));
        }
        finally {
            lock.current = false;
            setBusy(false);
        }
    }
    return <main className={styles.page}><header className={styles.heading}><span className={styles.eyebrow}>QUẢN TRỊ TÀI KHOẢN</span><h1>Người dùng & thống kê</h1><p>Tra cứu tài khoản và kiểm soát trạng thái truy cập hệ thống.</p></header>
    {demoMode && <div className={styles.notice} role="status">DEMO — dữ liệu mẫu trong bộ nhớ, không lưu vào máy chủ.</div>}
    <div className={styles.grid}>{metrics.map(([key, label]) => <div key={key} className={styles.metric}><span>{label}</span><strong>{typeof stats?.[key] === 'number' ? stats[key].toLocaleString('vi-VN') : '—'}</strong></div>)}</div>{statsError && <p role="alert" className={styles.error}>Thống kê: {statsError}</p>}
    <section className={styles.panel}><div className={styles.toolbar}><label className={styles.field}>Email<input value={query} onChange={e => { setQuery(e.target.value); setPage(0); }} placeholder="Tìm email"/></label><label className={styles.field}>Vai trò<select value={role} onChange={e => { setRole(e.target.value); setPage(0); }}><option value="">Tất cả</option><option value="ROLE_ADMIN">Quản trị</option><option value="ROLE_CANDIDATE">Ứng viên</option></select></label><label className={styles.field}>Trạng thái<select value={active} onChange={e => { setActive(e.target.value); setPage(0); }}><option value="">Tất cả</option><option value="true">Hoạt động</option><option value="false">Vô hiệu hóa</option></select></label><button className={styles.secondary} onClick={() => setVersion(v => v + 1)} disabled={busy}>Tải lại</button></div>
    {actionError && <p className={styles.error} role="alert">{actionError}</p>}{message && <p className={styles.success} role="status">{message}</p>}
    {loading ? <p role="status">Đang tải…</p> : error ? <p className={styles.error} role="alert">{error}</p> : data && (data.content.length ? <><div className={styles.tableWrap}><table className={styles.table}><thead><tr><th>Họ tên / Email</th><th>Vai trò</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>{data.content.map(u => <tr key={u.id}><td><strong>{u.fullName}</strong><br />{u.email}</td><td>{u.role === 'ROLE_ADMIN' ? 'Quản trị' : 'Ứng viên'}</td><td>{typeof u.active !== 'boolean' ? 'Chưa xác định' : u.active ? 'Hoạt động' : 'Vô hiệu hóa'}</td><td><button className={styles.secondary} disabled={busy || u.id === user?.id || (demoMode && u.role === 'ROLE_ADMIN') || typeof u.active !== 'boolean'} onClick={() => void toggle(u)}>{u.active ? 'Vô hiệu hóa' : 'Kích hoạt'}</button></td></tr>)}</tbody></table></div><div className={styles.pager}><button className={styles.secondary} disabled={page === 0 || busy} onClick={() => setPage(p => p - 1)}>Trước</button><span>Trang {page + 1}/{data.totalPages} · {data.totalElements} tài khoản</span><button className={styles.secondary} disabled={data.last || busy} onClick={() => setPage(p => p + 1)}>Sau</button></div></> : <p>Không có tài khoản phù hợp.</p>)}</section></main>;
}
