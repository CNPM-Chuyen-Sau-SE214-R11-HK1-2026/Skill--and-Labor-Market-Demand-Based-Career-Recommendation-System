/**
 * Standard API error field representation.
 * Aligns with docs/api/api-contract.md section 1.2.
 */
export interface ApiFieldError {
  field: string;
  rejectedValue?: unknown;
  reason: string;
}

/**
 * Standard API error response structure returned by backend GlobalExceptionHandler.
 * Aligns with docs/api/api-contract.md section 1.2.
 */
export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  errors?: ApiFieldError[];
}

/**
 * Paginated API response structure.
 * Aligns with docs/api/api-contract.md section 1.1.
 */
export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
