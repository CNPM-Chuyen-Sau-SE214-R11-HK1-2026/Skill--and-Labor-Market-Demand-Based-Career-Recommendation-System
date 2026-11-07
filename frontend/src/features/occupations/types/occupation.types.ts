export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface OccupationSummary {
  id: number;
  code: string;
  title: string;
  groupName: string;
  averageSalaryMin: number | null;
  averageSalaryMax: number | null;
  demandScore: number | null;
  growthRatePct: number | null;
}

export interface OccupationMarketIndicators {
  demandScore: number | null;
  growthRatePct: number | null;
  shortageLevel: string | null;
  openingsCount: number | null;
  dataPeriod: string | null;
  sourceName: string | null;
}

export interface OccupationSkill {
  skillId: number;
  skillCode: string;
  skillName: string;
  importanceType: 'ESSENTIAL' | 'OPTIONAL' | string;
  weight: number | null;
}

export interface OccupationDetail extends Omit<
  OccupationSummary,
  'averageSalaryMin' | 'averageSalaryMax' | 'demandScore' | 'growthRatePct'
> {
  description: string;
  salaryRange: {
    min: number | null;
    max: number | null;
    currency: string;
  };
  marketIndicators: OccupationMarketIndicators | null;
  skills: OccupationSkill[];
}

export interface OccupationSearchParams {
  query?: string;
  groupId?: number;
  page?: number;
  size?: number;
  sort?: string;
}