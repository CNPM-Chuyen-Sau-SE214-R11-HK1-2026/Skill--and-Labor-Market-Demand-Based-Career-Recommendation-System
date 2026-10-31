import axios, { AxiosError, AxiosHeaders, InternalAxiosRequestConfig } from 'axios';
import { env } from '../config/env';
import { tokenStorage } from '../utils/tokenStorage';

/**
 * Concurrency guard flag to prevent multiple 401 responses
 * from triggering repeated session cleanups and redirects.
 */
let isHandlingUnauthorized = false;

/**
 * Resets the unauthorized handling mutex.
 * Called when a user successfully logs in.
 */
export const resetUnauthorizedHandling = (): void => {
  isHandlingUnauthorized = false;
};

/**
 * Centralized Axios client instance.
 * Adheres to GAP-01: baseURL already includes /api/v1 prefix,
 * services must NOT repeat /api/v1 in endpoint paths.
 */
export const apiClient = axios.create({
  baseURL: env.API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request Interceptor: Attach JWT Bearer token if present and not a mock token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = tokenStorage.getToken();

    // Extract any existing Authorization header from caller
    const rawAuth = config.headers
      ? typeof config.headers.get === 'function'
        ? config.headers.get('Authorization')
        : AxiosHeaders.from(config.headers).get('Authorization')
      : undefined;

    const authStr = typeof rawAuth === 'string' ? rawAuth : '';
    const hasMockToken =
      authStr.includes('mock-token-') || (token?.startsWith('mock-token-') ?? false);

    // Hardening: Actively strip Authorization header if mock token exists
    if (hasMockToken && config.headers) {
      if (typeof config.headers.delete === 'function') {
        config.headers.delete('Authorization');
      } else {
        delete (config.headers as Record<string, unknown>)['Authorization'];
        delete (config.headers as Record<string, unknown>)['authorization'];
      }
    } else if (token && !token.startsWith('mock-token-') && config.headers) {
      if (typeof config.headers.set === 'function') {
        config.headers.set('Authorization', `Bearer ${token}`);
      } else {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error: unknown) => Promise.reject(error)
);

// Response Interceptor: Handle 401 with concurrency guard, skipping login/register endpoints
apiClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError;
      const requestUrl = axiosError.config?.url ?? '';
      const isAuthEndpoint =
        requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register');

      if (axiosError.response?.status === 401 && !isAuthEndpoint) {
        // Extract Authorization header safely supporting both AxiosHeaders and plain objects
        const rawHeaders = axiosError.config?.headers;
        const requestAuthHeader = rawHeaders
          ? typeof rawHeaders.get === 'function'
            ? rawHeaders.get('Authorization')
            : AxiosHeaders.from(rawHeaders).get('Authorization')
          : undefined;

        const requestToken =
          typeof requestAuthHeader === 'string' ? requestAuthHeader : null;
        const currentToken = tokenStorage.getToken();

        // Race condition guard: only handle 401 if the failed request's token
        // still matches the currently active session token.
        // Stale requests from a previous session that return 401 must NOT wipe the new session.
        const isCurrentSession =
          requestToken !== null &&
          currentToken !== null &&
          requestToken === `Bearer ${currentToken}`;

        if (isCurrentSession && !isHandlingUnauthorized) {
          isHandlingUnauthorized = true;
          tokenStorage.clearSession();
          window.dispatchEvent(new Event('auth:unauthorized'));
        }
      }
    }
    return Promise.reject(error);
  }
);
