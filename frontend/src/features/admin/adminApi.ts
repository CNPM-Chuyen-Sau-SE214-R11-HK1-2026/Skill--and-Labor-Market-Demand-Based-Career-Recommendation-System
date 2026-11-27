import { apiClient } from '../../api/client';
import type { PageResponse, OccupationSummary } from '../occupations/types/occupation.types';
import type { AdminUser, Stats, LmiItem, OccupationInput, SkillInput } from './model';
import { parseLmiResult } from './model';
export const demoMode = import.meta.env.DEV && import.meta.env.VITE_ADMIN_DEMO === 'true';
// W4-T2/T3 request/response DTOs are not present in the supplied snapshot.
// Keep the proposed integration shapes isolated here; never fall back on failure.
export const adminApi = {
    importLmi: async (items: LmiItem[]) => parseLmiResult((await apiClient.post<unknown>('/admin/lmi/import', { items })).data),
    users: async (params: {
        query: string;
        role: string;
        active: string;
        page: number;
        size: number;
    }, signal?: AbortSignal) => (await apiClient.get<PageResponse<AdminUser>>('/admin/users', { params: { query: params.query || undefined, role: params.role || undefined, active: params.active === '' ? undefined : params.active === 'true', page: params.page, size: params.size }, signal })).data,
    stats: async (signal?: AbortSignal) => (await apiClient.get<Stats>('/admin/stats', { signal })).data,
    status: async (id: number, active: boolean) => { await apiClient.patch(`/admin/users/${id}/status`, { active }); },
    occupations: async (query: string, page: number, signal?: AbortSignal) => (await apiClient.get<PageResponse<OccupationSummary>>('/occupations', { params: { query: query || undefined, page, size: 10, sort: 'title,asc' }, signal })).data,
    // Proposed read DTOs are needed to avoid destructive PUT from incomplete public DTOs.
    occupationForEdit: async (id: number, signal?: AbortSignal) => (await apiClient.get<OccupationInput>(`/admin/occupations/${id}`, { signal })).data,
    skillForEdit: async (id: number, signal?: AbortSignal) => (await apiClient.get<SkillInput>(`/admin/skills/${id}`, { signal })).data,
    skills: async (query: string, signal?: AbortSignal) => (await apiClient.get<{
        id: number;
        code: string;
        name: string;
        categoryName: string;
    }[]>('/skills', { params: { query: query.length >= 2 ? query : undefined }, signal })).data,
    saveOccupation: async (id: number | null, body: OccupationInput) => { if (id === null)
        await apiClient.post('/admin/occupations', body);
    else
        await apiClient.put(`/admin/occupations/${id}`, body); },
    saveSkill: async (id: number | null, body: SkillInput) => { if (id === null)
        await apiClient.post('/admin/skills', body);
    else
        await apiClient.put(`/admin/skills/${id}`, body); },
};
export function adminError(error: unknown): string {
    const e = error as {
        response?: {
            status?: number;
            data?: {
                message?: string;
                errors?: {
                    field: string;
                    reason: string;
                }[];
            };
        };
        message?: string;
    } | null;
    const status = e?.response?.status;
    const data = e?.response?.data;
    if (status === 401)
        return 'Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.';
    if (status === 403)
        return 'Không có quyền thực hiện thao tác này.';
    if (status === 404 || status === 405)
        return 'Không tìm thấy dữ liệu hoặc API chưa được triển khai. Kiểm tra backend W4-T2/W4-T3.';
    const details = Array.isArray(data?.errors) ? data.errors.map(x => `${x.field}: ${x.reason}`).join('; ') : '';
    return [data?.message || e?.message || 'Không thể kết nối máy chủ.', details].filter(Boolean).join(' — ');
}
