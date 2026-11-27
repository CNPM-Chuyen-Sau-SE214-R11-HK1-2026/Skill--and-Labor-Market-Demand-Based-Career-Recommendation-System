export interface LmiItem {
    occupationId?: number;
    occupationCode?: string;
    region?: string;
    demandScore: number;
    growthRatePct?: number;
    openJobPostingsCount?: number;
    competitiveIndex?: number;
    snapshotDate: string;
}
export interface LmiResult {
    totalRecordsProcessed: number;
    recordsInserted: number;
    recordsUpdated: number;
    timestamp: string;
}
export interface AdminUser {
    id: number;
    email: string;
    fullName: string;
    role: 'ROLE_ADMIN' | 'ROLE_CANDIDATE';
    active: boolean;
}
export interface Stats {
    totalUsers: number;
    totalProfiles: number;
    totalOccupations: number;
    totalSkills: number;
    totalLmiRecords: number;
    totalRecommendationLogs: number;
}
export interface OccupationInput {
    code: string;
    title: string;
    description: string;
    groupId?: number;
    industry?: string;
    entrySalaryAvg?: number;
    midSalaryAvg?: number;
}
export interface SkillInput {
    code: string;
    name: string;
    description: string;
    categoryId?: number;
    skillType: 'TECHNICAL' | 'SOFT' | 'DOMAIN';
}
export interface CatalogItem {
    id: number;
    code: string;
    label: string;
    group: string;
}
const fields = new Set(['occupationId', 'occupationCode', 'region', 'demandScore', 'growthRatePct', 'openJobPostingsCount', 'competitiveIndex', 'snapshotDate']);
const record = (v: unknown): v is Record<string, unknown> => typeof v === 'object' && v !== null && !Array.isArray(v);
export function parseLmiBatch(raw: string): {
    items: LmiItem[];
} {
    let input: unknown;
    try {
        input = JSON.parse(raw);
    }
    catch {
        throw new Error('JSON không hợp lệ.');
    }
    // Accept pasted array for convenience; always send the backend envelope.
    const items = Array.isArray(input) ? input : record(input) && Object.keys(input).length === 1 ? input.items : null;
    if (!Array.isArray(items) || items.length < 1 || items.length > 500)
        throw new Error('Cần từ 1 đến 500 bản ghi trong items.');
    return { items: items.map((item: unknown, index): LmiItem => {
            const fail = (message: string): never => { throw new Error(`Dòng ${index + 1}: ${message}`); };
            if (!record(item))
                throw new Error(`Dòng ${index + 1}: phải là object.`);
            for (const key of Object.keys(item))
                if (!fields.has(key))
                    fail(`Trường không hỗ trợ: ${key}`);
            const str = (key: string) => { const v = item[key]; if (v == null)
                return undefined; if (typeof v !== 'string' || v.length > 50)
                return fail(`${key} phải là chuỗi tối đa 50 ký tự.`); return v.trim() || undefined; };
            const num = (key: string, min: number, max: number, integer = false) => {
                const v = item[key];
                if (v == null)
                    return undefined;
                if (typeof v !== 'number' || !Number.isFinite(v) || v < min || v > max || (integer ? !Number.isSafeInteger(v) : !/^[-+]?\d+(\.\d{1,2})?$/.test(String(v))))
                    return fail(`${key} không hợp lệ (${min}–${max}${integer ? ', số nguyên' : ', tối đa 2 số lẻ'}).`);
                return v;
            };
            const occupationId = num('occupationId', 1, Number.MAX_SAFE_INTEGER, true);
            const occupationCode = str('occupationCode');
            if (!occupationId && !occupationCode)
                fail('Cần occupationId hoặc occupationCode.');
            const demandScore = num('demandScore', 0, 100);
            if (demandScore === undefined)
                return fail('Thiếu demandScore.');
            const date = item.snapshotDate;
            if (typeof date !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(date) || date.startsWith('0000'))
                return fail('snapshotDate phải là YYYY-MM-DD.');
            const parsed = new Date(`${date}T00:00:00Z`);
            if (!Number.isFinite(parsed.valueOf()) || parsed.toISOString().slice(0, 10) !== date)
                fail('snapshotDate không tồn tại.');
            return { occupationId, occupationCode, region: str('region')?.toUpperCase(), demandScore, growthRatePct: num('growthRatePct', -999.99, 999.99), openJobPostingsCount: num('openJobPostingsCount', 0, 2147483647, true), competitiveIndex: num('competitiveIndex', 0, 9.99), snapshotDate: date };
        }) };
}
export function parseLmiResult(value: unknown): LmiResult {
    if (!record(value))
        throw new Error('Phản hồi LMI không đúng hợp đồng.');
    const { totalRecordsProcessed, recordsInserted, recordsUpdated, timestamp } = value;
    const validCount = (n: unknown): n is number => typeof n === 'number' && Number.isSafeInteger(n) && n >= 0;
    if (!validCount(totalRecordsProcessed) || !validCount(recordsInserted) || !validCount(recordsUpdated) || totalRecordsProcessed !== recordsInserted + recordsUpdated || typeof timestamp !== 'string' || !Number.isFinite(Date.parse(timestamp))) {
        throw new Error('Phản hồi LMI không đúng hợp đồng. Kiểm tra trạng thái dữ liệu trên máy chủ trước khi gửi lại.');
    }
    return { totalRecordsProcessed, recordsInserted, recordsUpdated, timestamp };
}
