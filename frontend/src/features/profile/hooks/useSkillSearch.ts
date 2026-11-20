import { useEffect, useRef, useState } from 'react';
import type { SkillCatalogItem } from '../types/profile.types';
import {
  extractProfileErrorMessage,
  isRequestAbortedError,
  profileService,
} from '../services/profileService';

const SEARCH_DEBOUNCE_MS = 300;
export const MIN_SKILL_QUERY_LENGTH = 2;

export interface UseSkillSearchResult {
  results: SkillCatalogItem[];
  isSearching: boolean;
  error: string | null;
  isQueryTooShort: boolean;
}

export function useSkillSearch(query: string): UseSkillSearchResult {
  const [results, setResults] = useState<SkillCatalogItem[]>([]);
  const [isSearching, setIsSearching] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const requestIdRef = useRef<number>(0);
  const trimmedQuery = query.trim();
  const isQueryTooShort = trimmedQuery.length < MIN_SKILL_QUERY_LENGTH;

  useEffect(() => {
    if (isQueryTooShort) {
      requestIdRef.current += 1;
      setResults([]);
      setIsSearching(false);
      setError(null);
      return;
    }

    const controller = new AbortController();
    const currentRequestId = ++requestIdRef.current;

    const timer = window.setTimeout(async () => {
      setIsSearching(true);
      setError(null);

      try {
        const data = await profileService.searchSkills(trimmedQuery, controller.signal);
        if (requestIdRef.current !== currentRequestId) {
          return;
        }
        setResults(data);
      } catch (err: unknown) {
        if (isRequestAbortedError(err) || requestIdRef.current !== currentRequestId) {
          return;
        }
        setResults([]);
        setError(
          extractProfileErrorMessage(
            err,
            'Không thể tìm kiếm kỹ năng lúc này. Vui lòng thử lại.'
          )
        );
      } finally {
        if (requestIdRef.current === currentRequestId) {
          setIsSearching(false);
        }
      }
    }, SEARCH_DEBOUNCE_MS);

    return () => {
      window.clearTimeout(timer);
      controller.abort();
    };
  }, [trimmedQuery, isQueryTooShort]);

  return {
    results,
    isSearching,
    error,
    isQueryTooShort,
  };
}
