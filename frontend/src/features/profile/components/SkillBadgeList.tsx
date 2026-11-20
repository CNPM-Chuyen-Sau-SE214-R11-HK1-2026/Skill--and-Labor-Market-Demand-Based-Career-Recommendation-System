import React, { useMemo, useState } from 'react';
import type { CandidateSkill, ProficiencyLevel } from '../types/profile.types';
import styles from './SkillBadgeList.module.css';

export interface SkillBadgeListProps {
  skills: CandidateSkill[];
  deletingSkillId: number | null;
  onEdit: (skill: CandidateSkill) => void;
  onDelete: (skill: CandidateSkill) => Promise<void>;
}

const LEVEL_LABELS: Record<ProficiencyLevel, string> = {
  BEGINNER: 'Beginner',
  INTERMEDIATE: 'Intermediate',
  ADVANCED: 'Advanced',
  EXPERT: 'Expert',
};

const LEVEL_CLASS_MAP: Record<ProficiencyLevel, string> = {
  BEGINNER: styles.levelBeginner,
  INTERMEDIATE: styles.levelIntermediate,
  ADVANCED: styles.levelAdvanced,
  EXPERT: styles.levelExpert,
};

export const SkillBadgeList: React.FC<SkillBadgeListProps> = ({
  skills,
  deletingSkillId,
  onEdit,
  onDelete,
}) => {
  const [confirmingSkillId, setConfirmingSkillId] = useState<number | null>(null);

  const groupedSkills = useMemo(() => {
    const groups = new Map<string, CandidateSkill[]>();
    for (const skill of skills) {
      const category = skill.categoryName || 'Kỹ năng khác';
      const existing = groups.get(category) ?? [];
      existing.push(skill);
      groups.set(category, existing);
    }
    return Array.from(groups.entries());
  }, [skills]);

  if (skills.length === 0) {
    return (
      <div className={styles.emptyState}>
        <div className={styles.emptyIcon} aria-hidden="true">
          🎯
        </div>
        <h3 className={styles.emptyTitle}>Chưa có kỹ năng nào được khai báo</h3>
        <p className={styles.emptyDescription}>
          Bạn chưa khai báo kỹ năng nào. Hãy sử dụng ô tìm kiếm phía trên và thêm ít nhất 3 kỹ năng để hệ thống tính toán gợi ý nghề nghiệp chính xác nhất!
        </p>
      </div>
    );
  }

  const handleConfirmDelete = async (skill: CandidateSkill) => {
    setConfirmingSkillId(null);
    await onDelete(skill);
  };

  return (
    <div className={styles.container}>
      <div className={styles.summaryBar}>
        <span className={styles.countText}>
          Tổng cộng <strong>{skills.length}</strong> kỹ năng đã khai báo
        </span>
      </div>

      <div className={styles.groups}>
        {groupedSkills.map(([categoryName, categorySkills]) => (
          <div key={categoryName} className={styles.group}>
            <h3 className={styles.groupTitle}>{categoryName}</h3>
            <div className={styles.badgeGrid}>
              {categorySkills.map((skill) => {
                const isConfirming = confirmingSkillId === skill.skillId;
                const isDeleting = deletingSkillId === skill.skillId;

                return (
                  <div key={skill.skillId} className={styles.skillTag}>
                    <div className={styles.tagInfo}>
                      <span className={styles.skillName}>{skill.skillName}</span>
                      <div className={styles.tagMeta}>
                        <span
                          className={`${styles.levelBadge} ${
                            LEVEL_CLASS_MAP[skill.proficiencyLevel]
                          }`}
                        >
                          {LEVEL_LABELS[skill.proficiencyLevel]}
                        </span>
                        <span className={styles.yearsBadge}>
                          {skill.yearsExperience} năm
                        </span>
                      </div>
                    </div>

                    <div className={styles.tagActions}>
                      {isConfirming ? (
                        <div
                          className={styles.confirmBox}
                          role="group"
                          aria-label={`Xác nhận xóa kỹ năng ${skill.skillName}`}
                        >
                          <button
                            type="button"
                            className={styles.confirmDeleteBtn}
                            disabled={isDeleting}
                            onClick={() => void handleConfirmDelete(skill)}
                          >
                            Xóa
                          </button>
                          <button
                            type="button"
                            className={styles.cancelDeleteBtn}
                            disabled={isDeleting}
                            onClick={() => setConfirmingSkillId(null)}
                          >
                            Hủy
                          </button>
                        </div>
                      ) : (
                        <>
                          <button
                            type="button"
                            className={styles.iconBtn}
                            onClick={() => onEdit(skill)}
                            disabled={isDeleting}
                            title={`Chỉnh sửa ${skill.skillName}`}
                            aria-label={`Chỉnh sửa kỹ năng ${skill.skillName}`}
                          >
                            ✎
                          </button>
                          <button
                            type="button"
                            className={`${styles.iconBtn} ${styles.deleteIconBtn}`}
                            onClick={() => setConfirmingSkillId(skill.skillId)}
                            disabled={isDeleting}
                            title={`Xóa ${skill.skillName}`}
                            aria-label={`Xóa kỹ năng ${skill.skillName}`}
                          >
                            ×
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
