import type { OccupationDetail, OccupationSummary } from '../types/occupation.types';

const descriptions: Record<number, string> = {
  101: 'Phân tích yêu cầu, thiết kế, phát triển và bảo trì hệ thống phần mềm. Phối hợp với nhóm sản phẩm và kiểm thử để triển khai tính năng và xử lý lỗi.',
  102: 'Thiết kế kịch bản kiểm thử, phát hiện và theo dõi lỗi, đồng thời xây dựng kiểm thử tự động để đánh giá chất lượng phần mềm.',
  103: 'Xây dựng giao diện và chức năng web, tích hợp API, tối ưu hiệu năng và khả năng sử dụng trên nhiều thiết bị.',
  104: 'Thu thập và phân tích dữ liệu, xây dựng mô hình thống kê hoặc học máy và truyền đạt kết quả cho nhóm sản phẩm.',
  105: 'Vận hành cơ sở dữ liệu, giám sát hiệu năng, quản lý quyền truy cập và duy trì quy trình sao lưu, phục hồi.',
  106: 'Giám sát tín hiệu an ninh, phân tích cảnh báo và phối hợp xử lý sự cố để bảo vệ hệ thống và dữ liệu.',
};

export function buildMockOccupationDetail(item: OccupationSummary): OccupationDetail {
  const demand = item.demandScore ?? 0;
  const openings = 700 + (item.id - 100) * 110;
  const periods = ['2026-Q1', '2026-Q2', '2026-Q3', '2026-Q4'];
  const regions = [
    { name: 'Toàn quốc', scoreOffset: 0, share: 1 },
    { name: 'Hà Nội', scoreOffset: -5, share: 0.35 },
    { name: 'TP. Hồ Chí Minh', scoreOffset: 3, share: 0.5 },
  ];

  return {
    id: item.id,
    code: item.code,
    title: item.title,
    groupName: item.groupName,
    description: descriptions[item.id] ?? `${item.title} (${item.groupName}).`,
    salaryRange: { min: item.averageSalaryMin, max: item.averageSalaryMax, currency: 'VND' },
    marketIndicators: {
      demandScore: item.demandScore,
      growthRatePct: item.growthRatePct,
      shortageLevel: null,
      openingsCount: openings,
      dataPeriod: '2026-Q4',
      sourceName: 'Dữ liệu minh họa',
    },
    marketTrend: regions.flatMap((region) => periods.map((period, index) => ({
      period,
      region: region.name,
      demandScore: demand + region.scoreOffset - (periods.length - 1 - index) * 3,
      openingsCount: Math.round((openings - (periods.length - 1 - index) * 80) * region.share),
    }))),
    skills: [
      { skillId: 1, skillCode: 'SK-CORE-01', skillName: coreSkills[item.id] ?? item.title, importanceType: 'ESSENTIAL', weight: 1 },
      { skillId: 2, skillCode: 'SK-DOC-01', skillName: 'Đọc tài liệu kỹ thuật', importanceType: 'ESSENTIAL', weight: 0.85 },
      { skillId: 3, skillCode: 'SK-COL-01', skillName: 'Cộng tác liên chức năng', importanceType: 'OPTIONAL', weight: 0.55 },
    ],
  };
}

const coreSkills: Record<number, string> = {
  101: 'Lập trình ứng dụng',
  102: 'Thiết kế ca kiểm thử',
  103: 'Phát triển giao diện web',
  104: 'Phân tích thống kê',
  105: 'Quản trị cơ sở dữ liệu',
  106: 'Phân tích cảnh báo an ninh',
};
