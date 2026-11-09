import {
  useEffect,
  useState,
} from 'react';

import {
  getApiErrorMessage,
  isNotFoundError,
} from '../utils/apiError';

import {
  occupationService,
} from '../services/occupationService';

import type {
  OccupationDetail,
} from '../types/occupation.types';

interface UseOccupationDetailResult {
  data: OccupationDetail | null;
  loading: boolean;
  error: string | null;
  notFound: boolean;
}

export function useOccupationDetail(
  id: number | null
): UseOccupationDetailResult {
  const [data, setData] =
    useState<OccupationDetail | null>(null);

  const [loading, setLoading] =
    useState(id !== null);

  const [error, setError] =
    useState<string | null>(null);

  const [notFound, setNotFound] =
    useState(false);

  useEffect(() => {
    if (
      id === null ||
      !Number.isSafeInteger(id) ||
      id <= 0
    ) {
      setData(null);
      setLoading(false);
      setError('Mã nghề nghiệp không hợp lệ.');
      setNotFound(false);
      return;
    }

    const controller = new AbortController();
    let active = true;

    setLoading(true);
    setError(null);
    setNotFound(false);

    occupationService
      .getOccupationDetail(id, controller.signal)
      .then((result: OccupationDetail) => {
        if (active) {
          setData(result);
        }
      })
      .catch((cause: unknown) => {
        if (active && !controller.signal.aborted) {
          setData(null);

          setError(getApiErrorMessage(cause));

          setNotFound(
            isNotFoundError(cause) ||
            (
              cause instanceof Error &&
              cause.message
                .toLocaleLowerCase('vi-VN')
                .includes('không tìm thấy')
            )
          );
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
  }, [id]);

  return {
    data,
    loading,
    error,
    notFound,
  };
}