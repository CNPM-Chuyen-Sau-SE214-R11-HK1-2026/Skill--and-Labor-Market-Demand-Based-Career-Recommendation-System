import { buildMockOccupationDetail } from './occupationDetailMocks';
import type { OccupationDetail, OccupationSearchParams, OccupationSummary, PageResponse } from '../types/occupation.types';

const mockItems: OccupationSummary[] = [
  { id: 101, code: 'SOC-15-1252', title: 'Software Developer / Kỹ sư phần mềm', groupName: 'Phát triển phần mềm', averageSalaryMin: 15000000, averageSalaryMax: 35000000, demandScore: 88.5, growthRatePct: 12.4 },
  { id: 102, code: 'SOC-15-1244', title: 'Software Quality Assurance', groupName: 'Kiểm thử phần mềm', averageSalaryMin: 12000000, averageSalaryMax: 28000000, demandScore: 79, growthRatePct: 9.2 },
  { id: 103, code: 'SOC-15-1253', title: 'Web Developer', groupName: 'Phát triển web', averageSalaryMin: 13000000, averageSalaryMax: 30000000, demandScore: 84, growthRatePct: 11.1 },
  { id: 104, code: 'SOC-15-2051', title: 'Data Scientist', groupName: 'Dữ liệu & AI', averageSalaryMin: 18000000, averageSalaryMax: 42000000, demandScore: 92, growthRatePct: 16.8 },
  { id: 105, code: 'SOC-15-1243', title: 'Database Administrator', groupName: 'Hạ tầng & dữ liệu', averageSalaryMin: 16000000, averageSalaryMax: 32000000, demandScore: 74, growthRatePct: 7.5 },
  { id: 106, code: 'SOC-15-1299', title: 'Cybersecurity Analyst', groupName: 'An toàn thông tin', averageSalaryMin: 17000000, averageSalaryMax: 38000000, demandScore: 90, growthRatePct: 14.3 },
];

export async function mockGetOccupations(params: OccupationSearchParams): Promise<PageResponse<OccupationSummary>> {
  const query = params.query?.toLocaleLowerCase('vi');
  const filtered = query ? mockItems.filter((item) => `${item.title} ${item.groupName} ${item.code}`.toLocaleLowerCase('vi').includes(query)) : mockItems;
  const size = params.size ?? 9;
  const page = params.page ?? 0;
  const start = page * size;
  return { content: filtered.slice(start, start + size), pageNumber: page, pageSize: size, totalElements: filtered.length, totalPages: Math.ceil(filtered.length / size), last: start + size >= filtered.length };
}

export async function mockGetOccupationDetail(id: number): Promise<OccupationDetail> {
  const item = mockItems.find((occupation) => occupation.id === id);
  if (!item) {
    throw Object.assign(new Error('Không tìm thấy nghề nghiệp.'), {
      response: { status: 404 },
    });
  }
  return buildMockOccupationDetail(item);
}
