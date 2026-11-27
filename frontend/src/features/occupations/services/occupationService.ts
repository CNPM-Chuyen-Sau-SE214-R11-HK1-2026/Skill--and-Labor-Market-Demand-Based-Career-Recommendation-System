import { apiClient } from '../../../api/client';
import type {
  OccupationDetail,
  OccupationSearchParams,
  OccupationSummary,
  PageResponse,
} from '../types/occupation.types';

export async function getOccupations(
  params: OccupationSearchParams,
  signal?: AbortSignal
): Promise<PageResponse<OccupationSummary>> {
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