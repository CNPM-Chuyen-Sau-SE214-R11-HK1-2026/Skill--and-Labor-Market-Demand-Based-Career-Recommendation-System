export function formatVnd(value: number | null | undefined): string {
  if (value === null || value === undefined || !Number.isFinite(value)) return 'Chưa cập nhật';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(value);
}

export function formatVndRange(min: number | null | undefined, max: number | null | undefined): string {
  if (min == null && max == null) return 'Chưa cập nhật';
  if (min == null) return `Đến ${formatVnd(max)}`;
  if (max == null) return `Từ ${formatVnd(min)}`;
  return `${formatVnd(min)} – ${formatVnd(max)}`;
}

export function formatPercent(value: number | null | undefined, digits = 1): string {
  if (value === null || value === undefined || !Number.isFinite(value)) return 'Chưa cập nhật';
  const prefix = value > 0 ? '+' : '';
  return `${prefix}${new Intl.NumberFormat('vi-VN', { maximumFractionDigits: digits, minimumFractionDigits: digits }).format(value)}%`;
}

export function formatInteger(value: number | null | undefined): string {
  if (value === null || value === undefined || !Number.isFinite(value)) return 'Chưa cập nhật';
  return new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 }).format(value);
}
