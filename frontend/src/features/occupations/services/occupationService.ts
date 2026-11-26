import { apiClient } from '../../../api/client';
import { env } from '../../../config/env';
import type {
  OccupationDetail,
  OccupationSearchParams,
  OccupationSummary,
  PageResponse,
} from '../types/occupation.types';

const USE_MOCK_DATA =
  env.ENABLE_MOCK_API ||
  import.meta.env.VITE_USE_OCCUPATION_MOCKS === 'true';

/**
 * Indicates whether the occupation feature is explicitly
 * running with UI fixtures.
 */
export function isOccupationMockModeEnabled(): boolean {
  return USE_MOCK_DATA;
}

export async function getOccupations(
  params: OccupationSearchParams,
  signal?: AbortSignal
): Promise<PageResponse<OccupationSummary>> {
  if (USE_MOCK_DATA) {
    const { mockGetOccupations } =
      await import('../mocks/occupationMocks');

    return mockGetOccupations(params);
  }

  const trimmedQuery = params.query?.trim();
  const queryParams: Record<string, string | number> = {
    page: params.page ?? 0,
    size: params.size ?? 9,
    sort: params.sort ?? 'title,asc',
  };

  if (trimmedQuery) {
    queryParams.query = trimmedQuery;
  }

  if (params.groupId !== undefined) {
    queryParams.groupId = params.groupId;
  }

  const response = await apiClient.get<PageResponse<OccupationSummary>>(
    '/occupations',
    { params: queryParams, signal }
  );
  return response.data;
}

export async function getOccupationDetail(
  id: number,
  signal?: AbortSignal
): Promise<OccupationDetail> {
  if (USE_MOCK_DATA) {
    const { mockGetOccupationDetail } =
      await import('../mocks/occupationMocks');

    return mockGetOccupationDetail(id);
  }

  const response = await apiClient.get<OccupationDetail>(`/occupations/${id}`, { signal });
  return response.data;
}

/**
 * Stable service facade consumed by feature hooks and pages.
 */
export const occupationService = {
  getOccupations,
  getOccupationDetail,
};