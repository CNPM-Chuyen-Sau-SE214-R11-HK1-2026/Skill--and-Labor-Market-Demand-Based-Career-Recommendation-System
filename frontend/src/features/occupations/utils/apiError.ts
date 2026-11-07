function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

export function isNotFoundError(error: unknown): boolean {
  if (!isRecord(error)) return false;
  const response = error.response;
  return isRecord(response) && response.status === 404;
}

export function getApiErrorMessage(error: unknown): string {
  if (typeof error === 'string' && error.trim()) return error;
  if (isRecord(error)) {
    const response = error.response;
    if (isRecord(response)) {
      const data = response.data;
      if (isRecord(data)) {
        for (const key of ['message', 'detail', 'error', 'code']) {
          const value = data[key];
          if (typeof value === 'string' && value.trim()) return value;
        }
      }
      if (response.status === 404) return 'Không tìm thấy nghề nghiệp được yêu cầu.';
      if (response.status === 400) return 'Thông tin tìm kiếm không hợp lệ. Vui lòng kiểm tra lại.';
      if (response.status === 500) return 'Máy chủ đang gặp sự cố. Vui lòng thử lại sau.';
    }
    if (typeof error.message === 'string' && error.message.trim()) return error.message;
  }
  return 'Không thể tải dữ liệu. Vui lòng kiểm tra kết nối và thử lại.';
}
