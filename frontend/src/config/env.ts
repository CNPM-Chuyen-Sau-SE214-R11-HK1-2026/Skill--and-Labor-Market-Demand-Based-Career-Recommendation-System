/**
 * Centralized environment configuration.
 * Adheres to ENV-01 constraint: safe fallback values prevent White Screen of Death (WSOD).
 */
export const env = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  ENABLE_MOCK_API: import.meta.env.VITE_ENABLE_MOCK_API === 'true',
} as const;
