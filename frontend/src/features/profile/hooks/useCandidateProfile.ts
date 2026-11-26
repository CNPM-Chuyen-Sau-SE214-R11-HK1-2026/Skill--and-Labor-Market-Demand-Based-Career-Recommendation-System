import { useCallback, useEffect, useState } from 'react';
import type {
  CandidateProfile,
  UpdateCandidateProfileRequest,
} from '../types/profile.types';
import {
  extractProfileErrorMessage,
  isProfileNotFoundError,
  isRequestAbortedError,
  profileService,
} from '../services/profileService';

export interface UseCandidateProfileResult {
  profile: CandidateProfile | null;
  isNewProfile: boolean;
  isLoading: boolean;
  isSaving: boolean;
  error: string | null;
  reload: () => void;
  saveProfile: (payload: UpdateCandidateProfileRequest) => Promise<CandidateProfile>;
}

export function useCandidateProfile(): UseCandidateProfileResult {
  const [profile, setProfile] = useState<CandidateProfile | null>(null);
  const [isNewProfile, setIsNewProfile] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSaving, setIsSaving] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [reloadToken, setReloadToken] = useState<number>(0);

  useEffect(() => {
    const controller = new AbortController();
    let isActive = true;

    const loadProfile = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const data = await profileService.getCandidateProfile(controller.signal);
        if (!isActive) return;
        setProfile(data);
        setIsNewProfile(false);
      } catch (err: unknown) {
        if (!isActive || isRequestAbortedError(err)) return;

        if (isProfileNotFoundError(err)) {
          setProfile(null);
          setIsNewProfile(true);
          setError(null);
        } else {
          setError(
            extractProfileErrorMessage(
              err,
              'Không thể tải thông tin hồ sơ cá nhân. Vui lòng thử lại.'
            )
          );
        }
      } finally {
        if (isActive) {
          setIsLoading(false);
        }
      }
    };

    void loadProfile();

    return () => {
      isActive = false;
      controller.abort();
    };
  }, [reloadToken]);

  const reload = useCallback(() => {
    setReloadToken((prev) => prev + 1);
  }, []);

  const saveProfile = useCallback(
    async (payload: UpdateCandidateProfileRequest): Promise<CandidateProfile> => {
      setIsSaving(true);
      try {
        const updated = await profileService.updateCandidateProfile(payload);
        setProfile(updated);
        setIsNewProfile(false);
        setError(null);
        return updated;
      } finally {
        setIsSaving(false);
      }
    },
    []
  );

  return {
    profile,
    isNewProfile,
    isLoading,
    isSaving,
    error,
    reload,
    saveProfile,
  };
}
