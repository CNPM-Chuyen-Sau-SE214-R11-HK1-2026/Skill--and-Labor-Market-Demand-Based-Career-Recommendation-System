import { useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';
import { adminApi, adminError, demoMode } from './adminApi';
import type { CatalogItem, OccupationInput, SkillInput } from './model';
import styles from './Admin.module.css';
type Kind = 'occupation' | 'skill';
type Draft = {
    code: string;
    label: string;
    description: string;
    groupId: string;
    industry: string;
    entry: string;
    mid: string;
    skillType: SkillInput['skillType'];
};
const blank: Draft = { code: '', label: '', description: '', groupId: '', industry: '', entry: '', mid: '', skillType: 'TECHNICAL' };
const demoSeed: Record<Kind, CatalogItem[]> = { occupation: [{ id: 1, code: 'DEMO-OCC', label: 'Kỹ sư phần mềm (mẫu)', group: 'Công nghệ' }], skill: [{ id: 2, code: 'DEMO-SK', label: 'Java (mẫu)', group: 'Lập trình' }] };
export default function TaxonomyPage() {
    const [kind, setKind] = useState<Kind>('occupation');
    const [query, setQuery] = useState('');
    const [page, setPage] = useState(0);
    const [version, setVersion] = useState(0);
    const [rows, setRows] = useState<CatalogItem[]>([]);
    const [total, setTotal] = useState(0);
    const [pages, setPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [message, setMessage] = useState('');
    const [demo, setDemo] = useState(demoSeed);
    const [demoDrafts, setDemoDrafts] = useState<Record<string, Draft>>({});
    const nextDemoId = useRef(10);
    const [editing, setEditing] = useState<number | null>(null);
    const [draft, setDraft] = useState<Draft>(blank);
    const [modalError, setModalError] = useState('');
    const [busy, setBusy] = useState(false);
    const [preparing, setPreparing] = useState(false);
    const [editReady, setEditReady] = useState(false);
    const dialog = useRef<HTMLDialogElement>(null);
    const lock = useRef(false);
    const detailRequest = useRef<AbortController | null>(null);
    useEffect(() => () => detailRequest.current?.abort(), []);
    useEffect(() => {
        const controller = new AbortController();
        setLoading(true);
        setError('');
        setRows([]);
        const load = async () => {
            try {
                let items: CatalogItem[];
                let count: number;
                let pageCount: number;
                if (demoMode) {
                    const all = demo[kind].filter(x => `${x.code} ${x.label}`.toLowerCase().includes(query.toLowerCase()));
                    items = all.slice(page * 10, page * 10 + 10);
                    count = all.length;
                    pageCount = Math.ceil(count / 10);
                }
                else if (kind === 'occupation') {
                    const d = await adminApi.occupations(query.trim(), page, controller.signal);
                    items = d.content.map(x => ({ id: x.id, code: x.code, label: x.title, group: x.groupName }));
                    count = d.totalElements;
                    pageCount = d.totalPages;
                }
                else {
                    const d = await adminApi.skills(query.trim(), controller.signal);
                    const all = d.filter(x => `${x.code} ${x.name}`.toLowerCase().includes(query.trim().toLowerCase()));
                    items = all.slice(page * 10, page * 10 + 10).map(x => ({ id: x.id, code: x.code, label: x.name, group: x.categoryName }));
                    count = all.length;
                    pageCount = Math.ceil(count / 10);
                }
                if (controller.signal.aborted)
                    return;
                if (page > 0 && !items.length) {
                    setPage(page - 1);
                    return;
                }
                setRows(items);
                setTotal(count);
                setPages(pageCount);
            }
            catch (e) {
                if (!controller.signal.aborted)
                    setError(adminError(e));
            }
            finally {
                if (!controller.signal.aborted)
                    setLoading(false);
            }
        };
        const timer = window.setTimeout(() => void load(), 250);
        return () => { window.clearTimeout(timer); controller.abort(); };
    }, [kind, query, page, version, demo]);
    function open(row?: CatalogItem) {
        setEditing(row?.id ?? null);
        setDraft(row ? demoDrafts[`${kind}:${row.id}`] ?? { ...blank, code: row.code, label: row.label } : blank);
        setModalError('');
        setMessage('');
        dialog.current?.showModal();
        setEditReady(!row || demoMode);
        if (row && !demoMode) {
            const c = new AbortController();
            detailRequest.current = c;
            setPreparing(true);
            const load = async () => {
                try {
                    if (kind === 'occupation') {
                        const d = await adminApi.occupationForEdit(row.id, c.signal);
                        if (c.signal.aborted)
                            return;
                        if (typeof d.code !== 'string' || typeof d.title !== 'string' || !('description' in d) || !('groupId' in d) || !('industry' in d) || !('entrySalaryAvg' in d) || !('midSalaryAvg' in d))
                            throw new Error('API chưa cung cấp đủ các trường nghề để chỉnh sửa an toàn.');
                        setDraft({ ...blank, code: d.code, label: d.title, description: d.description ?? '', groupId: d.groupId?.toString() ?? '', industry: d.industry ?? '', entry: d.entrySalaryAvg?.toString() ?? '', mid: d.midSalaryAvg?.toString() ?? '' });
                    }
                    else {
                        const d = await adminApi.skillForEdit(row.id, c.signal);
                        if (c.signal.aborted)
                            return;
                        if (typeof d.code !== 'string' || typeof d.name !== 'string' || !('description' in d) || !('categoryId' in d) || !['TECHNICAL', 'SOFT', 'DOMAIN'].includes(d.skillType))
                            throw new Error('API chưa cung cấp đủ các trường kỹ năng để chỉnh sửa an toàn.');
                        setDraft({ ...blank, code: d.code, label: d.name, description: d.description ?? '', groupId: d.categoryId?.toString() ?? '', skillType: d.skillType });
                    }
                    setEditReady(true);
                }
                catch (e) {
                    if (!c.signal.aborted)
                        setModalError(adminError(e));
                }
                finally {
                    if (!c.signal.aborted)
                        setPreparing(false);
                }
            };
            void load();
        }
    }
    function close() { if (lock.current)
        return; detailRequest.current?.abort(); setPreparing(false); dialog.current?.close(); }
    function field(key: keyof Draft, value: string) { setDraft(prev => ({ ...prev, [key]: value })); }
    async function save(e: FormEvent) {
        e.preventDefault();
        if (lock.current || preparing)
            return;
        setModalError('');
        const code = draft.code.trim(), label = draft.label.trim();
        if (!code || !label) {
            setModalError('Mã và tên không được bỏ trống.');
            return;
        }
        const groupId = draft.groupId ? Number(draft.groupId) : undefined;
        if (groupId !== undefined && (!Number.isSafeInteger(groupId) || groupId <= 0)) {
            setModalError('ID nhóm phải là số nguyên dương.');
            return;
        }
        const entry = draft.entry === '' ? undefined : Number(draft.entry), mid = draft.mid === '' ? undefined : Number(draft.mid);
        if ([entry, mid].some(v => v !== undefined && (!Number.isFinite(v) || v < 0 || v > 9999999999.99))) {
            setModalError('Mức lương không hợp lệ.');
            return;
        }
        if (entry !== undefined && mid !== undefined && entry > mid) {
            setModalError('Mức lương entry không được lớn hơn mid.');
            return;
        }
        // Public DTOs omit edit-only metadata; live editing is gated until full admin DTO exists.
        if (!editReady) {
            setModalError('Chưa tải đủ dữ liệu để chỉnh sửa an toàn.');
            return;
        }
        if (!demoMode && !window.confirm('Ghi dữ liệu vào máy chủ?'))
            return;
        lock.current = true;
        setBusy(true);
        try {
            if (demoMode) {
                if (demo[kind].some(r => r.code.toLowerCase() === code.toLowerCase() && r.id !== editing))
                    throw new Error('Mã đã tồn tại.');
                const id = editing ?? nextDemoId.current++;
                setDemo(prev => ({ ...prev, [kind]: editing === null ? [...prev[kind], { id, code, label, group: draft.groupId || '—' }] : prev[kind].map(r => r.id === id ? { ...r, code, label, group: draft.groupId || '—' } : r) }));
                setDemoDrafts(prev => ({ ...prev, [`${kind}:${id}`]: { ...draft, code, label } }));
            }
            else if (kind === 'occupation') {
                const body: OccupationInput = { code, title: label, description: draft.description.trim(), groupId, industry: draft.industry.trim() || undefined, entrySalaryAvg: entry, midSalaryAvg: mid };
                await adminApi.saveOccupation(editing, body);
            }
            else {
                const body: SkillInput = { code, name: label, description: draft.description.trim(), categoryId: groupId, skillType: draft.skillType };
                await adminApi.saveSkill(editing, body);
            }
            dialog.current?.close();
            setMessage(demoMode ? 'Đã lưu thay đổi demo (không ghi DB).' : 'Đã lưu danh mục.');
            setVersion(v => v + 1);
        }
        catch (err) {
            setModalError(adminError(err));
        }
        finally {
            lock.current = false;
            setBusy(false);
        }
    }
    return <main className={styles.page}><header className={styles.heading}><span className={styles.eyebrow}>DANH MỤC CHUẨN HÓA</span><h1>Nghề nghiệp & kỹ năng</h1><p>Tra cứu danh mục và tạo, chỉnh sửa các mục chuẩn hóa.</p></header>{demoMode ? <p className={styles.notice} role="status">DEMO — dữ liệu mẫu trong bộ nhớ, không ghi máy chủ.</p> : <p className={styles.notice}>Danh sách dùng API thật. API ghi W4-T3 chưa có trong bản source bàn giao; hợp đồng ghi và đọc chi tiết quản trị là dự kiến; chỉ cho lưu chỉnh sửa sau khi tải đủ dữ liệu.</p>}
  <section className={styles.panel}><div className={styles.toolbar}><button type="button" className={kind === 'occupation' ? styles.button : styles.secondary} aria-pressed={kind === 'occupation'} onClick={() => { setKind('occupation'); setPage(0); setQuery(''); setMessage(''); }}>Nghề nghiệp</button><button type="button" className={kind === 'skill' ? styles.button : styles.secondary} aria-pressed={kind === 'skill'} onClick={() => { setKind('skill'); setPage(0); setQuery(''); setMessage(''); }}>Kỹ năng</button><label className={styles.field}>Tìm kiếm<input value={query} onChange={e => { setQuery(e.target.value); setPage(0); }}/></label><button className={styles.secondary} onClick={() => setVersion(v => v + 1)}>Tải lại</button><button className={styles.button} onClick={() => open()}>+ Thêm {kind === 'occupation' ? 'nghề' : 'kỹ năng'}</button></div>
  {message && <p className={styles.success} role="status">{message}</p>}{loading ? <p role="status">Đang tải…</p> : error ? <p role="alert" className={styles.error}>{error}</p> : rows.length ? <><div className={styles.tableWrap}><table className={styles.table}><thead><tr><th>Mã</th><th>Tên</th><th>Nhóm</th><th>Thao tác</th></tr></thead><tbody>{rows.map(r => <tr key={r.id}><td>{r.code}</td><td>{r.label}</td><td>{r.group || '—'}</td><td><button className={styles.secondary} onClick={() => open(r)}>{demoMode ? 'Chỉnh sửa' : 'Xem / sửa'}</button></td></tr>)}</tbody></table></div><div className={styles.pager}><button className={styles.secondary} disabled={page === 0} onClick={() => setPage(p => p - 1)}>Trước</button><span>Trang {page + 1}/{pages} · {total} mục</span><button className={styles.secondary} disabled={page + 1 >= pages} onClick={() => setPage(p => p + 1)}>Sau</button></div></> : <p>Không tìm thấy dữ liệu.</p>}</section>
  <dialog ref={dialog} className={styles.dialog} onCancel={e => { e.preventDefault(); close(); }} aria-labelledby="taxonomy-dialog-title"><form onSubmit={save}><h2 id="taxonomy-dialog-title">{editing === null ? 'Thêm' : 'Chỉnh sửa'} {kind === 'occupation' ? 'nghề nghiệp' : 'kỹ năng'}</h2>{!editReady && editing !== null && <p className={styles.notice}>Chưa có đủ dữ liệu chỉnh sửa; không thể lưu.</p>}{preparing && <p role="status">Đang tải chi tiết…</p>}
  <label className={styles.field}>Mã<input required maxLength={50} value={draft.code} onChange={e => field('code', e.target.value)}/></label><label className={styles.field}>Tên<input required maxLength={kind === 'occupation' ? 200 : 150} value={draft.label} onChange={e => field('label', e.target.value)}/></label><label className={styles.field}>Mô tả<textarea value={draft.description} onChange={e => field('description', e.target.value)}/></label><label className={styles.field}>ID {kind === 'occupation' ? 'nhóm nghề' : 'danh mục kỹ năng'} (không bắt buộc)<input type="number" min="1" step="1" value={draft.groupId} onChange={e => field('groupId', e.target.value)}/></label>{kind === 'occupation' ? <><label className={styles.field}>Ngành<input maxLength={100} value={draft.industry} onChange={e => field('industry', e.target.value)}/></label><label className={styles.field}>Lương entry (VND)<input type="number" min="0" step="0.01" value={draft.entry} onChange={e => field('entry', e.target.value)}/></label><label className={styles.field}>Lương mid (VND)<input type="number" min="0" step="0.01" value={draft.mid} onChange={e => field('mid', e.target.value)}/></label></> : <label className={styles.field}>Loại kỹ năng<select value={draft.skillType} onChange={e => field('skillType', e.target.value)}><option value="TECHNICAL">Kỹ thuật</option><option value="SOFT">Kỹ năng mềm</option><option value="DOMAIN">Chuyên ngành</option></select></label>}{modalError && <p className={styles.error} role="alert">{modalError}</p>}<div className={styles.toolbar}><button type="button" className={styles.secondary} disabled={busy} onClick={close}>Đóng</button><button type="submit" className={styles.button} disabled={busy || preparing || !editReady}>{busy ? 'Đang lưu…' : 'Lưu'}</button></div></form></dialog></main>;
}
