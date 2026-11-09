import type { FormEvent } from 'react';
interface SearchBarProps { value: string; onChange: (value: string) => void; onSubmit: () => void; onClear: () => void; }
export function SearchBar({ value, onChange, onSubmit, onClear }: SearchBarProps) {
  function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); onSubmit(); }
  return <form className="search-form" onSubmit={submit}>
    <span className="search-icon" aria-hidden="true">⌕</span>
    <input value={value} onChange={(event) => onChange(event.target.value)} placeholder="Tìm theo tên nghề nghiệp, lĩnh vực..." aria-label="Tìm kiếm nghề nghiệp" />
    {value && <button type="button" className="clear-search" onClick={onClear}>Xóa</button>}
    <button className="button button--primary" type="submit">Tìm kiếm</button>
  </form>;
}
