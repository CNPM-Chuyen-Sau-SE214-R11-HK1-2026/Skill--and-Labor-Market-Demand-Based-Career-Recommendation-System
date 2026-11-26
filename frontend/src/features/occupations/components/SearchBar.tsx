import { useRef } from 'react';
import type { FormEvent } from 'react';

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  onSubmit: () => void;
  onClear: () => void;
}

export function SearchBar({ value, onChange, onSubmit, onClear }: SearchBarProps) {
  const inputRef = useRef<HTMLInputElement>(null);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit();
  }

  return (
    <form className="search-form" role="search" onSubmit={submit}>
      <div className="search-input">
        <span className="search-icon" aria-hidden="true" />
        <input
          ref={inputRef}
          type="text"
          enterKeyHint="search"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder="Tìm theo tên nghề hoặc lĩnh vực"
          aria-label="Tìm kiếm nghề nghiệp"
        />
        {value && (
          <button type="button" className="clear-search" onClick={() => {
            onClear();
            inputRef.current?.focus();
          }}>Xóa</button>
        )}
      </div>
      <button className="button button--primary" type="submit">Tìm kiếm</button>
    </form>
  );
}
