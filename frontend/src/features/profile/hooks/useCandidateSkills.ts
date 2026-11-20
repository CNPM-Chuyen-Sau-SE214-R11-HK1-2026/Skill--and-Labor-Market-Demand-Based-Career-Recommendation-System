import { useCallback, useEffect, useRef, useState } from 'react';
import type {
  AddCandidateSkillRequest,
  CandidateSkill,
  UpdateCandidateSkillRequest,
} from '../types/profile.types';
import {
  extractProfileErrorMessage,
  isRequestAbortedError,
  profileService,
} from '../services/profileService';

export interface UseCandidateSkillsResult {
  skills: CandidateSkill[];
  isLoading: boolean;
  isSubmitting: boolean;
  deletingSkillId: number | null;
  error: string | null;
  reload: () => void;
  addSkill: (payload: AddCandidateSkillRequest) => Promise<void>;
  updateSkill: (
    skillId: number,
    payload: UpdateCandidateSkillRequest
  ) => Promise<CandidateSkill>;
  deleteSkill: (skillId: number) => Promise<void>;
}

/**
 * Manages candidate skills independently of candidate_profiles existence
 * (per docs/api/profile.md section 2.3).
 */
export function useCandidateSkills(): UseCandidateSkillsResult {
  const [skills, setSkills] = useState<CandidateSkill[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [deletingSkillId, setDeletingSkillId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [reloadToken, setReloadToken] = useState<number>(0);

  const skillsRef = useRef<CandidateSkill[]>(skills);
  skillsRef.current = skills;

  useEffect(() => {
    const controller = new AbortController();
    let isActive = true;

    const loadSkills = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const data = await profileService.getCandidateSkills(controller.signal);
        if (!isActive) return;
        setSkills(data);
      } catch (err: unknown) {
        if (!isActive || isRequestAbortedError(err)) return;
        setError(
          extractProfileErrorMessage(
            err,
            'Không thể tải danh sách kỹ năng cá nhân. Vui lòng thử lại.'
          )
        );
      } finally {
        if (isActive) {
          setIsLoading(false);
        }
      }
    };

    void loadSkills();

    return () => {
      isActive = false;
      controller.abort();
    };
  }, [reloadToken]);

  const reload = useCallback(() => {
    setReloadToken((prev) => prev + 1);
  }, []);

  const addSkill = useCallback(async (payload: AddCandidateSkillRequest): Promise<void> => {
    setIsSubmitting(true);
    try {
      await profileService.addCandidateSkill(payload);
      // Refetch full list to synchronize server-derived fields (skillCode, categoryName, verified)
      const refreshed = await profileService.getCandidateSkills();
      setSkills(refreshed);
      setError(null);
    } finally {
      setIsSubmitting(false);
    }
  }, []);

  const updateSkill = useCallback(
    async (
      skillId: number,
      payload: UpdateCandidateSkillRequest
    ): Promise<CandidateSkill> => {
      setIsSubmitting(true);
      try {
        const updated = await profileService.updateCandidateSkill(skillId, payload);
        setSkills((prev) =>
          prev.map((item) => (item.skillId === skillId ? updated : item))
        );
        setError(null);
        return updated;
      } finally {
        setIsSubmitting(false);
      }
    },
    []
  );

  const deleteSkill = useCallback(async (skillId: number): Promise<void> => {
    const previousSnapshot = skillsRef.current;
    setDeletingSkillId(skillId);
    // Optimistic removal
    setSkills((prev) => prev.filter((item) => item.skillId !== skillId));

    try {
      await profileService.deleteCandidateSkill(skillId);
    } catch (err: unknown) {
      // Rollback on API failure
      setSkills(previousSnapshot);
      throw err;
    } finally {
      setDeletingSkillId(null);
    }
  }, []);

  return {
    skills,
    isLoading,
    isSubmitting,
    deletingSkillId,
    error,
    reload,
    addSkill,
    updateSkill,
    deleteSkill,
  };
}
