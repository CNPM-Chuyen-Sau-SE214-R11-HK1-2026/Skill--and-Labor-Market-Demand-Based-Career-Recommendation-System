import React, { useEffect, useId, useRef, useState } from 'react';
import type { SkillCatalogItem } from '../types/profile.types';
import { MIN_SKILL_QUERY_LENGTH, useSkillSearch } from '../hooks/useSkillSearch';
import styles from './SkillSelector.module.css';

export interface SkillSelectorProps {
  existingSkillIds: Set<number>;
  onSelectSkill: (skill: SkillCatalogItem) => void;
  inputRef?: React.RefObject<HTMLInputElement>;
  disabled?: boolean;
}

export const SkillSelector: React.FC<SkillSelectorProps> = ({
  existingSkillIds,
  onSelectSkill,
  inputRef,
  disabled = false,
}) => {
  const [query, setQuery] = useState<string>('');
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [activeIndex, setActiveIndex] = useState<number>(-1);

  const containerRef = useRef<HTMLDivElement>(null);
  const internalInputRef = useRef<HTMLInputElement>(null);
  const resolvedInputRef = inputRef ?? internalInputRef;

  const listboxId = useId();
  const inputId = useId();

  const { results, isSearching, error, isQueryTooShort } = useSkillSearch(query);

  // Reset active highlight whenever results change
  useEffect(() => {
    setActiveIndex(-1);
  }, [results]);

  // Close dropdown on outside click
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
        setActiveIndex(-1);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const trimmedLength = query.trim().length;
  const shouldShowDropdown =
    isOpen && (trimmedLength >= MIN_SKILL_QUERY_LENGTH || trimmedLength === 1);

  const handleSelect = (skill: SkillCatalogItem) => {
    if (existingSkillIds.has(skill.id)) {
      return;
    }
    onSelectSkill(skill);
    setQuery('');
    setIsOpen(false);
    setActiveIndex(-1);
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Escape') {
      if (isOpen) {
        event.preventDefault();
        setIsOpen(false);
        setActiveIndex(-1);
      }
      return;
    }

    if (!shouldShowDropdown || results.length === 0) {
      return;
    }

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setActiveIndex((prev) => (prev < results.length - 1 ? prev + 1 : 0));
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      setActiveIndex((prev) => (prev > 0 ? prev - 1 : results.length - 1));
    } else if (event.key === 'Enter') {
      if (activeIndex >= 0 && activeIndex < results.length) {
        event.preventDefault();
        handleSelect(results[activeIndex]);
      }
    }
  };

  const activeOptionId =
    activeIndex >= 0 && activeIndex < results.length
      ? `${listboxId}-option-${results[activeIndex].id}`
      : undefined;

  return (
    <div className={styles.wrapper} ref={containerRef}>
      <label htmlFor={inputId} className={styles.label}>
        Tìm kiếm &amp; thêm kỹ năng từ danh mục chuẩn hóa
      </label>

      <div className={styles.inputBox}>
        <span className={styles.searchIcon} aria-hidden="true">
          🔍
        </span>
        <input
          ref={resolvedInputRef}
          id={inputId}
          type="text"
          role="combobox"
          aria-autocomplete="list"
          aria-expanded={shouldShowDropdown}
          aria-controls={listboxId}
          aria-activedescendant={activeOptionId}
          className={styles.input}
          placeholder="Nhập tên kỹ năng (vd: Java, React, Docker, SQL...)"
          value={query}
          disabled={disabled}
          onFocus={() => setIsOpen(true)}
          onChange={(e) => {
            setQuery(e.target.value);
            setIsOpen(true);
          }}
          onKeyDown={handleKeyDown}
          autoComplete="off"
        />
        {query && (
          <button
            type="button"
            className={styles.clearBtn}
            aria-label="Xóa từ khóa tìm kiếm"
            onClick={() => {
              setQuery('');
              setIsOpen(false);
              setActiveIndex(-1);
              resolvedInputRef.current?.focus();
            }}
          >
            ×
          </button>
        )}
      </div>

      <p className={styles.helperText}>
        Nhập ít nhất {MIN_SKILL_QUERY_LENGTH} ký tự để tìm kiếm từ điển kỹ năng ESCO / O*NET.
      </p>

      {shouldShowDropdown && (
        <div className={styles.dropdown}>
          {isQueryTooShort ? (
            <div className={styles.statusRow}>
              Nhập ít nhất {MIN_SKILL_QUERY_LENGTH} ký tự để bắt đầu tìm kiếm...
            </div>
          ) : isSearching ? (
            <div className={styles.statusRow}>
              <span className={styles.inlineSpinner} aria-hidden="true" />
              Đang tìm kiếm kỹ năng...
            </div>
          ) : error ? (
            <div className={styles.errorRow} role="alert">
              {error}
            </div>
          ) : results.length === 0 ? (
            <div className={styles.statusRow}>
              Không tìm thấy kỹ năng nào khớp với &ldquo;{query.trim()}&rdquo;.
            </div>
          ) : (
            <ul
              id={listboxId}
              role="listbox"
              aria-label="Gợi ý kỹ năng"
              className={styles.listbox}
            >
              {results.map((skill, index) => {
                const isAdded = existingSkillIds.has(skill.id);
                const isActive = index === activeIndex;
                return (
                  <li
                    key={skill.id}
                    id={`${listboxId}-option-${skill.id}`}
                    role="option"
                    aria-selected={isActive}
                    aria-disabled={isAdded}
                    className={`${styles.option} ${isActive ? styles.optionActive : ''} ${
                      isAdded ? styles.optionDisabled : ''
                    }`}
                    onMouseEnter={() => setActiveIndex(index)}
                    onMouseDown={(e) => {
                      e.preventDefault();
                      handleSelect(skill);
                    }}
                  >
                    <div className={styles.optionMain}>
                      <span className={styles.optionName}>{skill.name}</span>
                      <span className={styles.optionMeta}>
                        {skill.categoryName} · {skill.code}
                      </span>
                    </div>
                    {isAdded ? (
                      <span className={styles.addedTag}>Đã thêm</span>
                    ) : (
                      <span className={styles.addHint}>+ Chọn</span>
                    )}
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      )}
    </div>
  );
};
