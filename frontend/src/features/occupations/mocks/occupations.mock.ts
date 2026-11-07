import type {
  OccupationDetail,
  OccupationSearchParams,
  OccupationSummary,
  PageResponse,
} from '../types/occupation.types';

/**
 * DỮ LIỆU MÔ PHỎNG DÙNG CHO UI DEVELOPMENT ONLY.
 * Các mức lương/chỉ số bên dưới là số liệu tổng hợp giả lập,
 * không phải thống kê thị trường.
 *
 * Chỉ sử dụng khi VITE_USE_OCCUPATION_MOCKS=true.
 */
const mockDetails: OccupationDetail[] = [
  {
    id: 9101,
    code: 'DEMO-SWE-01',
    title: 'Software Developer / Kỹ sư phần mềm',
    groupName: 'Phát triển phần mềm',

    description:
      'Phân tích yêu cầu, thiết kế, xây dựng, kiểm thử và bảo trì các sản phẩm phần mềm. Vai trò có thể làm việc ở nhiều giai đoạn trong vòng đời phát triển phần mềm.',

    salaryRange: {
      min: 18000000,
      max: 38000000,
      currency: 'VND',
    },

    marketIndicators: {
      demandScore: 88.5,
      growthRatePct: 12.4,
      shortageLevel: 'HIGH',
      openingsCount: 1250,
      dataPeriod: 'DEMO-2026',
      sourceName: 'Synthetic demo fixture',
    },

    skills: [
      {
        skillId: 1,
        skillCode: 'DEMO-JAVA',
        skillName: 'Java',
        importanceType: 'ESSENTIAL',
        weight: 1.0,
      },
      {
        skillId: 2,
        skillCode: 'DEMO-SQL',
        skillName: 'SQL & Cơ sở dữ liệu',
        importanceType: 'ESSENTIAL',
        weight: 0.9,
      },
      {
        skillId: 3,
        skillCode: 'DEMO-GIT',
        skillName: 'Git',
        importanceType: 'ESSENTIAL',
        weight: 0.8,
      },
      {
        skillId: 4,
        skillCode: 'DEMO-DOCKER',
        skillName: 'Docker',
        importanceType: 'OPTIONAL',
        weight: 0.5,
      },
    ],
  },

  {
    id: 9102,
    code: 'DEMO-DATA-02',
    title: 'Data Analyst / Chuyên viên phân tích dữ liệu',
    groupName: 'Dữ liệu & Phân tích',

    description:
      'Thu thập, làm sạch, phân tích và trực quan hóa dữ liệu để hỗ trợ các quyết định vận hành và kinh doanh.',

    salaryRange: {
      min: 15000000,
      max: 30000000,
      currency: 'VND',
    },

    marketIndicators: {
      demandScore: 82.0,
      growthRatePct: 15.2,
      shortageLevel: 'HIGH',
      openingsCount: 780,
      dataPeriod: 'DEMO-2026',
      sourceName: 'Synthetic demo fixture',
    },

    skills: [
      {
        skillId: 5,
        skillCode: 'DEMO-SQL',
        skillName: 'SQL',
        importanceType: 'ESSENTIAL',
        weight: 1.0,
      },
      {
        skillId: 6,
        skillCode: 'DEMO-EXCEL',
        skillName: 'Excel',
        importanceType: 'ESSENTIAL',
        weight: 0.8,
      },
      {
        skillId: 7,
        skillCode: 'DEMO-PYTHON',
        skillName: 'Python',
        importanceType: 'ESSENTIAL',
        weight: 0.9,
      },
      {
        skillId: 8,
        skillCode: 'DEMO-BI',
        skillName: 'Power BI',
        importanceType: 'OPTIONAL',
        weight: 0.6,
      },
    ],
  },

  {
    id: 9103,
    code: 'DEMO-SEC-03',
    title: 'Cybersecurity Analyst / Chuyên viên an ninh mạng',
    groupName: 'An toàn thông tin',

    description:
      'Theo dõi, đánh giá và xử lý rủi ro an ninh; phân tích cảnh báo, kiểm tra lỗ hổng và đề xuất biện pháp bảo vệ hệ thống.',

    salaryRange: {
      min: 17000000,
      max: 36000000,
      currency: 'VND',
    },

    marketIndicators: {
      demandScore: 91.0,
      growthRatePct: 18.0,
      shortageLevel: 'HIGH',
      openingsCount: 540,
      dataPeriod: 'DEMO-2026',
      sourceName: 'Synthetic demo fixture',
    },

    skills: [
      {
        skillId: 9,
        skillCode: 'DEMO-NET',
        skillName: 'Networking',
        importanceType: 'ESSENTIAL',
        weight: 1.0,
      },
      {
        skillId: 10,
        skillCode: 'DEMO-LINUX',
        skillName: 'Linux',
        importanceType: 'ESSENTIAL',
        weight: 0.9,
      },
      {
        skillId: 11,
        skillCode: 'DEMO-SECOPS',
        skillName: 'Security Operations',
        importanceType: 'ESSENTIAL',
        weight: 1.0,
      },
      {
        skillId: 12,
        skillCode: 'DEMO-PY',
        skillName: 'Python scripting',
        importanceType: 'OPTIONAL',
        weight: 0.5,
      },
    ],
  },

  {
    id: 9104,
    code: 'DEMO-UX-04',
    title: 'UI/UX Designer / Nhà thiết kế UI/UX',
    groupName: 'Thiết kế sản phẩm số',

    description:
      'Nghiên cứu người dùng, xây dựng kiến trúc thông tin, thiết kế luồng tương tác và giao diện cho sản phẩm số.',

    salaryRange: {
      min: 14000000,
      max: 28000000,
      currency: 'VND',
    },

    marketIndicators: {
      demandScore: 72.5,
      growthRatePct: 8.3,
      shortageLevel: 'MEDIUM',
      openingsCount: 420,
      dataPeriod: 'DEMO-2026',
      sourceName: 'Synthetic demo fixture',
    },

    skills: [
      {
        skillId: 13,
        skillCode: 'DEMO-FIGMA',
        skillName: 'Figma',
        importanceType: 'ESSENTIAL',
        weight: 1.0,
      },
      {
        skillId: 14,
        skillCode: 'DEMO-UXR',
        skillName: 'User Research',
        importanceType: 'ESSENTIAL',
        weight: 0.9,
      },
      {
        skillId: 15,
        skillCode: 'DEMO-PROT',
        skillName: 'Prototyping',
        importanceType: 'ESSENTIAL',
        weight: 0.8,
      },
      {
        skillId: 16,
        skillCode: 'DEMO-HTML',
        skillName: 'HTML/CSS',
        importanceType: 'OPTIONAL',
        weight: 0.4,
      },
    ],
  },

  {
    id: 9105,
    code: 'DEMO-DEVOPS-05',
    title: 'DevOps Engineer / Kỹ sư DevOps',
    groupName: 'Hạ tầng & Vận hành',

    description:
      'Tự động hóa quy trình build, kiểm thử, triển khai và giám sát hệ thống; phối hợp giữa phát triển phần mềm và vận hành.',

    salaryRange: {
      min: 20000000,
      max: 42000000,
      currency: 'VND',
    },

    marketIndicators: {
      demandScore: 86.0,
      growthRatePct: 14.6,
      shortageLevel: 'HIGH',
      openingsCount: 610,
      dataPeriod: 'DEMO-2026',
      sourceName: 'Synthetic demo fixture',
    },

    skills: [
      {
        skillId: 17,
        skillCode: 'DEMO-LINUX',
        skillName: 'Linux',
        importanceType: 'ESSENTIAL',
        weight: 1.0,
      },
      {
        skillId: 18,
        skillCode: 'DEMO-DOCKER',
        skillName: 'Docker',
        importanceType: 'ESSENTIAL',
        weight: 0.9,
      },
      {
        skillId: 19,
        skillCode: 'DEMO-CICD',
        skillName: 'CI/CD',
        importanceType: 'ESSENTIAL',
        weight: 1.0,
      },
      {
        skillId: 20,
        skillCode: 'DEMO-CLOUD',
        skillName: 'Cloud Platform',
        importanceType: 'OPTIONAL',
        weight: 0.7,
      },
    ],
  },

  {
    id: 9106,
    code: 'DEMO-AI-06',
    title: 'Machine Learning Engineer / Kỹ sư học máy',
    groupName: 'AI & Học máy',

    description:
      'Xây dựng pipeline dữ liệu, huấn luyện, đánh giá và triển khai mô hình học máy vào sản phẩm.',

    salaryRange: {
      min: 22000000,
      max: 45000000,
      currency: 'VND',
    },

    marketIndicators: {
      demandScore: 89.0,
      growthRatePct: 20.1,
      shortageLevel: 'HIGH',
      openingsCount: 360,
      dataPeriod: 'DEMO-2026',
      sourceName: 'Synthetic demo fixture',
    },

    skills: [
      {
        skillId: 21,
        skillCode: 'DEMO-PYTHON',
        skillName: 'Python',
        importanceType: 'ESSENTIAL',
        weight: 1.0,
      },
      {
        skillId: 22,
        skillCode: 'DEMO-ML',
        skillName: 'Machine Learning',
        importanceType: 'ESSENTIAL',
        weight: 1.0,
      },
      {
        skillId: 23,
        skillCode: 'DEMO-STATS',
        skillName: 'Statistics',
        importanceType: 'ESSENTIAL',
        weight: 0.8,
      },
      {
        skillId: 24,
        skillCode: 'DEMO-MLOPS',
        skillName: 'MLOps',
        importanceType: 'OPTIONAL',
        weight: 0.6,
      },
    ],
  },
];

const mockSummaries: OccupationSummary[] =
  mockDetails.map((item) => ({
    id: item.id,
    code: item.code,
    title: item.title,
    groupName: item.groupName,
    averageSalaryMin: item.salaryRange.min,
    averageSalaryMax: item.salaryRange.max,
    demandScore:
      item.marketIndicators?.demandScore ?? null,
    growthRatePct:
      item.marketIndicators?.growthRatePct ?? null,
  }));

export function getMockOccupations(
  params: OccupationSearchParams
): PageResponse<OccupationSummary> {
  const normalizedQuery = (params.query ?? '')
    .trim()
    .toLocaleLowerCase('vi-VN');

  const filtered = normalizedQuery
    ? mockSummaries.filter((item) => {
        const detail = mockDetails.find(
          (candidate) => candidate.id === item.id
        );

        const searchableText =
          `${item.title} ${item.code} ${item.groupName} ${detail?.description ?? ''}`;

        return searchableText
          .toLocaleLowerCase('vi-VN')
          .includes(normalizedQuery);
      })
    : mockSummaries;

  // Normalize optional pagination values before arithmetic and slicing.
  const page = Math.max(
    0,
    Math.floor(params.page ?? 0)
  );

  const size = Math.max(
    1,
    Math.floor(params.size ?? 9)
  );

  const start = page * size;

  const content = filtered.slice(
    start,
    start + size
  );

  const totalPages = Math.ceil(
    filtered.length / size
  );

  return {
    content,
    pageNumber: page,
    pageSize: size,
    totalElements: filtered.length,
    totalPages,
    last: page >= Math.max(totalPages - 1, 0),
  };
}

export function getMockOccupationDetail(
  id: number
): OccupationDetail | null {
  return (
    mockDetails.find((item) => item.id === id) ?? null
  );
}