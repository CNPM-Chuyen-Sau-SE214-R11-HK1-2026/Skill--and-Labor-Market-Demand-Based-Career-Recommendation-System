import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useBlocker, useLocation, useNavigate } from 'react-router-dom';
import { Alert } from '../../../components/common/Alert';
import { Button } from '../../../components/common/Button';
import { LoadingSpinner } from '../../../components/common/LoadingSpinner';
import { useAuth } from '../../../hooks/useAuth';
import { ProfileEditForm } from '../components/ProfileEditForm';
import { SkillBadgeList } from '../components/SkillBadgeList';
import { SkillModal } from '../components/SkillModal';
import type { SkillModalTarget } from '../components/SkillModal';
import { SkillSelector } from '../components/SkillSelector';
import { ToastContainer } from '../components/Toast';
import { useCandidateProfile } from '../hooks/useCandidateProfile';
import { useCandidateSkills } from '../hooks/useCandidateSkills';
import { useToast } from '../hooks/useToast';
import {
  extractProfileErrorMessage,
  isProfileMockModeEnabled,
} from '../services/profileService';
import type {
  CandidateSkill,
  ProficiencyLevel,
  SkillCatalogItem,
  UpdateCandidateProfileRequest,
} from '../types/profile.types';
import styles from './ProfilePage.module.css';

type ProfileTab = 'profile' | 'skills';

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const activeTab: ProfileTab = location.pathname.startsWith('/profile/skills')
    ? 'skills'
    : 'profile';

  const {
    profile,
    isNewProfile,
    isLoading: isProfileLoading,
    isSaving: isProfileSaving,
    error: profileError,
    reload: reloadProfile,
    saveProfile,
  } = useCandidateProfile();

  const {
    skills,
    isLoading: isSkillsLoading,
    isSubmitting: isSkillSubmitting,
    deletingSkillId,
    error: skillsError,
    reload: reloadSkills,
    addSkill,
    updateSkill,
    deleteSkill,
  } = useCandidateSkills();

  const { toasts, showToast, dismissToast } = useToast();

  const [isProfileDirty, setIsProfileDirty] = useState<boolean>(false);
  const [modalTarget, setModalTarget] = useState<SkillModalTarget | null>(null);

  const skillsSectionRef = useRef<HTMLElement>(null);
  const skillInputRef = useRef<HTMLInputElement>(null);

  const existingSkillIds = useMemo(
    () => new Set<number>(skills.map((item) => item.skillId)),
    [skills]
  );

  // Desktop behavior for /profile/skills: scroll to and focus the skills section
  useEffect(() => {
    if (activeTab !== 'skills') return;
    if (window.innerWidth >= 1024) {
      skillsSectionRef.current?.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      });
      const timer = window.setTimeout(() => {
        skillInputRef.current?.focus();
      }, 150);
      return () => window.clearTimeout(timer);
    }
  }, [activeTab]);

  // Warn before closing/reloading browser tab when form has unsaved changes
  useEffect(() => {
    if (!isProfileDirty) return;

    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();
      event.returnValue = '';
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [isProfileDirty]);

  // Block in-app navigation ONLY when leaving /profile* routes (switching tabs is allowed)
  const blocker = useBlocker(
    ({ nextLocation }) =>
      isProfileDirty && !nextLocation.pathname.startsWith('/profile')
  );

  const handleDirtyChange = useCallback((dirty: boolean) => {
    setIsProfileDirty(dirty);
  }, []);

  const handleTabSwitch = (tab: ProfileTab) => {
    const targetPath = tab === 'skills' ? '/profile/skills' : '/profile';
    if (location.pathname !== targetPath) {
      navigate(targetPath);
    }
  };

  const handleProfileSave = async (payload: UpdateCandidateProfileRequest) => {
    try {
      await saveProfile(payload);
      setIsProfileDirty(false);
      showToast('success', 'Hồ sơ cá nhân đã được lưu thành công.');
    } catch (err: unknown) {
      showToast(
        'error',
        extractProfileErrorMessage(
          err,
          'Không thể lưu hồ sơ cá nhân. Vui lòng kiểm tra lại thông tin.'
        )
      );
    }
  };

  const handleSelectCatalogSkill = (skill: SkillCatalogItem) => {
    setModalTarget({ mode: 'add', skill });
  };

  const handleEditSkill = (skill: CandidateSkill) => {
    setModalTarget({ mode: 'edit', skill });
  };

  const handleModalConfirm = async (data: {
    proficiencyLevel: ProficiencyLevel;
    yearsExperience: number;
  }) => {
    if (!modalTarget) return;

    try {
      if (modalTarget.mode === 'add') {
        await addSkill({
          skillId: modalTarget.skill.id,
          proficiencyLevel: data.proficiencyLevel,
          yearsExperience: data.yearsExperience,
        });
        showToast('success', `Đã thêm kỹ năng "${modalTarget.skill.name}" vào hồ sơ.`);
      } else {
        await updateSkill(modalTarget.skill.skillId, {
          proficiencyLevel: data.proficiencyLevel,
          yearsExperience: data.yearsExperience,
        });
        showToast('success', `Đã cập nhật kỹ năng "${modalTarget.skill.skillName}".`);
      }
      setModalTarget(null);
    } catch (err: unknown) {
      showToast(
        'error',
        extractProfileErrorMessage(
          err,
          modalTarget.mode === 'add'
            ? 'Không thể thêm kỹ năng. Vui lòng thử lại.'
            : 'Không thể cập nhật kỹ năng. Vui lòng thử lại.'
        )
      );
    }
  };

  const handleDeleteSkill = async (skill: CandidateSkill) => {
    try {
      await deleteSkill(skill.skillId);
      showToast('success', `Đã gỡ bỏ kỹ năng "${skill.skillName}" khỏi hồ sơ.`);
    } catch (err: unknown) {
      showToast(
        'error',
        extractProfileErrorMessage(
          err,
          `Không thể xóa kỹ năng "${skill.skillName}". Danh sách đã được khôi phục.`
        )
      );
    }
  };

  return (
    <div className={`container ${styles.pageContainer}`}>
      {/* Page Header */}
      <header className={styles.heroCard}>
        <div className={styles.heroInfo}>
          <div className={styles.titleRow}>
            <h1 className={styles.pageTitle}>Hồ sơ ứng viên &amp; Kỹ năng cá nhân</h1>
            {isProfileMockModeEnabled() && (
              <span className={styles.mockBadge}>Mock Mode</span>
            )}
          </div>
          <p className={styles.pageSubtitle}>
            Cập nhật thông tin nguyện vọng nghề nghiệp và danh sách kỹ năng chuẩn hóa để nhận gợi ý việc làm phù hợp nhất.
          </p>
          {user && (
            <div className={styles.accountMeta}>
              <span>
                Ứng viên: <strong>{user.fullName}</strong>
              </span>
              <span className={styles.metaDot} aria-hidden="true">
                •
              </span>
              <span>{user.email}</span>
              <span className={styles.metaDot} aria-hidden="true">
                •
              </span>
              <span>
                Kỹ năng đã khai báo: <strong>{skills.length}</strong>
              </span>
            </div>
          )}
        </div>
      </header>

      {/* Mobile / Tablet Tab Bar (< 1024px) */}
      <div className={styles.tabBar} role="tablist" aria-label="Chuyển mục hồ sơ">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'profile'}
          className={`${styles.tabButton} ${
            activeTab === 'profile' ? styles.tabButtonActive : ''
          }`}
          onClick={() => handleTabSwitch('profile')}
        >
          Hồ sơ cá nhân
          {isProfileDirty && <span className={styles.unsavedDot} aria-label="Chưa lưu" />}
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'skills'}
          className={`${styles.tabButton} ${
            activeTab === 'skills' ? styles.tabButtonActive : ''
          }`}
          onClick={() => handleTabSwitch('skills')}
        >
          Kỹ năng cá nhân
          <span className={styles.tabCount}>{skills.length}</span>
        </button>
      </div>

      {/* Main Content Grid: Always mount both panels to preserve form state across tab switches */}
      <div className={styles.contentGrid}>
        {/* Left Column: Candidate Profile Form */}
        <section
          aria-labelledby="profile-section-heading"
          className={`${styles.card} ${
            activeTab !== 'profile' ? styles.hiddenOnCompact : ''
          }`}
        >
          <div className={styles.cardHeader}>
            <div>
              <h2 id="profile-section-heading" className={styles.cardTitle}>
                Thông tin &amp; Nguyện vọng nghề nghiệp
              </h2>
              <p className={styles.cardDescription}>
                Điền thông tin kinh nghiệm, mức lương kỳ vọng và hình thức làm việc bạn mong muốn.
              </p>
            </div>
          </div>

          {isProfileLoading ? (
            <LoadingSpinner text="Đang tải thông tin hồ sơ..." />
          ) : profileError ? (
            <div className={styles.errorBox}>
              <Alert type="error" message={profileError} />
              <Button type="button" variant="outline" onClick={reloadProfile}>
                Thử lại
              </Button>
            </div>
          ) : (
            <ProfileEditForm
              profile={profile}
              isNewProfile={isNewProfile}
              isSaving={isProfileSaving}
              onDirtyChange={handleDirtyChange}
              onSubmit={handleProfileSave}
            />
          )}
        </section>

        {/* Right Column: Skill Selector & Candidate Skills List */}
        <section
          ref={skillsSectionRef}
          aria-labelledby="skills-section-heading"
          className={`${styles.card} ${
            activeTab !== 'skills' ? styles.hiddenOnCompact : ''
          }`}
        >
          <div className={styles.cardHeader}>
            <div>
              <h2 id="skills-section-heading" className={styles.cardTitle}>
                Kỹ năng cá nhân ({skills.length})
              </h2>
              <p className={styles.cardDescription}>
                Tìm kiếm kỹ năng từ danh mục chuẩn, chọn mức độ thành thạo và số năm kinh nghiệm. Các thay đổi kỹ năng được lưu tự động ngay lập tức.
              </p>
            </div>
          </div>

          <div className={styles.skillsContent}>
            <SkillSelector
              existingSkillIds={existingSkillIds}
              onSelectSkill={handleSelectCatalogSkill}
              inputRef={skillInputRef}
              disabled={isSkillsLoading}
            />

            {isSkillsLoading ? (
              <LoadingSpinner text="Đang tải danh sách kỹ năng..." />
            ) : skillsError ? (
              <div className={styles.errorBox}>
                <Alert type="error" message={skillsError} />
                <Button type="button" variant="outline" onClick={reloadSkills}>
                  Thử lại
                </Button>
              </div>
            ) : (
              <SkillBadgeList
                skills={skills}
                deletingSkillId={deletingSkillId}
                onEdit={handleEditSkill}
                onDelete={handleDeleteSkill}
              />
            )}
          </div>
        </section>
      </div>

      {/* Add / Edit Skill Modal */}
      <SkillModal
        target={modalTarget}
        isSubmitting={isSkillSubmitting}
        onClose={() => setModalTarget(null)}
        onConfirm={handleModalConfirm}
      />

      {/* Unsaved Changes Navigation Confirmation Modal */}
      {blocker.state === 'blocked' && (
        <div className={styles.blockerBackdrop}>
          <div
            className={styles.blockerDialog}
            role="dialog"
            aria-modal="true"
            aria-labelledby="unsaved-dialog-title"
          >
            <h2 id="unsaved-dialog-title" className={styles.blockerTitle}>
              Thay đổi chưa được lưu
            </h2>
            <p className={styles.blockerMessage}>
              Biểu mẫu hồ sơ cá nhân của bạn đang có thay đổi chưa lưu. Nếu rời khỏi trang này, các thay đổi sẽ bị mất.
            </p>
            <div className={styles.blockerActions}>
              <Button
                type="button"
                variant="outline"
                onClick={() => blocker.reset()}
              >
                Ở lại trang
              </Button>
              <Button
                type="button"
                variant="danger"
                onClick={() => blocker.proceed()}
              >
                Rời khỏi không lưu
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Toast Notifications */}
      <ToastContainer toasts={toasts} onDismiss={dismissToast} />
    </div>
  );
};

export default ProfilePage;
