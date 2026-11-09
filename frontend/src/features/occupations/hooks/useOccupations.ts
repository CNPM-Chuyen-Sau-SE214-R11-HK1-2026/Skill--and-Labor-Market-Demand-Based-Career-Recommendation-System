import {
  useCallback,
  useEffect,
  useState,
} from 'react';

import { getApiErrorMessage } from '../utils/apiError';

import { occupationService } from '../services/occupationService';

import type {
  OccupationSearchParams,
  OccupationSummary,
  PageResponse,
} from '../types/occupation.types';

interface UseOccupationsResult {
  data: PageResponse<OccupationSummary> | null;
  loading: boolean;
  error: string | null;
  retry: () => void;
}

export function useOccupations(
  params: OccupationSearchParams
): UseOccupationsResult {
  const [data, setData] =
    useState<PageResponse<OccupationSummary> | null>(null);

  const [loading, setLoading] = useState(true);

  const [error, setError] =
    useState<string | null>(null);

  const [requestVersion, setRequestVersion] =
    useState(0);

  useEffect(() => {
    const controller = new AbortController();
    let active = true;

    setLoading(true);
    setError(null);

    occupationService
      .getOccupations(params, controller.signal)
      .then((result: PageResponse<OccupationSummary>) => {
        if (active) {
          setData(result);
        }
      })
      .catch((cause: unknown) => {
        if (active && !controller.signal.aborted) {
          setError(getApiErrorMessage(cause));
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
      controller.abort();
    };
  }, [
    params.query,
    params.groupId,
    params.page,
    params.size,
    params.sort,
    requestVersion,
  ]);

  const retry = useCallback(
    () => setRequestVersion((version) => version + 1),
    []
  );

  return {
    data,
    loading,
    error,
    retry,
  };
}