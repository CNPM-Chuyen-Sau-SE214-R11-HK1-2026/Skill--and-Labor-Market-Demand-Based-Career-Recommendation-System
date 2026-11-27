import { useRef, useState } from 'react';
import { adminApi, adminError } from './adminApi';
import { parseLmiBatch } from './model';
import type { LmiResult } from './model';
import styles from './Admin.module.css';
const example = JSON.stringify({ items: [{ occupationCode: 'SOC-15-1252', region: 'HCMC', demandScore: 80, openJobPostingsCount: 120, snapshotDate: '2026-11-01' }] }, null, 2);
export default function LmiPage() {
    const [raw, setRaw] = useState('');
    const [error, setError] = useState('');
    const [result, setResult] = useState<LmiResult | null>(null);
    const [busy, setBusy] = useState(false);
    const lock = useRef(false);
    async function submit() {
        if (lock.current)
            return;
        setError('');
        setResult(null);
        try {
            const { items } = parseLmiBatch(raw);
            if (!window.confirm(`Nạp ${items.length} bản ghi vào cơ sở dữ liệu thật?`))
                return;
            lock.current = true;
            setBusy(true);
            setResult(await adminApi.importLmi(items));
        }
        catch (e) {
            setError(adminError(e));
        }
        finally {
            lock.current = false;
            setBusy(false);
        }
    }
    return <main className={styles.page}><header className={styles.heading}><span className={styles.eyebrow}>DỮ LIỆU THỊ TRƯỜNG</span><h1>Nhập chỉ số LMI</h1><p>Nạp theo lô tối đa 500 bản ghi. Máy chủ kiểm tra và cập nhật nguyên tử theo nghề, khu vực, ngày thống kê.</p></header>
    <form className={styles.panel} onSubmit={e => { e.preventDefault(); void submit(); }}><div className={styles.toolbar}><button type="button" className={styles.secondary} disabled={busy} onClick={() => { setRaw(example); setResult(null); setError(''); }}>Chèn JSON mẫu</button><label className={styles.field}>Hoặc chọn tệp JSON<input type="file" accept=".json,application/json" disabled={busy} onChange={async (e) => { const file = e.target.files?.[0]; if (!file)
        return; setResult(null); setError(''); try {
        if (file.size > 2000000)
            throw new Error('Tệp tối đa 2 MB.');
        setRaw(await file.text());
    }
    catch (err) {
        setError(adminError(err));
    } e.target.value = ''; }}/></label></div>
    <label className={styles.field}>Nội dung JSON<textarea required value={raw} disabled={busy} onChange={e => { setRaw(e.target.value); setResult(null); setError(''); }} placeholder={'{"items": [...]}'}/></label><p className={styles.muted}>Mẫu chỉ minh họa cấu trúc; hãy kiểm tra mã nghề và thay bằng số liệu đã được duyệt trước khi nạp.</p><button className={styles.button} disabled={busy} type="submit">{busy ? 'Đang nạp…' : 'Kiểm tra và nạp dữ liệu'}</button></form>
    {error && <div role="alert" className={styles.error}>{error}</div>}{result && <section className={styles.success} role="status"><strong>Nạp thành công</strong><p>Đã xử lý: {result.totalRecordsProcessed} · Thêm mới: {result.recordsInserted} · Cập nhật: {result.recordsUpdated}</p><small>{result.timestamp}</small></section>}</main>;
}
