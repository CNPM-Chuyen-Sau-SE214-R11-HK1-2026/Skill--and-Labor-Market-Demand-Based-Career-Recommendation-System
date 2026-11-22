/**
 * Types & DTOs for Candidate Profile & Personal Skills feature.
 * Aligned with docs/api/profile.md and docs/api/taxonomy.md.
 */

export type ProficiencyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export type WorkMode = 'REMOTE' | 'HYBRID' | 'ONSITE';

export interface CandidateProfile {
  id: number;
  userId: number;
  headline: string | null;
  yearsOfExperience: number;
  expectedSalaryMin: number | null;
  expectedSalaryMax: number | null;
  preferredLocation: string | null;
  workMode: WorkMode;
  targetIndustry: string | null;
  skillsCount: number;
  updatedAt: string;
}

export interface UpdateCandidateProfileRequest {
  headline?: string | null;
  yearsOfExperience?: number | null;
  expectedSalaryMin?: number | null;
  expectedSalaryMax?: number | null;
  preferredLocation?: string | null;
  workMode?: WorkMode | null;
  targetIndustry?: string | null;
}

export interface CandidateSkill {
  skillId: number;
  skillCode: string;
  skillName: string;
  categoryName: string;
  proficiencyLevel: ProficiencyLevel;
  yearsExperience: number;
  verified: boolean;
}

export interface AddCandidateSkillRequest {
  skillId: number;
  proficiencyLevel: ProficiencyLevel;
  yearsExperience?: number | null;
}

export interface AddCandidateSkillResponse {
  skillId: number;
  skillName: string;
  proficiencyLevel: ProficiencyLevel;
  yearsExperience: number;
  createdAt: string;
}

export interface UpdateCandidateSkillRequest {
  proficiencyLevel?: ProficiencyLevel | null;
  yearsExperience?: number | null;
}

export interface SkillCatalogItem {
  id: number;
  code: string;
  name: string;
  categoryName: string;
  standardSource: string;
}

export interface ProfileApiError {
  status: number;
  error: string;
  message: string;
}

export type ToastType = 'success' | 'error' | 'info';

export interface ToastMessage {
  id: string;
  type: ToastType;
  message: string;
}

export const PROFICIENCY_LEVEL_OPTIONS: ReadonlyArray<{
  value: ProficiencyLevel;
  label: string;
  shortLabel: string;
  description: string;
}> = [
  {
    value: 'BEGINNER',
    label: 'Beginner — Mới bắt đầu',
    shortLabel: 'Beginner',
    description: 'Thường tương ứng khoảng dưới 1 năm làm quen hoặc thực hành cơ bản',
  },
  {
    value: 'INTERMEDIATE',
    label: 'Intermediate — Trung cấp',
    shortLabel: 'Intermediate',
    description: 'Thường tương ứng khoảng 1 đến 3 năm sử dụng trong dự án thực tế',
  },
  {
    value: 'ADVANCED',
    label: 'Advanced — Nâng cao',
    shortLabel: 'Advanced',
    description: 'Thường tương ứng khoảng 3 đến 5 năm làm chủ công nghệ và tối ưu hệ thống',
  },
  {
    value: 'EXPERT',
    label: 'Expert — Chuyên gia',
    shortLabel: 'Expert',
    description: 'Thường tương ứng khoảng trên 5 năm kinh nghiệm chuyên sâu, dẫn dắt kỹ thuật',
  },
];

export const WORK_MODE_OPTIONS: ReadonlyArray<{
  value: WorkMode;
  label: string;
}> = [
  { value: 'HYBRID', label: 'Hybrid (Linh hoạt)' },
  { value: 'REMOTE', label: 'Remote (Từ xa)' },
  { value: 'ONSITE', label: 'Onsite (Tại văn phòng)' },
];
