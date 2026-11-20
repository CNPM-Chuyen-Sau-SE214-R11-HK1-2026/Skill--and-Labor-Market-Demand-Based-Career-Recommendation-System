import React, { useEffect, useMemo, useState } from 'react';
import { Button } from '../../../components/common/Button';
import type {
  CandidateProfile,
  UpdateCandidateProfileRequest,
  WorkMode,
} from '../types/profile.types';
import { WORK_MODE_OPTIONS } from '../types/profile.types';
import styles from './ProfileEditForm.module.css';

export interface ProfileEditFormProps {
  profile: CandidateProfile | null;
  isNewProfile: boolean;
  isSaving: boolean;
  onDirtyChange: (isDirty: boolean) => void;
  onSubmit: (payload: UpdateCandidateProfileRequest) => Promise<void>;
}

interface FormValues {
  headline: string;
  yearsOfExperience: string;
  expectedSalaryMin: string;
  expectedSalaryMax: string;
  preferredLocation: string;
  workMode: WorkMode;
  targetIndustry: string;
}

function buildFormValues(profile: CandidateProfile | null): FormValues {
  if (!profile) {
    return {
      headline: '',
      yearsOfExperience: '0',
      expectedSalaryMin: '',
      expectedSalaryMax: '',
      preferredLocation: '',
      workMode: 'HYBRID',
      targetIndustry: '',
    };
  }

  return {
    headline: profile.headline ?? '',
    yearsOfExperience: String(profile.yearsOfExperience ?? 0),
    expectedSalaryMin:
      profile.expectedSalaryMin !== null && profile.expectedSalaryMin !== undefined
        ? String(profile.expectedSalaryMin)
        : '',
    expectedSalaryMax:
      profile.expectedSalaryMax !== null && profile.expectedSalaryMax !== undefined
        ? String(profile.expectedSalaryMax)
        : '',
    preferredLocation: profile.preferredLocation ?? '',
    workMode: profile.workMode ?? 'HYBRID',
    targetIndustry: profile.targetIndustry ?? '',
  };
}

function areFormValuesEqual(a: FormValues, b: FormValues): boolean {
  return (
    a.headline === b.headline &&
    a.yearsOfExperience === b.yearsOfExperience &&
    a.expectedSalaryMin === b.expectedSalaryMin &&
    a.expectedSalaryMax === b.expectedSalaryMax &&
    a.preferredLocation === b.preferredLocation &&
    a.workMode === b.workMode &&
    a.targetIndustry === b.targetIndustry
  );
}

export const ProfileEditForm: React.FC<ProfileEditFormProps> = ({
  profile,
  isNewProfile,
  isSaving,
  onDirtyChange,
  onSubmit,
}) => {
  const initialValues = useMemo(() => buildFormValues(profile), [profile]);
  const [values, setValues] = useState<FormValues>(initialValues);
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof FormValues, string>>>({});

  // Sync form values when profile loads or after a successful save
  useEffect(() => {
    setValues(initialValues);
    setFieldErrors({});
  }, [initialValues]);

  const isDirty = useMemo(
    () => !areFormValuesEqual(values, initialValues),
    [values, initialValues]
  );

  useEffect(() => {
    onDirtyChange(isDirty);
  }, [isDirty, onDirtyChange]);

  const updateField = <K extends keyof FormValues>(key: K, value: FormValues[K]) => {
    setValues((prev) => ({ ...prev, [key]: value }));
    setFieldErrors((prev) => {
      if (!prev[key]) return prev;
      const next = { ...prev };
      delete next[key];
      return next;
    });
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const errors: Partial<Record<keyof FormValues, string>> = {};

    const yearsTrimmed = values.yearsOfExperience.trim();
    const parsedYears = yearsTrimmed === '' ? 0 : Number(yearsTrimmed);
    if (Number.isNaN(parsedYears) || parsedYears < 0) {
      errors.yearsOfExperience = 'Số năm kinh nghiệm phải lớn hơn hoặc bằng 0.';
    } else if (parsedYears > 99.9) {
      errors.yearsOfExperience = 'Số năm kinh nghiệm tối đa là 99.9 năm.';
    }

    const minTrimmed = values.expectedSalaryMin.trim();
    const maxTrimmed = values.expectedSalaryMax.trim();
    const parsedMin = minTrimmed === '' ? null : Number(minTrimmed);
    const parsedMax = maxTrimmed === '' ? null : Number(maxTrimmed);

    if (parsedMin !== null && (Number.isNaN(parsedMin) || parsedMin < 0)) {
      errors.expectedSalaryMin = 'Mức lương tối thiểu phải lớn hơn hoặc bằng 0.';
    }
    if (parsedMax !== null && (Number.isNaN(parsedMax) || parsedMax < 0)) {
      errors.expectedSalaryMax = 'Mức lương tối đa phải lớn hơn hoặc bằng 0.';
    }
    if (
      parsedMin !== null &&
      parsedMax !== null &&
      !errors.expectedSalaryMin &&
      !errors.expectedSalaryMax &&
      parsedMin > parsedMax
    ) {
      errors.expectedSalaryMax =
        'Mức lương tối đa phải lớn hơn hoặc bằng mức lương tối thiểu.';
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    const payload: UpdateCandidateProfileRequest = {
      headline: values.headline.trim() ? values.headline.trim() : null,
      yearsOfExperience: Math.round(parsedYears * 10) / 10,
      expectedSalaryMin: parsedMin,
      expectedSalaryMax: parsedMax,
      preferredLocation: values.preferredLocation.trim()
        ? values.preferredLocation.trim()
        : null,
      workMode: values.workMode,
      targetIndustry: values.targetIndustry.trim()
        ? values.targetIndustry.trim()
        : null,
    };

    await onSubmit(payload);
  };

  return (
    <form onSubmit={handleSubmit} className={styles.form} noValidate>
      {isNewProfile && (
        <div className={styles.infoBanner} role="status">
          <strong>Chưa có hồ sơ ứng viên:</strong> Hãy điền thông tin nguyện vọng nghề nghiệp bên dưới và nhấn <em>Lưu thay đổi</em> để khởi tạo hồ sơ của bạn.
        </div>
      )}

      <div className={styles.fieldGroup}>
        <label htmlFor="profile-headline" className={styles.label}>
          Tiêu đề nghề nghiệp (Headline)
        </label>
        <input
          id="profile-headline"
          type="text"
          className={styles.input}
          placeholder="Ví dụ: Fullstack Java & React Developer"
          value={values.headline}
          onChange={(e) => updateField('headline', e.target.value)}
        />
      </div>

      <div className={styles.rowTwoCols}>
        <div className={styles.fieldGroup}>
          <label htmlFor="profile-years" className={styles.label}>
            Tổng số năm kinh nghiệm
          </label>
          <input
            id="profile-years"
            type="number"
            inputMode="decimal"
            min="0"
            max="99.9"
            step="0.5"
            className={`${styles.input} ${
              fieldErrors.yearsOfExperience ? styles.inputError : ''
            }`}
            value={values.yearsOfExperience}
            onChange={(e) => updateField('yearsOfExperience', e.target.value)}
          />
          {fieldErrors.yearsOfExperience && (
            <span className={styles.errorText} role="alert">
              {fieldErrors.yearsOfExperience}
            </span>
          )}
        </div>

        <div className={styles.fieldGroup}>
          <label htmlFor="profile-workmode" className={styles.label}>
            Chế độ làm việc mong muốn
          </label>
          <select
            id="profile-workmode"
            className={styles.select}
            value={values.workMode}
            onChange={(e) => updateField('workMode', e.target.value as WorkMode)}
          >
            {WORK_MODE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className={styles.rowTwoCols}>
        <div className={styles.fieldGroup}>
          <label htmlFor="profile-salary-min" className={styles.label}>
            Lương kỳ vọng tối thiểu (VNĐ)
          </label>
          <input
            id="profile-salary-min"
            type="number"
            inputMode="numeric"
            min="0"
            step="500000"
            placeholder="Ví dụ: 15000000"
            className={`${styles.input} ${
              fieldErrors.expectedSalaryMin ? styles.inputError : ''
            }`}
            value={values.expectedSalaryMin}
            onChange={(e) => updateField('expectedSalaryMin', e.target.value)}
          />
          {fieldErrors.expectedSalaryMin && (
            <span className={styles.errorText} role="alert">
              {fieldErrors.expectedSalaryMin}
            </span>
          )}
        </div>

        <div className={styles.fieldGroup}>
          <label htmlFor="profile-salary-max" className={styles.label}>
            Lương kỳ vọng tối đa (VNĐ)
          </label>
          <input
            id="profile-salary-max"
            type="number"
            inputMode="numeric"
            min="0"
            step="500000"
            placeholder="Ví dụ: 25000000"
            className={`${styles.input} ${
              fieldErrors.expectedSalaryMax ? styles.inputError : ''
            }`}
            value={values.expectedSalaryMax}
            onChange={(e) => updateField('expectedSalaryMax', e.target.value)}
          />
          {fieldErrors.expectedSalaryMax && (
            <span className={styles.errorText} role="alert">
              {fieldErrors.expectedSalaryMax}
            </span>
          )}
        </div>
      </div>

      <div className={styles.rowTwoCols}>
        <div className={styles.fieldGroup}>
          <label htmlFor="profile-location" className={styles.label}>
            Địa điểm làm việc ưu tiên
          </label>
          <input
            id="profile-location"
            type="text"
            className={styles.input}
            placeholder="Ví dụ: Hồ Chí Minh, Hà Nội, Đà Nẵng"
            value={values.preferredLocation}
            onChange={(e) => updateField('preferredLocation', e.target.value)}
          />
        </div>

        <div className={styles.fieldGroup}>
          <label htmlFor="profile-industry" className={styles.label}>
            Ngành nghề mục tiêu
          </label>
          <input
            id="profile-industry"
            type="text"
            className={styles.input}
            placeholder="Ví dụ: Công nghệ thông tin"
            value={values.targetIndustry}
            onChange={(e) => updateField('targetIndustry', e.target.value)}
          />
        </div>
      </div>

      <div className={styles.actions}>
        {isDirty && (
          <span className={styles.dirtyHint}>Bạn có thay đổi chưa lưu</span>
        )}
        <div className={styles.actionButtons}>
          {isDirty && (
            <Button
              type="button"
              variant="outline"
              disabled={isSaving}
              onClick={() => {
                setValues(initialValues);
                setFieldErrors({});
              }}
            >
              Hoàn tác
            </Button>
          )}
          <Button
            type="submit"
            variant="primary"
            isLoading={isSaving}
            disabled={!isDirty && !isNewProfile}
          >
            {isNewProfile ? 'Khởi tạo hồ sơ' : 'Lưu thay đổi'}
          </Button>
        </div>
      </div>
    </form>
  );
};
