import type {
  AddCandidateSkillRequest,
  AddCandidateSkillResponse,
  CandidateProfile,
  CandidateSkill,
  SkillCatalogItem,
  UpdateCandidateProfileRequest,
  UpdateCandidateSkillRequest,
} from '../types/profile.types';
import {
  mockCandidateSkillsFixture,
  mockProfileFixture,
  mockTaxonomySkillsFixture,
} from './profileFixtures';

const PROFILE_STORAGE_KEY = 'career_rec_mock_candidate_profile_v1';
const SKILLS_STORAGE_KEY = 'career_rec_mock_candidate_skills_v1';
const MOCK_DELAY_MS = 220;

export class MockApiError extends Error {
  public readonly status: number;
  public readonly error: string;

  constructor(status: number, error: string, message: string) {
    super(message);
    this.name = 'MockApiError';
    this.status = status;
    this.error = error;
  }
}

function delay(ms: number, signal?: AbortSignal): Promise<void> {
  return new Promise((resolve, reject) => {
    if (signal?.aborted) {
      reject(new DOMException('The operation was aborted.', 'AbortError'));
      return;
    }

    const timer = window.setTimeout(() => {
      cleanup();
      resolve();
    }, ms);

    const onAbort = () => {
      window.clearTimeout(timer);
      cleanup();
      reject(new DOMException('The operation was aborted.', 'AbortError'));
    };

    const cleanup = () => {
      signal?.removeEventListener('abort', onAbort);
    };

    signal?.addEventListener('abort', onAbort, { once: true });
  });
}

let inMemoryProfile: CandidateProfile | null | undefined = undefined;
let inMemorySkills: CandidateSkill[] | undefined = undefined;

function loadStoredProfile(): CandidateProfile | null {
  if (inMemoryProfile !== undefined) {
    return inMemoryProfile;
  }

  try {
    const raw = window.localStorage.getItem(PROFILE_STORAGE_KEY);
    if (raw === null) {
      inMemoryProfile = { ...mockProfileFixture };
      return inMemoryProfile;
    }
    if (raw === 'null') {
      inMemoryProfile = null;
      return null;
    }
    inMemoryProfile = JSON.parse(raw) as CandidateProfile;
    return inMemoryProfile;
  } catch {
    inMemoryProfile = { ...mockProfileFixture };
    return inMemoryProfile;
  }
}

function saveStoredProfile(profile: CandidateProfile | null): void {
  inMemoryProfile = profile ? { ...profile } : null;
  try {
    window.localStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(inMemoryProfile));
  } catch {
    // Ignore storage quota or private mode errors
  }
}

function loadStoredSkills(): CandidateSkill[] {
  if (inMemorySkills !== undefined) {
    return inMemorySkills.map((s) => ({ ...s }));
  }

  try {
    const raw = window.localStorage.getItem(SKILLS_STORAGE_KEY);
    if (raw === null) {
      inMemorySkills = mockCandidateSkillsFixture.map((s) => ({ ...s }));
      return inMemorySkills.map((s) => ({ ...s }));
    }
    const parsed = JSON.parse(raw) as CandidateSkill[];
    inMemorySkills = Array.isArray(parsed) ? parsed : [];
    return inMemorySkills.map((s) => ({ ...s }));
  } catch {
    inMemorySkills = mockCandidateSkillsFixture.map((s) => ({ ...s }));
    return inMemorySkills.map((s) => ({ ...s }));
  }
}

function saveStoredSkills(skills: CandidateSkill[]): void {
  inMemorySkills = skills.map((s) => ({ ...s }));
  try {
    window.localStorage.setItem(SKILLS_STORAGE_KEY, JSON.stringify(inMemorySkills));
  } catch {
    // Ignore storage quota or private mode errors
  }
}

/**
 * Helper for QA / demo: switches mock store to "no profile yet" (404 PROFILE_NOT_FOUND).
 */
export function setMockProfileNotFoundScenario(): void {
  saveStoredProfile(null);
}

/**
 * Helper for QA / demo: restores default profile & skills fixtures.
 */
export function resetMockProfileFixtures(): void {
  saveStoredProfile({ ...mockProfileFixture });
  saveStoredSkills(mockCandidateSkillsFixture.map((s) => ({ ...s })));
}

export async function mockGetCandidateProfile(
  signal?: AbortSignal
): Promise<CandidateProfile> {
  await delay(MOCK_DELAY_MS, signal);
  const profile = loadStoredProfile();
  if (!profile) {
    throw new MockApiError(
      404,
      'PROFILE_NOT_FOUND',
      'Hồ sơ ứng viên chưa được khởi tạo. Vui lòng cập nhật qua PUT /api/v1/profile.'
    );
  }
  const skills = loadStoredSkills();
  return {
    ...profile,
    skillsCount: skills.length,
  };
}

export async function mockUpdateCandidateProfile(
  payload: UpdateCandidateProfileRequest
): Promise<CandidateProfile> {
  await delay(MOCK_DELAY_MS);

  const yearsOfExperience = payload.yearsOfExperience ?? 0;
  if (yearsOfExperience < 0) {
    throw new MockApiError(
      400,
      'VALIDATION_ERROR',
      'Số năm kinh nghiệm phải lớn hơn hoặc bằng 0.'
    );
  }

  const salaryMin = payload.expectedSalaryMin ?? null;
  const salaryMax = payload.expectedSalaryMax ?? null;
  if (salaryMin !== null && salaryMin < 0) {
    throw new MockApiError(400, 'VALIDATION_ERROR', 'Mức lương tối thiểu không được âm.');
  }
  if (salaryMax !== null && salaryMax < 0) {
    throw new MockApiError(400, 'VALIDATION_ERROR', 'Mức lương tối đa không được âm.');
  }
  if (salaryMin !== null && salaryMax !== null && salaryMin > salaryMax) {
    throw new MockApiError(
      400,
      'VALIDATION_ERROR',
      'Mức lương tối thiểu không được lớn hơn mức lương tối đa.'
    );
  }

  const existing = loadStoredProfile();
  const skills = loadStoredSkills();

  const updated: CandidateProfile = {
    id: existing?.id ?? 1,
    userId: existing?.userId ?? 1,
    headline: payload.headline?.trim() ? payload.headline.trim() : null,
    yearsOfExperience,
    expectedSalaryMin: salaryMin,
    expectedSalaryMax: salaryMax,
    preferredLocation: payload.preferredLocation?.trim()
      ? payload.preferredLocation.trim()
      : null,
    workMode: payload.workMode ?? 'HYBRID',
    targetIndustry: payload.targetIndustry?.trim() ? payload.targetIndustry.trim() : null,
    skillsCount: skills.length,
    updatedAt: new Date().toISOString(),
  };

  saveStoredProfile(updated);
  return { ...updated };
}

export async function mockGetCandidateSkills(
  signal?: AbortSignal
): Promise<CandidateSkill[]> {
  await delay(MOCK_DELAY_MS, signal);
  return loadStoredSkills();
}

export async function mockAddCandidateSkill(
  payload: AddCandidateSkillRequest
): Promise<AddCandidateSkillResponse> {
  await delay(MOCK_DELAY_MS);

  const catalogItem = mockTaxonomySkillsFixture.find((item) => item.id === payload.skillId);
  if (!catalogItem) {
    throw new MockApiError(
      404,
      'SKILL_NOT_FOUND',
      'Không tìm thấy kỹ năng trong danh mục chuẩn hóa.'
    );
  }

  const yearsExperience = payload.yearsExperience ?? 0;
  if (yearsExperience < 0) {
    throw new MockApiError(
      400,
      'VALIDATION_ERROR',
      'Số năm kinh nghiệm kỹ năng phải lớn hơn hoặc bằng 0.'
    );
  }

  const currentSkills = loadStoredSkills();
  const alreadyExists = currentSkills.some((s) => s.skillId === payload.skillId);
  if (alreadyExists) {
    throw new MockApiError(
      409,
      'SKILL_ALREADY_EXISTS',
      `Kỹ năng "${catalogItem.name}" đã tồn tại trong hồ sơ của bạn.`
    );
  }

  const newSkill: CandidateSkill = {
    skillId: catalogItem.id,
    skillCode: catalogItem.code,
    skillName: catalogItem.name,
    categoryName: catalogItem.categoryName,
    proficiencyLevel: payload.proficiencyLevel,
    yearsExperience,
    verified: false,
  };

  saveStoredSkills([...currentSkills, newSkill]);

  return {
    skillId: newSkill.skillId,
    skillName: newSkill.skillName,
    proficiencyLevel: newSkill.proficiencyLevel,
    yearsExperience: newSkill.yearsExperience,
    createdAt: new Date().toISOString(),
  };
}

export async function mockUpdateCandidateSkill(
  skillId: number,
  payload: UpdateCandidateSkillRequest
): Promise<CandidateSkill> {
  await delay(MOCK_DELAY_MS);

  if (payload.yearsExperience !== undefined && payload.yearsExperience !== null && payload.yearsExperience < 0) {
    throw new MockApiError(
      400,
      'VALIDATION_ERROR',
      'Số năm kinh nghiệm kỹ năng phải lớn hơn hoặc bằng 0.'
    );
  }

  const currentSkills = loadStoredSkills();
  const index = currentSkills.findIndex((s) => s.skillId === skillId);
  if (index === -1) {
    throw new MockApiError(
      404,
      'CANDIDATE_SKILL_NOT_FOUND',
      'Không tìm thấy kỹ năng này trong hồ sơ của bạn.'
    );
  }

  const target = currentSkills[index];
  const updated: CandidateSkill = {
    ...target,
    proficiencyLevel: payload.proficiencyLevel ?? target.proficiencyLevel,
    yearsExperience: payload.yearsExperience ?? target.yearsExperience,
  };

  const nextSkills = [...currentSkills];
  nextSkills[index] = updated;
  saveStoredSkills(nextSkills);

  return { ...updated };
}

export async function mockDeleteCandidateSkill(skillId: number): Promise<void> {
  await delay(MOCK_DELAY_MS);

  const currentSkills = loadStoredSkills();
  const exists = currentSkills.some((s) => s.skillId === skillId);
  if (!exists) {
    throw new MockApiError(
      404,
      'CANDIDATE_SKILL_NOT_FOUND',
      'Không tìm thấy kỹ năng này trong hồ sơ của bạn.'
    );
  }

  saveStoredSkills(currentSkills.filter((s) => s.skillId !== skillId));
}

export async function mockSearchSkills(
  query: string,
  signal?: AbortSignal
): Promise<SkillCatalogItem[]> {
  await delay(180, signal);

  const normalized = query.trim().toLowerCase();
  if (normalized.length < 2) {
    return [];
  }

  return mockTaxonomySkillsFixture.filter(
    (item) =>
      item.name.toLowerCase().includes(normalized) ||
      item.code.toLowerCase().includes(normalized) ||
      item.categoryName.toLowerCase().includes(normalized)
  );
}
