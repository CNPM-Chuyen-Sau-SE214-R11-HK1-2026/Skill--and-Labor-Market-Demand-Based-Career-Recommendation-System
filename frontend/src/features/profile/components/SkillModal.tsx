import React, { useEffect, useId, useRef, useState } from 'react';
import { Button } from '../../../components/common/Button';
import type {
  CandidateSkill,
  ProficiencyLevel,
  SkillCatalogItem,
} from '../types/profile.types';
import { PROFICIENCY_LEVEL_OPTIONS } from '../types/profile.types';
import styles from './SkillModal.module.css';

export type SkillModalTarget =
  | { mode: 'add'; skill: SkillCatalogItem }
  | { mode: 'edit'; skill: CandidateSkill };

export interface SkillModalProps {
  target: SkillModalTarget | null;
  isSubmitting: boolean;
  onClose: () => void;
  onConfirm: (data: {
    proficiencyLevel: ProficiencyLevel;
    yearsExperience: number;
  }) => Promise<void>;
}

const FOCUSABLE_SELECTOR =
  'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

export const SkillModal: React.FC<SkillModalProps> = ({
  target,
  isSubmitting,
  onClose,
  onConfirm,
}) => {
  const [proficiencyLevel, setProficiencyLevel] =
    useState<ProficiencyLevel>('INTERMEDIATE');
  const [yearsInput, setYearsInput] = useState<string>('1');
  const [validationError, setValidationError] = useState<string | null>(null);

  const dialogRef = useRef<HTMLDivElement>(null);
  const previousFocusRef = useRef<HTMLElement | null>(null);
  const titleId = useId();
  const yearsInputId = useId();

  // Initialize form state when modal opens
  useEffect(() => {
    if (!target) return;

    previousFocusRef.current = document.activeElement as HTMLElement | null;
    setValidationError(null);

    if (target.mode === 'edit') {
      setProficiencyLevel(target.skill.proficiencyLevel);
      setYearsInput(String(target.skill.yearsExperience));
    } else {
      setProficiencyLevel('INTERMEDIATE');
      setYearsInput('1');
    }

    // Focus first interactive element inside modal
    const frame = window.requestAnimationFrame(() => {
      const dialog = dialogRef.current;
      if (!dialog) return;
      const focusable = dialog.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR);
      if (focusable.length > 0) {
        focusable[0].focus();
      }
    });

    return () => {
      window.cancelAnimationFrame(frame);
      if (previousFocusRef.current && typeof previousFocusRef.current.focus === 'function') {
        previousFocusRef.current.focus();
      }
    };
  }, [target]);

  // Keyboard handling: Escape to close & Tab focus trap
  useEffect(() => {
    if (!target) return;

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !isSubmitting) {
        event.preventDefault();
        onClose();
        return;
      }

      if (event.key === 'Tab') {
        const dialog = dialogRef.current;
        if (!dialog) return;
        const focusable = Array.from(
          dialog.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR)
        );
        if (focusable.length === 0) return;

        const first = focusable[0];
        const last = focusable[focusable.length - 1];

        if (event.shiftKey) {
          if (document.activeElement === first || !dialog.contains(document.activeElement)) {
            event.preventDefault();
            last.focus();
          }
        } else {
          if (document.activeElement === last || !dialog.contains(document.activeElement)) {
            event.preventDefault();
            first.focus();
          }
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [target, isSubmitting, onClose]);

  if (!target) {
    return null;
  }

  const skillName =
    target.mode === 'add' ? target.skill.name : target.skill.skillName;
  const skillCategory = target.skill.categoryName;
  const skillCode =
    target.mode === 'add' ? target.skill.code : target.skill.skillCode;

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setValidationError(null);

    const trimmedYears = yearsInput.trim();
    let parsedYears = 0;

    if (trimmedYears !== '') {
      parsedYears = Number(trimmedYears);
      if (Number.isNaN(parsedYears) || parsedYears < 0) {
        setValidationError('Số năm kinh nghiệm phải là số lớn hơn hoặc bằng 0.');
        return;
      }
      if (parsedYears > 99.9) {
        setValidationError('Số năm kinh nghiệm tối đa là 99.9 năm.');
        return;
      }
    }

    await onConfirm({
      proficiencyLevel,
      yearsExperience: Math.round(parsedYears * 10) / 10,
    });
  };

  return (
    <div
      className={styles.backdrop}
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !isSubmitting) {
          onClose();
        }
      }}
    >
      <div
        ref={dialogRef}
        className={styles.dialog}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <div className={styles.header}>
          <div>
            <h2 id={titleId} className={styles.title}>
              {target.mode === 'add' ? 'Thêm kỹ năng vào hồ sơ' : 'Cập nhật kỹ năng'}
            </h2>
            <p className={styles.subtitle}>
              <strong>{skillName}</strong> · {skillCategory} ({skillCode})
            </p>
          </div>
          <button
            type="button"
            className={styles.closeBtn}
            onClick={onClose}
            disabled={isSubmitting}
            aria-label="Đóng hộp thoại"
          >
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit} className={styles.form} noValidate>
          <fieldset className={styles.fieldset}>
            <legend className={styles.legend}>
              Mức độ thành thạo <span className={styles.required}>*</span>
            </legend>
            <p className={styles.hint}>
              Mô tả bên dưới chỉ mang tính tham khảo, bạn có thể tự chọn cấp độ phù hợp nhất.
            </p>

            <div className={styles.levelGrid}>
              {PROFICIENCY_LEVEL_OPTIONS.map((option) => {
                const isSelected = proficiencyLevel === option.value;
                return (
                  <label
                    key={option.value}
                    className={`${styles.levelCard} ${
                      isSelected ? styles.levelCardSelected : ''
                    }`}
                  >
                    <input
                      type="radio"
                      name="proficiencyLevel"
                      value={option.value}
                      checked={isSelected}
                      onChange={() => setProficiencyLevel(option.value)}
                      className={styles.radioInput}
                    />
                    <span className={styles.levelLabel}>{option.label}</span>
                    <span className={styles.levelDesc}>{option.description}</span>
                  </label>
                );
              })}
            </div>
          </fieldset>

          <div className={styles.fieldGroup}>
            <label htmlFor={yearsInputId} className={styles.fieldLabel}>
              Số năm kinh nghiệm
            </label>
            <input
              id={yearsInputId}
              type="number"
              inputMode="decimal"
              min="0"
              max="99.9"
              step="0.5"
              value={yearsInput}
              onChange={(e) => {
                setYearsInput(e.target.value);
                if (validationError) setValidationError(null);
              }}
              className={styles.numberInput}
              placeholder="Ví dụ: 1.5 (bỏ trống mặc định 0)"
            />
            {validationError && (
              <p className={styles.errorText} role="alert">
                {validationError}
              </p>
            )}
          </div>

          <div className={styles.footer}>
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Hủy
            </Button>
            <Button type="submit" variant="primary" isLoading={isSubmitting}>
              {target.mode === 'add' ? 'Thêm kỹ năng' : 'Lưu thay đổi'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
