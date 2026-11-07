import type {
  OccupationDetail,
  OccupationSearchParams,
  OccupationSummary,
  PageResponse,
} from '../types/occupation.types';

const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL ||
  'http://localhost:8080/api/v1'
).replace(/\/$/, '');

const USE_MOCK_DATA =
  import.meta.env.VITE_USE_OCCUPATION_MOCKS === 'true';

/**
 * Indicates whether the occupation feature is explicitly
 * running with UI fixtures.
 */
export function isOccupationMockModeEnabled(): boolean {
  return USE_MOCK_DATA;
}

function getErrorMessage(
  payload: unknown,
  fallback: string
): string {
  if (
    typeof payload === 'object' &&
    payload !== null &&
    'message' in payload
  ) {
    const message = (
      payload as { message?: unknown }
    ).message;

    if (typeof message === 'string' && message.trim()) {
      return message;
    }
  }

  return fallback;
}

async function request<T>(
  path: string,
  signal?: AbortSignal
): Promise<T> {
  const token = localStorage.getItem('token');

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'GET',
    headers: {
      Accept: 'application/json',
      ...(token
        ? { Authorization: `Bearer ${token}` }
        : {}),
    },
    signal,
  });

  if (!response.ok) {
    let payload: unknown = null;

    try {
      payload = await response.json();
    } catch {
      // Response may be empty.
    }

    throw new Error(
      getErrorMessage(
        payload,
        `Không thể tải dữ liệu (HTTP ${response.status}).`
      )
    );
  }

  return response.json() as Promise<T>;
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

  const search = new URLSearchParams();

  if (params.query?.trim()) {
    search.set('query', params.query.trim());
  }

  if (params.groupId !== undefined) {
    search.set('groupId', String(params.groupId));
  }

  search.set('page', String(params.page ?? 0));
  search.set('size', String(params.size ?? 9));
  search.set('sort', params.sort ?? 'title,asc');

  return request<PageResponse<OccupationSummary>>(
    `/occupations?${search.toString()}`,
    signal
  );
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

  return request<OccupationDetail>(
    `/occupations/${id}`,
    signal
  );
}

/**
 * Stable service facade consumed by feature hooks and pages.
 */
export const occupationService = {
  getOccupations,
  getOccupationDetail,
};