import { useEffect, useState } from 'react';
import { getOccupationDetail } from '../services/occupationService';
import type { OccupationDetail } from '../types/occupation.types';
import { getApiErrorMessage, isNotFoundError } from '../utils/apiError';

type DetailState =
  | { status: 'loading' }
  | { status: 'notFound' }
  | { status: 'ready'; data: OccupationDetail }
  | { status: 'error'; message: string };

export function useOccupationDetail(id: number | null) {
  const [attempt, setAttempt] = useState(0);
  const [result, setResult] = useState<{ id: number; attempt: number; state: DetailState }>();

  useEffect(() => {
    if (id === null) return;

    const controller = new AbortController();
    const settle = (state: DetailState) => {
      if (!controller.signal.aborted) setResult({ id, attempt, state });
    };

    getOccupationDetail(id, controller.signal).then(
      (data) => settle({ status: 'ready', data }),
      (error: unknown) => settle(isNotFoundError(error)
        ? { status: 'notFound' }
        : { status: 'error', message: getApiErrorMessage(error) }),
    );

    return () => controller.abort();
  }, [id, attempt]);

  let state: DetailState = { status: 'loading' };
  if (id === null) state = { status: 'notFound' };
  else if (result?.id === id && result.attempt === attempt) state = result.state;

  return { state, retry: () => setAttempt((current) => current + 1) };
}
