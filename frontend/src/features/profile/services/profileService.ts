import axios from 'axios';
import { apiClient } from '../../../api/client';
import { env } from '../../../config/env';
import type { ApiErrorResponse } from '../../../types/api';
import type {
  AddCandidateSkillRequest,
  AddCandidateSkillResponse,
  CandidateProfile,
  CandidateSkill,
  ProfileApiError,
  SkillCatalogItem,
  UpdateCandidateProfileRequest,
  UpdateCandidateSkillRequest,
} from '../types/profile.types';

const USE_MOCK_DATA =
  env.ENABLE_MOCK_API ||
  import.meta.env.VITE_USE_PROFILE_MOCKS === 'true';

/**
 * Structural type guard for MockApiError so profileMockAdapter.ts stays
 * strictly dynamically imported (never statically pulled into production bundle).
 */
function isMockApiError(error: unknown): error is Error & ProfileApiError {
  if (!(error instanceof Error) || error.name !== 'MockApiError') {
    return false;
  }
  const candidate = error as Partial<ProfileApiError>;
  return typeof candidate.status === 'number' && typeof candidate.error === 'string';
}

/**
 * Indicates whether the profile feature is running against the local mock adapter.
 */
export function isProfileMockModeEnabled(): boolean {
  return USE_MOCK_DATA;
}

/**
 * Type guard for cancelled / aborted requests (Axios CanceledError or DOM AbortError).
 */
export function isRequestAbortedError(error: unknown): boolean {
  if (axios.isCancel(error)) {
    return true;
  }
  if (error instanceof DOMException && error.name === 'AbortError') {
    return true;
  }
  if (axios.isAxiosError(error) && error.code === 'ERR_CANCELED') {
    return true;
  }
  return false;
}

/**
 * Checks whether an error corresponds to HTTP 404 PROFILE_NOT_FOUND
 * (normal state for a new candidate who has not created a profile yet).
 */
export function isProfileNotFoundError(error: unknown): boolean {
  if (isMockApiError(error)) {
    return error.status === 404 && error.error === 'PROFILE_NOT_FOUND';
  }
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const status = error.response?.status;
    const errorCode = error.response?.data?.error;
    return status === 404 && (!errorCode || errorCode === 'PROFILE_NOT_FOUND');
  }
  return false;
}

/**
 * Extracts a user-friendly error message from Axios or Mock API errors.
 */
export function extractProfileErrorMessage(error: unknown, fallback: string): string {
  if (isMockApiError(error)) {
    return error.message || fallback;
  }
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const apiMessage = error.response?.data?.message;
    if (typeof apiMessage === 'string' && apiMessage.trim().length > 0) {
      return apiMessage;
    }
    if (!error.response) {
      return 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra đường truyền và thử lại.';
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export async function getCandidateProfile(
  signal?: AbortSignal
): Promise<CandidateProfile> {
  if (USE_MOCK_DATA) {
    const { mockGetCandidateProfile } = await import('../mocks/profileMockAdapter');
    return mockGetCandidateProfile(signal);
  }

  const response = await apiClient.get<CandidateProfile>('/profile', { signal });
  return response.data;
}

export async function updateCandidateProfile(
  payload: UpdateCandidateProfileRequest
): Promise<CandidateProfile> {
  if (USE_MOCK_DATA) {
    const { mockUpdateCandidateProfile } = await import('../mocks/profileMockAdapter');
    return mockUpdateCandidateProfile(payload);
  }

  const response = await apiClient.put<CandidateProfile>('/profile', payload);
  return response.data;
}

export async function getCandidateSkills(
  signal?: AbortSignal
): Promise<CandidateSkill[]> {
  if (USE_MOCK_DATA) {
    const { mockGetCandidateSkills } = await import('../mocks/profileMockAdapter');
    return mockGetCandidateSkills(signal);
  }

  const response = await apiClient.get<CandidateSkill[]>('/profile/skills', { signal });
  return response.data;
}

export async function addCandidateSkill(
  payload: AddCandidateSkillRequest
): Promise<AddCandidateSkillResponse> {
  if (USE_MOCK_DATA) {
    const { mockAddCandidateSkill } = await import('../mocks/profileMockAdapter');
    return mockAddCandidateSkill(payload);
  }

  const response = await apiClient.post<AddCandidateSkillResponse>('/profile/skills', payload);
  return response.data;
}

export async function updateCandidateSkill(
  skillId: number,
  payload: UpdateCandidateSkillRequest
): Promise<CandidateSkill> {
  if (USE_MOCK_DATA) {
    const { mockUpdateCandidateSkill } = await import('../mocks/profileMockAdapter');
    return mockUpdateCandidateSkill(skillId, payload);
  }

  const response = await apiClient.put<CandidateSkill>(
    `/profile/skills/${skillId}`,
    payload
  );
  return response.data;
}

export async function deleteCandidateSkill(skillId: number): Promise<void> {
  if (USE_MOCK_DATA) {
    const { mockDeleteCandidateSkill } = await import('../mocks/profileMockAdapter');
    return mockDeleteCandidateSkill(skillId);
  }

  await apiClient.delete(`/profile/skills/${skillId}`);
}

export async function searchSkills(
  query: string,
  signal?: AbortSignal
): Promise<SkillCatalogItem[]> {
  const trimmed = query.trim();
  if (trimmed.length < 2) {
    return [];
  }

  if (USE_MOCK_DATA) {
    const { mockSearchSkills } = await import('../mocks/profileMockAdapter');
    return mockSearchSkills(trimmed, signal);
  }

  const response = await apiClient.get<SkillCatalogItem[]>('/skills', {
    params: { query: trimmed },
    signal,
  });
  return response.data;
}

export const profileService = {
  isProfileMockModeEnabled,
  getCandidateProfile,
  updateCandidateProfile,
  getCandidateSkills,
  addCandidateSkill,
  updateCandidateSkill,
  deleteCandidateSkill,
  searchSkills,
};
